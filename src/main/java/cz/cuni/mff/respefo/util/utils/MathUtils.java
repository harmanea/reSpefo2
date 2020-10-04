package cz.cuni.mff.respefo.util.utils;

import cz.cuni.mff.respefo.util.UtilityClass;

import java.util.Arrays;

public class MathUtils extends UtilityClass {
    public static final double DOUBLE_PRECISION = 0.0000001;

    /**
     * The INTEP interpolation algorithm
     * <p>
     * The INTEP interpolation algorithm is described by Hill 1982, PDAO 16, 67 ("Intep - an Effective Interpolation Subroutine"). This implementation is based on the FORTRAN code stated therein.
     *
     * @param x      Independent values sorted in strictly ascending order
     * @param y      Dependent values
     * @param xinter Values at which to interpolate the tabulated data given by 'x' and 'y'
     * @return Interpolated values at the locations specified by 'xinter'
     */
    public static double[] intep(double[] x, double[] y, double[] xinter) {
        return intep(x, y, xinter, null);
    }

    /**
     * The INTEP interpolation algorithm
     * <p>
     * The INTEP interpolation algorithm is described by Hill 1982, PDAO 16, 67 ("Intep - an Effective Interpolation Subroutine"). This implementation is based on the FORTRAN code stated therein.
     *
     * @param x         Independent values sorted in strictly ascending order
     * @param y         Dependent values
     * @param xinter    Values at which to interpolate the tabulated data given by 'x' and 'y'
     * @param fillValue This value will be used to represent values outside of the given bounds (null implies the last value in the bounds)
     * @return Interpolated values at the locations specified by 'xinter'
     */
    public static double[] intep(double[] x, double[] y, double[] xinter, Double fillValue) {
        // create result array
        double[] result = new double[xinter.length];

        // treat points outside of given bounds
        int ilow = 0; // lowest index of xinter value in the given bounds
        for (int i = 0; i < xinter.length; i++) {
            if (xinter[i] < x[0]) {
                ilow = i + 1;
                if (fillValue == null) {
                    result[i] = y[0];
                } else {
                    result[i] = fillValue;
                }
            } else {
                break;
            }
        }
        int iup = xinter.length - 1; // highest index of xinter value in the given bounds
        for (int i = xinter.length - 1; i >= 0; i--) {
            if (xinter[i] > x[x.length - 1]) {
                iup = i - 1;
                if (fillValue == null) {
                    result[i] = y[y.length - 1];
                } else {
                    result[i] = fillValue;
                }
            } else {
                break;
            }
        }

        // treat points inside bounds
        double xp, xpi, xpi1, l1, l2, lp1, lp2, fp1, fp2;

        for (int i = ilow; i <= iup; i++) {
            xp = xinter[i];
            int infl = ArrayUtils.findFirstGreaterThen(x, xp);
            if (infl == x.length) {
                result[i] = y[y.length - 1];
                continue;
            }
            infl--;
            lp1 = 1 / (x[infl] - x[infl + 1]);
            lp2 = -lp1;

            if (infl <= 0) {
                // first point
                fp1 = (y[1] - y[0]) / (x[1] - x[0]);
            } else {
                fp1 = (y[infl + 1] - y[infl - 1]) / (x[infl + 1] - x[infl - 1]);
            }

            if (infl >= x.length - 2) {
                // last point
                fp2 = (y[y.length - 1] - y[y.length - 2]) / (x[x.length - 1] - x[x.length - 2]);
            } else {
                fp2 = (y[infl + 2] - y[infl]) / (x[infl + 2] - x[infl]);
            }

            xpi1 = xp - x[infl + 1];
            xpi = xp - x[infl];
            l1 = xpi1 * lp1;
            l2 = xpi * lp2;

            result[i] = y[infl] * (1 - 2 * lp1 * xpi) * l1 * l1 + y[infl + 1] * (1 - 2 * lp2 * xpi1) * l2 * l2
                    + fp2 * xpi1 * l2 * l2 + fp1 * xpi * l1 * l1;
        }


        return result;
    }

    public static double linearInterpolation(double x0, double y0, double x1, double y1, double x) {
        return y0 + (x - x0)*(y1 - y0)/(x1 - x0);
    }

    public static boolean isNotNaN(double value) {
        return !Double.isNaN(value);
    }

    public static boolean doublesEqual(double first, double second) {
        return Math.abs(first - second) < DOUBLE_PRECISION;
    }

    /**
     * Transform an index to wavelength using Taylor polynomials
     *
     * @param x input value
     * @param coefficients array of up to 5 polynomial coefficients
     * @return y output value
     */
    public static double polynomial(double x, double[] coefficients) {
        int degree = coefficients.length;
        if (degree > 6) {
            degree = 6;
        }

        // Horner's schema
        double result = coefficients[degree - 1];
        for (int i = degree - 2; i >= 0; --i) {
            result *= x;
            result += coefficients[i];
        }
        return result;
    }

    public static double[] fitPolynomial(double[] x, double[] y, int degree) {
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

        //Build the Normal matrix by storing the corresponding coefficients at the right positions except the last column of the matrix
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
        for (int i = 0; i < degree; i++) { // From now Gaussian Elimination starts(can be ignored) to solve the set of linear equations (Pivotisation)
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
                    normal[k][j] = normal[k][j] - t * normal[i][j]; //make the elements below the pivot elements equal to zero or eliminate the variables
                }
            }
        }
        for (int i = degree - 1; i >= 0; i--) { // back-substitution
            coefs[i] = normal[i][degree]; //make the variable to be calculated equal to the rhs of the last equation
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
     * Calculate root mean square error
     * @param predicted values
     * @param actual values
     * @return root mean square error
     */
    public static double rmse(double[] predicted, double[] actual) {
        if (predicted.length == 0 || actual.length == 0) {
            throw new IllegalArgumentException("Arrays must contain some values.");
        }

        double rss = 0;
        int n = 0;

        for (int i = 0; i < predicted.length && i < actual.length; i++) {
            rss += Math.pow(predicted[i] - actual[i], 2);
            n++;
        }

        rss /= n;

        return Math.sqrt(rss);
    }

    /**
     * Calculate root mean square error
     * @param values
     * @param actual predicted value
     * @return root mean square error
     */
    public static double rmse(double[] values, double actual) {
        if (values.length <= 1) {
            throw new IllegalArgumentException("Array must contain at least two values.");
        }

        double sum = Arrays.stream(values).map(value -> Math.pow(value - actual, 2)).sum();
        sum /= values.length;
        sum /= values.length - 1;

        return Math.sqrt(sum);
    }

    public static double clamp(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }

    public static int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

    public static double robustMean(double[] values) {
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

    public static double median(double[] values) {
        if (values.length % 2 == 0)
            return (values[values.length / 2] + values[values.length / 2 - 1]) / 2;
        else
            return values[values.length / 2];
    }

    protected MathUtils() throws IllegalAccessException {
        super();
    }
}
