package cz.cuni.mff.respefo.function.rv;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.component.ToolBar;
import cz.cuni.mff.respefo.component.VerticalToggle;
import cz.cuni.mff.respefo.function.common.HorizontalDragMouseListener;
import cz.cuni.mff.respefo.function.common.Measurement;
import cz.cuni.mff.respefo.function.common.Measurements;
import cz.cuni.mff.respefo.resources.ColorManager;
import cz.cuni.mff.respefo.resources.ColorResource;
import cz.cuni.mff.respefo.resources.ImageResource;
import cz.cuni.mff.respefo.util.Async;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;
import cz.cuni.mff.respefo.util.utils.ChartUtils;
import cz.cuni.mff.respefo.util.widget.ButtonBuilder;
import cz.cuni.mff.respefo.util.widget.CompositeBuilder;
import cz.cuni.mff.respefo.util.widget.DefaultSelectionListener;
import cz.cuni.mff.respefo.util.widget.LabelBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;

import java.util.Arrays;
import java.util.function.Consumer;

import static cz.cuni.mff.respefo.resources.ColorResource.CYAN;
import static cz.cuni.mff.respefo.resources.ColorResource.GREEN;
import static cz.cuni.mff.respefo.util.layout.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.utils.FormattingUtils.round;
import static cz.cuni.mff.respefo.util.widget.ButtonBuilder.newRadioButton;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.AxisLabel.RELATIVE_FLUX;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.LineSeriesBuilder.lineSeries;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.newChart;
import static cz.cuni.mff.respefo.util.widget.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.widget.LabelBuilder.newLabel;
import static cz.cuni.mff.respefo.util.widget.TableBuilder.newTable;
import static cz.cuni.mff.respefo.util.widget.TextBuilder.newText;

public class MeasureRVController {
    private static final String MIRRORED_SERIES_NAME = "mirrored";

    private final XYSeries series;
    private final double deltaRV;

    private Measurements measurements;
    private Consumer<MeasureRVResults> callback;

    private MeasureRVResults results;
    private int index;
    private double shift;
    private double rvStep;

    private Table table;
    private Text relativeStepText;

