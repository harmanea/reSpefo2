package cz.cuni.mff.respefo.function.asset.rv;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.component.ToolBar;
import cz.cuni.mff.respefo.component.VerticalToggle;
import cz.cuni.mff.respefo.format.XYSeries;
import cz.cuni.mff.respefo.function.asset.common.ChartKeyListener;
import cz.cuni.mff.respefo.function.asset.common.HorizontalDragMouseListener;
import cz.cuni.mff.respefo.function.asset.common.Measurement;
import cz.cuni.mff.respefo.function.asset.common.Measurements;
import cz.cuni.mff.respefo.resources.ImageResource;
import cz.cuni.mff.respefo.util.DefaultSelectionListener;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;
import cz.cuni.mff.respefo.util.utils.ChartUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;

import java.util.Arrays;
import java.util.function.Consumer;

import static cz.cuni.mff.respefo.resources.ColorResource.BLUE;
import static cz.cuni.mff.respefo.resources.ColorResource.GREEN;
import static cz.cuni.mff.respefo.util.builders.ChartBuilder.AxisLabel.RELATIVE_FLUX;
import static cz.cuni.mff.respefo.util.builders.ChartBuilder.LineSeriesBuilder.lineSeries;
import static cz.cuni.mff.respefo.util.builders.ChartBuilder.chart;
import static cz.cuni.mff.respefo.util.builders.LabelBuilder.label;
import static cz.cuni.mff.respefo.util.utils.ChartUtils.getRelativeHorizontalStep;

public class RVMeasurementController {
    private static final String MIRRORED_SERIES_NAME = "mirrored";

    private final XYSeries series;
    private final double deltaRV;

    private Measurements measurements;
    private Consumer<MeasureRVResults> callback;

    private MeasureRVResults results;
    private int index;
    private double shift;

    private Table table;

    public RVMeasurementController(XYSeries series, double deltaRV) {
        this.series = series;
        this.deltaRV = deltaRV;
    }

    public void measure(Measurements measurements, Consumer<MeasureRVResults> callback) {
        this.measurements = measurements;
        this.callback = callback;

        results = new MeasureRVResults();

        ComponentManager.clearScene(true);

        setUpLinesBar();
        setUpRVStepBar();

        index = 0;
        measureSingle();
    }

    // TODO: implement this
    private void setUpRVStepBar() {
        final ToolBar.Tab rvStepTab = ComponentManager.getRightToolBar().addTab(parent -> new VerticalToggle(parent, SWT.DOWN),
                "RV Step", "RV Step", ImageResource.RULER_LARGE);

        label(rvStepTab.getWindow(), SWT.CENTER)
                .layoutData(new GridData(GridData.FILL_BOTH))
                .text("Not yet implemented")
                .build();
    }

    private void setUpLinesBar() {
        final ToolBar.Tab linesTab = ComponentManager.getRightToolBar().addTab(parent -> new VerticalToggle(parent, SWT.DOWN),
                "Lines", "Measurements", ImageResource.LINES_LARGE);

        linesTab.addTopBarButton("Previous line", ImageResource.LEFT_ARROW, () -> {
            if (index > 0) {
                index--;
                measureSingle();
            }
        });
        linesTab.addTopBarButton("Next line", ImageResource.RIGHT_ARROW, () -> {
            if (index + 1 < measurements.size()) {
                index++;
                measureSingle();
            }
        });
        linesTab.addTopBarButton("Finish", ImageResource.CHECK, this::finish);

        table = new Table(linesTab.getWindow(), SWT.SINGLE);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        table.setHeaderVisible(false);
        table.setLinesVisible(true);

        TableColumn nameColumn = new TableColumn(table, SWT.LEFT);
        TableColumn lZeroColumn = new TableColumn(table, SWT.RIGHT);

        for (Measurement measurement : measurements) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(0, measurement.getName());
            item.setText(1, Double.toString(measurement.getL0()));
        }

        table.getParent().addControlListener(ControlListener.controlResizedAdapter(e -> {
            Rectangle area = table.getParent().getClientArea();
            ScrollBar vBar = table.getVerticalBar();
            int width = area.width - table.computeTrim(0, 0, 0, 0).width - vBar.getSize().x;
            if (table.computeSize(SWT.DEFAULT, SWT.DEFAULT).y > area.height + table.getHeaderHeight()) {
                // Subtract the scrollbar width from the total column width
                Point vBarSize = vBar.getSize();
                width -= vBarSize.x;
            }

            if (table.getSize().x > area.width) {
                // Table is shrinking
                lZeroColumn.setWidth(width / 3);
                nameColumn.setWidth(width - lZeroColumn.getWidth());
                table.setSize(area.width, area.height);

            } else {
                // Table is expanding
                table.setSize(area.width, area.height);
                lZeroColumn.setWidth(width / 3);
                nameColumn.setWidth(width - lZeroColumn.getWidth());
            }
        }));

        table.addSelectionListener(new DefaultSelectionListener(event -> {
            index = table.getSelectionIndex();
            measureSingle();
        }));

        linesTab.show();
    }

    private void measureSingle() {
        Measurement measurement = measurements.get(index);

        shift = 0;

        Chart chart = chart(ComponentManager.clearAndGetScene(false))
                .title(measurement.getName() + " " + measurement.getL0())
                .xAxisLabel("index")
                .yAxisLabel(RELATIVE_FLUX)
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
                        if (dialog.openIsOk()) {
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

                    case SWT.END:
                    case SWT.ESC:
                        if (index + 1 < measurements.size()) {
                            index += 1;
                            ComponentManager.getDisplay().asyncExec(() -> measureSingle());
                        } else {
                            finish();
                        }
                        break;

                    case SWT.TAB:
                        if (e.stateMask == SWT.CTRL) {
                            measurement.decreaseRadius();
                        } else {
                            measurement.increaseRadius();
                        }

                        XYSeries newSeries = computeSeries(measurement);
                        chart.getSeriesSet().getSeries(MIRRORED_SERIES_NAME).setXSeries(ArrayUtils.addValueToArrayElements(newSeries.getXSeries(), shift));
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

        table.setSelection(index);
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
        int midIndex = Arrays.binarySearch(series.getXSeries(), measurement.getL0());
        if (midIndex < 0) {
            midIndex = -midIndex - 1;

            double low = series.getX(midIndex - 1);
            double high = series.getX(midIndex);

            mid = midIndex - 1 + ((measurement.getL0() - low) / (high - low));
        } else {
            mid = midIndex;
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

    private void finish() {
        if (results.isEmpty()){
            if (!Message.question("No measurements were performed, are you sure you want to finish?")) {
                return;
            }
        } else if (!Message.question("Are you sure you want to finish?")) {
            return;
        }

        ComponentManager.clearScene(true);
        callback.accept(results);
    }
}
