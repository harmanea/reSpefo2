package cz.cuni.mff.respefo.function.compare;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.component.FileExplorer;
import cz.cuni.mff.respefo.component.ToolBar;
import cz.cuni.mff.respefo.component.VerticalToggle;
import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.common.ChartKeyListener;
import cz.cuni.mff.respefo.function.common.DragMouseListener;
import cz.cuni.mff.respefo.function.common.ZoomMouseWheelListener;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.function.rectify.RectifyFunction;
import cz.cuni.mff.respefo.resources.ImageResource;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.spectrum.format.EchelleSpectrum;
import cz.cuni.mff.respefo.util.FileDialogs;
import cz.cuni.mff.respefo.util.FileType;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;
import cz.cuni.mff.respefo.util.utils.MathUtils;
import cz.cuni.mff.respefo.util.widget.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Spinner;
import org.swtchart.Chart;
import org.swtchart.ISeries;
import org.swtchart.Range;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.function.Consumer;

import static cz.cuni.mff.respefo.resources.ColorManager.getColor;
import static cz.cuni.mff.respefo.resources.ColorResource.BLUE;
import static cz.cuni.mff.respefo.resources.ColorResource.GREEN;
import static cz.cuni.mff.respefo.util.Constants.SPEED_OF_LIGHT;
import static cz.cuni.mff.respefo.util.FileType.ASCII_FILES;
import static cz.cuni.mff.respefo.util.layout.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.utils.ChartUtils.rangeWithMargin;
import static cz.cuni.mff.respefo.util.utils.FileUtils.stripFileExtension;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.LineSeriesBuilder.lineSeries;
import static java.lang.Math.max;
import static java.lang.Math.min;

@Fun(name = "Compare To", fileFilter = SpefoFormatFileFilter.class)
public class CompareToFunction implements SingleFileFunction {

    @Override
    public void execute(File file) {
        FileDialogs.openFileDialog(FileType.SPECTRUM)
                .ifPresent(otherFileName -> {
                    try {
                        Spectrum a = Spectrum.open(file);
                        Spectrum b = Spectrum.open(new File(otherFileName));

                        checkIfBothRectified(a, b);
                        compare(a, b);

                    } catch (SpefoException exception) {
                        Message.error("Couldn't open spectrum", exception);
                    }
                });
    }

    private void checkIfBothRectified(Spectrum a, Spectrum b) {
        boolean aIsRectified = a.containsFunctionAsset(RectifyFunction.RECTIFY_SERIALIZE_KEY)
                || (a.getFormat() == EchelleSpectrum.FORMAT && ((EchelleSpectrum) a).isRectified());
        boolean bIsRectified = b.containsFunctionAsset(RectifyFunction.RECTIFY_SERIALIZE_KEY)
                || (b.getFormat() == EchelleSpectrum.FORMAT && ((EchelleSpectrum) b).isRectified());

        if (aIsRectified != bIsRectified) {
            Message.warning("Not both spectra are rectified, this may lead to incorrect behavior");
        }
    }

