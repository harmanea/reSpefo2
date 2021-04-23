package cz.cuni.mff.respefo.function.disp;

import cz.cuni.mff.respefo.util.utils.MathUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

import static java.lang.Math.pow;
import static java.util.stream.Collectors.toList;

public class ComparisonLineMeasurements {
    private final List<ComparisonLineMeasurement> measurements;

    public ComparisonLineMeasurements(double[] cmpValues) {
        measurements = new ArrayList<>(cmpValues.length);

        for (double cmpValue : cmpValues) {
            measurements.add(new ComparisonLineMeasurement(cmpValue));
        }
    }

    public int size() {
        return measurements.size();
    }

    public int numberOfMeasured() {
        return (int) measurements.stream()
                .filter(ComparisonLineMeasurement::isMeasured)
                .count();
    }

    public ComparisonLineMeasurement getMeasurement(int index) {
        return measurements.get(index);
    }

    public double hint(int index) {
        // find up to three closest (by index, not value) valid measurements
        List<Integer> indices = new ArrayList<>();

        int i = 1;
        while (indices.size() < 3 && (index - i >= 0 || index + i < size())) {
            if (index - i >= 0 && measurements.get(index - i).isMeasured()) {
                indices.add(index - i);
                if (indices.size() == 3) {
                    break;
                }
            }
            if (index + i < size() && measurements.get(index + i).isMeasured()) {
                indices.add(index + i);
            }

            i++;
        }

        double x = measurements.get(index).getLaboratoryValue();

        double x1 = measurements.get(indices.get(0)).getLaboratoryValue();
        double x2 = measurements.get(indices.get(1)).getLaboratoryValue();

        double y1 = measurements.get(indices.get(0)).getX();
        double y2 = measurements.get(indices.get(1)).getX();

        if (indices.size() > 2) {
            double x3 = measurements.get(indices.get(2)).getLaboratoryValue();
            double y3 = measurements.get(indices.get(2)).getX();

            double denominator = (x1 - x2) * (x1 - x3) * (x2 - x3);
            double a = (x3 * (y2 - y1) + x2 * (y1 - y3) + x1 * (y3 - y2)) / denominator;
            double b = (pow(x3, 2) * (y1 - y2) + pow(x2, 2) * (y3 - y1) + pow(x1, 2) * (y2 - y3)) / denominator;
            double c = (x2 * x3 * (x2 - x3) * y1 + x3 * x1 * (x3 - x1) * y2 + x1 * x2 * (x1 - x2) * y3) / denominator;

            return MathUtils.polynomial(x, new double[]{c, b, a});

        } else {
            return MathUtils.linearInterpolation(x1, y1, x2, y2, x);
        }
    }

    public ComparisonLineResults getResults() {
        return new ComparisonLineResults(
                measurements.stream()
                        .filter(ComparisonLineMeasurement::isMeasured)
                        .collect(toList())
        );
    }

    public Iterator<Integer> unmeasuredIndicesIterator() {
        int start = IntStream.range(0, size())
                .filter(index -> getMeasurement(index).isMeasured())
                .findFirst()
                .orElseThrow(IllegalStateException::new);

        return new Iterator<Integer>() {
            boolean ascending = true;
            int i = increment(start);

            @Override
            public boolean hasNext() {
                return i >= 0;
            }

            @Override
            public Integer next() {
                if (i < 0) {
                    throw new NoSuchElementException();
                }

                int value = i;
                i = increment(i);
                return value;
            }

            private int increment(int j) {
                do {
                    j += ascending ? 1 : -1;
                    if (j == size()) {
                        j = start - 1;
                        ascending = false;
                    }
                } while (j >= 0 && getMeasurement(j).isMeasured());

                return j;
            }
        };
    }
}
