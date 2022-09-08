package cz.cuni.mff.respefo.function.rectify;

import cz.cuni.mff.respefo.spectrum.asset.FunctionAsset;
import cz.cuni.mff.respefo.util.utils.MathUtils;

import java.util.*;
import java.util.stream.IntStream;

public class BlazeAsset implements FunctionAsset {
    private final Map<Integer, double[]> parameters;
    private final Set<Integer> excludedOrders;
    private int polyDegree;  // a value <= 0 means use intep
    private double[] xCoordinates;
    private double[] yCoordinates;

    public BlazeAsset() {
        parameters = new HashMap<>();
        excludedOrders = new HashSet<>();
        polyDegree = 9;
    }

    public void setParameters(int order, double centralWavelength, double scale) {
        parameters.put(order, new double[]{centralWavelength, scale});
    }

    public boolean hasParameters(int order) {
        return parameters.containsKey(order);
    }

    public double getCentralWavelength(int order) {
        return parameters.get(order)[0];
    }

    public double getScale(int order) {
        return parameters.get(order)[1];
    }

    public void removeIfPresent(int order) {
        parameters.remove(order);
    }

    public boolean isEmpty() {
        return parameters.isEmpty();
    }

    public Set<Integer> getExcludedOrders() {
        return excludedOrders;
    }

    public void addExcludedOrders(Set<Integer> orders) {
        excludedOrders.addAll(orders);
    }

    public void addExcludedOrder(int order) {
        excludedOrders.add(order);
    }

    public void removeExcludedOrder(int order) {
        excludedOrders.remove(order);
    }

    public boolean isExcluded(int order) {
        return excludedOrders.contains(order);
    }

    public int getPolyDegree() {
        return polyDegree;
    }

    public void setPolyDegree(int polyDegree) {
        this.polyDegree = polyDegree;
    }

    public boolean useIntep() {
        return polyDegree <= 0;
    }

    public boolean createCoordinatesIfNull(int length) {
        if (xCoordinates == null && yCoordinates == null) {
            xCoordinates = new double[length];
            yCoordinates = new double[length];

            return true;

        } else {
            return false;
        }
    }

    public void setXCoordinate(int index, double value) {
        xCoordinates[index] = value;
    }

    public double getXCoordinate(int index) {
        return xCoordinates[index];
    }

    public void setYCoordinate(int index, double value) {
        yCoordinates[index] = value;
    }

    public double getYCoordinate(int index) {
        return yCoordinates[index];
    }

    public double[] pointXSeries() {
        return IntStream.range(0, xCoordinates.length)
                .filter(index -> !isExcluded(index))
                .mapToDouble(index -> xCoordinates[index])
                .toArray();
    }

    public double[] pointYSeries() {
        return IntStream.range(0, yCoordinates.length)
                .filter(index -> !isExcluded(index))
                .mapToDouble(index -> yCoordinates[index])
                .toArray();
    }

    public double[] recalculatePolyCoeffs() {
        double[] xSeries = pointXSeries();
        double[] ySeries = pointYSeries();

        return MathUtils.fitPolynomial(xSeries, ySeries, polyDegree);
    }

    public int findXIndex(double value) {
        return Arrays.binarySearch(xCoordinates, value);
    }
}
