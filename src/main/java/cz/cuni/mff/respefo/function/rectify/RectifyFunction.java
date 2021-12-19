package cz.cuni.mff.respefo.function.rectify;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.component.SpectrumExplorer;
import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.Serialize;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.common.ChartKeyListener;
import cz.cuni.mff.respefo.function.common.ZoomMouseWheelListener;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.function.open.OpenFunction;
import cz.cuni.mff.respefo.resources.ColorResource;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.spectrum.format.EchelleSpectrum;
import cz.cuni.mff.respefo.spectrum.format.SimpleSpectrum;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.Progress;
import cz.cuni.mff.respefo.util.collections.DoubleArrayList;
import cz.cuni.mff.respefo.util.collections.Point;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;
import cz.cuni.mff.respefo.util.utils.ChartUtils;
import cz.cuni.mff.respefo.util.utils.MathUtils;
import cz.cuni.mff.respefo.util.widget.ChartBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Display;
import org.orangepalantir.leastsquares.Fitter;
import org.orangepalantir.leastsquares.Function;
import org.orangepalantir.leastsquares.fitters.MarquardtFitter;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries;

import java.io.File;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntToDoubleFunction;
import java.util.function.UnaryOperator;

import static cz.cuni.mff.respefo.resources.ColorManager.getColor;
import static cz.cuni.mff.respefo.resources.ColorResource.*;
import static cz.cuni.mff.respefo.util.utils.MathUtils.linearInterpolation;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.AxisLabel.RELATIVE_FLUX;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.AxisLabel.WAVELENGTH;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.LineSeriesBuilder.lineSeries;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.ScatterSeriesBuilder.scatterSeries;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.newChart;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.util.Arrays.stream;

@Fun(name = "Rectify", fileFilter = SpefoFormatFileFilter.class, group = "Preprocessing")
@Serialize(key = RectifyFunction.SERIALIZE_KEY, assetClass = RectifyAsset.class)
public class RectifyFunction implements SingleFileFunction {

    public static final String SERIALIZE_KEY = "rectify";
    public static final String POINTS_SERIES_NAME = "points";
    public static final String SELECTED_SERIES_NAME = "selected";
    public static final String CONTINUUM_SERIES_NAME = "continuum";

    private static final double K = 565754; // More like 565660.2133333334 by my calculations
    private static final double[] COEFFICIENTS = new double[]{-3.0817976563120606, 0.21780270894619688,
            -0.004371189338350063, 4.139193926208102e-05, -1.8545313075173e-07, 3.12485701485604e-10};
    private static final IntToDoubleFunction ALPHA_FOR_ORDER = order -> MathUtils.polynomial(order, COEFFICIENTS);

    private static RectifyAsset previousAsset;
// TODO: see if there is a way to remember previous assets for echelle spectra

    @Override
    public void execute(File file) {
        Spectrum spectrum;
        try {
            spectrum = Spectrum.open(file);
        } catch (SpefoException e) {
            Message.error("Couldn't open file", e);
            return;
        }

        switch (spectrum.getFormat()) {
            case SimpleSpectrum.FORMAT:
                rectifySimpleSpectrum((SimpleSpectrum) spectrum);
                break;
            case EchelleSpectrum.FORMAT:
                rectifyEchelleSpectrum((EchelleSpectrum) spectrum);
                break;
            default:
                Message.warning("Spectrum has an unexpected file format");
        }
    }

    private static void rectifySimpleSpectrum(SimpleSpectrum spectrum) {
        RectifyAsset asset = spectrum.getFunctionAsset(SERIALIZE_KEY, RectifyAsset.class)
                .orElse(previousAsset != null
                        ? previousAsset.adjustToNewData(spectrum.getProcessedSeries())
                        : RectifyAsset.withDefaultPoints(spectrum.getProcessedSeries()));

        XYSeries series = spectrum.getProcessedSeriesWithout(asset);

        rectify(spectrum.getFile().getName(), series, asset, UnaryOperator.identity(), a -> finishSimpleSpectrum(spectrum, a));
    }

