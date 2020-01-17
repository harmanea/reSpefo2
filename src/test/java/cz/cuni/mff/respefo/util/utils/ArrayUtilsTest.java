package cz.cuni.mff.respefo.util.utils;

import org.junit.Test;

import static cz.cuni.mff.respefo.util.utils.MathUtils.DOUBLE_PRECISION;
import static org.junit.Assert.*;

public class ArrayUtilsTest {
    @Test
    public void testFillArray() {
        assertArrayEquals(new double[]{0, 1, 2, 3}, ArrayUtils.fillArray(4, 0, 1), DOUBLE_PRECISION);
        assertArrayEquals(new double[]{0, 0.2, 0.4, 0.6, 0.8, 1}, ArrayUtils.fillArray(6, 0, 0.2), DOUBLE_PRECISION);
        assertArrayEquals(new double[]{10, 7, 4, 1, -2, -5, -8, -11, -14}, ArrayUtils.fillArray(9, 10, -3), DOUBLE_PRECISION);
        assertArrayEquals(new double[]{-5, -3.5, -2, -0.5, 1, 2.5}, ArrayUtils.fillArray(6, -5, 1.5), DOUBLE_PRECISION);
        assertArrayEquals(new double[]{42}, ArrayUtils.fillArray(1, 42, -5), DOUBLE_PRECISION);

        try {
            //noinspection ResultOfMethodCallIgnored
            ArrayUtils.fillArray(-1, 0, 0);
            fail();
        } catch (IllegalArgumentException exception) {
            // test passed
        }
    }

    @Test
    public void testApplyBscale() {
        assertArrayEquals(new double[]{1, 3, 5}, ArrayUtils.applyBScale(new double[]{0, 1, 2}, 1, 2), DOUBLE_PRECISION);
        assertArrayEquals(new double[]{0, 5.5, 16.5, 1.1}, ArrayUtils.applyBScale(new double[]{0, 1, 3, 0.2}, 0, 5.5), DOUBLE_PRECISION);
        assertArrayEquals(new double[]{42, 42, 42, 42, 42}, ArrayUtils.applyBScale(new double[]{0, 2, 3, 4, 5}, 42, 0), DOUBLE_PRECISION);
        assertArrayEquals(new double[0], ArrayUtils.applyBScale(new double[0], 123, 456), DOUBLE_PRECISION);
        assertArrayEquals(new double[]{-106.725, -95.965, -88.702, -109.5764}, ArrayUtils.applyBScale(new double[]{-5, 3, 8.4, -7.12}, -100, 1.345), DOUBLE_PRECISION);
        assertArrayEquals(new double[]{78945, 78937.7, 78959.6, 78815.06, 79675}, ArrayUtils.applyBScale(new double[]{0, 1, -2, 17.8, -100}, 78945, -7.3), DOUBLE_PRECISION);
    }

    @Test
    public void testNDims() {
        Object data = new double[3][2][3];
        assertEquals(3, ArrayUtils.nDims(data));

        data = new double[5];
        assertEquals(1, ArrayUtils.nDims(data));

        data = new double[0];
        assertEquals(1, ArrayUtils.nDims(data));

        data = "not an array";
        assertEquals(0, ArrayUtils.nDims(data));
    }
}