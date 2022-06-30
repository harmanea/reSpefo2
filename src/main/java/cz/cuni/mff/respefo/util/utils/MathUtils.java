package cz.cuni.mff.respefo.util.utils;

import cz.cuni.mff.respefo.util.UtilityClass;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

// TODO: add tests for intep
public class MathUtils extends UtilityClass {
    public static final double DOUBLE_PRECISION = 1E-8;

    /**
     * The INTEP interpolation algorithm is described by Hill 1982, PDAO 16, 67 ("Intep - an Effective Interpolation Subroutine").
     * This implementation is based on the FORTRAN code stated therein.
     *
     * @param x      Independent values sorted in ascending order
     * @param y      Dependent values
     * @param xinter Values at which to interpolate the tabulated data given by 'x' and 'y'
     * @return Interpolated values at the locations specified by 'xinter',
     *  values outside of given bounds are replaced with the last value within the bounds
     */
    public static double[] intep(double[] x, double[] y, double[] xinter) {
        Objects.requireNonNull(x);
        Objects.requireNonNull(y);
        Objects.requireNonNull(xinter);

        double[] result = new double[xinter.length];

        // Treat points outside of given bounds
        int iLow = xinter.length; // lowest index of xinter value in the given bounds
        for (int i = 0; i < xinter.length; i++) {
            if (xinter[i] <= x[0]) {
                result[i] = y[0];
            } else {
                iLow = i;
                break;
            }
        }
        int iHigh = -1; // highest index of xinter value in the given bounds
        for (int i = xinter.length - 1; i >= 0; i--) {
            if (xinter[i] >= x[x.length - 1]) {
                result[i] = y[y.length - 1];
            } else {
                iHigh = i;
                break;
            }
        }

        // Treat points inside bounds
        for (int i = iLow; i <= iHigh; i++) {
            result[i] = intepInsideBounds(x, y, xinter[i]);
        }

        return result;
    }

    /**
     * The INTEP interpolation algorithm is described by Hill 1982, PDAO 16, 67 ("Intep - an Effective Interpolation Subroutine").
     * This implementation is based on the FORTRAN code stated therein.
     *
     * @param x      Independent values sorted in ascending order
     * @param y      Dependent values
     * @param xp     Value at which to interpolate the tabulated data given by 'x' and 'y'
     * @return Interpolated value at the location specified by 'xp',
     *  values outside of given bounds are replaced with the last value within the bounds
     */
    public static double intep(double[] x, double[] y, double xp) {
        Objects.requireNonNull(x);
        Objects.requireNonNull(y);

        if (xp <= x[0]) {
            return y[0];
        } else if (xp >= x[x.length - 1]) {
            return y[y.length - 1];
        } else {
            return intepInsideBounds(x, y, xp);
        }
    }

    private static double intepInsideBounds(double[] x, double[] y, double xp) {
        int j = ArrayUtils.indexOfFirstGreaterThan(x, xp) - 1;
        if (x[j] == xp) {
            return y[j];  // short-circuit speedup
        }

        double lp1 = 1 / (x[j] - x[j + 1]);
        double lp2 = -lp1;

        double fp1 = j <= 0
                ? (y[1] - y[0]) / (x[1] - x[0]) // first point
                : (y[j + 1] - y[j - 1]) / (x[j + 1] - x[j - 1]);

        double fp2 = j >= x.length - 2
                ? (y[y.length - 1] - y[y.length - 2]) / (x[x.length - 1] - x[x.length - 2]) // last point
                : (y[j + 2] - y[j]) / (x[j + 2] - x[j]);

        double xpi1 = xp - x[j + 1];
        double xpi = xp - x[j];
        double l1 = xpi1 * lp1;
        double l2 = xpi * lp2;

        return y[j] * (1 - 2 * lp1 * xpi) * l1 * l1
                + y[j + 1] * (1 - 2 * lp2 * xpi1) * l2 * l2
                + fp2 * xpi1 * l2 * l2
                + fp1 * xpi * l1 * l1;
    }

