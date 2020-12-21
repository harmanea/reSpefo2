package cz.cuni.mff.respefo.function.asset.dispersion;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.format.Spectrum;
import cz.cuni.mff.respefo.format.XYSeries;
import cz.cuni.mff.respefo.format.formats.fits.ImportFitsFormat;
import cz.cuni.mff.respefo.function.asset.common.ChartKeyListener;
import cz.cuni.mff.respefo.function.asset.common.HorizontalDragMouseListener;
import cz.cuni.mff.respefo.function.scan.OpenFunction;
import cz.cuni.mff.respefo.resources.ColorManager;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.builders.widgets.ButtonBuilder;
import cz.cuni.mff.respefo.util.builders.widgets.CompositeBuilder;
import cz.cuni.mff.respefo.util.utils.FileUtils;
import cz.cuni.mff.respefo.util.utils.MathUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.Range;

import java.io.File;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Locale;

import static cz.cuni.mff.respefo.resources.ColorResource.*;
import static cz.cuni.mff.respefo.util.Constants.SPEED_OF_LIGHT;
import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.builders.widgets.ButtonBuilder.newButton;
import static cz.cuni.mff.respefo.util.builders.widgets.ChartBuilder.AxisLabel.PIXELS;
import static cz.cuni.mff.respefo.util.builders.widgets.ChartBuilder.AxisLabel.WAVELENGTH;
import static cz.cuni.mff.respefo.util.builders.widgets.ChartBuilder.LineSeriesBuilder.lineSeries;
import static cz.cuni.mff.respefo.util.builders.widgets.ChartBuilder.ScatterSeriesBuilder.scatterSeries;
import static cz.cuni.mff.respefo.util.builders.widgets.ChartBuilder.newChart;
import static cz.cuni.mff.respefo.util.builders.widgets.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.builders.widgets.TableBuilder.newTable;
import static cz.cuni.mff.respefo.util.builders.widgets.TextBuilder.newText;
import static cz.cuni.mff.respefo.util.utils.ChartUtils.getRelativeHorizontalStep;
import static cz.cuni.mff.respefo.util.utils.FormattingUtils.formatDouble;
import static cz.cuni.mff.respefo.util.utils.FormattingUtils.formatInteger;
import static cz.cuni.mff.respefo.util.utils.MathUtils.isNotNaN;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

public class DispersionController {
    private final ComparisonLineMeasurements measurements;

    private final XYSeries seriesA;
    private final XYSeries seriesB;
    private final File file;

    private double newPoint;

    private final DispersionMeasurementController measurementController;

    public DispersionController(double[] cmpValues, XYSeries seriesA, XYSeries seriesB, File file) {
        measurements = new ComparisonLineMeasurements(cmpValues);

        this.seriesA = seriesA;
        this.seriesB = seriesB;

        this.file = file;

        newPoint = Double.NaN;

        measurementController = new DispersionMeasurementController(seriesA, seriesB);
    }

    public void start() {
        firstStage();
    }

