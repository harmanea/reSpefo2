package cz.cuni.mff.respefo.function.asset.trim;

import cz.cuni.mff.respefo.format.Data;
import cz.cuni.mff.respefo.format.FunctionAsset;

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
    public Data process(Data data) {
        double[] xSeries = data.getX();

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

        return new Data(copyOfRange(xSeries, lowerIndex, upperIndex), copyOfRange(data.getY(), lowerIndex, upperIndex));
    }
}
