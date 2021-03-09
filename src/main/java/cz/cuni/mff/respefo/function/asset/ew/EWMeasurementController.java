package cz.cuni.mff.respefo.function.asset.ew;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.component.ToolBar;
import cz.cuni.mff.respefo.component.VerticalToggle;
import cz.cuni.mff.respefo.format.XYSeries;
import cz.cuni.mff.respefo.function.asset.common.ChartKeyListener;
import cz.cuni.mff.respefo.function.asset.common.Measurement;
import cz.cuni.mff.respefo.function.asset.common.Measurements;
import cz.cuni.mff.respefo.resources.ColorManager;
import cz.cuni.mff.respefo.resources.ImageResource;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Table;
import org.swtchart.Chart;
import org.swtchart.Range;

import java.util.function.Consumer;

import static cz.cuni.mff.respefo.resources.ColorResource.*;
import static cz.cuni.mff.respefo.util.builders.widgets.ChartBuilder.AxisLabel.RELATIVE_FLUX;
import static cz.cuni.mff.respefo.util.builders.widgets.ChartBuilder.AxisLabel.WAVELENGTH;
import static cz.cuni.mff.respefo.util.builders.widgets.ChartBuilder.LineSeriesBuilder.lineSeries;
import static cz.cuni.mff.respefo.util.builders.widgets.ChartBuilder.newChart;
import static cz.cuni.mff.respefo.util.builders.widgets.TableBuilder.newTable;
import static cz.cuni.mff.respefo.util.utils.MathUtils.DOUBLE_PRECISION;

public class EWMeasurementController {
    private final XYSeries series;

    private Measurements measurements;
    private Consumer<MeasureEWResults> callback;

    private MeasureEWResults results;
    private int index;
    private double shift;
    private int activeLine = -2;

    private Table table;

    public EWMeasurementController(XYSeries series) {
        this.series = series;
    }

    public void measure(Measurements measurements, Consumer<MeasureEWResults> callback) {
        this.measurements = measurements;
        this.callback = callback;

        results = new MeasureEWResults();

        ComponentManager.clearScene(true);

        setUpLinesTab();

        index = 0;
        measureSingle();
    }

    private void setUpLinesTab() {
        final ToolBar.Tab tab = ComponentManager.getRightToolBar().addTab(parent -> new VerticalToggle(parent, SWT.DOWN),
                "Lines", "Measurements", ImageResource.LINES_LARGE);

        tab.addTopBarButton("Previous line", ImageResource.LEFT_ARROW, () -> {
            if (index > 0) {
                index--;
                measureSingle();
            }
        });
        tab.addTopBarButton("Next line", ImageResource.RIGHT_ARROW, () -> {
            if (index + 1 < measurements.size()) {
                index++;
                measureSingle();
            }
        });

        table = newTable(SWT.SINGLE)
                .gridLayoutData(GridData.FILL_BOTH)
                .headerVisible(false)
                .linesVisible(true)
                .onSelection(event -> table.setSelection(index))
                .columns(2)
                .fixedAspectColumns(3, 1)
                .items(measurements, measurement -> new String[]{
                        measurement.getName(),
                        Double.toString(measurement.getL0())
                })
                .build(tab.getWindow());
        table.getColumn(1).setAlignment(SWT.RIGHT);

        tab.show();
    }

