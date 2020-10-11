package cz.cuni.mff.respefo.util.utils;

import org.junit.Test;

import java.util.Arrays;

import static cz.cuni.mff.respefo.util.utils.MathUtils.rmse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MathUtilsTest {

    @Test
    public void testRobustMean() {
        double[] values = {29.3936, 27.9973, 25.5745, 23.6468, 29.7703, 29.4919, 28.4588, 27.6771, 24.1238, 28.1947,
                30.3977, 26.3970, 27.2327, 23.0827, 33.0418, 31.5371, 34.0490};
        Arrays.sort(values);
        double expected = 28.2193;

        assertEquals(expected, MathUtils.robustMean(values), 0.0001);


        values = new double[]{28.392, 23.949, 28.848, 22.65, 27.622, 27.937, 27.5, 29.081, 26.116, 25.472, 32.387,
                27.795, 24.517, 25.654, 30.337, 29.419, 30.763, 23.517};
        Arrays.sort(values);
        expected = 27.345;

        assertEquals(expected, MathUtils.robustMean(values), 0.001);


        values = new double[]{-23.233, -0.378};
        expected = -11.805;

        assertEquals(expected, MathUtils.robustMean(values), 0.001);
    }

    @Test
    public void testRmse() {
        double[] values = { 29.3936, 27.9973, 25.5745, 23.6468, 29.7703, 29.4919, 28.4588, 27.6771, 24.1238, 28.1947,
                30.3977, 26.3970, 27.2327, 23.0827, 33.0418, 31.5371, 34.0490 };
        double predicted = 28.2193;
        double mean = 0.7528;

        assertEquals(mean, MathUtils.rmse(values, predicted), 0.0001);


        values = new double[] { 28.392, 23.949, 28.848, 22.65, 27.622, 27.937, 27.5, 29.081, 26.116, 25.472, 32.387,
                27.795, 24.517, 25.654, 30.337, 29.419, 30.763, 23.517 };
        predicted = 27.345;
        mean = 0.63;

        assertEquals(mean, MathUtils.rmse(values, predicted), 0.001);


        values = new double[] { -23.233, -0.378 };
        predicted = -11.805;
        mean = 11.428;

        assertEquals(mean, MathUtils.rmse(values, predicted), 0.001);
    }

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