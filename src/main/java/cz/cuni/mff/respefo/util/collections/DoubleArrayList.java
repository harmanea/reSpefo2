package cz.cuni.mff.respefo.util.collections;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;

/**
 * A resizable list holding primitive <code>double</code> elements. It is designed to be fast and scalable as well as
 * to ease the transition between arrays and streams by removing the need to use <code>List&lt;Double&gt;</code> and boxing.
 */
public class DoubleArrayList implements Iterable<Double> {
    private static final int DEFAULT_INITIAL_CAPACITY = 10;

    private int size;
    private double[] elements;

    /**
     * Constructs an empty list with an initial capacity of ten.
     */
    public DoubleArrayList() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param initialCapacity the initial capacity of the list
     * @throws IllegalArgumentException if the specified initial capacity is negative
     */
    public DoubleArrayList(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Initial capacity cannot be negative");
        }

        elements = new double[initialCapacity];
        size = 0;
    }

    /**
     * Constructs a list containing the specified elements.
     * The initial size and capacity of the list is the length of the array.
     *
     * @param elements the array to back the constructed list
     * @throws NullPointerException if the specified array is null
     */
    public DoubleArrayList(double[] elements) {
        Objects.requireNonNull(elements);

        this.elements = Arrays.copyOf(elements, elements.length);
        this.size = elements.length;
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list
     */
    public int size() {
        return size;
    }

    /**
     * Returns <tt>true</tt> if this list contains no elements.
     *
     * @return <tt>true</tt> if this list contains no elements
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the array of elements that the list is backed by including invalid elements between size and capacity, if any.
     *
     * <p><b>WARNING:</b> This does not copy the array. Any modifications to the array will also influence the list.
     *
     * @return the elements currently stored.
     */
    public double[] elements() {
        return elements;
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * @param element element to be appended to this list
     */
    public void add(double element) {
        ensureCapacity(size + 1);
        elements[size++] = element;
    }

    /**
     * Inserts the specified element at the specified position in this list.
     * Shifts the element currently at that position (if any) and any subsequent elements to the right (adds one to their indices).
     *
     * @param index   index at which the specified element is to be inserted
     * @param element element to be inserted
     * @throws IndexOutOfBoundsException if the index is out of range (<tt>index &lt; 0 || index &gt; size()</tt>)
     */
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

    /**
     * Appends all of the elements in the specified list to the end of this list.
     *
     * @param other list containing elements to be added to this list
     * @throws NullPointerException if the specified list is null
     */
    public void addAll(DoubleArrayList other) {
        Objects.requireNonNull(other);

        ensureCapacity(size + other.size);
        System.arraycopy(other.elements, 0, this.elements, size, other.size);
        size += other.size;
    }

    /**
     * Removes all of the elements from this list.
     * The list will be empty after this call returns.
     */
    public void clear() {
        size = 0;
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException if the index is out of range (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    public double get(int index) {
        boundsCheck(index);
        return elements[index];
    }

    /**
     * Replaces the element at the specified position in this list with the specified element.
     *
     * @param index   index of the element to replace
     * @param element element to be stored at the specified position
     * @throws IndexOutOfBoundsException if the index is out of range (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    public void set(int index, double element) {
        boundsCheck(index);
        elements[index] = element;
    }

    /**
     * Removes the element at the specified position in this list.
     * Shifts any subsequent elements to the left (subtracts one from their indices).
     *
     * @param index the index of the element to be removed
     * @throws IndexOutOfBoundsException if the index is out of range (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    public void remove(int index) {
        boundsCheck(index);
        if (size - 1 > index) {
            System.arraycopy(elements, index + 1, elements, index, size - index - 1);
        }
        size--;
    }

    /**
     * Trims the capacity of this <tt>DoubleArrayList</tt> instance to be the list's current size.
     * An application can use this operation to minimize the storage of an <tt>DoubleArrayList</tt> instance.
     */
    public void trimToSize() {
        if (elements.length > size) {
            elements = toArray();
        }
    }

    /**
     * Returns an array containing all of the elements in this list in proper sequence (from first to last element).
     *
     * <p>The returned array will be "safe" in that no references to it are maintained by this list.
     * (In other words, this method must allocate a new array).
     * The caller is thus free to modify the returned array.
     *
     * @return an array containing all of the elements in this list in proper sequence
     */
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

    /**
     * Increases the capacity of this <tt>DoubleArrayList</tt> instance, if necessary,
     * to ensure that it can hold at least the number of elements specified by the minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     */
    public void ensureCapacity(int minCapacity) {
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

    /**
     * Checks if the given index if the index is in the (<tt>index &gt;= 0 || index &lt; size()</tt>) range.
     * Throws an IndexOutOfBoundsException if it isn't.
     */
    private void boundsCheck(int index) {
        if (index >= size || index < 0) {
            throw new IndexOutOfBoundsException("Tried to access element with index [" + index + "] in a list of size [" + size + "]");
        }
    }

    /**
     * Returns a sequential {@code Stream} with this list as its source.
     *
     * @return a sequential {@code Stream} over the elements in this collection
     */
    public DoubleStream stream() {
        return Arrays.stream(toArray());
    }

    public static Collector<Double, DoubleArrayList, DoubleArrayList> toDoubleArrayList() {
        return Collector.of(DoubleArrayList::new, DoubleArrayList::add, (left, right) -> { left.addAll(right); return left; });
    }
}
