package cz.cuni.mff.respefo.function.asset.ew;

import cz.cuni.mff.respefo.format.asset.FunctionAsset;
import cz.cuni.mff.respefo.function.asset.common.Measurement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

public class MeasureEWResults implements FunctionAsset, Iterable<MeasureEWResults.MeasurementAndResult> {
    List<Measurement> measurements;
    List<MeasureEWResult> results;

    private MeasureEWResults() {
        // default empty constructor
    }

    public MeasureEWResults(List<Measurement> measurements) {
        results = new ArrayList<>();
        this.measurements = measurements;
    }

    public void add(MeasureEWResult result) {
        results.add(result);
    }

    public void append(MeasureEWResults other) {
        measurements.addAll(other.measurements);
        results.addAll(other.results);
    }

    public String[] getMeasurementNames() {
        return measurements.stream().map(Measurement::getName).toArray(String[]::new);
    }

    public boolean hasMeasurementWithName(String name) {
        return measurements.stream().map(Measurement::getName).anyMatch(n -> n.equals(name));
    }

    public MeasureEWResult getResultForName(String name) {
        int index = IntStream.range(0, measurements.size())
                .filter(i -> measurements.get(i).getName().equals(name))
                .findFirst()
                .orElse(-1);

        if (index >= 0) {
            return results.get(index);
        } else {
            return null;
        }
    }

    @Override
    public Iterator<MeasurementAndResult> iterator() {
        return new Iterator<MeasurementAndResult>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < results.size();
            }

            @Override
            public MeasurementAndResult next() {
                if (i >= results.size()) {
                    throw new NoSuchElementException();
                }

                return new MeasurementAndResult() {
                    final int j = i++;

                    @Override
                    public Measurement getMeasurement() {
                        return measurements.get(j);
                    }

                    @Override
                    public MeasureEWResult getResult() {
                        return results.get(j);
                    }
                };
            }
        };
    }


    public interface MeasurementAndResult {
        Measurement getMeasurement();
        MeasureEWResult getResult();
    }
}
