package cz.cuni.mff.respefo.function.debug;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.component.ToolBar;
import cz.cuni.mff.respefo.component.VerticalToggle;
import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.common.ChartKeyListener;
import cz.cuni.mff.respefo.function.common.DragMouseListener;
import cz.cuni.mff.respefo.function.common.ZoomMouseWheelListener;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.function.rectify.EchelleSelectionDialog;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.resources.ColorManager;
import cz.cuni.mff.respefo.resources.ImageResource;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.spectrum.format.EchelleSpectrum;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.collections.DoubleArrayList;
import cz.cuni.mff.respefo.util.collections.Tuple;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;
import cz.cuni.mff.respefo.util.utils.MathUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Display;
import org.orangepalantir.leastsquares.Fitter;
import org.orangepalantir.leastsquares.Function;
import org.orangepalantir.leastsquares.fitters.MarquardtFitter;
import org.swtchart.Chart;
import org.swtchart.Range;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import static cz.cuni.mff.respefo.resources.ColorResource.*;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.LineSeriesBuilder.lineSeries;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.newChart;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.util.Arrays.stream;

@Fun(name = "Blaze", fileFilter = SpefoFormatFileFilter.class, group = "Debug")
public class BlazeFunction implements SingleFileFunction {

    private static final double K = 565754; // More like 565660.2133333334 by my calculations

    @Override
    public void execute(File file) {
        Spectrum spectrum;
        try {
            spectrum = Spectrum.open(file);
        } catch (SpefoException e) {
            Message.error("Couldn't open file", e);
            return;
        }

        if (spectrum.getFormat() != EchelleSpectrum.FORMAT) {
            Message.warning("Spectrum is not of Echelle type");
            return;
        }

        EchelleSpectrum echelleSpectrum = (EchelleSpectrum) spectrum;
        XYSeries[] xySeries = echelleSpectrum.getOriginalSeries();

        String[][] names = new String[xySeries.length][3];
        for (int i = 0; i <= xySeries.length - 1; i++) {
            XYSeries series = xySeries[i];
            names[i] = new String[]{
                    "",
                    Integer.toString(i + 1),
                    Double.toString(series.getX(0)),
                    Double.toString(series.getLastX())
            };
        }

        EchelleSelectionDialog dialog = new EchelleSelectionDialog(names, new boolean[xySeries.length]);
        if (dialog.openIsNotOk()) {
            return;
        }

        List<Integer> indicesToEdit = dialog.getIndicesToEdit();
        Display.getCurrent().asyncExec(() -> single(echelleSpectrum, indicesToEdit.iterator(), xySeries));
    }

    private static void setUpTab() {
        final ToolBar.Tab tab = ComponentManager.getRightToolBar().addTab(parent -> new VerticalToggle(parent, SWT.DOWN),
                "Parameters", "Parameters", ImageResource.TOOLS_LARGE);


    }