    public MeasureRVController(XYSeries series, double deltaRV) {
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

    private void setUpRVStepBar() {
        final ToolBar.Tab rvStepTab = ComponentManager.getRightToolBar().addTab(parent -> new VerticalToggle(parent, SWT.DOWN),
                "RV Step", "RV Step", ImageResource.RULER_LARGE);

        CompositeBuilder stepCompositeBuilder = newComposite()
                .gridLayoutData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING)
                .layout(gridLayout(2, false).marginWidth(3).marginHeight(5).horizontalSpacing(5));
        ButtonBuilder stepButtonBuilder = newRadioButton()
                .gridLayoutData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1);
        LabelBuilder stepUnitsLabelBuilder = newLabel(SWT.CENTER)
                .text("km/s")
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_CENTER);
        LabelBuilder stepLabelBuilder = newLabel(SWT.CENTER | SWT.WRAP)
                .gridLayoutData(SWT.FILL, SWT.FILL, true, false, 2, 1);
        LabelBuilder horizontalSeparator = newLabel(SWT.SEPARATOR | SWT.HORIZONTAL)
                .gridLayoutData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER);

        final Composite relativeStepComposite = stepCompositeBuilder.build(rvStepTab.getWindow());

        final Button relativeStepButton = stepButtonBuilder.text("Relative").selection(true).build(relativeStepComposite);

        relativeStepText = newText(SWT.SINGLE | SWT.READ_ONLY | SWT.RIGHT)
                .gridLayoutData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END)
                .build(relativeStepComposite);

        final Label relativeStepUnitsLabel = stepUnitsLabelBuilder.build(relativeStepComposite);

        final Label relativeStepLabel = stepLabelBuilder
                .text("The step size is computed relatively based on the current zoom.")
                .build(relativeStepComposite);


        horizontalSeparator.build(rvStepTab.getWindow());


        final Composite manualStepComposite = stepCompositeBuilder.build(rvStepTab.getWindow());

        final Button manualStepButton = stepButtonBuilder.text("Manual").selection(false).build(manualStepComposite);

        final Consumer<Text> verifyManualStepText = (Text text) -> {
            try {
                double newRvStep = Double.parseDouble(text.getText());

                if (newRvStep <= 0 || !Double.isFinite(newRvStep)) {
                    throw new NumberFormatException();
                }

                rvStep = 2 * newRvStep / deltaRV;
                text.setForeground(ComponentManager.getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));

            } catch (NumberFormatException exception) {
                text.setForeground(ColorManager.getColor(ColorResource.RED));
            }
        };

        final Text manualStepText = newText(SWT.SINGLE | SWT.RIGHT)
                .gridLayoutData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END)
                .text("1.0")
                .enabled(false)
                .onModify(event -> verifyManualStepText.accept((Text) event.widget))
                .listener(SWT.Traverse, event -> {
                    // This is a hacky way to force focus the chart
                    if (event.detail == SWT.TRAVERSE_ESCAPE || event.detail == SWT.TRAVERSE_RETURN) {
                        ComponentManager.getScene().getChildren()[0].forceFocus();
                    }
                })
                .build(manualStepComposite);

        final Label manualStepUnitLabel = stepUnitsLabelBuilder.build(manualStepComposite);

        final Label manualStepLabel = stepLabelBuilder
                .text("The step size is manually selected.")
                .enabled(false)
                .build(manualStepComposite);


        horizontalSeparator.build(rvStepTab.getWindow());


        relativeStepButton.addSelectionListener(new DefaultSelectionListener(event -> {
            rvStep = Double.parseDouble(relativeStepText.getText());

            manualStepButton.setSelection(false);

            relativeStepLabel.setEnabled(true);
            relativeStepText.setEnabled(true);
            relativeStepUnitsLabel.setEnabled(true);

            manualStepLabel.setEnabled(false);
            manualStepText.setEnabled(false);
            manualStepUnitLabel.setEnabled(false);
        }));

        manualStepButton.addSelectionListener(new DefaultSelectionListener(event -> {
            verifyManualStepText.accept(manualStepText);

            relativeStepButton.setSelection(false);

            relativeStepLabel.setEnabled(false);
            relativeStepText.setEnabled(false);
            relativeStepUnitsLabel.setEnabled(false);

            manualStepLabel.setEnabled(true);
            manualStepText.setEnabled(true);
            manualStepUnitLabel.setEnabled(true);

            manualStepText.forceFocus();
            manualStepText.setSelection(0, manualStepText.getText().length());
        }));
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

        table = newTable(SWT.SINGLE)
                .gridLayoutData(GridData.FILL_BOTH)
                .headerVisible(false)
                .linesVisible(true)
                .onSelection(event -> {
                    index = table.getSelectionIndex();
                    measureSingle();
                })
                .columns(2)
                .fixedAspectColumns(3, 1)
                .items(measurements, measurement -> new String[]{
                        measurement.getName(),
                        Double.toString(measurement.getL0())
                })
                .build(linesTab.getWindow());
        table.getColumn(1).setAlignment(SWT.RIGHT);

        linesTab.show();
    }

    private void measureSingle() {
        Measurement measurement = measurements.get(index);

        shift = 0;

        final Chart chart = newChart()
                .title(measurement.getName() + " " + measurement.getL0())
                .xAxisLabel("index")
                .yAxisLabel(RELATIVE_FLUX)
                .series(lineSeries()
                        .name("original")
                        .xSeries(ArrayUtils.fillArray(series.getLength(), 0, 1))
                        .ySeries(series.getYSeries())
                        .color(GREEN))
                .series(lineSeries()
                        .name(MIRRORED_SERIES_NAME)
                        .series(computeSeries(measurement))
                        .color(CYAN))
                .keyListener(ch -> new MeasureRVKeyListener(ch, MIRRORED_SERIES_NAME, computeMiddle(measurement),
                        () -> updateRelativeStep(ch),
                        () -> applyShift(ch, -rvStep),
                        () -> applyShift(ch, rvStep)))
                .keyListener(ch -> KeyListener.keyPressedAdapter(e -> {
                    switch (e.keyCode) {
                        case 'i':
                            if (e.stateMask != SWT.COMMAND) break;
                        case SWT.CR:
                        case SWT.HELP:
                        case SWT.INSERT:
                            MeasurementInputDialog dialog = new MeasurementInputDialog(measurement.isCorrection());
                            if (dialog.openIsOk()) {
                                MeasureRVResult result = new MeasureRVResult(
                                        deltaRV * shift / 2,
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
                                Async.exec(MeasureRVController.this::measureSingle);
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
                            ch.getSeriesSet().getSeries(MIRRORED_SERIES_NAME).setXSeries(ArrayUtils.addValueToArrayElements(newSeries.getXSeries(), shift));
                            ch.getSeriesSet().getSeries(MIRRORED_SERIES_NAME).setYSeries(newSeries.getYSeries());
                            ch.redraw();
                            break;
                    }
                }))
                .mouseAndMouseMoveListener(ch -> new HorizontalDragMouseListener(ch, value -> applyShift(ch, value)))
                .centerAroundSeries(MIRRORED_SERIES_NAME)
                .zoomOut()
                .forceFocus()
                .build(ComponentManager.clearAndGetScene(false));

        table.setSelection(index);
        updateRelativeStep(chart);
    }

    private void updateRelativeStep(Chart chart) {
        double relativeStep = ChartUtils.getRelativeHorizontalStep(chart);
        relativeStepText.setText(round(relativeStep * deltaRV / 2, 4));

        // This is a hacky way to detect which type of step is selected
        if (relativeStepText.isEnabled()) {
            rvStep = relativeStep;
        }
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

        double mid = computeMiddle(measurement);

        double[] mirroredXSeries = new double[mirroredYSeries.length];
        for (int i = 0; i < mirroredXSeries.length; i++) {
            mirroredXSeries[i] = 2 * mid - to + 1 + i;
        }

        return new XYSeries(mirroredXSeries, mirroredYSeries);
    }

    private double computeMiddle(Measurement measurement) {
        int midIndex = Arrays.binarySearch(series.getXSeries(), measurement.getL0());
        if (midIndex < 0) {
            midIndex = -midIndex - 1;

            double low = series.getX(midIndex - 1);
            double high = series.getX(midIndex);

            return midIndex - 1 + ((measurement.getL0() - low) / (high - low));
        } else {
            return midIndex;
        }
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

        callback.accept(results);
    }
}
