package cz.cuni.mff.respefo.util.utils;

import cz.cuni.mff.respefo.util.UtilityClass;
import cz.cuni.mff.respefo.util.collections.Point;
import org.swtchart.*;

import java.util.Objects;
import java.util.function.Function;

import static java.util.Arrays.stream;

// TODO: add tests
public class ChartUtils extends UtilityClass {

    /**
     * Set the range of all chart axes so that all are fully visible.
     * @param chart whose axes will be adjusted
     */
    public static void makeAllSeriesEqualRange(Chart chart) {
        Objects.requireNonNull(chart);

        double xMax = Double.NEGATIVE_INFINITY;
        double xMin = Double.POSITIVE_INFINITY;
        double yMax = Double.NEGATIVE_INFINITY;
        double yMin = Double.POSITIVE_INFINITY;

        for (ISeries series : chart.getSeriesSet().getSeries()) {
            double[] xSeries = series.getXSeries();
            double[] ySeries = series.getYSeries();

            if (xSeries.length == 0 || ySeries.length == 0) {
                continue;
            }

            if (xMax < xSeries[xSeries.length - 1]) {
                xMax = xSeries[xSeries.length - 1];
            }
            if (xMin > xSeries[0]) {
                xMin = xSeries[0];
            }

            double ySeriesMax = stream(ySeries).max().getAsDouble();
            double ySeriesMin = stream(ySeries).min().getAsDouble();

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

    /**
     * Set the range of all axes so that the series with the given name are fully visible.
     * @param chart whose axes will be adjusted
     * @param seriesName name of the series to center around
     */
    public static void centerAroundSeries(Chart chart, String seriesName) {
        center(chart, seriesName, xSeries -> rangeWithMargin(xSeries[0], xSeries[xSeries.length - 1]));
    }

    /**
     * Set the range of all axes so that the series with the given name are fully visible while maintaining the
     * horizontal midpoint in the center of the chart.
     * @param chart whose axes will be adjusted
     * @param seriesName name of the series to center around
     * @param midpoint which should be in the horizontal center
     */
    public static void centerAroundSeriesAndMidpoint(Chart chart, String seriesName, double midpoint) {
        center(chart, seriesName, xSeries -> {
            double distance = Math.max(Math.abs(xSeries[0] - midpoint), Math.abs(xSeries[xSeries.length - 1] - midpoint));
            return rangeWithMargin(midpoint - distance, midpoint + distance);
        });
    }

    private static void center(Chart chart, String seriesName, Function<double[], Range> xRangeCreator) {
        Objects.requireNonNull(chart);
        Objects.requireNonNull(seriesName);

        ISeries series = chart.getSeriesSet().getSeries(seriesName);

        double[] xSeries = series.getXSeries();
        double[] ySeries = series.getYSeries();
        if (xSeries.length == 0 || ySeries.length == 0) {
            throw new IllegalArgumentException("The selected series has no data points");
        }

        Range xRange = xRangeCreator.apply(xSeries);

        double ySeriesMax = stream(ySeries).max().getAsDouble();
        double ySeriesMin = stream(ySeries).min().getAsDouble();
        Range yRange = rangeWithMargin(ySeriesMin, ySeriesMax);

        for (IAxis xAxis : chart.getAxisSet().getXAxes()) {
            xAxis.setRange(xRange);
        }
        for (IAxis yAxis : chart.getAxisSet().getYAxes()) {
            yAxis.setRange(yRange);
        }
    }

    private static Range rangeWithMargin(double lower, double upper) {
        double length = upper - lower;
        double margin = length / 100;

        return new Range(lower - margin, upper + margin);
    }

    /**
     * Transform the x and y coordinates of the chart plot area to corresponding values in the displayed plot.
     * @param chart whose coordinate should be transformed
     * @param x coordinate of the chart plot area
     * @param y coordinate of the chart plot area
     * @return real values
     */
    public static Point getRealValuesFromCoordinates(Chart chart, int x, int y) {
        Objects.requireNonNull(chart);

        IAxisSet axisSet = chart.getAxisSet();
        return new Point(axisSet.getXAxis(0).getDataCoordinate(x), axisSet.getYAxis(0).getDataCoordinate(y));
    }

    /**
     * Get the step value that is 1/1000th of the x range.
     * @param chart to calculate relative step for
     * @return relative step
     */
    public static double getRelativeHorizontalStep(Chart chart) {
        Objects.requireNonNull(chart);

        Range range = chart.getAxisSet().getXAxis(0).getRange();
        return (range.upper - range.lower) / 1000;
    }

    /**
     * Get the step value that is 1/1000th of the y range.
     * @param chart to calculate relative step for
     * @return relative step
     */
    public static double getRelativeVerticalStep(Chart chart) {
        Objects.requireNonNull(chart);

        Range range = chart.getAxisSet().getYAxis(0).getRange();
        return (range.upper - range.lower) / 1000;
    }

    protected ChartUtils() throws IllegalAccessException {
        super();
    }
}
