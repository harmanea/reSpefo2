package cz.cuni.mff.respefo.util.utils;

import org.junit.Test;

import static cz.cuni.mff.respefo.util.utils.MathUtils.rmse;
import static org.junit.Assert.assertTrue;

public class MathUtilsTest {
    @Test
    public void testFitPolynomial() {
        double[] x = new double[]{
                194.950,
                231.780,
                335.000,
                476.490,
                546.000,
                697.980,
                844.370,
                1036.490,
                1168.610,
                1273.780,
                1313.550,
                1470.410,
                1574.970,
                1740.480,
                2047.570,
                2274.480,
                2421.050,
                2549.800,
                2704.960,
                2861.600,
                2938.400,
                3440.680,
                3543.080,
                3591.450,
                4060.010
        };

        double[] y = new double[]{
                6182.622,
                6188.125,
                6203.493,
                6224.527,
                6234.856,
                6257.424,
                6279.167,
                6307.657,
                6327.278,
                6342.859,
                6348.737,
                6371.943,
                6387.396,
                6411.899,
                6457.282,
                6490.737,
                6512.364,
                6531.342,
                6554.160,
                6577.215,
                6588.540,
                6662.269,
                6677.282,
                6684.293,
                6752.834
        };

        double[] a = MathUtils.fitPolynomial(x, y, 5);

        double[] predicted = new double[x.length];

        for (int i = 0; i < x.length; i++) {
            predicted[i] = MathUtils.polynomial(x[i], a);
        }

        assertTrue(rmse(predicted, y) < 0.02);
    }
}