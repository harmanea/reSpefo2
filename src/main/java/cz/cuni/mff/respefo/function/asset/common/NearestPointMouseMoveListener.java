package cz.cuni.mff.respefo.function.asset.common;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Rectangle;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;
import org.swtchart.Range;

import java.util.function.IntConsumer;

public class NearestPointMouseMoveListener implements MouseMoveListener {
    private final Chart chart;
    private final String seriesName;
    private final IntConsumer callback;

    public NearestPointMouseMoveListener(Chart chart, String seriesName, IntConsumer callback) {
        this.chart = chart;
        this.seriesName = seriesName;
        this.callback = callback;
    }

    @Override
    public void mouseMove(MouseEvent event) {
        ILineSeries series = (ILineSeries) chart.getSeriesSet().getSeries(seriesName);

        double[] xSeries = series.getXSeries();
        double[] ySeries = series.getYSeries();

        Rectangle bounds = chart.getPlotArea().getBounds();

        Range xRange = chart.getAxisSet().getXAxis(0).getRange();
        Range yRange = chart.getAxisSet().getYAxis(0).getRange();

        int index = -1;
        int closest = Integer.MAX_VALUE;

        for (int i = 0; i < xSeries.length; i++) {
            if (xSeries[i] >= xRange.lower && xSeries[i] <= xRange.upper && ySeries[i] >= yRange.lower && ySeries[i] <= yRange.upper) {
                double x = (xSeries[i] - xRange.lower) / (xRange.upper - xRange.lower) * bounds.width;
                double y = (1 - (ySeries[i] - yRange.lower) / (yRange.upper - yRange.lower)) * bounds.height;

                int distance = (int) Math.sqrt(Math.pow(x - event.x, 2) + Math.pow(y - event.y, 2));

                if (distance < closest) {
                    index = i;
                    closest = distance;
                }
            }
        }

        if (index != -1) {
            callback.accept(index);
        }
    }
}
