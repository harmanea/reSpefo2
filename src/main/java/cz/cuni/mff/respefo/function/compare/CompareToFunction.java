package cz.cuni.mff.respefo.function.compare;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.common.ChartKeyListener;
import cz.cuni.mff.respefo.function.common.DragMouseListener;
import cz.cuni.mff.respefo.function.common.ZoomMouseWheelListener;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.function.rectify.RectifyFunction;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.spectrum.format.EchelleSpectrum;
import cz.cuni.mff.respefo.util.FileDialogs;
import cz.cuni.mff.respefo.util.FileType;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.widget.ChartBuilder;
import org.eclipse.swt.events.KeyListener;
import org.swtchart.Chart;
import org.swtchart.ISeries;
import org.swtchart.Range;

import java.io.File;
import java.util.Arrays;
import java.util.function.Consumer;

import static cz.cuni.mff.respefo.resources.ColorManager.getColor;
import static cz.cuni.mff.respefo.resources.ColorResource.BLUE;
import static cz.cuni.mff.respefo.resources.ColorResource.GREEN;
import static cz.cuni.mff.respefo.util.Constants.SPEED_OF_LIGHT;
import static cz.cuni.mff.respefo.util.utils.ChartUtils.rangeWithMargin;
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
                            updateYScale(ch, bSeries.getYSeries(), -0.05);
                            break;
                        case 'i':
                            updateYScale(ch, bSeries.getYSeries(), 0.05);
                            break;
                        case 'j':
                            updateXShift(ch, bSeries.getXSeries(), -1);
                            break;
                        case 'l':
                            updateXShift(ch, bSeries.getXSeries(), 1);
                            break;
                    }
                }))
                .keyListener(ch -> new ChartKeyListener.CustomAction(ch, adjustRange))
                .mouseAndMouseMoveListener(DragMouseListener::new)
                .mouseWheelListener(ZoomMouseWheelListener::new)
                .accept(adjustRange)
                .forceFocus()
                .build(ComponentManager.clearAndGetScene());

        chart.getAxisSet().getYAxis(0).getTick().setForeground(getColor(GREEN));
        chart.getAxisSet().getYAxis(0).getTitle().setForeground(getColor(GREEN));
        chart.getAxisSet().getYAxis(1).getTick().setForeground(getColor(BLUE));
        chart.getAxisSet().getYAxis(1).getTitle().setForeground(getColor(BLUE));
    }

    private void updateXShift(Chart chart, double[] xSeries, double diff) {
        double newXShift = (double) chart.getData("x shift") + diff;
        chart.getSeriesSet().getSeries("b")
                .setXSeries(Arrays.stream(xSeries)
                        .map(value -> value + newXShift * (value / SPEED_OF_LIGHT))
                        .toArray());
        chart.setData("x shift", newXShift);
        chart.redraw();
    }

    private void updateYScale(Chart chart, double[] ySeries, double diff) {
        double oldYScale = (double) chart.getData("y scale");
        double newYScale = oldYScale + diff;  // TODO: handle <= 0

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
}
