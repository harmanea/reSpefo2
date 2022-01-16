package cz.cuni.mff.respefo.function.rectify;

import cz.cuni.mff.respefo.spectrum.asset.FunctionAsset;

import java.util.HashMap;
import java.util.Map;

public class BlazeAsset implements FunctionAsset {
    private final Map<Integer, double[]> parameters;

    public BlazeAsset() {
        parameters = new HashMap<>();
    }

    public void setParameters(int order, double centralWavelength, double scale) {
        parameters.put(order, new double[]{centralWavelength, scale});
    }

    public boolean hasParameters(int order) {
        return parameters.containsKey(order);
    }

    public double[] getParameters(int order) {
        return parameters.get(order);
    }

    public void removeIfPresent(int order) {
        parameters.remove(order);
    }

    public boolean isEmpty() {
        return parameters.isEmpty();
    }
}
