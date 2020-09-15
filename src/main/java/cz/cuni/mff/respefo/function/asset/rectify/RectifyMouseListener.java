package cz.cuni.mff.respefo.function.asset.rectify;

import cz.cuni.mff.respefo.function.asset.common.DragMouseListener;
import cz.cuni.mff.respefo.util.Point;
import cz.cuni.mff.respefo.util.utils.ChartUtils;
import org.eclipse.swt.events.MouseEvent;
import org.swtchart.Chart;

import java.util.function.Consumer;

public class RectifyMouseListener extends DragMouseListener {
    private final Consumer<Point> insertCallback;
    private final Runnable deleteCallback;

    private boolean dragged;

    public RectifyMouseListener(Chart chart, Consumer<Point> insertCallback, Runnable deleteCallback) {
        super(chart);

        this.insertCallback = insertCallback;
        this.deleteCallback = deleteCallback;
    }

    @Override
    public void mouseDown(MouseEvent event) {
        super.mouseDown(event);

        dragged = false;
    }

    @Override
    public void mouseUp(MouseEvent event) {
        super.mouseUp(event);

        if (!dragged) {
            if (event.button == 1) {
                Point point = ChartUtils.getRealValuesFromEventPosition(chart, event.x, event.y);
                insertCallback.accept(point);

            } else if (event.button == 3) {
                deleteCallback.run();
            }
        }
    }

    @Override
    public void mouseMove(MouseEvent event) {
        super.mouseMove(event);

        if (drag) {
            dragged = true;
        }
    }
}
