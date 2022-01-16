package cz.cuni.mff.respefo.function.rectify;

import cz.cuni.mff.respefo.function.common.DragMouseListener;
import cz.cuni.mff.respefo.util.collections.Point;
import cz.cuni.mff.respefo.util.utils.ChartUtils;
import org.eclipse.swt.events.MouseEvent;
import org.swtchart.Chart;

public class BlazeMouseListener extends DragMouseListener {
    private final Runnable update;
    private boolean horizontal;

    public BlazeMouseListener(Chart chart, Runnable update) {
        super(chart);

        this.update = update;
    }

    @Override
    public void mouseMove(MouseEvent event) {
        Blaze blaze = (Blaze) chart.getData("blaze");

        if (drag) {
            // Move the active line

            if (horizontal) {
                double diff = ChartUtils.getRealHorizontalDifferenceFromCoordinates(chart, prevX, event.x);
                blaze.updateCentralWavelength(diff);
            } else {
                double diff = ChartUtils.getRealVerticalDifferenceFromCoordinates(chart, prevY, event.y);
                blaze.updateScale(diff);
            }

            prevX = event.x;
            prevY = event.y;

            update.run();

        } else {
            // Select the active line

            Point coordinates = ChartUtils.getCoordinatesFromRealValues(chart, blaze.getCentralWavelength(), blaze.getScale());

            double horizontalDistance = Math.abs(coordinates.x - event.x);
            double verticalDistance = Math.abs(coordinates.y - event.y);

            horizontal = horizontalDistance < verticalDistance;

            chart.setData("horizontal", horizontal);
            chart.redraw();
        }
    }
}
