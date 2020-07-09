package cz.cuni.mff.respefo.util;

import cz.cuni.mff.respefo.util.utils.MathUtils;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class DoubleArrayListTest {

    @Test
    public void testIsEmpty() {
        DoubleArrayList list = new DoubleArrayList();
        assertTrue(list.isEmpty());

        list.add(0);
        assertFalse(list.isEmpty());

        list.clear();
        assertTrue(list.isEmpty());
    }

    @Test
    public void testAdd() {
        DoubleArrayList list = new DoubleArrayList();
        list.add(1);

        assertArrayEquals(new double[]{1}, list.toArray(), MathUtils.DOUBLE_PRECISION);

        list.add(2);
        assertArrayEquals(new double[]{1, 2}, list.toArray(), MathUtils.DOUBLE_PRECISION);

        for (int i = 3; i <= 15; i++) {
            list.add(i);
        }

        assertArrayEquals(new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15}, list.toArray(), MathUtils.DOUBLE_PRECISION);
    }

    @Test
    public void testAddIndex() {
        DoubleArrayList list = new DoubleArrayList();

        list.add(1, 0);
        assertArrayEquals(new double[]{1}, list.toArray(), MathUtils.DOUBLE_PRECISION);

        list.add(0, 0);
        assertArrayEquals(new double[]{0, 1}, list.toArray(), MathUtils.DOUBLE_PRECISION);

        list.add(2, 2);
        assertArrayEquals(new double[]{0, 1, 2}, list.toArray(), MathUtils.DOUBLE_PRECISION);
    }

    @Test
    public void testClear() {
        DoubleArrayList list = new DoubleArrayList(new double[]{1, 2, 3, 4, 5});
        assertEquals(5, list.size());

        list.clear();
        assertEquals(0, list.size());

        list.add(0);
        assertEquals(1, list.size());

        list.clear();
        assertEquals(0, list.size());
    }

    @Test
    public void testGet() {
        DoubleArrayList list = new DoubleArrayList(new double[]{1, 2, 3, 4, 5});

        for (int i = 0; i < 5; i++) {
            assertEquals(i + 1, list.get(i), MathUtils.DOUBLE_PRECISION);
        }
    }

    @Test
    public void testSet() {
        DoubleArrayList list = new DoubleArrayList(new double[]{1, 2, 3, 4, 5});

        for (int i = 0; i < 5; i++) {
            list.set(i, (i + 1) * 2);
            assertEquals((i + 1) * 2, list.get(i), MathUtils.DOUBLE_PRECISION);
        }
    }

    @Test
    public void testRemove() {
        DoubleArrayList list = new DoubleArrayList(new double[]{1, 2, 8, 3, 4, 5});

        list.remove(2);
        assertArrayEquals(new double[]{1, 2, 3, 4, 5}, list.toArray(), MathUtils.DOUBLE_PRECISION);

        list.remove(4);
        assertArrayEquals(new double[]{1, 2, 3, 4}, list.toArray(), MathUtils.DOUBLE_PRECISION);

        list.remove(0);
        assertArrayEquals(new double[]{2, 3, 4}, list.toArray(), MathUtils.DOUBLE_PRECISION);

        for (int i = 0; i < 3; i++) {
            list.remove(0);
        }
        assertEquals(0, list.size());
    }

    @Test
    public void testTrimToSize() {
        DoubleArrayList list = new DoubleArrayList();
        for (int i = 0; i < 50; i++) {
            list.add(i);
        }

        list.trimToSize();
        assertEquals(50, list.elements().length);

        for (int i = 0; i < 20; i++) {
            list.remove(0);
        }

        list.trimToSize();
        assertEquals(30, list.elements().length);

        list.clear();
        list.trimToSize();
        assertEquals(0, list.elements().length);
    }

    @Test
    public void testValidElements() {
        DoubleArrayList list = new DoubleArrayList(10);
        for (int i = 0; i < 5; i++) {
            list.add(i + 1);
        }

        assertArrayEquals(new double[]{1, 2, 3, 4, 5}, list.toArray(), MathUtils.DOUBLE_PRECISION);

        list.clear();
        assertArrayEquals(new double[0], list.toArray(), MathUtils.DOUBLE_PRECISION);
    }

    @Test
    public void testEquals() {
        DoubleArrayList list = new DoubleArrayList();

        assertEquals(list, new DoubleArrayList());
        assertEquals(list, new DoubleArrayList(5));
        assertEquals(list, new DoubleArrayList(10));
        assertNotEquals(list, new ArrayList<>());
        assertNotEquals(list, null);

        list.add(42);
        assertNotEquals(list, new DoubleArrayList());
        assertEquals(list, new DoubleArrayList(new double[] {42}));
        assertNotEquals(list, new DoubleArrayList(new double[] {0}));

        list.add(69);
        assertEquals(list, new DoubleArrayList(new double[] {42, 69}));
        assertNotEquals(list, new DoubleArrayList(new double[] {42, 0}));

        list.trimToSize();
        assertEquals(list, new DoubleArrayList(new double[] {42, 69}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalConstructorArgument() {
        new DoubleArrayList(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testBoundsCheckLargeValue() {
        DoubleArrayList list = new DoubleArrayList();
        list.get(100);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testBoundsCheckNegativeValue() {
        DoubleArrayList list = new DoubleArrayList();
        list.get(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testBoundsCheckSizeValue() {
        DoubleArrayList list = new DoubleArrayList(new double[]{1, 2, 3, 4, 5});
        list.get(5);
    }
}