    /**
     * Linearly interpolate or extrapolate a value for a point given two other points.
     *
     * @param x0 x coordinate of the first point
     * @param y0 y coordinate of the first point
     * @param x1 x coordinate of the second point
     * @param y1 y coordinate of the second point
     * @param x x coordinate of the point for which the y coordinate is calculated
     * @return y coordinate
     */
    public static double linearInterpolation(double x0, double y0, double x1, double y1, double x) {
        if (doublesEqual(x0, x1)) {
            throw new IllegalArgumentException("The given points must be different");
        }

        return y0 + (x - x0) * (y1 - y0) / (x1 - x0);
    }

    /**
     * Returns false if the specified number is a Not-a-Number (NaN) value, true otherwise.
     * @param value to be tested
     * @return false if the value of the argument is NaN; true otherwise.
     */
    public static boolean isNotNaN(double value) {
        return !Double.isNaN(value);
    }

    /**
     * Checks whether the difference between two numbers is smaller than the required precision.
     *
     * @param first number to be compared
     * @param second number to be compared
     * @return true if their difference is sufficiently small, false otherwise
     * @see MathUtils#DOUBLE_PRECISION
     */
    public static boolean doublesEqual(double first, double second) {
        return Math.abs(first - second) < DOUBLE_PRECISION;
    }

    /**
     * Transform an index to wavelength using Taylor polynomials.
     *
     * @param x            input value
     * @param coefficients array of polynomial coefficients
     * @return y output value
     */
    public static double polynomial(double x, double[] coefficients) {
        Objects.requireNonNull(coefficients);

        // Horner's schema
        int degree = coefficients.length;
        double result = coefficients[degree - 1];
        for (int i = degree - 2; i >= 0; --i) {
            result *= x;
            result += coefficients[i];
        }
        return result;
    }

    /**
     * Fit a polynomial of a given degree to the provided data.
     * <p>
     * Based on the implementation from <a href="https://www.bragitoff.com/2017/04/polynomial-fitting-java-codeprogram-works-android-well/">Bragitoff.com</a>
     *
     * @param x independent variables
     * @param y dependent variables
     * @param degree of the polynomial
     * @return coefficients of the polynomial
     */
    public static double[] fitPolynomial(double[] x, double[] y, int degree) {
        Objects.requireNonNull(x);
        Objects.requireNonNull(y);

        int n = x.length;

        double[] sigmasX = new double[2 * degree + 1]; // store the values of N, sigma(xi), sigma(xi^2), sigma(xi^3), ..., sigma(xi^2n)
        for (int i = 0; i < 2 * degree + 1; i++) {
            sigmasX[i] = 0;
            for (double v : x) {
                sigmasX[i] = sigmasX[i] + Math.pow(v, i);
            }
        }

        double[][] normal = new double[degree + 1][degree + 2]; // the normal matrix(augmented) that will store the equations
        double[] coefs = new double[degree + 1]; // final polynomial coefficients

        // build the Normal matrix by storing the corresponding coefficients at the right positions except the last column of the matrix
        for (int i = 0; i <= degree; i++) {
            if (degree + 1 >= 0) {
                System.arraycopy(sigmasX, i, normal[i], 0, degree + 1);
            }
        }

        double[] sigmasY = new double[degree + 1]; // store the values of sigma(yi), sigma(xi*yi), sigma(xi^2*yi), ..., sigma(xi^n*yi)
        for (int i = 0; i < degree + 1; i++) {
            sigmasY[i] = 0;
            for (int j = 0; j < n; j++) {
                sigmasY[i] = sigmasY[i] + Math.pow(x[j], i) * y[j];
            }
        }
        for (int i = 0; i <= degree; i++) {
            normal[i][degree + 1] = sigmasY[i]; // load the values of sigmasY as the last column of the normal matrix
        }
        degree = degree + 1;
        for (int i = 0; i < degree; i++) { // from now Gaussian Elimination starts (can be ignored) to solve the set of linear equations (Pivotisation)
            for (int k = i + 1; k < degree; k++) {
                if (normal[i][i] < normal[k][i]) {
                    for (int j = 0; j <= degree; j++) {
                        double temp = normal[i][j];
                        normal[i][j] = normal[k][j];
                        normal[k][j] = temp;
                    }
                }
            }
        }

        for (int i = 0; i < degree - 1; i++) { // loop to perform the gaussian elimination
            for (int k = i + 1; k < degree; k++) {
                double t = normal[k][i] / normal[i][i];
                for (int j = 0; j <= degree; j++) {
                    normal[k][j] = normal[k][j] - t * normal[i][j]; // make the elements below the pivot elements equal to zero or eliminate the variables
                }
            }
        }
        for (int i = degree - 1; i >= 0; i--) { // back-substitution
            coefs[i] = normal[i][degree]; // make the variable to be calculated equal to the rhs of the last equation
            for (int j = 0; j < degree; j++) {
                if (j != i) { // then subtract all the lhs values except the coefficient of the variable whose value s being calculated
                    coefs[i] = coefs[i] - normal[i][j] * coefs[j];
                }
            }
            coefs[i] = coefs[i] / normal[i][i]; // now finally divide the rhs by the coefficient of the variable to be calculated
        }
        return coefs;
    }

