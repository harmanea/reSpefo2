package cz.cuni.mff.respefo.function.disp;

import cz.cuni.mff.respefo.util.collections.DoubleArrayList;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;
import cz.cuni.mff.respefo.util.utils.MathUtils;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

public class ComparisonLineResults implements Iterable<ComparisonLineResults.ComparisonLineResult> {
    public static final int MAX_POLY_DEGREE = 9;

    private final int n;

    private final double[] xUp;
    private final double[] xDown;
    private final double[] x;
    private final double[] laboratoryValues;
    private final boolean[] used;
    private double[] coefficients;
    private double[] actualY;
    private double[] residuals;

    private int polyDegree;

    public ComparisonLineResults(List<ComparisonLineMeasurement> validMeasurements) {
        polyDegree = 3;

        n = validMeasurements.size();

        xUp = new double[n];
        xDown = new double[n];
        x = new double[n];
        laboratoryValues = new double[n];
        used = new boolean[n];

        for (int i = 0; i < n; i++) {
            ComparisonLineMeasurement measurement = validMeasurements.get(i);

            xUp[i] = measurement.getXUp();
            xDown[i] = measurement.getXDown();
            x[i] = measurement.getX();
            laboratoryValues[i] = measurement.getLaboratoryValue();
            used[i] = true;
        }

        calculateCoefficients();
        calculateValues();

        double threshold = meanRms() * 1.5;
        for (int i = 0; i < n; i++) {
            if (Math.abs(residuals[i]) > threshold) {
                used[i] = false;
            }
        }

        calculateCoefficients();
        calculateValues();
    }

    public void calculateCoefficients() {
        DoubleArrayList usedXs = new DoubleArrayList();
        DoubleArrayList usedLaboratoryValues = new DoubleArrayList();

        for (int i = 0; i < x.length; i++) {
            if (used[i]) {
                usedXs.add(x[i]);
                usedLaboratoryValues.add(laboratoryValues[i]);
            }
        }

        coefficients = MathUtils.fitPolynomial(usedXs.toArray(), usedLaboratoryValues.toArray(), polyDegree);
    }

    public void setPolyDegree(int polyDegree) {
        this.polyDegree = polyDegree;
    }

    public void calculateValues() {
        actualY = ArrayUtils.createArray(x.length, i -> MathUtils.polynomial(x[i], coefficients));
        residuals = ArrayUtils.createArray(x.length, i -> actualY[i] - laboratoryValues[i]);
    }

    public double meanRms() {
        // TODO: This is mean absolute error, not root mean square
        return IntStream.range(0, residuals.length)
                .filter(i -> used[i])
                .mapToDouble(i -> residuals[i])
                .map(Math::abs)
                .average()
                .orElseThrow(IllegalStateException::new);
    }

    public double[] getX() {
        return x;
    }

    public double[] getLaboratoryValues() {
        return laboratoryValues;
    }

    public double[] getCoefficients() {
        return coefficients;
    }

    public double[] getActualY() {
        return actualY;
    }

    public double[] getResiduals() {
        return residuals;
    }

    public void inverseUsed(int index) {
        used[index] = !used[index];
    }

    public XYSeries getUnusedResidualSeries() {
        DoubleArrayList xList = new DoubleArrayList();
        DoubleArrayList residualsList = new DoubleArrayList();

        for (int i = 0; i < n; i++) {
            if (!used[i]) {
                xList.add(x[i]);
                residualsList.add(residuals[i]);
            }
        }

        return new XYSeries(xList.toArray(), residualsList.toArray());
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
