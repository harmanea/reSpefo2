package cz.cuni.mff.respefo.function.disp;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.function.common.ChartKeyListener;
import cz.cuni.mff.respefo.function.common.HorizontalDragMouseListener;
import cz.cuni.mff.respefo.resources.ColorManager;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.utils.MathUtils;
import cz.cuni.mff.respefo.util.widget.ButtonBuilder;
import cz.cuni.mff.respefo.util.widget.CompositeBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.Range;

import java.util.Iterator;
import java.util.function.Consumer;

import static cz.cuni.mff.respefo.resources.ColorResource.*;
import static cz.cuni.mff.respefo.util.layout.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.utils.ChartUtils.getRelativeHorizontalStep;
import static cz.cuni.mff.respefo.util.utils.MathUtils.isNotNaN;
import static cz.cuni.mff.respefo.util.widget.ButtonBuilder.newPushButton;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.AxisLabel.PIXELS;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.AxisLabel.WAVELENGTH;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.LineSeriesBuilder.lineSeries;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.ScatterSeriesBuilder.scatterSeries;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.newChart;
import static cz.cuni.mff.respefo.util.widget.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.widget.TableBuilder.newTable;
import static cz.cuni.mff.respefo.util.widget.TextBuilder.newText;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

public class DispersionController {
    private final ComparisonLineMeasurements measurements;

    private final XYSeries seriesA;
    private final XYSeries seriesB;

    private double newPoint;

    private final DispersionMeasurementController measurementController;

    private Consumer<ComparisonLineResults> printCallback;
    private Consumer<double[]> finishCallback;

    public DispersionController(double[] cmpValues, XYSeries seriesA, XYSeries seriesB) {
        measurements = new ComparisonLineMeasurements(cmpValues);

        this.seriesA = seriesA;
        this.seriesB = seriesB;

        newPoint = Double.NaN;

        measurementController = new DispersionMeasurementController(seriesA, seriesB);
    }

    public void start(Consumer<ComparisonLineResults> printCallback, Consumer<double[]> finishCallback) {
        this.printCallback = printCallback;
        this.finishCallback = finishCallback;

        firstStage();
    }

    private void firstStage() {
        newChart()
                .title("Derive dispersion")
                .xAxisLabel(PIXELS)
                .hideYAxes()
                .newYAxis()
                .series(lineSeries()
                        .name("a")
                        .color(GREEN)
                        .series(seriesA))
                .series(lineSeries()
                        .name("b")
                        .color(GREEN)
                        .series(seriesB)
                        .yAxis(2))
                .keyListener(ch -> new ChartKeyListener.CustomAction(ch, this::stackAbove))
                .keyListener(ch -> KeyListener.keyPressedAdapter(e -> {
                    switch (e.keyCode) {
                        case SWT.INSERT:
                            if (Double.isNaN(newPoint)) {
                                Range range = ch.getAxisSet().getXAxis(0).getRange();
                                newPoint = (range.lower + range.upper) / 2;
                                ch.redraw();
                            }
                            break;
                        case SWT.ESC:
                            if (!Double.isNaN(newPoint)) {
                                newPoint = Double.NaN;
                                ch.redraw();
                            }
                            break;
                        case SWT.CR:
                            if (Double.isNaN(newPoint)) {
                                if (measurements.numberOfMeasured() < 2) {
                                    Message.warning("You must first manually select at least two lines.");
                                } else {
                                    secondStage(measurements.unmeasuredIndicesIterator());
                                }

                            } else {
                                NumberDialog dialog = new NumberDialog(measurements.size(), "Select line number", "Line number:");
                                if (dialog.openIsOk()) {
                                    ComparisonLineMeasurement measurement = measurements.getMeasurement(dialog.getNumber() - 1);

                                    measurementController.measure(measurement, newPoint, () -> {
                                        newPoint = Double.NaN;
                                        firstStage();
                                    });
                                }
                            }
                            break;
                        case 'j':
                            if (isNotNaN(newPoint)) {
                                newPoint -= getRelativeHorizontalStep(ch);
                                ch.redraw();
                            }
                            break;
                        case 'l':
                            if (isNotNaN(newPoint)) {
                                newPoint += getRelativeHorizontalStep(ch);
                                ch.redraw();
                            }
                            break;
                    }
                }))
                .mouseAndMouseMoveListener(ch -> new HorizontalDragMouseListener(ch, shift -> {
                    if (isNotNaN(newPoint)) {
                        newPoint += shift;
                        ch.redraw();
                    }
                }))
                .plotAreaPaintListener(ch -> event -> {
                    Range range = ch.getAxisSet().getXAxis(0).getRange();
                    double diff = range.upper - range.lower;

                    for (int i = 0; i < measurements.size(); i++){
                        ComparisonLineMeasurement measurement = measurements.getMeasurement(i);
                        if (measurement.isMeasured()) {
                            int x = (int) (event.width * (measurement.getX() - range.lower) / diff);

                            event.gc.setForeground(ColorManager.getColor(GRAY));
                            event.gc.drawLine(x, 0, x, event.height);
                        }
                    }

                    if (isNotNaN(newPoint)) {
                        int x = (int) (event.width * (newPoint - range.lower) / diff);

                        event.gc.setForeground(ColorManager.getColor(ORANGE));
                        event.gc.drawLine(x, 0, x, event.height);
                    }
                })
                .accept(this::stackAbove)
                .forceFocus()
                .build(ComponentManager.clearAndGetScene());
    }