    private static void rectifyEchelleSpectrum(EchelleSpectrum spectrum) {
        XYSeries[] series = spectrum.getOriginalSeries();

        String[][] names = new String[series.length][3];
        for (int i = 0; i <= series.length - 1; i++) {
            XYSeries xySeries = series[i];
            names[i] = new String[]{
                    Integer.toString(i + 1),
                    Double.toString(xySeries.getX(0)),
                    Double.toString(xySeries.getLastX())
            };
        }

        EchelleSelectionDialog dialog = new EchelleSelectionDialog(names);
        if (dialog.openIsNotOk()) {
            return;
        }

        Progress.withProgressTracking(p -> {
            p.refresh("Estimating blaze parameters", series.length);

            double[][] blazeParameters = new double[series.length][2];

            for (int i = 0; i < series.length; i++) {
                XYSeries xySeries = series[i];
                int order = 125 - i;
                blazeParameters[i] = calculateBlazeParameters(xySeries, order);
                p.step();
            }

            return blazeParameters;
        }, blazeParameters -> rectifySingleEchelle(spectrum, dialog.getSelectedIndices(), blazeParameters, new RectifyAsset[series.length], 0));
    }

    private static double[] calculateBlazeParameters(XYSeries series, int order) {
        double centralWavelength = K / order;
        double scale = MathUtils.intep(series.getXSeries(), series.getYSeries(), centralWavelength);

        Function fun = new Function() {
            private final double alpha = ALPHA_FOR_ORDER.applyAsDouble(order);

            @Override
            public double evaluate(double[] values, double[] parameters) {
                return parameters[1] * r(values[0], order, parameters[0], alpha);
            }

            @Override
            public int getNParameters() {
                return 2; // central wavelength, scale
            }

            @Override
            public int getNInputs() {
                return 1; // wavelength
            }
        };

        Fitter fit = new MarquardtFitter(fun);
        fit.setData(
                stream(series.getXSeries()).mapToObj(x -> new double[]{x}).toArray(double[][]::new),
                series.getYSeries()
        );
        fit.setParameters(new double[] {centralWavelength, scale});
        fit.fitData();

        return fit.getParameters();
    }

    private static double[] blaze(double[] xSeries, double[] parameters, int order) {
        double alpha = ALPHA_FOR_ORDER.applyAsDouble(order);
        return stream(xSeries)
                .map(x -> parameters[1] * r(x, order, parameters[0], alpha))
                .toArray();
    }

    private static double r(double lambda, int order, double centralWavelength, double alpha) {
        double x = order * (1 - centralWavelength / lambda);
        double argument = PI * alpha * x;
        return Math.pow(Math.sin(argument) / argument, 2);
    }

    private static void rectifySingleEchelle(EchelleSpectrum spectrum, Set<Integer> selectedIndices,
                                             double[][] blazeParameters, RectifyAsset[] rectifyAssets, int index) {
        if (index >= rectifyAssets.length) {
            finishEchelleSpectrum(spectrum, rectifyAssets);

        } else if (selectedIndices.contains(index)) {
            // interactive

            Display.getCurrent().asyncExec(() -> finetuneBlazeParameters(spectrum, selectedIndices, blazeParameters, rectifyAssets, index));
        } else {
            // automatic

            XYSeries currentSeries = spectrum.getOriginalSeries()[index];
            int order = 125 - index;
            double[] parameters = blazeParameters[index];
            RectifyAsset asset = blazeToRectify(currentSeries, order, parameters);
            rectifyAssets[index] = asset;
            Display.getCurrent().asyncExec(() -> rectifySingleEchelle(spectrum, selectedIndices, blazeParameters, rectifyAssets, index + 1));
        }
    }

