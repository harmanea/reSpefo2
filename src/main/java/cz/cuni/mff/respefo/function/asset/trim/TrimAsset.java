package cz.cuni.mff.respefo.function.asset.trim;

import cz.cuni.mff.respefo.format.XYSeries;
import cz.cuni.mff.respefo.format.asset.FunctionAsset;

import static java.util.Arrays.binarySearch;
import static java.util.Arrays.copyOfRange;

public class TrimAsset implements FunctionAsset {
    private double min = Double.NEGATIVE_INFINITY;
    private double max = Double.POSITIVE_INFINITY;

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    @Override
    public XYSeries process(XYSeries series) {
        double[] xSeries = series.getXSeries();

        int lowerIndex = binarySearch(xSeries, min);
        int upperIndex = binarySearch(xSeries, max);

        if (lowerIndex < 0) {
            lowerIndex = -lowerIndex - 1;
        } else if (lowerIndex >= xSeries.length) {
            lowerIndex = xSeries.length - 1;
        }

        if (upperIndex < 0) {
            upperIndex = -upperIndex - 1;
        } else if (upperIndex >= xSeries.length) {
            upperIndex = xSeries.length - 1;
        }

        return new XYSeries(copyOfRange(xSeries, lowerIndex, upperIndex), copyOfRange(series.getYSeries(), lowerIndex, upperIndex));
    }
}
