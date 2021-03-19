package cz.cuni.mff.respefo.function.disp;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.function.common.HorizontalDragMouseListener;
import cz.cuni.mff.respefo.util.collections.XYSeries;
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
import static cz.cuni.mff.respefo.util.utils.ChartUtils.getRelativeHorizontalStep;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.AxisLabel.PIXELS;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.AxisLabel.RELATIVE_FLUX;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.LineSeriesBuilder.lineSeries;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.newChart;

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

        newChart()
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
                .keyListener(ch -> new DispersionKeyListener(ch, MIRRORED_SERIES_NAME, hint,
                        () -> applyShift(ch, -getRelativeHorizontalStep(ch)),
                        () -> applyShift(ch, getRelativeHorizontalStep(ch))))
                .keyListener(ch -> new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        switch (e.keyCode) {
                            case SWT.CR:
                            case SWT.INSERT:
                                callback.accept(value);
                                break;

                            case SWT.END:
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
                                ch.getSeriesSet().getSeries(MIRRORED_SERIES_NAME).setXSeries(newSeries.getXSeries());
                                ch.getSeriesSet().getSeries(MIRRORED_SERIES_NAME).setYSeries(newSeries.getYSeries());
                                ch.redraw();
                                break;
                        }
                    }
                })
                .mouseAndMouseMoveListener(ch -> new HorizontalDragMouseListener(ch, shift -> applyShift(ch, shift)))
                .centerAroundSeries(MIRRORED_SERIES_NAME)
                .zoomOut()
                .forceFocus()
                .build(ComponentManager.clearAndGetScene());
    }

    private XYSeries computeSeries(XYSeries series) {
        int from = Math.max((int) Math.rint(value) - radius, 0);
        int to = Math.min((int) Math.rint(value) + radius, series.getLength() - 1);

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
