package cz.cuni.mff.respefo.function.asset.dispersion;

import cz.cuni.mff.respefo.util.DoubleArrayList;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;
import cz.cuni.mff.respefo.util.utils.MathUtils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class ComparisonLineResults implements Iterable<ComparisonLineResults.ComparisonLineResult> {
    private final int n;

    private final double[] xUp;
    private final double[] xDown;
    private final double[] x;
    private final double[] laboratoryValues;
    private final boolean[] used;
    private double[] coeffs;
    private double[] actualY;
    private double[] residuals;

    public ComparisonLineResults(List<ComparisonLineMeasurement> validMeasurements) {
        n = validMeasurements.size();

        xUp = new double[n];
        xDown = new double[n];
        x = new double[n];
        laboratoryValues = new double[n];
        used = new boolean[n];

        for (int i = 0; i < n; i++) {
            ComparisonLineMeasurement measurement = validMeasurements.get(i);

            xUp[i] = measurement.getxUp();
            xDown[i] = measurement.getxDown();
            x[i] = measurement.getX();
            laboratoryValues[i] = measurement.getLaboratoryValue();
            used[i] = true;
        }

        calculateCoeffs();
        calculateValues();

        double threshold = meanRms() * 1.5;
        for (int i = 0; i < n; i++) {
            if (residuals[i] > threshold) {
                used[i] = false;
            }
        }

        calculateCoeffs();
        calculateValues();
    }

    public void calculateCoeffs() {
        DoubleArrayList usedXs = new DoubleArrayList();
        DoubleArrayList usedLaboratoryValues = new DoubleArrayList();

        for (int i = 0; i < x.length; i++) {
            if (used[i]) {
                usedXs.add(x[i]);
                usedLaboratoryValues.add(laboratoryValues[i]);
            }
        }

        coeffs = MathUtils.fitPolynomial(usedXs.toArray(), usedLaboratoryValues.toArray(), 3);
    }

    public void calculateValues() {
        actualY = ArrayUtils.createArray(x.length, i -> MathUtils.polynomial(x[i], coeffs));
        residuals = ArrayUtils.createArray(x.length, i -> actualY[i] - laboratoryValues[i]);
    }

    public double meanRms() {
        return Arrays.stream(residuals).map(Math::abs).average().orElseThrow(IllegalStateException::new);
    }

    public double[] getX() {
        return x;
    }

    public double[] getLaboratoryValues() {
        return laboratoryValues;
    }

    public double[] getCoeffs() {
        return coeffs;
    }

    public double[] getActualY() {
        return actualY;
    }

    public double[] getResiduals() {
        return residuals;
    }

    public boolean isUsed(int index) {
        return used[index];
    }

    public void inverseUsed(int index) {
        used[index] = !used[index];
    }

    @Override
    public Iterator<ComparisonLineResult> iterator() {
        return new Iterator<ComparisonLineResult>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < n;
            }

            @Override
            public ComparisonLineResult next() {
                if (i >= n) {
                    throw new NoSuchElementException();
                }

                ComparisonLineResult result = new ComparisonLineResult() {
                    final int j = i;

                    @Override
                    public int getIndex() {
                        return j;
                    }

                    @Override
                    public double getXUp() {
                        return xUp[j];
                    }

                    @Override
                    public double getXDown() {
                        return xDown[j];
                    }

                    @Override
                    public double getX() {
                        return x[j];
                    }

                    @Override
                    public double getLaboratoryValue() {
                        return laboratoryValues[j];
                    }

                    @Override
                    public double getActualY() {
                        return actualY[j];
                    }

                    @Override
                    public double getResidual() {
                        return residuals[j];
                    }

                    @Override
                    public boolean isUsed() {
                        return used[j];
                    }
                };

                i++;
                return result;
            }
        };
    }

    public interface ComparisonLineResult {
        int getIndex();
        double getXUp();
        double getXDown();
        double getX();
        double getLaboratoryValue();
        double getActualY();
        double getResidual();
        boolean isUsed();
    }
}