    private void compare(Spectrum a, Spectrum b) {
        XYSeries aSeries = a.getProcessedSeries();
        XYSeries bSeries = b.getProcessedSeries();

        double aYMin = aSeries.getMinY();
        double aYMax = aSeries.getMaxY();
        double bYMin = bSeries.getMinY();
        double bYMax = bSeries.getMaxY();

        Consumer<Chart> adjustRange = chart -> {
            adjustXRange(chart);
            adjustYRange(chart, aYMin, aYMax, bYMin, bYMax);
            chart.redraw();
        };

        ComponentManager.getRightToolBar().disposeTabs();
        final ToolBar.Tab tab = ComponentManager.getRightToolBar().addTab(parent -> new VerticalToggle(parent, SWT.DOWN),
                "Parameters", "Parameters", ImageResource.RULER_LARGE);
        tab.getWindow().setLayout(gridLayout().build());

        LabelBuilder labelBuilder = LabelBuilder.newLabel(SWT.LEFT).gridLayoutData(GridData.FILL_HORIZONTAL).bold();

        labelBuilder.text("X shift (km/s):").build(tab.getWindow());
        final Spinner xShiftSpinner = SpinnerBuilder.newSpinner()
                .gridLayoutData(GridData.FILL_HORIZONTAL)
                .digits(2)
                .bounds(-100_00, 100_00)
                .increment(50, 200)
                .selection(0)
                .build(tab.getWindow());

        labelBuilder.text("Y scale:").build(tab.getWindow());
        final Spinner yScaleSpinner = SpinnerBuilder.newSpinner()
                .gridLayoutData(GridData.FILL_HORIZONTAL)
                .digits(2)
                .bounds(1, 10_00)
                .increment(1, 10)
                .selection(100)
                .build(tab.getWindow());

        LabelBuilder.newLabel(SWT.SEPARATOR | SWT.HORIZONTAL).gridLayoutData(GridData.FILL_HORIZONTAL).build(tab.getWindow());
        final Button button =  ButtonBuilder.newPushButton()
                .text("Print difference spectrum")
                .gridLayoutData(GridData.FILL_HORIZONTAL)
                .build(tab.getWindow());

        Chart chart = ChartBuilder.newChart()
                .series(lineSeries()
                        .name("a")
                        .color(GREEN)
                        .series(aSeries))
                .series(lineSeries()
                        .name("b")
                        .color(BLUE)
                        .series(bSeries))
                .data("y scale", 1.0)
                .data("x shift", 0.0)
                .keyListener(ch -> KeyListener.keyPressedAdapter(event -> {
                    switch (event.keyCode) {
                        case 'k':
                            updateYScale(ch, bSeries.getYSeries(), -0.05, yScaleSpinner);
                            break;
                        case 'i':
                            updateYScale(ch, bSeries.getYSeries(), 0.05, yScaleSpinner);
                            break;
                        case 'j':
                            updateXShift(ch, bSeries.getXSeries(), -1, xShiftSpinner);
                            break;
                        case 'l':
                            updateXShift(ch, bSeries.getXSeries(), 1, xShiftSpinner);
                            break;
                    }
                }))
                .keyListener(ch -> new ChartKeyListener.CustomAction(ch, adjustRange))
                .mouseAndMouseMoveListener(DragMouseListener::new)
                .mouseWheelListener(ZoomMouseWheelListener::new)
                .accept(adjustRange)
                .forceFocus()
                .build(ComponentManager.clearAndGetScene(false));

        chart.getAxisSet().getYAxis(0).getTick().setForeground(getColor(GREEN));
        chart.getAxisSet().getYAxis(0).getTitle().setForeground(getColor(GREEN));
        chart.getAxisSet().getYAxis(1).getTick().setForeground(getColor(BLUE));
        chart.getAxisSet().getYAxis(1).getTitle().setForeground(getColor(BLUE));

        xShiftSpinner.addModifyListener(event -> updateXShift(chart, bSeries.getXSeries(),
                xShiftSpinner.getSelection() / 100.0 - (double) chart.getData("x shift"), xShiftSpinner));
        yScaleSpinner.addModifyListener(event -> updateYScale(chart, bSeries.getYSeries(),
                yScaleSpinner.getSelection() / 100.0 - (double) chart.getData("y scale"), yScaleSpinner));
        button.addSelectionListener(new DefaultSelectionListener(event -> printDifferenceSpectrum(chart, a, b)));
    }

    private void updateXShift(Chart chart, double[] xSeries, double diff, Spinner spinner) {
        double oldXShift = (double) chart.getData("x shift");
        double newXShift = MathUtils.clamp(oldXShift + diff, -100, 100);

        if (oldXShift == newXShift) {
            return;
        }

        chart.getSeriesSet().getSeries("b")
                .setXSeries(Arrays.stream(xSeries)
                        .map(value -> value + newXShift * (value / SPEED_OF_LIGHT))
                        .toArray());
        chart.setData("x shift", newXShift);
        chart.redraw();

        spinner.setSelection(MathUtils.roundForSpinner(newXShift, spinner.getDigits()));
    }