    private void stackAbove(Chart chart) {
        chart.getAxisSet().getXAxis(0).adjustRange();

        IAxis aAxis = chart.getAxisSet().getYAxis(chart.getSeriesSet().getSeries("a").getYAxisId());
        aAxis.adjustRange();
        Range aRange = aAxis.getRange();
        aAxis.setRange(new Range(2 * aRange.lower - aRange.upper, aRange.upper));

        IAxis bAxis = chart.getAxisSet().getYAxis(chart.getSeriesSet().getSeries("b").getYAxisId());
        bAxis.adjustRange();
        Range bRange = bAxis.getRange();
        bAxis.setRange(new Range(bRange.lower, 2 * bRange.upper  - bRange.lower));
    }

    private void secondStage(Iterator<Integer> iterator) {
        if (iterator.hasNext()) {
            int i = iterator.next();
            double y = measurements.hint(i);

            if (y > 10 && y < seriesA.getLength() - 10 && y < seriesB.getLength() - 10) {
                ComparisonLineMeasurement measurement = measurements.getMeasurement(i);
                measurementController.measure(measurement, y, () -> secondStage(iterator));

            } else {
                secondStage(iterator);
            }

        } else {
            thirdStage(measurements.getResults());
        }
    }

    private void thirdStage(ComparisonLineResults results) {
        double[] x = results.getX();
        double[] laboratoryValues = results.getLaboratoryValues();

        double[] coefficients = results.getCoefficients();

        double[] actualY = results.getActualY();
        double[] residuals = results.getResiduals();

        final Composite composite = newComposite()
                .gridLayoutData(GridData.FILL_BOTH)
                .layout(gridLayout(2, true).margins(0).spacings(1))
                .build(ComponentManager.clearAndGetScene());

        CompositeBuilder fillBothNoMarginsNoSpacingsComposite = newComposite()
                .gridLayoutData(GridData.FILL_BOTH)
                .layout(gridLayout().margins(0).spacings(0));

        final Composite chartsComposite = fillBothNoMarginsNoSpacingsComposite.build(composite);

        newChart()
                .title("Dispersion function")
                .xAxisLabel(PIXELS)
                .yAxisLabel(WAVELENGTH)
                .series(scatterSeries()
                        .xSeries(x)
                        .ySeries(actualY)
                        .color(GREEN)
                        .symbolSize(3))
                .adjustRange()
                .build(chartsComposite);

        newChart()
                .title("Residuals")
                .xAxisLabel(PIXELS)
                .yAxisLabel("Error")
                .series(scatterSeries()
                        .name("points")
                        .xSeries(x)
                        .ySeries(residuals)
                        .color(RED)
                        .symbolSize(3)
                )
                .series(scatterSeries()
                        .name("deleted")
                        .series(results.getUnusedResidualSeries())
                        .color(BLUE)
                        .symbolSize(3)
                )
                .series(lineSeries()
                        .xSeries(new double[] {0, x[x.length - 1]})
                        .ySeries(new double[] {0, 0})
                        .color(GRAY)
                )
                .centerAroundSeries("points")
                .build(chartsComposite);

        final Composite tableComposite = fillBothNoMarginsNoSpacingsComposite.build(composite);

        newTable(SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL)
                .gridLayoutData(GridData.FILL_BOTH)
                .linesVisible(true)
                .headerVisible(true)
                .columns("N.", "x up", "x down", "x mean", "lab.", "comp.", "error")
                .items(results, result -> new String[]{
                        Integer.toString(result.getIndex() + 1),
                        format(result.getXUp()),
                        format(result.getXDown()),
                        format(result.getX()),
                        format(result.getLaboratoryValue()),
                        format(result.getActualY()),
                        format(result.getResidual())
                }, (result, item) -> {
                    if (!result.isUsed()) {
                        item.setForeground(ComponentManager.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
                    }
                })
                .item(t -> {
                    final TableItem tableItem = new TableItem(t, SWT.NONE);
                    tableItem.setText(6, format(MathUtils.rmse(actualY, laboratoryValues)));
                })
                .packColumns()
                .listener(SWT.KeyDown, event -> {
                    int index = ((Table) event.widget).getSelectionIndex();
                    if (index >= 0 && index < ((Table) event.widget).getItemCount() - 1 && event.keyCode == SWT.DEL) {
                        results.inverseUsed(index);
                        results.calculateCoefficients();
                        results.calculateValues();
                        thirdStage(results);
                    }
                })
                .build(tableComposite);

        newText(SWT.MULTI | SWT.READ_ONLY)
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_END)
                .text("Coefficients of dispersion polynomial:\n\n" + stream(coefficients).mapToObj(Double::toString).collect(joining("\n")))
                .build(tableComposite);

        final Composite buttonsComposite = newComposite()
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_END)
                .layout(gridLayout(3, true).margins(10).spacings(10))
                .build(tableComposite);

        ButtonBuilder buttonBuilder = newPushButton().gridLayoutData(GridData.FILL_BOTH);

        buttonBuilder
                .text("Poly degree")
                .onSelection(event -> {
                    NumberDialog dialog = new NumberDialog(9, "Select poly degree", "Poly number:");
                    if (dialog.openIsOk()) {
                        results.setPolyDegree(dialog.getNumber());
                        results.calculateCoefficients();
                        results.calculateValues();
                        thirdStage(results);
                    }
                })
                .build(buttonsComposite);

        buttonBuilder
                .text("Print to file")
                .onSelection(event -> printCallback.accept(results))
                .build(buttonsComposite);

        buttonBuilder
                .text("Finish")
                .onSelection(event -> finishCallback.accept(coefficients))
                .build(buttonsComposite);

        ComponentManager.getScene().layout();
    }

    private static String format(double value) {
        return String.format("%5.3f", value);
    }
}
