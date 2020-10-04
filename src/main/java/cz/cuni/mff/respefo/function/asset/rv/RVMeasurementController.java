package cz.cuni.mff.respefo.function.asset.rv;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.format.XYSeries;
import cz.cuni.mff.respefo.function.asset.common.ChartKeyListener;
import cz.cuni.mff.respefo.function.asset.common.HorizontalDragMouseListener;
import cz.cuni.mff.respefo.function.asset.common.Measurement;
import cz.cuni.mff.respefo.function.asset.common.Measurements;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;
import cz.cuni.mff.respefo.util.utils.ChartUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Consumer;

import static cz.cuni.mff.respefo.resources.ColorResource.BLUE;
import static cz.cuni.mff.respefo.resources.ColorResource.GREEN;
import static cz.cuni.mff.respefo.util.builders.ChartBuilder.LineSeriesBuilder.lineSeries;
import static cz.cuni.mff.respefo.util.builders.ChartBuilder.chart;
import static cz.cuni.mff.respefo.util.utils.ChartUtils.getRelativeHorizontalStep;

public class RVMeasurementController {
    private static final String MIRRORED_SERIES_NAME = "mirrored";

    private final XYSeries series;
    private final double deltaRV;

    private MeasureRVResults results;
    private double shift;

    public RVMeasurementController(XYSeries series, double deltaRV) {
        this.series = series;
        this.deltaRV = deltaRV;
    }

    public void measure(Measurements measurements, Consumer<MeasureRVResults> callback) {
        results = new MeasureRVResults();

        measureSingle(measurements.iterator(), callback);
    }

    private void measureSingle(Iterator<Measurement> measurements, Consumer<MeasureRVResults> callback) {
        Measurement measurement = measurements.next();

        shift = 0;

        Chart chart = chart(ComponentManager.clearAndGetScene())
                .title(measurement.getName() + " " + measurement.getL0())
                .xAxisLabel("pixels")
                .yAxisLabel("relative flux I(λ)")
                .series(lineSeries()
                        .name("original")
                        .xSeries(ArrayUtils.fillArray(series.getYSeries().length, 0, 1))
                        .ySeries(series.getYSeries())
                        .color(GREEN))
                .series(lineSeries()
                        .name(MIRRORED_SERIES_NAME)
                        .series(computeSeries(measurement))
                        .color(BLUE))
                .keyListener(ch -> ChartKeyListener.customAction(ch, ch2 -> {
                    ChartUtils.centerAroundSeries(ch, MIRRORED_SERIES_NAME);
                    ch.getAxisSet().zoomOut();
                }))
                .mouseAndMouseMoveListener(ch -> new HorizontalDragMouseListener(ch, value -> applyShift(ch, value)))
                .centerAroundSeries(MIRRORED_SERIES_NAME)
                .forceFocus()
                .build();

        chart.getAxisSet().zoomOut();

        chart.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.keyCode) {
                    case SWT.CR:
                        MeasurementInputDialog dialog = new MeasurementInputDialog(measurement.isCorrection());
                        if (dialog.open() == SWT.OK) {
                            MeasureRVResult result = new MeasureRVResult(
                                    deltaRV * (shift / 2),
                                    shift,
                                    measurement.getRadius(),
                                    dialog.getCategory(),
                                    measurement.getL0(),
                                    measurement.getName(),
                                    dialog.getComment()
                            );
                            results.add(result);
                        }
                        break;

                    case SWT.ESC:
                        if (measurements.hasNext()) {
                            ComponentManager.getDisplay().asyncExec(() -> measureSingle(measurements, callback));
                        } else {
                            callback.accept(results);
                        }
                        break;

                    case SWT.TAB:
                        if (e.stateMask == SWT.CTRL) {
                            measurement.decreaseRadius();
                        } else {
                            measurement.increaseRadius();
                        }

                        XYSeries newSeries = computeSeries(measurement);
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

    private XYSeries computeSeries(Measurement measurement) {
        int from = Arrays.binarySearch(series.getXSeries(), measurement.getLowerBound());
        if (from < 0) {
            from = -from - 1;
        }

        int to = Arrays.binarySearch(series.getXSeries(), measurement.getUpperBound());
        if (to < 0) {
            to = -to - 1;
        }

        double[] mirroredYSeries = ArrayUtils.reverseArray(Arrays.copyOfRange(series.getYSeries(), from, to));

        double mid;
        int index = Arrays.binarySearch(series.getXSeries(), measurement.getL0());
        if (index < 0) {
            index = -index - 1;

            double low = series.getX(index - 1);
            double high = series.getX(index);

            mid = index - 1 + ((measurement.getL0() - low) / (high - low));
        } else {
            mid = index;
        }

        double[] mirroredXSeries = new double[mirroredYSeries.length];
        for (int i = 0; i < mirroredXSeries.length; i++) {
            mirroredXSeries[i] = 2 * mid - to + 1 + i;
        }

        return new XYSeries(mirroredXSeries, mirroredYSeries);
    }

    private void applyShift(Chart chart, double value) {
        shift += value;

        ILineSeries mirroredSeries = (ILineSeries) chart.getSeriesSet().getSeries(MIRRORED_SERIES_NAME);
        mirroredSeries.setXSeries(ArrayUtils.addValueToArrayElements(mirroredSeries.getXSeries(), value));

        chart.redraw();
        chart.forceFocus();
    }
}
