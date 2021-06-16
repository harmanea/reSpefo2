package cz.cuni.mff.respefo.function.ew;

import cz.cuni.mff.respefo.function.common.HorizontalDragMouseListener;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.swtchart.Chart;
import org.swtchart.Range;

import java.util.function.Consumer;

public class MeasureEWMouseListener extends HorizontalDragMouseListener {
    private final XYSeries series;

    private final Runnable mouseUpCallback;
    private final MeasureEWResult result;
    private final Consumer<Integer> activeLineCallback;

    public MeasureEWMouseListener(Chart chart, XYSeries series, Consumer<Double> dragCallback, Runnable mouseUpCallback, MeasureEWResult result, Consumer<Integer> activeLineCallback) {
        super(chart, dragCallback);

        this.series = series;

        this.mouseUpCallback = mouseUpCallback;
        this.result = result;
        this.activeLineCallback = activeLineCallback;
    }

    @Override
    public void mouseUp(MouseEvent event) {
        super.mouseUp(event);

        mouseUpCallback.run();
    }

    @Override
    public void mouseMove(MouseEvent event) {
        super.mouseMove(event);

        if (!drag) {
            Range range = chart.getAxisSet().getXAxis(0).getRange();
            Rectangle bounds = chart.getPlotArea().getBounds();

            int minIndex = Integer.MIN_VALUE;
            double minDistance = Integer.MAX_VALUE;

            for (int i = -2; i < result.pointsCount(); i++) {
                int currentIndex = i < 0 ? result.getBound(i + 2) : result.getPoint(i);
                double x = (series.getX(currentIndex) - range.lower) / (range.upper - range.lower) * bounds.width;
                double distance = (int) Math.abs(x - event.x);

                if (distance < minDistance) {
                    minDistance = distance;
                    minIndex = i;
                }
            }

            activeLineCallback.accept(minIndex);
        }
    }
}
