package cz.cuni.mff.respefo.util.utils;

import cz.cuni.mff.respefo.util.Point;
import cz.cuni.mff.respefo.util.UtilityClass;
import org.eclipse.swt.graphics.Rectangle;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.ISeries;
import org.swtchart.Range;

import static java.lang.Double.isNaN;
import static java.util.Arrays.stream;

public class ChartUtils extends UtilityClass {

    public static void makeAllSeriesEqualRange(Chart chart) {
        double xMax = Double.NEGATIVE_INFINITY;
        double xMin = Double.POSITIVE_INFINITY;
        double yMax = Double.NEGATIVE_INFINITY;
        double yMin = Double.POSITIVE_INFINITY;

        for (ISeries series : chart.getSeriesSet().getSeries()) {
            double[] xSeries = series.getXSeries();

            if (xSeries.length == 0) {
                continue;
            }

            if (xMax < xSeries[xSeries.length - 1]) {
                xMax = xSeries[xSeries.length - 1];
            }
            if (xMin > xSeries[0]) {
                xMin = xSeries[0];
            }

            double[] ySeries = series.getYSeries();
            double ySeriesMax = stream(ySeries).filter(d -> !isNaN(d)).max().getAsDouble();
            double ySeriesMin = stream(ySeries).filter(d -> !isNaN(d)).min().getAsDouble();

            if (yMax < ySeriesMax) {
                yMax = ySeriesMax;
            }
            if (yMin > ySeriesMin) {
                yMin = ySeriesMin;
            }

        }

        Range xRange = new Range(xMin, xMax);
        Range yRange = new Range(yMin, yMax);

        for (IAxis xAxis : chart.getAxisSet().getXAxes()) {
            xAxis.setRange(xRange);
        }
        for (IAxis yAxis : chart.getAxisSet().getYAxes()) {
            yAxis.setRange(yRange);
        }
    }

    public static void centerAroundSeries(Chart chart, String seriesName) {
        ISeries series = chart.getSeriesSet().getSeries(seriesName);

        double[] xSeries = series.getXSeries();
        Range xRange = new Range(xSeries[0], xSeries[xSeries.length - 1]);

        double[] ySeries = series.getYSeries();
        double ySeriesMax = stream(ySeries).filter(d -> !isNaN(d)).max().getAsDouble();
        double ySeriesMin = stream(ySeries).filter(d -> !isNaN(d)).min().getAsDouble();
        Range yRange = new Range(ySeriesMin, ySeriesMax);

        for (IAxis xAxis : chart.getAxisSet().getXAxes()) {
            xAxis.setRange(xRange);
        }
        for (IAxis yAxis : chart.getAxisSet().getYAxes()) {
            yAxis.setRange(yRange);
        }
    }

    public static Point getRealValuesFromEventPosition(Chart chart, int x, int y) {
        Range yRange = chart.getAxisSet().getYAxis(0).getRange();
        Range xRange = chart.getAxisSet().getXAxis(0).getRange();

        Rectangle bounds = chart.getPlotArea().getBounds();

        double realX = xRange.lower + ((xRange.upper - xRange.lower) * ((double) x / bounds.width));
        double realY = yRange.lower + ((yRange.upper - yRange.lower) * ((double) (bounds.height - y) / bounds.height));

        return new Point(realX, realY);
    }

    protected ChartUtils() throws IllegalAccessException {
        super();
    }
}
