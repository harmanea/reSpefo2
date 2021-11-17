package cz.cuni.mff.respefo.util.utils;

import org.junit.Test;

import static cz.cuni.mff.respefo.util.TestUtils.arrayOf;
import static cz.cuni.mff.respefo.util.TestUtils.emptyArray;
import static cz.cuni.mff.respefo.util.utils.ArrayUtils.*;
import static cz.cuni.mff.respefo.util.utils.MathUtils.DOUBLE_PRECISION;
import static org.junit.Assert.*;

public class ArrayUtilsTest {

    @Test
    public void testFindFirstGreaterThan() {
        assertEquals(1, findFirstGreaterThan(arrayOf(1, 2, 3), 1));
        assertEquals(0, findFirstGreaterThan(arrayOf(0.1, 0.5, 1), 0));
        assertEquals(3, findFirstGreaterThan(arrayOf(1000.1111, 2000.2222, 3000.3333), 5000.5555));
        assertEquals(2, findFirstGreaterThan(arrayOf(-100, -50, -10, -5, -1), -25));
        assertEquals(0, findFirstGreaterThan(arrayOf(1), 0));
        assertEquals(1, findFirstGreaterThan(arrayOf(1), 1));
        assertEquals(0, findFirstGreaterThan(emptyArray(), 1));
        assertEquals(8, findFirstGreaterThan(arrayOf(1, 1, 1, 1, 1, 1, 1, 1), 1));
        assertEquals(0, findFirstGreaterThan(arrayOf(1, 1, 1, 1, 1, 1, 1, 1), 0));
        assertEquals(6, findFirstGreaterThan(arrayOf(1, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 6), 3));
        assertEquals(6, findFirstGreaterThan(arrayOf(1, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 6), 3.5));
        assertEquals(8, findFirstGreaterThan(arrayOf(1, 2, 3, 3, 4, 4, 4, 4, 5, 5, 6), 4));
        assertEquals(4, findFirstGreaterThan(arrayOf(1, 1, 1, 1, 2, 2, 2, 2), 1));
    }

    @Test
    public void testFindClosest() {
        assertEquals(1, findClosest(arrayOf(0, 1, 2, 3, 4, 5), 1));
        assertEquals(0, findClosest(arrayOf(100, 200, 300), 0));
        assertEquals(0, findClosest(arrayOf(100, 200, 300), 0));
        assertEquals(0, findClosest(arrayOf(1), 1));
        assertEquals(0, findClosest(emptyArray(), 1));
    }

