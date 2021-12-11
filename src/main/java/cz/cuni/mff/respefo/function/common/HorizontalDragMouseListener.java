package cz.cuni.mff.respefo.function.common;

import cz.cuni.mff.respefo.util.utils.ChartUtils;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.swtchart.Chart;

import java.util.function.Consumer;

public class HorizontalDragMouseListener extends MouseAdapter implements MouseMoveListener {

    protected final Chart chart;
    private final Consumer<Double> dragCallback;

    protected boolean drag;
    private int prevX;

    public HorizontalDragMouseListener(Chart chart, Consumer<Double> dragCallback) {
        this.chart = chart;
        this.dragCallback = dragCallback;

        drag = false;
    }

    @Override
    public void mouseDown(MouseEvent event) {
        drag = true;
        prevX = event.x;
    }

    @Override
    public void mouseUp(MouseEvent event) {
        drag = false;
    }

    @Override
    public void mouseMove(MouseEvent event) {
        if (drag) {
            double shift = ChartUtils.getRealHorizontalDifferenceFromCoordinates(chart, prevX, event.x);
            dragCallback.accept(shift);

            prevX = event.x;
        }
    }
}
