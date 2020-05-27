package cz.cuni.mff.respefo.util.utils;

import cz.cuni.mff.respefo.util.UtilityClass;

public class MathUtils extends UtilityClass {
    public static final double DOUBLE_PRECISION = 0.0000001;

    /**
     * The INTEP interpolation algorithm
     *
     * The INTEP interpolation algorithm is described by Hill 1982, PDAO 16, 67 ("Intep - an Effective Interpolation Subroutine"). This implementation is based on the FORTRAN code stated therein.
     *
     * @param x Independent values sorted in strictly ascending order
     * @param y Dependent values
     * @param xinter Values at which to interpolate the tabulated data given by 'x' and 'y'
     * @return Interpolated values at the locations specified by 'xinter'
     */
    public static double[] intep(double[] x, double[] y, double[] xinter) {
        return intep(x, y, xinter, null);
    }

    /**
     * The INTEP interpolation algorithm
     *
     * The INTEP interpolation algorithm is described by Hill 1982, PDAO 16, 67 ("Intep - an Effective Interpolation Subroutine"). This implementation is based on the FORTRAN code stated therein.
     *
     * @param x Independent values sorted in strictly ascending order
     * @param y Dependent values
     * @param xinter Values at which to interpolate the tabulated data given by 'x' and 'y'
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

    public static boolean isNotNaN(double value) {
        return !Double.isNaN(value);
    }

    protected MathUtils() throws IllegalAccessException {
        super();
    }
}
