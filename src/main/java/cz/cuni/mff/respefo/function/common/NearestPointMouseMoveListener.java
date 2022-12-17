package cz.cuni.mff.respefo.function.common;

import cz.cuni.mff.respefo.util.collections.Point;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;
import cz.cuni.mff.respefo.util.utils.ChartUtils;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;

import java.util.function.IntConsumer;

public class NearestPointMouseMoveListener implements MouseMoveListener {
    protected final Chart chart;
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

        Point mouse = new Point(event.x, event.y);

        int index = ArrayUtils.indexOfMin(xSeries.length,
                i -> mouse.squaredDistanceTo(ChartUtils.getCoordinatesFromRealValues(chart, xSeries[i], ySeries[i])),
                i -> ChartUtils.isRealValueVisible(chart, xSeries[i], ySeries[i])
        );

        if (index >= 0) {
            callback.accept(index);
        }
    }
}
