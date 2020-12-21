package cz.cuni.mff.respefo.function.asset.dispersion;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.format.XYSeries;
import cz.cuni.mff.respefo.function.asset.common.ChartKeyListener;
import cz.cuni.mff.respefo.function.asset.common.HorizontalDragMouseListener;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;

import java.util.Arrays;
import java.util.function.DoubleConsumer;

import static cz.cuni.mff.respefo.resources.ColorResource.BLUE;
import static cz.cuni.mff.respefo.resources.ColorResource.GREEN;
import static cz.cuni.mff.respefo.util.builders.widgets.ChartBuilder.AxisLabel.PIXELS;
import static cz.cuni.mff.respefo.util.builders.widgets.ChartBuilder.AxisLabel.RELATIVE_FLUX;
import static cz.cuni.mff.respefo.util.builders.widgets.ChartBuilder.LineSeriesBuilder.lineSeries;
import static cz.cuni.mff.respefo.util.builders.widgets.ChartBuilder.newChart;
import static cz.cuni.mff.respefo.util.utils.ChartUtils.getRelativeHorizontalStep;

public class DispersionMeasurementController {
    private static final String MIRRORED_SERIES_NAME = "mirrored";

    private final XYSeries seriesA;
    private final XYSeries seriesB;

    private double value;
    private int radius;

    public DispersionMeasurementController(XYSeries seriesA, XYSeries seriesB) {
        this.seriesA = seriesA;
        this.seriesB = seriesB;
    }

    public void measure(ComparisonLineMeasurement measurement, double hint, Runnable callback) {
        measureSingle(seriesA, "A", measurement, hint, xUp ->
            measureSingle(seriesB, "B", measurement, hint, xDown -> {
                measurement.setxUp(xUp);
                measurement.setxDown(xDown);
                ComponentManager.getDisplay().asyncExec(callback);
            }));
    }

    private void measureSingle(XYSeries series, String label, ComparisonLineMeasurement measurement, double hint, DoubleConsumer callback) {
        value = hint;
        radius = 10;

        final Chart chart = newChart()
                .title(label + " " + measurement.getLaboratoryValue())
                .xAxisLabel(PIXELS)
                .yAxisLabel(RELATIVE_FLUX)
                .series(lineSeries()
                        .name("original")
                        .series(series)
                        .color(GREEN))
                .series(lineSeries()
                        .name(MIRRORED_SERIES_NAME)
                        .series(computeSeries(series))
                        .color(BLUE))
                .keyListener(ch -> ChartKeyListener.centerAroundSeries(ch, MIRRORED_SERIES_NAME))
                .mouseAndMouseMoveListener(ch -> new HorizontalDragMouseListener(ch, shift -> applyShift(ch, shift)))
                .centerAroundSeries(MIRRORED_SERIES_NAME)
                .focus()
                .build(ComponentManager.clearAndGetScene());

        chart.getAxisSet().zoomOut();

        chart.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.keyCode) {
                    case SWT.CR:
                        callback.accept(value);
                        break;

                    case SWT.ESC:
                        callback.accept(Double.NaN);
                        break;

                    case SWT.TAB:
                        if (e.stateMask == SWT.CTRL) {
                            radius = Math.max(1, radius / 2);
                        } else {
                            radius *= 2;
                        }

                        XYSeries newSeries = computeSeries(series);
                        chart.getSeriesSet().getSeries(MIRRORED_SERIES_NAME).setXSeries(newSeries.getXSeries());
                        chart.getSeriesSet().getSeries(MIRRORED_SERIES_NAME).setYSeries(newSeries.getYSeries());
                        chart.redraw();
                        break;

                    case 'n':
                        applyShift(chart, -getRelativeHorizontalStep(chart));
                        break;

                    case 'm':
                        applyShift(chart, getRelativeHorizontalStep(chart));
                        break;

                }
            }
        });

        chart.redraw();
        chart.forceFocus();
    }

    private XYSeries computeSeries(XYSeries series) {
        int from = Math.max((int) Math.rint(value) - radius, 0);
        int to = Math.min((int) Math.rint(value) + radius, series.getYSeries().length - 1);

        double[] mirroredYSeries = ArrayUtils.reverseArray(Arrays.copyOfRange(series.getYSeries(), from, to));
        double[] mirroredXSeries = ArrayUtils.createArray(mirroredYSeries.length, i -> 2 * value - series.getX(to - i - 1));

        return new XYSeries(mirroredXSeries, mirroredYSeries);
    }

    private void applyShift(Chart chart, double shift) {
        value += shift / 2;

        ILineSeries series = (ILineSeries) chart.getSeriesSet().getSeries(MIRRORED_SERIES_NAME);
        series.setXSeries(ArrayUtils.addValueToArrayElements(series.getXSeries(), shift));

        chart.redraw();
        chart.forceFocus();
    }
}
