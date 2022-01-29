package cz.cuni.mff.respefo.function.rectify;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.component.SpectrumExplorer;
import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.Serialize;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.clean.CleanAsset;
import cz.cuni.mff.respefo.function.clean.CleanFunction;
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
import cz.cuni.mff.respefo.util.collections.Point;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;
import cz.cuni.mff.respefo.util.utils.ChartUtils;
import cz.cuni.mff.respefo.util.utils.MathUtils;
import cz.cuni.mff.respefo.util.widget.ChartBuilder;
import org.eclipse.swt.widgets.Display;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

import static cz.cuni.mff.respefo.resources.ColorManager.getColor;
import static cz.cuni.mff.respefo.resources.ColorResource.*;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.AxisLabel.RELATIVE_FLUX;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.AxisLabel.WAVELENGTH;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.LineSeriesBuilder.lineSeries;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.ScatterSeriesBuilder.scatterSeries;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.newChart;
import static java.lang.Math.*;
import static java.util.function.UnaryOperator.identity;

@Fun(name = "Rectify", fileFilter = SpefoFormatFileFilter.class, group = "Preprocessing")
@Serialize(key = RectifyFunction.RECTIFY_SERIALIZE_KEY, assetClass = RectifyAsset.class)
@Serialize(key = RectifyFunction.BLAZE_SERIALIZE_KEY, assetClass = BlazeAsset.class)
public class RectifyFunction implements SingleFileFunction {

    public static final String RECTIFY_SERIALIZE_KEY = "rectify";
    public static final String BLAZE_SERIALIZE_KEY = "blaze";

    public static final String POINTS_SERIES_NAME = "points";
    public static final String SELECTED_SERIES_NAME = "selected";
    public static final String CONTINUUM_SERIES_NAME = "continuum";

    private static RectifyAsset previousAsset;

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
        RectifyAsset asset = spectrum.getFunctionAsset(RECTIFY_SERIALIZE_KEY, RectifyAsset.class)
                .orElse(previousAsset != null
                        ? previousAsset.adjustToNewData(spectrum.getProcessedSeries())
                        : RectifyAsset.withDefaultPoints(spectrum.getProcessedSeries()));

        XYSeries series = spectrum.getProcessedSeriesWithout(asset);

