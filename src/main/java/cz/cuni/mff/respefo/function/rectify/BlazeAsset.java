package cz.cuni.mff.respefo.function.rectify;

import cz.cuni.mff.respefo.spectrum.asset.FunctionAsset;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BlazeAsset implements FunctionAsset {
    private final Map<Integer, double[]> parameters;
    private final Set<Integer> excludedOrders;
    private int polyDegree;  // a value <= 0 means use intep

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
}
