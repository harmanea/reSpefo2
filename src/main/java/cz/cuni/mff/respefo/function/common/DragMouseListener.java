package cz.cuni.mff.respefo.function.common;

import cz.cuni.mff.respefo.util.utils.ChartUtils;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.Range;

public class DragMouseListener extends MouseAdapter implements MouseMoveListener {
    protected final Chart chart;

    protected boolean drag;
    protected int prevX;
    protected int prevY;

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
            double xChange = ChartUtils.getRealHorizontalDifferenceFromCoordinates(chart, event.x, prevX);
            Range xRange = new Range(chartXRange.lower + xChange, chartXRange.upper + xChange);

            for (IAxis xAxis : chart.getAxisSet().getXAxes()) {
                xAxis.setRange(xRange);
            }

            Range chartYRange = chart.getAxisSet().getYAxis(0).getRange();
            double yChange = ChartUtils.getRealVerticalDifferenceFromCoordinates(chart, event.y, prevY);
            Range yRange = new Range(chartYRange.lower + yChange, chartYRange.upper + yChange);

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