        rectify(spectrum.getFile().getName(), series, asset, identity(), a -> finishSimpleSpectrum(spectrum, a));
    }

    private static void rectifyEchelleSpectrum(EchelleSpectrum spectrum) {
        XYSeries[] series = spectrum.getOriginalSeries();

        Optional<CleanAsset> optionalCleanAsset = spectrum.getFunctionAsset(CleanFunction.SERIALIZE_KEY, CleanAsset.class);
        optionalCleanAsset.ifPresent(asset -> cleanSeries(series, asset));

        BlazeAsset blazeAsset = spectrum.getFunctionAsset(BLAZE_SERIALIZE_KEY, BlazeAsset.class).orElseGet(BlazeAsset::new);

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

            Blaze[] blazes = new Blaze[series.length];
            for (int i = 0; i < series.length; i++) {
                int order = 125 - i;
                if (blazeAsset.hasParameters(order)) {
                    double[] parameters = blazeAsset.getParameters(order);
                    blazes[i] = new Blaze(series[i], order, parameters[0], parameters[1]);
                } else {
                    blazes[i] = new Blaze(series[i], order);
                }
                p.step();
            }

            return blazes;
        }, blazes -> rectifySingleEchelle(spectrum, dialog.getSelectedIndices(), blazes, new RectifyAsset[series.length], 0));
    }

    private static void cleanSeries(XYSeries[] series, CleanAsset asset) {
        Map<Integer, List<Integer>> deletedIndices = new HashMap<>();
        int n = series[0].getLength();
        for (int index : asset) {
            int order = Math.floorDiv(index, n);
            int indexInOrder = index % order;
            deletedIndices.putIfAbsent(order, new ArrayList<>());
            deletedIndices.get(order).add(indexInOrder);
        }

        for (Map.Entry<Integer, List<Integer>> entries : deletedIndices.entrySet()) {
            int order = entries.getKey();
            Set<Integer> indices = new HashSet<>(entries.getValue());
            XYSeries currentSeries = series[order];

            double[] remainingXSeries = IntStream.range(0, currentSeries.getLength())
                    .filter(index -> !indices.contains(index))
                    .mapToDouble(currentSeries::getX)
                    .toArray();

            double[] remainingYSeries = IntStream.range(0, currentSeries.getLength())
                    .filter(index -> !indices.contains(index))
                    .mapToDouble(currentSeries::getY)
                    .toArray();

            double[] newYSeries = MathUtils.intep(remainingXSeries, remainingYSeries, currentSeries.getXSeries());

            series[order] = new XYSeries(currentSeries.getXSeries(), newYSeries);
        }
    }

    private static void rectifySingleEchelle(EchelleSpectrum spectrum, Set<Integer> selectedIndices,
                                             Blaze[] blazes, RectifyAsset[] rectifyAssets, int index) {
        if (index >= rectifyAssets.length) {
            finishEchelleSpectrum(spectrum, rectifyAssets);

        } else if (selectedIndices.contains(index)) {
            // interactive
            Display.getCurrent().asyncExec(() -> fineTuneBlazeParameters(spectrum, selectedIndices, blazes, rectifyAssets, index));
        } else {
            // automatic
            rectifyAssets[index] = blazes[index].toRectifyAsset();
            spectrum.getFunctionAsset(BLAZE_SERIALIZE_KEY, BlazeAsset.class).ifPresent(asset -> asset.removeIfPresent(125 - index));
            Display.getCurrent().asyncExec(() -> rectifySingleEchelle(spectrum, selectedIndices, blazes, rectifyAssets, index + 1));
        }
    }

    private static void fineTuneBlazeParameters(EchelleSpectrum spectrum, Set<Integer> selectedIndices,
                                                Blaze[] blazes, RectifyAsset[] rectifyAssets, int index) {
        XYSeries currentSeries = spectrum.getOriginalSeries()[index];
        XYSeries blazeSeries = blazes[index].series();
        XYSeries residuals = new XYSeries(currentSeries.getXSeries(),
                ArrayUtils.createArray(currentSeries.getLength(), i -> abs(currentSeries.getY(i) - blazeSeries.getY(i))));

        newChart()
                .title("#" + (index + 1))
                .xAxisLabel("X axis")
                .yAxisLabel("Y axis")
                .series(lineSeries()
                        .name("series")
                        .series(currentSeries))
                .series(lineSeries()
                        .name("blaze")
                        .color(GRAY)
                        .series(blazeSeries))
                .series(lineSeries()
                        .name("residuals")
                        .color(ORANGE)
                        .series(residuals))
                .keyListener(ch -> new BlazeKeyListener(ch, blazes[index],
                        () -> updateChart(ch),
                        () -> {
                            Blaze blaze = (Blaze) ch.getData("blaze");
                            BlazeAsset asset = spectrum.getFunctionAsset(BLAZE_SERIALIZE_KEY, BlazeAsset.class).orElseGet(BlazeAsset::new);
                            asset.setParameters(blaze.getOrder(), blaze.getCentralWavelength(), blaze.getScale());
                            spectrum.putFunctionAsset(BLAZE_SERIALIZE_KEY, asset);
                            ComponentManager.getDisplay().asyncExec(()
                                    -> fineTuneRectificationPoints(spectrum, selectedIndices, blazes, rectifyAssets, index));
                        }))
                .mouseAndMouseMoveListener(ch -> new BlazeMouseListener(ch, () -> updateChart(ch)))
                .mouseWheelListener(ZoomMouseWheelListener::new)
                .plotAreaPaintListener(ch -> event -> {
                    Blaze blaze = (Blaze) ch.getData("blaze");
                    boolean horizontal = (boolean) ch.getData("horizontal");

                    Point coordinates = ChartUtils.getCoordinatesFromRealValues(ch, blaze.getCentralWavelength(), blaze.getScale());

                    event.gc.setForeground(getColor(horizontal ? BLUE : CYAN));
                    event.gc.drawLine(0, (int) coordinates.y, event.width, (int) coordinates.y);

                    event.gc.setForeground(getColor(horizontal ? CYAN : BLUE));
                    event.gc.drawLine((int) coordinates.x, 0, (int) coordinates.x, event.height);
                })
                .makeAllSeriesEqualRange()
                .data("blaze", blazes[index])
                .data("horizontal", false)
                .forceFocus()
                .build(ComponentManager.clearAndGetScene());
    }

    private static void fineTuneRectificationPoints(EchelleSpectrum spectrum, Set<Integer> selectedIndices,
                                                    Blaze[] blazes, RectifyAsset[] rectifyAssets, int index) {
        XYSeries currentSeries = spectrum.getOriginalSeries()[index];
        rectify("#" + (index + 1),
                currentSeries,
                blazes[index].toRectifyAsset(),
                builder -> {
                    for (int i = max(index - 2, 0); i <= min(index + 2, rectifyAssets.length - 1); i++) {
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
                            -> rectifySingleEchelle(spectrum, selectedIndices, blazes, rectifyAssets, index + 1));
                });
    }

    private static void updateChart(Chart chart) {
        ISeries iSeries = chart.getSeriesSet().getSeries("series");
        XYSeries series = new XYSeries(iSeries.getXSeries(), iSeries.getYSeries());

        Blaze blaze = (Blaze) chart.getData("blaze");

        double[] blazeYSeries = blaze.series().getYSeries();
        double[] residuals = ArrayUtils.createArray(series.getLength(), i -> abs(series.getY(i) - blazeYSeries[i]));

        chart.getSeriesSet().getSeries("blaze").setYSeries(blazeYSeries);
        chart.getSeriesSet().getSeries("residuals").setYSeries(residuals);
        chart.redraw();
    }

    private static void rectify(String title, XYSeries series, RectifyAsset asset,
                                UnaryOperator<ChartBuilder> operator, Consumer<RectifyAsset> finish) {
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
            spectrum.removeFunctionAsset(RECTIFY_SERIALIZE_KEY);
        } else {
            spectrum.putFunctionAsset(RECTIFY_SERIALIZE_KEY, asset);
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
        spectrum.getFunctionAsset(BLAZE_SERIALIZE_KEY, BlazeAsset.class)
                .ifPresent(asset -> {
                    if (asset.isEmpty()) {
                        spectrum.removeFunctionAsset(BLAZE_SERIALIZE_KEY);
                    }
                });

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