    private static void finetuneBlazeParameters(EchelleSpectrum spectrum, Set<Integer> selectedIndices,
                                                double[][] blazeParameters, RectifyAsset[] rectifyAssets, int index) {
        XYSeries currentSeries = spectrum.getOriginalSeries()[index];
        XYSeries blaze = new XYSeries(currentSeries.getXSeries(), blaze(currentSeries.getXSeries(), blazeParameters[index], 125 - index));
        XYSeries residuals = new XYSeries(currentSeries.getXSeries(),
                ArrayUtils.createArray(currentSeries.getLength(), i -> abs(currentSeries.getY(i) - blaze.getY(i))));
        Chart chart = newChart()
                .title("#" + (index + 1))
                .xAxisLabel("X axis")
                .yAxisLabel("Y axis")
                .keyListener(ChartKeyListener::makeAllSeriesEqualRange)
                .keyListener(ch -> new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.keyCode == SWT.CR) {
                            blazeParameters[index] = (double[]) ch.getData("parameters");
                            ComponentManager.getDisplay().asyncExec(()
                                    -> finetuneRectificationPoints(spectrum, selectedIndices, blazeParameters, rectifyAssets, index));
                        }
                    }
                })
                .mouseAndMouseMoveListener(ch -> new BlazeMouseListener(ch, () -> updateChart(ch)))
                .mouseWheelListener(ZoomMouseWheelListener::new)
                .series(lineSeries()
                        .name("series")
                        .series(currentSeries))
                .series(lineSeries()
                        .name("blaze")
                        .color(GRAY)
                        .series(blaze))
                .series(lineSeries()
                        .name("residuals")
                        .color(ORANGE)
                        .series(residuals))
                .makeAllSeriesEqualRange()
                .data("parameters", blazeParameters[index])
                .data("order", 125 - index)
                .data("horizontal", false)
                .forceFocus()
                .build(ComponentManager.clearAndGetScene());

        chart.getPlotArea().addPaintListener(event -> {
            double[] params = (double[]) chart.getData("parameters");
            boolean horizontal = (boolean) chart.getData("horizontal");

            Point coordinates = ChartUtils.getCoordinatesFromRealValues(chart, params[0], params[1]);

            event.gc.setForeground(getColor(horizontal ? BLUE : CYAN));
            event.gc.drawLine(0, (int) coordinates.y, event.width, (int) coordinates.y);

            event.gc.setForeground(getColor(horizontal ? CYAN : BLUE));
            event.gc.drawLine((int) coordinates.x, 0, (int) coordinates.x, event.height);
        });
    }

    private static void finetuneRectificationPoints(EchelleSpectrum spectrum, Set<Integer> selectedIndices,
                                                    double[][] blazeParameters, RectifyAsset[] rectifyAssets, int index) {
        XYSeries currentSeries = spectrum.getOriginalSeries()[index];
        rectify("#" + (index + 1), currentSeries, blazeToRectify(currentSeries, 125 - index, blazeParameters[index]),
                builder -> {
                    for (int i = Math.max(index - 2, 0); i <= Math.min(index + 2, rectifyAssets.length - 1); i++) {
                        if (i == index) {
                            continue;
                        }

                        builder.series(lineSeries()
                                .series(spectrum.getOriginalSeries()[i])
                                .color(GRAY));
                    }
                    return builder;
                },
                asset -> {
                    rectifyAssets[index] = asset;
                    Display.getCurrent().asyncExec(()
                            -> rectifySingleEchelle(spectrum, selectedIndices, blazeParameters, rectifyAssets, index + 1));
                });
    }

    private static void updateChart(Chart chart) {
        ISeries iSeries = chart.getSeriesSet().getSeries("series");
        XYSeries series = new XYSeries(iSeries.getXSeries(), iSeries.getYSeries());

        double[] parameters = (double[]) chart.getData("parameters");
        int order = (int) chart.getData("order");

        double[] blaze = blaze(series.getXSeries(), parameters, order);
        double[] residuals = ArrayUtils.createArray(series.getLength(), i -> abs(series.getY(i) - blaze[i]));

        chart.getSeriesSet().getSeries("blaze").setYSeries(blaze);
        chart.getSeriesSet().getSeries("residuals").setYSeries(residuals);
        chart.redraw();
    }

    private static RectifyAsset blazeToRectify(XYSeries series, int order, double[] parameters) {
        double centralWavelength = parameters[0];
        double scale = parameters[1];
        double alpha = ALPHA_FOR_ORDER.applyAsDouble(order);

        // TODO: Is sampling 20 points enough?

        DoubleArrayList xCoordinates = new DoubleArrayList(20);
        DoubleArrayList yCoordinates = new DoubleArrayList(20);

        double low = series.getX(0);
        double high = series.getLastX();

        for (int i = 0; i < 20; i++) {
            double x = linearInterpolation(0, low, 19, high, i);
            double y = scale * r(x, order, centralWavelength, alpha);

            xCoordinates.add(x);
            yCoordinates.add(y);
        }

        return new RectifyAsset(xCoordinates, yCoordinates);
    }

    private static void rectify(String title, XYSeries series, RectifyAsset asset,
                                                     UnaryOperator<ChartBuilder> operator,
                                                     Consumer<RectifyAsset> finish) {
        newChart()
                .title(title)
                .xAxisLabel(WAVELENGTH)
                .yAxisLabel(RELATIVE_FLUX)
                .series(lineSeries()
                        .name("original")
                        .color(ColorResource.GREEN)
                        .series(series))
                .series(lineSeries()
                        .name(CONTINUUM_SERIES_NAME)
                        .color(ColorResource.YELLOW)
                        .xSeries(series.getXSeries())
                        .ySeries(asset.getIntepData(series.getXSeries())))
                .series(scatterSeries()
                        .name(POINTS_SERIES_NAME)
                        .color(ColorResource.WHITE)
                        .symbolSize(3)
                        .xSeries(asset.getXCoordinatesArray())
                        .ySeries(asset.getYCoordinatesArray()))
                .series(scatterSeries()
                        .name(SELECTED_SERIES_NAME)
                        .color(ColorResource.RED)
                        .symbolSize(3)
                        .series(asset.getActivePoint()))
                .apply(operator)
                .keyListener(ChartKeyListener::makeAllSeriesEqualRange)
                .keyListener(ch -> new RectifyKeyListener(ch, asset,
                        () -> updateAllSeries(ch, asset, series),
                        newIndex -> updateActivePoint(ch, asset, newIndex),
                        () -> finish.accept(asset)))
                .mouseAndMouseMoveListener(ch -> new RectifyMouseListener(ch,
                        POINTS_SERIES_NAME,
                        index -> {
                            if (asset.getActiveIndex() != index) {
                                updateActivePoint(ch, asset, index);
                            }
                        },
                        point -> {
                            asset.moveActivePoint(point.x, point.y);
                            updateAllSeries(ch, asset, series);
                        },
                        point -> {
                            asset.addPoint(point);
                            updateAllSeries(ch, asset, series);
                        },
                        () -> {
                            asset.deleteActivePoint();
                            updateAllSeries(ch, asset, series);
                        }))
                .mouseWheelListener(ZoomMouseWheelListener::new)
                .makeAllSeriesEqualRange()
                .forceFocus()
                .build(ComponentManager.clearAndGetScene());
    }

    private static void updateActivePoint(Chart chart, RectifyAsset asset, int newIndex) {
        asset.setActiveIndex(newIndex);

        ILineSeries series = (ILineSeries) chart.getSeriesSet().getSeries(SELECTED_SERIES_NAME);

        series.setXSeries(asset.getActivePoint().getXSeries());
        series.setYSeries(asset.getActivePoint().getYSeries());

        chart.redraw();
    }

    private static void updateAllSeries(Chart chart, RectifyAsset asset, XYSeries xySeries) {
        ILineSeries lineSeries = (ILineSeries) chart.getSeriesSet().getSeries(POINTS_SERIES_NAME);
        lineSeries.setXSeries(asset.getXCoordinatesArray());
        lineSeries.setYSeries(asset.getYCoordinatesArray());

        lineSeries = (ILineSeries) chart.getSeriesSet().getSeries(SELECTED_SERIES_NAME);
        lineSeries.setXSeries(asset.getActivePoint().getXSeries());
        lineSeries.setYSeries(asset.getActivePoint().getYSeries());

        lineSeries = (ILineSeries) chart.getSeriesSet().getSeries(CONTINUUM_SERIES_NAME);
        lineSeries.setYSeries(asset.getIntepData(xySeries.getXSeries()));

        chart.redraw();
    }

    private static void finishSimpleSpectrum(SimpleSpectrum spectrum, RectifyAsset asset) {
        if (asset.isEmpty()) { // This can never occur
            spectrum.removeFunctionAsset(SERIALIZE_KEY);
        } else {
            spectrum.putFunctionAsset(SERIALIZE_KEY, asset);
            previousAsset = asset;
        }

        try {
            spectrum.save();
            SpectrumExplorer.getDefault().refresh();
            OpenFunction.displaySpectrum(spectrum);
            Message.info("Rectified spectrum saved successfully.");

        } catch (SpefoException exception) {
            Message.error("Spectrum file couldn't be saved.", exception);
        }
    }

    private static void finishEchelleSpectrum(EchelleSpectrum spectrum, RectifyAsset[] assets) {
        spectrum.setRectifyAssets(assets);

        try {
            spectrum.save();
            SpectrumExplorer.getDefault().refresh();
            OpenFunction.displaySpectrum(spectrum);
            Message.info("Rectified spectrum saved successfully.");

        } catch (SpefoException exception) {
            Message.error("Spectrum file couldn't be saved.", exception);
        }
    }
}
