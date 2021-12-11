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
import cz.cuni.mff.respefo.resources.ImageResource;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.spectrum.format.EchelleSpectrum;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.collections.Point;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;
import cz.cuni.mff.respefo.util.utils.ChartUtils;
import cz.cuni.mff.respefo.util.utils.MathUtils;
import cz.cuni.mff.respefo.util.widget.ButtonBuilder;
import cz.cuni.mff.respefo.util.widget.CompositeBuilder;
import cz.cuni.mff.respefo.util.widget.LabelBuilder;
import cz.cuni.mff.respefo.util.widget.TextBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.orangepalantir.leastsquares.Fitter;
import org.orangepalantir.leastsquares.Function;
import org.orangepalantir.leastsquares.fitters.MarquardtFitter;
import org.swtchart.Chart;
import org.swtchart.ISeries;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import static cz.cuni.mff.respefo.resources.ColorManager.getColor;
import static cz.cuni.mff.respefo.resources.ColorResource.*;
import static cz.cuni.mff.respefo.util.utils.MathUtils.linearInterpolation;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.LineSeriesBuilder.lineSeries;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.newChart;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.String.valueOf;
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

        ComponentManager.clearScene(true);
        Consumer<double[]> textsUpdater = setUpTab();

        Display.getCurrent().asyncExec(() -> single(indicesToEdit.iterator(), xySeries, textsUpdater));
    }

    private static Consumer<double[]> setUpTab() {
        final ToolBar.Tab tab = ComponentManager.getRightToolBar().addTab(parent -> new VerticalToggle(parent, SWT.DOWN),
                "Parameters", "Parameters", ImageResource.TOOLS_LARGE);

        CompositeBuilder compositeBuilder = CompositeBuilder.newComposite()
                .gridLayoutData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING)
                .layout(new GridLayout());
        LabelBuilder labelBuilder = LabelBuilder.newLabel(SWT.LEFT).bold();
        TextBuilder textBuilder = TextBuilder.newText(SWT.SINGLE).gridLayoutData(GridData.FILL_HORIZONTAL);
        ButtonBuilder buttonBuilder = ButtonBuilder.newButton(SWT.PUSH).text("Confirm");
        LabelBuilder separatorBuilder = LabelBuilder.newLabel(SWT.SEPARATOR | SWT.HORIZONTAL).gridLayoutData(GridData.FILL_HORIZONTAL);

        // central wavelength
        Composite composite = compositeBuilder.build(tab.getWindow());
        labelBuilder.text("Central wavelength").build(composite);
        Text cwText = textBuilder.build(composite);
        buildButton(buttonBuilder, composite, cwText, 0);

        separatorBuilder.build(tab.getWindow());

        // alpha
        composite = compositeBuilder.build(tab.getWindow());
        labelBuilder.text("Alpha left").build(composite);
        Text alphaLeftText = textBuilder.build(composite);
        buildButton(buttonBuilder, composite, alphaLeftText, 1);

        composite = compositeBuilder.build(tab.getWindow());
        labelBuilder.text("Alpha right").build(composite);
        Text alphaRightText = textBuilder.build(composite);
        buildButton(buttonBuilder, composite, alphaRightText, 2);

        separatorBuilder.build(tab.getWindow());

        // scale
        composite = compositeBuilder.build(tab.getWindow());
        labelBuilder.text("Scale").build(composite);
        Text scaleText = textBuilder.build(composite);
        buildButton(buttonBuilder, composite, scaleText, 3);

        tab.show();

        return params -> {
            cwText.setText(valueOf(params[0]));
            alphaLeftText.setText(valueOf(params[1]));
            alphaRightText.setText(valueOf(params[2]));
            scaleText.setText(valueOf(params[3]));
        };
    }

    private static void buildButton(ButtonBuilder buttonBuilder, Composite composite, Text text, int parameter) {
        buttonBuilder
                .onSelection(e -> {
                    try {
                        Chart chart = (Chart) ComponentManager.getScene().getChildren()[0];
                        double[] parameters = (double[]) chart.getData("parameters");
                        parameters[parameter] = Double.parseDouble(text.getText());
                        updateChart(chart);
                        chart.forceFocus();

                    } catch (NumberFormatException exception) {
                        // ignore
                    }
                })
                .build(composite);
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

    private static void single(Iterator<Integer> indicesIterator, XYSeries[] xySeries, Consumer<double[]> textsUpdater) {
        int currentIndex = indicesIterator.next();
        int order = 125 - currentIndex;

        XYSeries currentSeries = xySeries[currentIndex];
        double[] parameters = calculateBlazeParameters(currentSeries, order);
        XYSeries blaze = new XYSeries(currentSeries.getXSeries(), blaze(currentSeries.getXSeries(), parameters, order));
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
                            XYSeries rectified = new XYSeries(currentSeries.getXSeries(),
                                    ArrayUtils.divideArrayValues(currentSeries.getYSeries(), ch.getSeriesSet().getSeries("blaze").getYSeries()));
                            ComponentManager.getDisplay().asyncExec(() -> rectified(indicesIterator, xySeries, rectified, textsUpdater));
                        }
                    }
                })
                .mouseAndMouseMoveListener(ch -> new BlazeMouseListener(ch, () -> {
                    textsUpdater.accept((double[]) ch.getData("parameters"));
                    updateChart(ch);
                }))
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
                .data("parameters", parameters)
                .data("order", 125 - currentIndex)
                .data("horizontal", false)
                .forceFocus()
                .build(ComponentManager.clearAndGetScene(false));

        chart.getPlotArea().addPaintListener(event -> {
            double[] params = (double[]) chart.getData("parameters");
            boolean horizontal = (boolean) chart.getData("horizontal");

            Point coordinates = ChartUtils.getCoordinatesFromRealValues(chart, params[0], params[3]);

            event.gc.setForeground(getColor(horizontal ? BLUE : CYAN));
            event.gc.drawLine(0, (int) coordinates.y, event.width, (int) coordinates.y);

            event.gc.setForeground(getColor(horizontal ? CYAN : BLUE));
            event.gc.drawLine((int) coordinates.x, 0, (int) coordinates.x, event.height);
        });

        Log.info("Calculated blaze function for index #" + (currentIndex + 1) + " = order " + (125 - currentIndex)
                + "\nCentral wavelength: " + parameters[0]
                + "\nAlpha: " + parameters[1] + " - " + parameters[2]
                + "\nScale: " + parameters[3]
        );

        textsUpdater.accept(parameters);
    }

    private static void rectified(Iterator<Integer> indicesIterator, XYSeries[] xySeries, XYSeries rectified, Consumer<double[]> textsUpdater) {
        newChart()
                .xAxisLabel("X axis")
                .yAxisLabel("Y axis")
                .keyListener(ChartKeyListener::makeAllSeriesEqualRange)
                .keyListener(ch -> new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.keyCode == SWT.END) {
                            if (indicesIterator.hasNext()) {
                                ComponentManager.getDisplay().asyncExec(() -> single(indicesIterator, xySeries, textsUpdater));
                            } else {
                                ComponentManager.clearScene(true);
                            }
                        }
                    }
                })
                .mouseAndMouseMoveListener(DragMouseListener::new)
                .mouseWheelListener(ZoomMouseWheelListener::new)
                .series(lineSeries()
                        .name("rectfied")
                        .color(GREEN)
                        .series(rectified))
                .makeAllSeriesEqualRange()
                .forceFocus()
                .build(ComponentManager.clearAndGetScene(false));
    }

    private static double[] calculateBlazeParameters(XYSeries series, int order) {
        double centralWavelength = K / order;
        double scale = MathUtils.intep(series.getXSeries(), series.getYSeries(), new double[]{centralWavelength})[0];

        Function fun = new Function() {
            final double xLow = series.getX(0);
            final double xHigh = series.getLastX();

            @Override
            public double evaluate(double[] values, double[] parameters) {
                double alpha = linearInterpolation(xLow, parameters[1], xHigh, parameters[2], values[0]);
                return parameters[3] * r(values[0], order, parameters[0], alpha);
            }

            @Override
            public int getNParameters() {
                return 4; // central wavelength, alpha left, alpha right, scale
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
        fit.setParameters(new double[] {centralWavelength, 1.0, 1.0, scale});
        fit.fitData();

        return fit.getParameters();
    }

    private static double[] blaze(double[] xSeries, double[] parameters, int order) {
        double xLow = xSeries[0];
        double xHigh = xSeries[xSeries.length - 1];

        return stream(xSeries)
                .map(x -> parameters[3] * r(x, order, parameters[0], linearInterpolation(xLow, parameters[1], xHigh, parameters[2], x)))
                .toArray();
    }

    private static double r(double lambda, int order, double centralWavelength, double alpha) {
        double x = order * (1 - centralWavelength / lambda);
        double argument = PI * alpha * x;
        return Math.pow(Math.sin(argument) / argument, 2);
    }
}