    private void updateYScale(Chart chart, double[] ySeries, double diff, Spinner spinner) {
        double oldYScale = (double) chart.getData("y scale");
        double newYScale = MathUtils.clamp(oldYScale + diff, 0.01, 10);

        if (oldYScale == newYScale) {
            return;
        }

        chart.getSeriesSet().getSeries("b")
                .setYSeries(Arrays.stream(ySeries)
                        .map(value -> 1 + (value - 1) * newYScale)
                        .toArray());

        Range oldYRange = chart.getAxisSet().getYAxis(1).getRange();
        Range newYRange = new Range(1 + (oldYRange.lower - 1) / newYScale * oldYScale,
                                    1 + (oldYRange.upper - 1) / newYScale * oldYScale);
        chart.getAxisSet().getYAxis(1).setRange(newYRange);

        chart.setData("y scale", newYScale);
        chart.redraw();

        spinner.setSelection(MathUtils.roundForSpinner(newYScale, spinner.getDigits()));
    }

    private void adjustXRange(Chart chart) {
        ISeries aSeries = chart.getSeriesSet().getSeries("a");
        ISeries bSeries = chart.getSeriesSet().getSeries("b");

        double xMin = min(aSeries.getXSeries()[0], bSeries.getXSeries()[0]);
        double xMax = max(aSeries.getXSeries()[aSeries.getXSeries().length - 1],
                          bSeries.getXSeries()[bSeries.getXSeries().length - 1]);

        Range xRange = rangeWithMargin(xMin, xMax);
        chart.getAxisSet().getXAxis(0).setRange(xRange);
    }

    private void adjustYRange(Chart chart, double aYMin, double aYMax, double bYMin, double bYMax) {
        double yScale = (double) chart.getData("y scale");

        double overYDistance = max(0, max(aYMax - 1, yScale * (bYMax - 1)));
        double underYDistance = max(0, max(1 - aYMin, yScale * (1 - bYMin)));

        Range aYRange = rangeWithMargin(1 - underYDistance, 1 + overYDistance);
        Range bYRange = rangeWithMargin(1 - underYDistance / yScale, 1 + overYDistance / yScale);

        chart.getAxisSet().getYAxis(0).setRange(aYRange);
        chart.getAxisSet().getYAxis(1).setRange(bYRange);
    }

    private void printDifferenceSpectrum(Chart chart, Spectrum a, Spectrum b) {
        String aFileName = stripFileExtension(a.getFile().getName());
        String bFileName = stripFileExtension(b.getFile().getName());

        FileDialogs.saveFileDialog(ASCII_FILES, String.format("diff_%s_%s.txt", aFileName, bFileName))
                .ifPresent(fileName -> {
                    XYSeries aSeries = new XYSeries(chart.getSeriesSet().getSeries("a"));
                    XYSeries bSeries = new XYSeries(chart.getSeriesSet().getSeries("b"));

                    double[] newXSeries = aSeries.getXSeries();
                    double[] newYSeries = ArrayUtils.subtractArrayValues(aSeries.getYSeries(),
                            MathUtils.intep(bSeries.getXSeries(), bSeries.getYSeries(), newXSeries));

                    try {
                        printData(fileName, aFileName, bFileName, newXSeries, newYSeries);
                        FileExplorer.getDefault().refresh();

                    } catch (SpefoException exception) {
                        Message.error("An error occurred while printing to file", exception);
                    }
                });
    }

    private void printData(String fileName, String a, String b, double[] x, double[] y) throws SpefoException {
        try (PrintWriter writer = new PrintWriter(fileName)) {
            writer.print("# ");
            writer.print(a);
            writer.print(" x ");
            writer.println(b);

            for (int i = 0; i < x.length; i++) {
                String line = String.format("%01.04f  %01.04f", x[i], y[i]);
                writer.println(line);
            }

            if (writer.checkError()) {
                throw new SpefoException("The PrintWriter is in an error state");
            }

        } catch (FileNotFoundException exception) {
            throw new SpefoException("Cannot find file [" + fileName + "]", exception);
        }
    }
}
