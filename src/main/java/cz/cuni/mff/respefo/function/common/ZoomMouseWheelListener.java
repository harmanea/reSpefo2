package cz.cuni.mff.respefo.function.common;

import cz.cuni.mff.respefo.util.collections.Point;
import cz.cuni.mff.respefo.util.utils.ChartUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxisSet;

import java.util.function.Function;

public class ZoomMouseWheelListener implements MouseWheelListener {
    private final Chart chart;
    private final boolean xCentered;
    private final boolean yCentered;

    public ZoomMouseWheelListener(Chart chart) {
        this(chart, false, false);
    }

    public ZoomMouseWheelListener(Chart chart, boolean xCentered, boolean yCentered) {
        this.chart = chart;
        this.xCentered = xCentered;
        this.yCentered = yCentered;
    }

    @Override
    public void mouseScrolled(MouseEvent event) {
        if ((event.stateMask & SWT.CTRL) == SWT.CTRL) {
            if (event.count > 0) {
                zoomIn(event.x, event.y);

            } else if (event.count < 0) {
                zoomOut();
            }
        }
    }

    private void zoomIn(int x, int y) {
        if (xCentered && yCentered) {
            chart.getAxisSet().zoomIn();

        } else {
            Point mouse = ChartUtils.getRealValuesFromCoordinates(chart, x, y);

            if (xCentered) {
                zoomInCentered(IAxisSet::getXAxes);
            } else {
                zoomInOnMouse(mouse.x, IAxisSet::getXAxes);
            }

            if (yCentered) {
                zoomInCentered(IAxisSet::getYAxes);
            } else {
                zoomInOnMouse(mouse.y, IAxisSet::getYAxes);
            }
        }
        chart.redraw();
    }

    private void zoomOut() {
        chart.getAxisSet().zoomOut();
        chart.redraw();
    }

    private void zoomInCentered(Function<IAxisSet, IAxis[]> axesProvider) {
        for (IAxis axis : axesProvider.apply(chart.getAxisSet())) {
            axis.zoomIn();
        }
    }

    private void zoomInOnMouse(double realValue, Function<IAxisSet, IAxis[]> axesProvider) {
        for (IAxis axis : axesProvider.apply(chart.getAxisSet())) {
            axis.zoomIn(realValue);
        }
    }
}