    private void firstStage() {
        Chart chart = newChart()
                .title("Derive dispersion")
                .xAxisLabel(PIXELS)
                .hideYAxis()
                .series(lineSeries()
                        .name("a")
                        .color(GREEN)
                        .series(seriesA))
                .series(lineSeries()
                        .name("b")
                        .color(GREEN)
                        .series(seriesB))
                .keyListener(ch -> ChartKeyListener.customAction(ch, this::stackAbove))
                .mouseAndMouseMoveListener(ch -> new HorizontalDragMouseListener(ch, shift -> {
                    if (isNotNaN(newPoint)) {
                        newPoint += shift;
                        ch.redraw();
                    }
                }))
                .focus()
                .build(ComponentManager.clearAndGetScene());

        chart.getAxisSet().adjustRange();
        stackAbove(chart);

        chart.getPlotArea().addPaintListener(event -> {
            Range range = chart.getAxisSet().getXAxis(0).getRange();
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
        });

        chart.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.keyCode) {
                    case SWT.INSERT:
                        if (Double.isNaN(newPoint)) {
                            Range range = chart.getAxisSet().getXAxis(0).getRange();
                            newPoint = (range.lower + range.upper) / 2;
                            chart.redraw();
                        }
                        break;
                    case SWT.ESC:
                        if (!Double.isNaN(newPoint)) {
                            newPoint = Double.NaN;
                            chart.redraw();
                        }
                        break;
                    case SWT.CR:
                        if (Double.isNaN(newPoint)) {
                            if (measurements.numberOfMeasured() < 2) {
                                Message.warning("You must first manually select at least two lines.");
                            } else {
                                secondStage(measurements.unmeasuredIndexesIterator());
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
                    case 'n':
                        if (isNotNaN(newPoint)) {
                            newPoint -= getRelativeHorizontalStep(chart);
                            chart.redraw();
                        }
                        break;
                    case 'm':
                        if (isNotNaN(newPoint)) {
                            newPoint += getRelativeHorizontalStep(chart);
                            chart.redraw();
                        }
                        break;
                }
            }
        });

        chart.redraw();
        chart.forceFocus();
    }

    private void stackAbove(Chart chart) {
        IAxis aAxis = chart.getAxisSet().getYAxis(chart.getSeriesSet().getSeries("a").getYAxisId());
        Range aRange = aAxis.getRange();
        aAxis.setRange(new Range(2 * aRange.lower - aRange.upper, aRange.upper));

        IAxis bAxis = chart.getAxisSet().getYAxis(chart.getSeriesSet().getSeries("b").getYAxisId());
        Range bRange = bAxis.getRange();
        bAxis.setRange(new Range(bRange.lower, 2 * bRange.upper  - bRange.lower));
    }

    private void secondStage(Iterator<Integer> iterator) {
        if (iterator.hasNext()) {
            int i = iterator.next();
            double y = measurements.hint(i);

            if (y > 10 && y < seriesA.getXSeries().length - 10 && y < seriesB.getYSeries().length - 10) {
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

        double[] coeffs = results.getCoeffs();

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
                .yAxisLabel("error")
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
                        .color(ORANGE)
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

        final Table table = newTable(SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL)
                .gridLayoutData(GridData.FILL_BOTH)
                .linesVisible(true)
                .headerVisible(true)
                .columns("N.", "x up", "x down", "x mean", "lab.", "comp.", "error")
                .build(tableComposite);

        for (ComparisonLineResults.ComparisonLineResult result : results) {
            final TableItem tableItem = new TableItem(table, SWT.NONE);
            tableItem.setText(0, Integer.toString(result.getIndex() + 1));
            tableItem.setText(1, String.format(Locale.US, "%5.3f", result.getXUp()));
            tableItem.setText(2, String.format(Locale.US, "%5.3f", result.getXDown()));
            tableItem.setText(3, String.format(Locale.US, "%5.3f", result.getX()));
            tableItem.setText(4, String.format(Locale.US, "%5.3f", result.getLaboratoryValue()));
            tableItem.setText(5, String.format(Locale.US, "%5.3f", result.getActualY()));
            tableItem.setText(6, String.format(Locale.US, "%5.3f", result.getResidual()));

            if (!result.isUsed()) {
                tableItem.setForeground(ComponentManager.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
            }
        }

        final TableItem tableItem = new TableItem(table, SWT.NONE);
        tableItem.setText(6, String.format(Locale.US, "%5.3f", MathUtils.rmse(actualY, laboratoryValues)));

        for (TableColumn column : table.getColumns()) {
            column.pack();
        }

        table.addListener(SWT.KeyDown, event -> {
            int index = table.getSelectionIndex();
            if (index >= 0 && index < table.getItemCount() - 1 && event.keyCode == SWT.DEL) {
                results.inverseUsed(index);
                results.calculateCoeffs();
                results.calculateValues();
                thirdStage(results);
            }
        });

        newText(SWT.MULTI | SWT.READ_ONLY)
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_END)
                .text("Coefficients of dispersion polynomial:\n\n" + stream(coeffs).mapToObj(Double::toString).collect(joining("\n")))
                .build(tableComposite);

        final Composite buttonsComposite = newComposite()
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_END)
                .layout(gridLayout(3, true).margins(10).spacings(10))
                .build(tableComposite);

        ButtonBuilder buttonBuilder = newButton(SWT.PUSH).gridLayoutData(GridData.FILL_BOTH);

        buttonBuilder
                .text("Poly degree")
                .onSelection(event -> {
                    NumberDialog dialog = new NumberDialog(5, "Select poly degree", "Poly number:");
                    if (dialog.openIsOk()) {
                        results.setPolyDegree(dialog.getNumber());
                        results.calculateCoeffs();
                        results.calculateValues();
                        thirdStage(results);
                    }
                })
                .build(buttonsComposite);

        buttonBuilder
                .text("Print to file")
                .onSelection(event -> printResults(results))
                .build(buttonsComposite);

        buttonBuilder
                .text("Finish")
                .onSelection(event -> fourthStage(coeffs))
                .build(buttonsComposite);

        ComponentManager.getScene().layout();
    }

    private void printResults(ComparisonLineResults results) {
        File cmfFile = new File(FileUtils.replaceFileExtension(file.getPath(), "cmf"));
        try (PrintWriter writer = new PrintWriter(cmfFile)) {
            writer.println("Comparison lines measured from files " + "FILE A" + " & " + "FILE B"); // TODO: fix this
            writer.println("  ----------------------------------------------------------------\n");
            writer.println("   N.     x up    x down    x mean     rms        lab.       comp.    c.-l.\n");

            for (ComparisonLineResults.ComparisonLineResult result : results) {
                writer.print(formatInteger(result.getIndex() + 1, 5));
                writer.print(formatDouble(result.getXUp(), 5, 3));
                writer.print(formatDouble(result.getXDown(), 5, 3));
                writer.print(formatDouble(result.getX(), 5, 3));
                writer.print("   1.000");
                writer.print(formatDouble(result.getLaboratoryValue(), 7, 3));
                writer.print(formatDouble(result.getActualY(), 7, 3));
                writer.print(formatDouble(result.getResidual(), 3, 3));

                if (!result.isUsed()) {
                    writer.print("  not used");
                }

                writer.println();
            }

            writer.print("\n                                                              rms =");
            writer.println(formatDouble(results.meanRms(), 4, 3));
            writer.println("\n\n  Coefficients of dispersion polynomial:\n");

            double[] coeffs = results.getCoeffs();
            for (int i = 0; i < coeffs.length; i++) {
                writer.println("   order  " + i + "    " + String.format("%1.8e", coeffs[i]));
            }

            if (writer.checkError()) {
                throw new SpefoException("The print stream has encountered an error");
            }

            ComponentManager.getFileExplorer().refresh();
        } catch (Exception exception) {
            Message.error("An exception occurred while printing to file.", exception);
        }
    }

    private void fourthStage(double[] coeffs) {
        try {
            Spectrum spectrum = new ImportFitsFormat().importFrom(file.getPath());

            double[] xSeries = spectrum.getSeries().getXSeries();
            for (int i = 0; i < xSeries.length; i++) {
                xSeries[i] = MathUtils.polynomial(i, coeffs);

                if (isNotNaN(spectrum.getRvCorrection())) {
                    xSeries[i] += spectrum.getRvCorrection() * (xSeries[i] / SPEED_OF_LIGHT);
                }
            }
            spectrum.getSeries().updateXSeries(xSeries);
            spectrum.saveAs(new File(FileUtils.replaceFileExtension(file.getPath(), "spf")));
            ComponentManager.getFileExplorer().refresh();

            OpenFunction.displaySpectrum(spectrum);

        } catch (SpefoException exception) {
            Message.error("An error occurred while saving file", exception);
        }
    }
}