    @Test
    public void testDivideArrayValues() {
        assertArrayEquals(arrayOf(1, 1, 1, 1, 1), divideArrayValues(arrayOf(5, 5, 5, 5, 5), arrayOf(5, 5, 5, 5, 5)), DOUBLE_PRECISION);
        assertArrayEquals(arrayOf(10, 20, 30), divideArrayValues(arrayOf(100, 400, 900), arrayOf(10, 20, 30)), DOUBLE_PRECISION);
        assertArrayEquals(arrayOf(-150.250, -123.456, -789456.123), divideArrayValues(arrayOf(-6310.5, -5185.152, -33157157.166), arrayOf(42, 42, 42)), DOUBLE_PRECISION);
        assertArrayEquals(emptyArray(), divideArrayValues(emptyArray(), emptyArray()), DOUBLE_PRECISION);

        assertArrayEquals(arrayOf(1, 1, 1, 1, 1), divideArrayValues(arrayOf(10, 10, 10, 10, 10), 10), DOUBLE_PRECISION);
        assertArrayEquals(arrayOf(1000), divideArrayValues(arrayOf(1000000), 1000), DOUBLE_PRECISION);
        assertArrayEquals(arrayOf(-234.546, -741, -963.258), divideArrayValues(arrayOf(-156207.636, -493506, -641529.828), 666), DOUBLE_PRECISION);
        assertArrayEquals(emptyArray(), divideArrayValues(emptyArray(), 1), DOUBLE_PRECISION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDivideArrayValuesDifferentLengths() {
        divideArrayValues(arrayOf(1, 2, 3), arrayOf(5, 5, 5, 5, 5));
    }

    @Test
    public void testAddValueToArrayElements() {
        assertArrayEquals(arrayOf(2, 3, 4, 5, 6), addValueToArrayElements(arrayOf(1, 2, 3, 4, 5), 1), DOUBLE_PRECISION);
        assertArrayEquals(arrayOf(1000, 20000, 500000), addValueToArrayElements(arrayOf(0, 19000, 499000), 1000), DOUBLE_PRECISION);
        assertArrayEquals(emptyArray(), addValueToArrayElements(emptyArray(), 0), DOUBLE_PRECISION);
    }

    @Test
    public void testFillArray() {
        assertArrayEquals(arrayOf(0, 1, 2, 3), fillArray(4, 0, 1), DOUBLE_PRECISION);
        assertArrayEquals(arrayOf(0, 0.2, 0.4, 0.6, 0.8, 1), fillArray(6, 0, 0.2), DOUBLE_PRECISION);
        assertArrayEquals(arrayOf(10, 7, 4, 1, -2, -5, -8, -11, -14), fillArray(9, 10, -3), DOUBLE_PRECISION);
        assertArrayEquals(arrayOf(-5, -3.5, -2, -0.5, 1, 2.5), fillArray(6, -5, 1.5), DOUBLE_PRECISION);
        assertArrayEquals(arrayOf(42), fillArray(1, 42, -5), DOUBLE_PRECISION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFillArrayNegativeValue() {
        fillArray(-1, 0, 0);
    }

    @Test
    public void testCreateArray() {
        assertArrayEquals(arrayOf(0, 1, 2), createArray(3, i -> i), DOUBLE_PRECISION);
        assertArrayEquals(arrayOf(0, 1, 4, 9, 16, 25), createArray(6, i -> i * i), DOUBLE_PRECISION);
        assertArrayEquals(arrayOf(-5, -6, -7, -8), createArray(4, i -> -i - 5), DOUBLE_PRECISION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArrayNegativeValue() {
        createArray(-1, i -> 0);
    }

    @Test
    public void testReverseArray() {
        assertArrayEquals(arrayOf(0, 1, 2), reverseArray(arrayOf(2, 1, 0)), DOUBLE_PRECISION);
        assertArrayEquals(arrayOf(40, 30, 20, 10), reverseArray(arrayOf(10, 20, 30, 40)), DOUBLE_PRECISION);
        assertArrayEquals(arrayOf(42), reverseArray(arrayOf(42)), DOUBLE_PRECISION);
        assertArrayEquals(emptyArray(), reverseArray(emptyArray()), DOUBLE_PRECISION);
    }

    @Test
    public void testApplyBscale() {
        assertArrayEquals(arrayOf(1, 3, 5), applyBScale(arrayOf(0, 1, 2), 1, 2), DOUBLE_PRECISION);
        assertArrayEquals(arrayOf(0, 5.5, 16.5, 1.1), applyBScale(arrayOf(0, 1, 3, 0.2), 0, 5.5), DOUBLE_PRECISION);
        assertArrayEquals(arrayOf(42, 42, 42, 42, 42), applyBScale(arrayOf(0, 2, 3, 4, 5), 42, 0), DOUBLE_PRECISION);
        assertArrayEquals(arrayOf(-106.725, -95.965, -88.702, -109.5764), applyBScale(arrayOf(-5, 3, 8.4, -7.12), -100, 1.345), DOUBLE_PRECISION);
        assertArrayEquals(arrayOf(78945, 78937.7, 78959.6, 78815.06, 79675), applyBScale(arrayOf(0, 1, -2, 17.8, -100), 78945, -7.3), DOUBLE_PRECISION);
        assertArrayEquals(emptyArray(), applyBScale(emptyArray(), 123, 456), DOUBLE_PRECISION);
    }

    @Test
    public void testNDims() {
        Object data = new double[3][2][3];
        assertEquals(3, nDims(data));

        data = new double[5];
        assertEquals(1, nDims(data));

        data = new double[0];
        assertEquals(1, nDims(data));

        data = "not an array";
        assertEquals(0, nDims(data));
    }

    @Test
    public void testValuesHaveSameDifference() {
        assertTrue(valuesHaveSameDifference(arrayOf(1, 2)));
        assertTrue(valuesHaveSameDifference(arrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
        assertTrue(valuesHaveSameDifference(arrayOf(0, 0.1, 0.2, 0.3, 0.4, 0.5)));
        assertTrue(valuesHaveSameDifference(arrayOf(-100, -110.100, -120.200, -130.300)));
        assertFalse(valuesHaveSameDifference(arrayOf(1, 2, 4)));
        assertFalse(valuesHaveSameDifference(arrayOf(0, 0.1, 0.11)));
        assertFalse(valuesHaveSameDifference(arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.00001)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValuesHaveSameDifferenceSmallArray() {
        valuesHaveSameDifference(arrayOf(1));
    }
}