package cz.cuni.mff.respefo.function.asset.common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.Rectangle;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxisSet;
import org.swtchart.Range;

import java.util.function.Function;
import java.util.function.ToDoubleFunction;

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
                if (xCentered && yCentered) {
                    chart.getAxisSet().zoomIn();

                } else {
                    Rectangle bounds = chart.getPlotArea().getBounds();

                    if (xCentered) {
                        zoomInCentered(IAxisSet::getXAxes);
                    } else {
                        zoomInOnMouse(
                                axisSet -> axisSet.getXAxis(0).getRange(),
                                range -> range.lower + ((range.upper - range.lower) * ((double) event.x / bounds.width)),
                                IAxisSet::getXAxes
                        );
                    }

                    if (yCentered) {
                        zoomInCentered(IAxisSet::getYAxes);
                    } else {
                        zoomInOnMouse(
                                axisSet -> axisSet.getYAxis(0).getRange(),
                                range -> range.upper - ((range.upper - range.lower) * ((double) event.y / bounds.height)),
                                IAxisSet::getYAxes
                        );
                    }
                }
                chart.redraw();

            } else if (event.count < 0) {
                chart.getAxisSet().zoomOut();
                chart.redraw();
            }
        }
    }

    private void zoomInCentered(Function<IAxisSet, IAxis[]> axesProvider) {
        for (IAxis axis : axesProvider.apply(chart.getAxisSet())) {
            axis.zoomIn();
        }
    }

    private void zoomInOnMouse(Function<IAxisSet, Range> rangeProvider, ToDoubleFunction<Range> realValueProvider, Function<IAxisSet, IAxis[]> axesProvider) {
        Range chartRange = rangeProvider.apply(chart.getAxisSet());
        double realValue = realValueProvider.applyAsDouble(chartRange);

        for (IAxis axis : axesProvider.apply(chart.getAxisSet())) {
            axis.zoomIn(realValue);
        }
    }
}