    private static void single(EchelleSpectrum spectrum, Iterator<Integer> indicesIterator, XYSeries[] xySeries) {
        int currentIndex = indicesIterator.next();

        XYSeries currentSeries = xySeries[currentIndex];
        Tuple.Two<XYSeries, double[]> result = calculateBlaze(currentSeries, currentIndex);
        XYSeries blaze = result.a;
        double[] parameters = result.b;
        XYSeries residuals = new XYSeries(currentSeries.getXSeries(),
                ArrayUtils.createArray(currentSeries.getLength(), i -> abs(currentSeries.getY(i) - blaze.getY(i))));

        Chart chart = newChart()
                .title("#" + (currentIndex + 1))
                .xAxisLabel("X axis")
                .yAxisLabel("Y axis")
                .keyListener(ChartKeyListener::makeAllSeriesEqualRange)
                .keyListener(ch -> new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.keyCode == SWT.END) {
                            if (indicesIterator.hasNext()) {
                                ComponentManager.getDisplay().asyncExec(() -> single(spectrum, indicesIterator, xySeries));
                            } else {
                                ComponentManager.clearScene(true);
                            }
                        }
                    }
                })
                .mouseAndMouseMoveListener(DragMouseListener::new)
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
                .forceFocus()
                .build(ComponentManager.clearAndGetScene());

        chart.getPlotArea().addPaintListener(event -> {
            Range xRange = chart.getAxisSet().getXAxis(0).getRange();
            Range yRange = chart.getAxisSet().getYAxis(0).getRange();
            double xDiff = xRange.upper - xRange.lower;
            double yDiff = yRange.upper - yRange.lower;

            event.gc.setForeground(ColorManager.getColor(ALPHA_BLUE));

            int y = (int) (event.height * (yRange.upper - parameters[2]) / yDiff);
            event.gc.drawLine(0, y, event.width, y);

            int x = (int) (event.width * (parameters[0] - xRange.lower) / xDiff);
            event.gc.drawLine(x, 0, x, event.height);
        });

        Log.info("Calculated blaze function for index #" + (currentIndex + 1) + " = order " + (125 - currentIndex)
                + "\nCentral wavelength: " + parameters[0]
                + "\nAlpha: " + parameters[1]
                + "\nScale: " + parameters[2]
        );
    }

    private static Tuple.Two<XYSeries, double[]> calculateBlaze(XYSeries series, int index) {
        int order = 125 - index;

        double centralWavelength = K / order;
        double scale = MathUtils.intep(series.getXSeries(), series.getYSeries(), new double[]{centralWavelength})[0];

        Function fun = new Function() {
            @Override
            public double evaluate(double[] values, double[] parameters) {
                return parameters[2] * r(values[0], order, parameters[0], parameters[1]);
            }

            @Override
            public int getNParameters() {
                return 3; // central wavelength, alpha, scale
            }

            @Override
            public int getNInputs() {
                return 1;
            }
        };

        Fitter fit = new MarquardtFitter(fun);
        fit.setData(
                stream(series.getXSeries()).mapToObj(x -> new double[]{x}).toArray(double[][]::new),
                series.getYSeries()
        );
        fit.setParameters(new double[] {centralWavelength, 1.0, scale});
        fit.fitData();

        final double[] parameters = fit.getParameters();
        double[] ySeries = stream(series.getXSeries())
                .map(x -> parameters[2] * r(x, order, parameters[0], parameters[1]))
                .toArray();

        return new Tuple.Two<>(new XYSeries(series.getXSeries(), ySeries), parameters);
    }

    private static double r(double lambda, int order, double centralWavelength, double alpha) {
        double x = order * (1 - centralWavelength / lambda);
        double argument = PI * alpha * x;
        return Math.pow(Math.sin(argument) / argument, 2);
    }

    private static XYSeries calculateBlazeFunction(XYSeries[] xySeries) {
        double[] xSeries = new double[62 * 801];
        double[] ySeries = new double[62 * 801];

        for (int i = 0; i < 62; i++) {
            int order = 125 - i;
            XYSeries series = xySeries[i];

            double centralWavelength = K / order;
            double scale = MathUtils.intep(series.getXSeries(), series.getYSeries(), new double[]{centralWavelength})[0];

            Function fun = new Function() {
                @Override
                public double evaluate(double[] values, double[] parameters) {
                    return parameters[2] * r(values[0], order, parameters[0], parameters[1]);
                }

                @Override
                public int getNParameters() {
                    return 3;
                }

                @Override
                public int getNInputs() {
                    return 1;
                }
            };

            Fitter fit = new MarquardtFitter(fun);
            fit.setData(
                    stream(series.getXSeries()).mapToObj(x -> new double[]{x}).toArray(double[][]::new),
                    series.getYSeries()
            );
            fit.setParameters(new double[] {centralWavelength, 1.0, scale});
            fit.fitData();

            double[] parameters = fit.getParameters();
            centralWavelength = parameters[0];
            double alpha = parameters[1];
            scale = parameters[2];

            double[] residuals = calculateResiduals(series, scale, order, centralWavelength, alpha);
            double mean = stream(residuals).average().orElseThrow(IllegalStateException::new);

            DoubleArrayList xs = new DoubleArrayList(801);
            DoubleArrayList ys = new DoubleArrayList(801);

            for (int j = 0; j < 801; j++) {
                if (residuals[j] < mean * 1.5) {
                    xs.add(series.getX(j));
                    ys.add(series.getY(j));
                }
            }

            fit.setData(
                    xs.stream().mapToObj(x -> new double[]{x}).toArray(double[][]::new),
                    ys.toArray()
            );
            fit.fitData();

            parameters = fit.getParameters();
            centralWavelength = parameters[0];
            alpha = parameters[1];
            scale = parameters[2];

            for (int j = 0; j < 801; j++) {
                xSeries[i * 801 + j] = series.getX(j);
                ySeries[i * 801 + j] = scale * r(series.getX(j), order, centralWavelength, alpha);
            }
        }

        return new XYSeries(xSeries, ySeries);
    }

    private static double[] calculateResiduals(XYSeries series, double scale, int order, double centralWavelength, double alpha) {
        return ArrayUtils.createArray(801,
                j -> abs(series.getY(j) - scale * r(series.getX(j), order, centralWavelength, alpha)));
    }
}
