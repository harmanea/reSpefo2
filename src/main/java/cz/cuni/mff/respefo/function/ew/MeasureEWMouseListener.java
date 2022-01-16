package cz.cuni.mff.respefo.function.ew;

import cz.cuni.mff.respefo.function.common.HorizontalDragMouseListener;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;
import org.eclipse.swt.events.MouseEvent;
import org.swtchart.Chart;
import org.swtchart.IAxis;

import java.util.function.Consumer;

public class MeasureEWMouseListener extends HorizontalDragMouseListener {
    private final XYSeries series;

    private final Runnable mouseUpCallback;
    private final MeasureEWResult result;
    private final Consumer<Integer> activeLineCallback;

    public MeasureEWMouseListener(Chart chart, XYSeries series, Consumer<Double> dragCallback, Runnable mouseUpCallback,
                                  MeasureEWResult result, Consumer<Integer> activeLineCallback) {
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
            IAxis xAxis = chart.getAxisSet().getXAxis(0);

            int index = ArrayUtils.indexOfMin(result.pointsCount() + 2,
                    i -> {
                        int currentIndex = i < 2 ? result.getBound(i) : result.getPoint(i - 2);
                        double x = xAxis.getPixelCoordinate(series.getX(currentIndex));
                        return Math.abs(x - event.x);
                    });

            activeLineCallback.accept(index - 2);
        }
    }
}
