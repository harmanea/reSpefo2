package cz.cuni.mff.respefo.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public class DoubleArrayList implements Iterable<Double> {
    private static final int DEFAULT_INITIAL_CAPACITY = 10;

    private int size;
    private double[] elements;

    public DoubleArrayList() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    public DoubleArrayList(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Initial capacity cannot be negative");
        }

        elements = new double[initialCapacity];
        size = 0;
    }

    public DoubleArrayList(double[] elements) {
        Objects.requireNonNull(elements);

        this.elements = elements;
        this.size = elements.length;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public double[] elements() {
        return elements;
    }

    public void add(double element) {
        if (size == elements.length) {
            ensureCapacity(size + 1);
        }
        elements[size++] = element;
    }

    public void add(double element, int index) {
        if (size == index) {
            add(element);
        } else {
            boundsCheck(index);
            ensureCapacity(size + 1);
            System.arraycopy(elements, index, elements, index + 1, size - index);
            elements[index] = element;
            size++;
        }
    }

    public void clear() {
        size = 0;
    }

    public double get(int index) {
        boundsCheck(index);
        return elements[index];
    }

    public void set(int index, double element) {
        boundsCheck(index);
        elements[index] = element;
    }

    public void remove(int index) {
        boundsCheck(index);
        if (size - 1 > index) {
            System.arraycopy(elements, index + 1, elements, index, size - index - 1);
        }
        size--;
    }

    public void trimToSize() {
        if (elements.length > size) {
            elements = toArray();
        }
    }

    public double[] toArray() {
        double[] newArray = new double[size];
        System.arraycopy(elements, 0, newArray, 0, size);
        return newArray;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DoubleArrayList that = (DoubleArrayList) o;
        if (size != that.size) return false;
        for (int i = size; --i >= 0; ) {
            if (elements[i] != that.elements[i]) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(size);
        result = 31 * result + Arrays.hashCode(elements);
        return result;
    }

    @Override
    public Iterator<Double> iterator() {
        return Arrays.stream(toArray()).iterator();
    }

    private void ensureCapacity(int minCapacity) {
        int oldCapacity = elements.length;
        if (minCapacity > oldCapacity) {
            int newCapacity = (oldCapacity * 3) / 2 + 1;
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }

            double[] newArray = new double[newCapacity];
            System.arraycopy(elements, 0, newArray, 0, oldCapacity);
            elements = newArray;
        }
    }

    private void boundsCheck(int index) {
        if (index >= size || index < 0) {
            throw new IndexOutOfBoundsException("Tried to access element with index [" + index + "] in a list of size [" + size + "]");
        }
    }
}