    /**
     * Calculate root mean square error.
     *
     * @param predicted values
     * @param actual    values
     * @return root mean square error
     */
    public static double rmse(double[] predicted, double[] actual) {
        Objects.requireNonNull(predicted);
        Objects.requireNonNull(actual);

        if (predicted.length != actual.length) {
            throw new IllegalArgumentException("Arrays must have the same length.");
        }

        double mse = IntStream.range(0, predicted.length)
                .mapToDouble(i -> Math.pow(predicted[i] - actual[i], 2))
                .average()
                .orElseThrow(() -> new IllegalArgumentException("Arrays must not be empty."));

        return Math.sqrt(mse);
    }

    /**
     * Calculate standard error of the mean.
     *
     * @param observations values
     * @param mean    value
     * @return root mean square error
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")  // We check for array size before the statement
    public static double sem(double[] observations, double mean) {
        Objects.requireNonNull(observations);

        if (observations.length <= 1) {
            throw new IllegalArgumentException("Array must contain at least two values.");
        }

        double mse = Arrays.stream(observations).map(value -> Math.pow(value - mean, 2)).average().getAsDouble();

        return Math.sqrt(mse / (observations.length - 1));
    }

    /**
     * Compute the robust mean of the given sorted values.
     * <p>
     * Andrews, D. F. (1972): Robust Estimates of Location, Princeton Univ. Press, Princeton
     * <p>
     * This implementation is based on the FORTRAN code stated therein.
     *
     * @param values sorted ascending numbers
     * @return robust mean
     */
    public static double robustMean(double[] values) {
        Objects.requireNonNull(values);

        int n = values.length;
        double[] dd = new double[n];
        double t = median(values);

        for (int i = 0; i < 5; i++) {
            for (int k = 0; k < n; k++) {
                dd[k] = Math.abs(values[k] - t);
            }

            Arrays.sort(dd);
            double s = 2.1 * median(dd);

            int m1 = 0;
            int m3 = 0;

            int m1old = -1;
            int m3old = -1;

            while (m1 != m1old && m3 != m3old) {

                // Compute Winsorizing counts and decide on stopping
                double xLower = t - Math.PI * s;
                double xUpper = t + Math.PI * s;

                m1 = 0;
                while (m1 < n && values[m1] <= xLower) {
                    m1++;
                }
                m1--;

                m3 = n - 1;
                while (m3 >= 0 && values[m3] >= xUpper) {
                    m3--;
                }
                m3++;

                if (m1 != m1old || m3 != m3old) {

                    m1old = m1;
                    m3old = m3;

                    // Update estimate set
                    double sSin = 0;
                    double sCos = 0;
                    for (int j = m1 + 1; j <= m3 - 1; j++) {
                        double z = (values[j] - t) / s;
                        sSin += Math.sin(z);
                        sCos += Math.cos(z);
                    }

                    t += s * Math.atan(sSin / sCos);
                }
            }

        }

        return t;
    }

    /**
     * Calculate the median of the given sorted values.
     * @param values whose median should be computed
     * @return the median
     */
    public static double median(double[] values) {
        Objects.requireNonNull(values);

        if (values.length == 0) {
            throw new IllegalArgumentException("Array must not be empty.");
        }

        if (values.length % 2 == 0)
            return (values[values.length / 2] + values[values.length / 2 - 1]) / 2;
        else
            return values[values.length / 2];
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    protected MathUtils() throws IllegalAccessException {
        super();
    }
}
