package cz.cuni.mff.respefo.util;

import cz.cuni.mff.respefo.util.collections.DoubleArrayList;
import org.junit.Test;

import java.util.ArrayList;
import java.util.stream.IntStream;

import static cz.cuni.mff.respefo.util.TestUtils.arrayOf;
import static cz.cuni.mff.respefo.util.TestUtils.emptyArray;
import static cz.cuni.mff.respefo.util.utils.MathUtils.DOUBLE_PRECISION;
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

        assertArrayEquals(arrayOf(1), list.toArray(), DOUBLE_PRECISION);

        list.add(2);
        assertArrayEquals(arrayOf(1, 2), list.toArray(), DOUBLE_PRECISION);

        for (int i = 3; i <= 15; i++) {
            list.add(i);
        }

        assertArrayEquals(arrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15), list.toArray(), DOUBLE_PRECISION);
    }

    @Test
    public void testAddIndex() {
        DoubleArrayList list = new DoubleArrayList();

        list.add(1, 0);
        assertArrayEquals(arrayOf(1), list.toArray(), DOUBLE_PRECISION);

        list.add(0, 0);
        assertArrayEquals(arrayOf(0, 1), list.toArray(), DOUBLE_PRECISION);

        list.add(2, 2);
        assertArrayEquals(arrayOf(0, 1, 2), list.toArray(), DOUBLE_PRECISION);
    }

    @Test
    public void testAddAll() {
        DoubleArrayList list = new DoubleArrayList();

        list.addAll(new DoubleArrayList(arrayOf(1, 2, 3)));
        assertArrayEquals(arrayOf(1, 2, 3), list.toArray(), DOUBLE_PRECISION);

        list.addAll(new DoubleArrayList(arrayOf(-123, 42, 69)));
        assertArrayEquals(arrayOf(1, 2, 3, -123, 42, 69), list.toArray(), DOUBLE_PRECISION);

        list.addAll(new DoubleArrayList(arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)));
        assertArrayEquals(arrayOf(1, 2, 3, -123, 42, 69, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0), list.toArray(), DOUBLE_PRECISION);
    }

    @Test
    public void testClear() {
        DoubleArrayList list = new DoubleArrayList(arrayOf(1, 2, 3, 4, 5));
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
        DoubleArrayList list = new DoubleArrayList(arrayOf(1, 2, 3, 4, 5));

        for (int i = 0; i < 5; i++) {
            assertEquals(i + 1, list.get(i), DOUBLE_PRECISION);
        }
    }

    @Test
    public void testGetLast() {
        DoubleArrayList list = new DoubleArrayList(arrayOf(1, 2, 3, 4, 5));

        for (int i = 5; i > 0; i--) {
            assertEquals(i, list.getLast(), DOUBLE_PRECISION);
            list.remove(list.size() - 1);
        }
    }

    @Test
    public void testSet() {
        DoubleArrayList list = new DoubleArrayList(arrayOf(1, 2, 3, 4, 5));

        for (int i = 0; i < 5; i++) {
            list.set(i, (i + 1) * 2);
            assertEquals((i + 1) * 2, list.get(i), DOUBLE_PRECISION);
        }
    }

    @Test
    public void testRemove() {
        DoubleArrayList list = new DoubleArrayList(arrayOf(1, 2, 8, 3, 4, 5));

        list.remove(2);
        assertArrayEquals(arrayOf(1, 2, 3, 4, 5), list.toArray(), DOUBLE_PRECISION);

        list.remove(4);
        assertArrayEquals(arrayOf(1, 2, 3, 4), list.toArray(), DOUBLE_PRECISION);

        list.remove(0);
        assertArrayEquals(arrayOf(2, 3, 4), list.toArray(), DOUBLE_PRECISION);

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
    public void testToArray() {
        DoubleArrayList list = new DoubleArrayList(10);
        for (int i = 0; i < 5; i++) {
            list.add(i + 1);
        }

        assertArrayEquals(arrayOf(1, 2, 3, 4, 5), list.toArray(), DOUBLE_PRECISION);

        list.clear();
        assertArrayEquals(emptyArray(), list.toArray(), DOUBLE_PRECISION);
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
        assertEquals(list, new DoubleArrayList(arrayOf(42)));
        assertNotEquals(list, new DoubleArrayList(arrayOf(0)));

        list.add(69);
        assertEquals(list, new DoubleArrayList(arrayOf(42, 69)));
        assertNotEquals(list, new DoubleArrayList(arrayOf(42, 0)));

        list.trimToSize();
        assertEquals(list, new DoubleArrayList(arrayOf(42, 69)));
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
        DoubleArrayList list = new DoubleArrayList(arrayOf(1, 2, 3, 4, 5));
        list.get(5);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetLastEmptyList() {
        DoubleArrayList list = new DoubleArrayList();
        list.getLast();
    }

    @Test
    public void testStreamAndCollect() {
        DoubleArrayList list = new DoubleArrayList(arrayOf(42, 0.0001, 5e9, -1, 555_555_555));
        assertEquals(list, list.stream().boxed().collect(DoubleArrayList.toDoubleArrayList()));

        assertTrue(new DoubleArrayList(arrayOf(1, 2, 3)).stream().allMatch(x -> x > 0));

        assertArrayEquals(arrayOf(1, 4, 9, 16, 25),
                IntStream.rangeClosed(1, 5).mapToDouble(x -> x * x).boxed().collect(DoubleArrayList.toDoubleArrayList()).toArray(),
                DOUBLE_PRECISION);
    }

    @Test
    public void testSort() {
        DoubleArrayList list = new DoubleArrayList(arrayOf(42, 0.0001, 5e9, -1, 555_555_555));
        list.sort();

        assertArrayEquals(arrayOf(-1, 0.0001, 42, 555_555_555, 5e9), list.toArray(), DOUBLE_PRECISION);
    }
}