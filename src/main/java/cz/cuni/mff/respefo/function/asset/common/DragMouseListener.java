package cz.cuni.mff.respefo.function.asset.common;

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Rectangle;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.Range;

public class DragMouseListener extends MouseAdapter implements MouseMoveListener {
    protected final Chart chart;

    protected boolean drag;
    private int prevX;
    private int prevY;

    public DragMouseListener(Chart chart) {
        this.chart = chart;

        drag = false;
    }

    @Override
    public void mouseDown(MouseEvent event) {
        drag = true;

        prevX = event.x;
        prevY = event.y;
    }

    @Override
    public void mouseUp(MouseEvent event) {
        drag = false;
    }

    @Override
    public void mouseMove(MouseEvent event) {
        if (drag) {
            Range chartXRange = chart.getAxisSet().getXAxis(0).getRange();
            Range chartYRange = chart.getAxisSet().getYAxis(0).getRange();

            Rectangle bounds = chart.getPlotArea().getBounds();

            double xChange = (double)(prevX - event.x) / bounds.width;
            Range xRange = new Range(chartXRange.lower + (chartXRange.upper - chartXRange.lower) * xChange,
                    chartXRange.upper + (chartXRange.upper - chartXRange.lower) * xChange);

            double yChange = (double)(event.y - prevY) / bounds.height;
            Range yRange = new Range(chartYRange.lower + (chartYRange.upper - chartYRange.lower) * yChange,
                    chartYRange.upper + (chartYRange.upper - chartYRange.lower) * yChange);

            for (IAxis xAxis : chart.getAxisSet().getXAxes()) {
                xAxis.setRange(xRange);
            }
            for (IAxis yAxis : chart.getAxisSet().getYAxes()) {
                yAxis.setRange(yRange);
            }

            chart.redraw();
            chart.forceFocus();

            prevX = event.x;
            prevY = event.y;
        }
    }
}
