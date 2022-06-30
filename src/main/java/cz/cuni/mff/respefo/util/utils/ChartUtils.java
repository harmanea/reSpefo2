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

        IAxisSet axisSet = chart.getAxisSet();
        axisSet.getXAxis(0).setRange(xRange);
        axisSet.getYAxis(0).setRange(yRange);
        axisSet.getYAxis(1).setRange(yRange);
    }

    public static Range rangeWithMargin(double lower, double upper) {
        double length = upper - lower;
        double margin = length / 100;

        return new Range(lower - margin, upper + margin);
    }

    /**
     * Set the range of all chart axes so that all are fully visible.
     * @param chart whose axes will be adjusted
     */
    public static void adjustRange(Chart chart) {
        Objects.requireNonNull(chart);

        IAxisSet axisSet = chart.getAxisSet();
        axisSet.getXAxis(0).adjustRange();

        IAxis primaryYAxis = axisSet.getYAxis(0);
        IAxis secondaryYAxis = axisSet.getYAxis(1);

        primaryYAxis.adjustRange();
        secondaryYAxis.setRange(primaryYAxis.getRange());
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
     * Transform the x and y values of the displayed plot to their corresponding chart plot area coordinates.
     * @param chart whose real values should be transformed
     * @param x real chart value
     * @param y real chart value
     * @return chart plot area coordinates
     */
    public static Point getCoordinatesFromRealValues(Chart chart, double x, double y) {
        Objects.requireNonNull(chart);

        IAxisSet axisSet = chart.getAxisSet();
        return new Point(axisSet.getXAxis(0).getPixelCoordinate(x), axisSet.getYAxis(0).getPixelCoordinate(y));
    }

    /**
     * Get the difference of the two x chart coordinates as the corresponding value of the displayed plot.
     * @param chart the difference of whose coordinates should be calculated
     * @param x0 base coordinate
     * @param x1 moved coordinate
     * @return difference
     */
    public static double getRealHorizontalDifferenceFromCoordinates(Chart chart, int x0, int x1) {
        Objects.requireNonNull(chart);

        IAxis xAxis = chart.getAxisSet().getXAxis(0);
        return xAxis.getDataCoordinate(x1) - xAxis.getDataCoordinate(x0);
    }

    /**
     * Get the difference of the two y chart coordinates as the corresponding value of the displayed plot.
     * @param chart the difference of whose coordinates should be calculated
     * @param y0 base coordinate
     * @param y1 moved coordinate
     * @return difference
     */
    public static double getRealVerticalDifferenceFromCoordinates(Chart chart, int y0, int y1) {
        Objects.requireNonNull(chart);

        IAxis yAxis = chart.getAxisSet().getYAxis(0);
        return yAxis.getDataCoordinate(y1) - yAxis.getDataCoordinate(y0);
    }

    /**
     * Check whether the values are visible in the displayed plot.
     * @param chart in which to check visibility
     * @param x real chart value
     * @param y real chart value
     * @return True if the plot values are visible, False otherwise
     */
    public static boolean isRealValueVisible(Chart chart, double x, double y) {
        Objects.requireNonNull(chart);

        Range xRange = chart.getAxisSet().getXAxis(0).getRange();
        Range yRange = chart.getAxisSet().getYAxis(0).getRange();

        return x > xRange.lower && x < xRange.upper && y > yRange.lower && y < yRange.upper;
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
