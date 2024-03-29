package cz.cuni.mff.respefo.function.ew;

import cz.cuni.mff.respefo.spectrum.asset.AppendableFunctionAsset;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MeasureEWResults implements AppendableFunctionAsset<MeasureEWResults>, Iterable<MeasureEWResult> {
    final List<MeasureEWResult> results;

    public MeasureEWResults() {
        results = new ArrayList<>();
    }

    public boolean isEmpty() {
        return results.isEmpty();
    }

    public void add(MeasureEWResult result) {
        results.add(result);
    }

    public void remove(MeasureEWResult result) {
        results.remove(result);
    }

    @Override
    public void append(MeasureEWResults other) {
        results.addAll(other.results);
    }

    public String[] getMeasurementNames() {
        return results.stream().map(MeasureEWResult::getName).toArray(String[]::new);
    }

    public boolean hasMeasurementWithName(String name) {
        return results.stream().map(MeasureEWResult::getName).anyMatch(n -> n.equals(name));
    }

    public MeasureEWResult getResultForName(String name) {
        return results.stream()
                .filter(result -> result.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public boolean isRepeatedMeasurement(MeasureEWResult result) {
        return results.stream().anyMatch(result::isRepeated);
    }

    @Override
    public Iterator<MeasureEWResult> iterator() {
        return results.iterator();
    }
}