    private void measureSingle() {
        Measurement measurement = measurements.get(index);

        shift = 0;

        MeasureEWResult result = new MeasureEWResult(
                measurement.getL0(),
                measurement.getRadius(),
                measurement.getName(),
                ArrayUtils.findClosest(series.getXSeries(), measurement.getLowerBound()),
                ArrayUtils.findClosest(series.getXSeries(), measurement.getUpperBound())
        );

        final Chart chart = newChart()
                .title(measurement.getName() + " " + measurement.getL0())
                .xAxisLabel(WAVELENGTH)
                .yAxisLabel(RELATIVE_FLUX)
                .series(lineSeries()
                        .series(series)
                        .color(GREEN))
                .keyListener(ch -> ChartKeyListener.customAction(ch, ch2 -> adjustView(ch, result)))
                .mouseAndMouseMoveListener(ch -> new MeasureEWMouseListener(ch, series, sh -> {
                    updateShift(sh);
                    ch.redraw();
                    ch.forceFocus();
                }, () ->  {
                    snapToPoint(series, result);
                    ch.redraw();
                }, result, i -> {
                    activeLine = i;
                    ch.redraw();
                }))
                .forceFocus()
                .build(ComponentManager.clearAndGetScene(false));

        chart.getPlotArea().addPaintListener(event -> {
            Range range = chart.getAxisSet().getXAxis(0).getRange();
            double diff = range.upper - range.lower;

            for (int i = 0; i <= 1; i++) {
                int x = (int) (event.width * (series.getX(result.getBound(i)) + (activeLine + 2 == i ? shift : 0) - range.lower) / diff);
                event.gc.setForeground(ColorManager.getColor(activeLine + 2 == i ? CYAN : BLUE));
                event.gc.drawLine(x, 0, x, event.height);
            }
            for (int i = result.pointsCount() - 1; i >= 0; i--) {
                int x = (int) (event.width * (series.getX(result.getPoint(i)) + (activeLine == i ? shift : 0) - range.lower) / diff);
                event.gc.setForeground(ColorManager.getColor(activeLine == i ? ORANGE : GRAY));
                event.gc.drawLine(x, 0, x, event.height);
                event.gc.drawString(result.getCategory(i).name(), x + 10, 10);
            }
        });

        chart.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.keyCode) {
                    case SWT.END:
                    case SWT.ESC:
                        results.add(result);
                        if (index + 1 < measurements.size()) {
                            index += 1;
                            ComponentManager.getDisplay().asyncExec(() -> measureSingle());
                        } else {
                            finish();
                        }
                        break;

                    case SWT.CR:
                    case SWT.INSERT:
                        MeasureEWCategoryDialog dialog = new MeasureEWCategoryDialog();
                        if (dialog.openIsOk()) {
                            Range xRange = chart.getAxisSet().getXAxis(0).getRange();
                            int newIndex = ArrayUtils.findClosest(series.getXSeries(), (xRange.upper + xRange.lower) / 2);

                            result.add(newIndex, dialog.getCategory());

                            activeLine = result.pointsCount() - 1;
                            chart.redraw();
                        }
                        break;

                    case SWT.DEL:
                        if (activeLine >= 0) {
                            result.remove(activeLine--);
                            chart.redraw();
                        }
                        break;
                }
            }
        });

        adjustView(chart, result);

        table.setSelection(index);
    }

    private void adjustView(Chart chart, MeasureEWResult result) {
        int lowerBound = result.getLowerBound();
        int upperBound = result.getUpperBound();

        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        for (int i = lowerBound; i <= upperBound; i++) {
            double y = series.getY(i);

            if (y < min) {
                min = y;
            }
            if (y > max) {
                max = y;
            }
        }

        chart.getAxisSet().getXAxis(0).setRange(new Range(series.getX(lowerBound), series.getX(upperBound)));
        chart.getAxisSet().getYAxis(0).setRange(new Range(min - DOUBLE_PRECISION, max + DOUBLE_PRECISION));

        chart.getAxisSet().zoomOut();
    }

    private void updateShift(double shift) {
        this.shift += shift;
    }

    private void snapToPoint(XYSeries series, MeasureEWResult result) {
        if (activeLine < 0) {
            int newIndex = ArrayUtils.findClosest(series.getXSeries(), series.getX(result.getBound(activeLine + 2)) + shift);
            result.setBound(activeLine + 2, newIndex);
        } else {
            int newIndex = ArrayUtils.findClosest(series.getXSeries(), series.getX(result.getPoint(activeLine)) + shift);
            result.setPoint(activeLine, newIndex);
        }

        shift = 0;
    }

    private void finish() {
        if (!Message.question("Are you sure you want to finish?")) {
            return;
        }

        ComponentManager.clearScene(true);
        callback.accept(results);
    }
}
