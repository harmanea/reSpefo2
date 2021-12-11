package cz.cuni.mff.respefo.function.rectify;

import cz.cuni.mff.respefo.function.common.NearestPointMouseMoveListener;
import cz.cuni.mff.respefo.util.collections.Point;
import cz.cuni.mff.respefo.util.utils.ChartUtils;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.swtchart.Chart;

import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class RectifyMouseListener extends NearestPointMouseMoveListener implements MouseListener {
    private final Consumer<Point> dragCallback;
    private final Consumer<Point> insertCallback;
    private final Runnable deleteCallback;

    private boolean drag;
    private boolean dragged;

    private int prevX;
    private int prevY;

    public RectifyMouseListener(Chart chart, String seriesName, IntConsumer activeCallback, Consumer<Point> dragCallback, Consumer<Point> insertCallback, Runnable deleteCallback) {
        super(chart, seriesName, activeCallback);

        this.dragCallback = dragCallback;
        this.insertCallback = insertCallback;
        this.deleteCallback = deleteCallback;
    }

    @Override
    public void mouseDoubleClick(MouseEvent e) {
        // no action
    }

    @Override
    public void mouseDown(MouseEvent event) {
        drag = true;
        dragged = false;

        prevX = event.x;
        prevY = event.y;
    }

    @Override
    public void mouseUp(MouseEvent event) {
        drag = false;

        if (!dragged) {
            if (event.button == 1) {
                Point point = ChartUtils.getRealValuesFromCoordinates(chart, event.x, event.y);
                insertCallback.accept(point);

            } else if (event.button == 3) {
                deleteCallback.run();
            }
        }
    }

    @Override
    public void mouseMove(MouseEvent event) {
        if (drag) {
            double xShift = ChartUtils.getRealHorizontalDifferenceFromCoordinates(chart, prevX, event.x);
            double yShift = ChartUtils.getRealVerticalDifferenceFromCoordinates(chart, prevY, event.y);

            dragCallback.accept(new Point(xShift, yShift));

            prevX = event.x;
            prevY = event.y;

            dragged = true;
        } else {
            super.mouseMove(event);
        }
    }
}
