package cz.cuni.mff.respefo.util.utils;

import cz.cuni.mff.respefo.util.UtilityClass;
import cz.cuni.mff.respefo.util.collections.tuple.Pair;
import cz.cuni.mff.respefo.util.collections.tuple.Tuple;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntPredicate;
import java.util.function.IntToDoubleFunction;
import java.util.stream.IntStream;

public class ArrayUtils extends UtilityClass {

    /**
     * Find first array entry that is greater than target. If there are multiple such values, the first one is returned.
     * @param array to search, must be sorted in ascending order
     * @param target to search for
     * @return index of the value matching the criteria, returns array.length if all values are smaller than or equal to the target
     */
    public static int indexOfFirstGreaterThan(double[] array, double target) {
        Objects.requireNonNull(array);

        int index = Arrays.binarySearch(array, target);
        if (index >= 0) {
            while (index < array.length - 1 && array[index] == array[index + 1]) {
                index++;
            }
            return index + 1;
        } else {
            return -1 * (index + 1);
        }
    }

    /**
     * Find the array entry that is, by value, the closest to the target.
     * @param array to search, must be sorted in ascending order
     * @param target to search for
     * @return index of the value matching the criteria
     */
    public static int indexOfClosest(double[] array, double target) {
        Objects.requireNonNull(array);
        if (array.length == 0) {
            throw new IllegalArgumentException("Array must not be empty.");
        }

        int index = Arrays.binarySearch(array, target);
        if (index >= 0) {
            return index;

        } else {
            int insertionPoint = -1 * (index + 1);
            if (insertionPoint == 0) {
                return insertionPoint;
            } else if (insertionPoint == array.length) {
                return insertionPoint - 1;
            } else {
                double lowDiff = target - array[insertionPoint - 1];
                double topDiff = array[insertionPoint] - target;

                return lowDiff > topDiff ? insertionPoint : insertionPoint - 1;
            }
        }
    }

    /**
     * Find the index with the lowest value provided by a function.
     * The value function will only be called once per index.
     * @param size number of indices
     * @param valueFunction a function that returns the value for a given index
     * @return index of the smallest value
     */
    public static int indexOfMin(int size, IntToDoubleFunction valueFunction) {
        return indexOfMin(size, valueFunction, i -> true);
    }

    /**
     * Find the index with the lowest value provided by a function matching a filtering criteria.
     * The value and filter functions will only be called once per index.
     * @param size number of indices
     * @param valueFunction a function that returns the value for a given index
     * @param filter a function that returns whether the index should be used
     * @return index of the smallest value, returns -1 if no index is accepted by the filter
     */
    public static int indexOfMin(int size, IntToDoubleFunction valueFunction, IntPredicate filter) {
        Objects.requireNonNull(valueFunction);

        if (size < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }

        int minIndex = -1;
        double minValue = Double.MAX_VALUE;

        for (int i = 0; i < size; i++) {
            if (filter.test(i)) {
                double currentValue = valueFunction.applyAsDouble(i);
                if (currentValue < minValue) {
                    minIndex = i;
                    minValue = currentValue;
                }
            }
        }

        return minIndex;
    }

    /**
     * Compute the quotient of the two arrays.
     * @param numerators array to be divided
     * @param denominators array to divide by
     * @return array of quotients
     */
    public static double[] divideArrayValues(double[] numerators, double[] denominators) {
        Objects.requireNonNull(numerators);
        Objects.requireNonNull(denominators);

        if (numerators.length != denominators.length) {
            throw new IllegalArgumentException("Arrays must be of equal length");
        }

        return IntStream.range(0, numerators.length).mapToDouble(i -> numerators[i] / denominators[i]).toArray();
    }

    /**
     * Compute the quotient of the array and a value.
     * @param numerators array to be divided
     * @param denominator value to divide by
     * @return array of quotients
     */
    public static double[] divideArrayValues(double[] numerators, double denominator) {
        Objects.requireNonNull(numerators);

        return Arrays.stream(numerators).map(numerator -> numerator / denominator).toArray();
    }

    /**
     * Compute the difference of the two arrays.
     * @param minuends array to be subtracted from
     * @param subtrahends array to subtract with
     * @return array of differences
     */
    public static double[] subtractArrayValues(double[] minuends, double[] subtrahends) {
        Objects.requireNonNull(minuends);
        Objects.requireNonNull(subtrahends);

        if (minuends.length != subtrahends.length) {
            throw new IllegalArgumentException("Arrays must be of equal length");
        }

        return IntStream.range(0, minuends.length).mapToDouble(i -> minuends[i] - subtrahends[i]).toArray();
    }

    /**
     * Add a value to all array entries.
     * @param array to add to
     * @param value to add
     * @return adjusted array
     */
    public static double[] addValueToArrayElements(double[] array, double value) {
        Objects.requireNonNull(array);

        return Arrays.stream(array).map(num -> num + value).toArray();
    }

    /**
     * Create a new array and fill it with the given range.
     * @param size of the new array
     * @param start value (inclusive)
     * @param step increment
     * @return filled array
     */
    public static double[] fillArray(int size, double start, double step) {
        if (size < 1) {
            throw new IllegalArgumentException("Size must be a positive integer");
        }

        double[] result = new double[size];

        result[0] = start;
        for (int i = 1; i < size; i++) {
            result[i] = result[i - 1] + step;
        }

        return result;
    }

    /**
     * Create a new array using the given function.
     * @param size of the new array
     * @param creator function that maps the array index to a value
     * @return created array
     */
    public static double[] createArray(int size, IntToDoubleFunction creator) {
        Objects.requireNonNull(creator);

        if (size < 1) {
            throw new IllegalArgumentException("Size must be a positive integer");
        }

        return IntStream.range(0, size).mapToDouble(creator).toArray();
    }

    /**
     * Create a new array with evenly spaced numbers over a specified interval.
     * @param start the starting value of the sequence
     * @param stop the end value of the sequence
     * @param size number of samples to generate
     * @return created array
     */
    public static double[] linspace(double start, double stop, int size) {
        if (size < 1) {
            throw new IllegalArgumentException("Size must be a positive integer");
        }

        return IntStream.range(0, size).mapToDouble(i -> start + i * (stop - start) / (size - 1)).toArray();
    }

    /**
     * Create a new array that is the mirrored version of the given array.
     * @param array to mirror
     * @return mirrored array
     */
    public static double[] reverseArray(double[] array) {
        Objects.requireNonNull(array);

        return IntStream.rangeClosed(1, array.length).mapToDouble(i -> array[array.length - i]).toArray();
    }

    /**
     * Transform array pixel values to they're physical values.
     * <p>
     * As defined by the FITS Standard. This transformation shall be used,
     * when the array pixel values are not the true physical values, to
     * transform the primary data array values to the true values using the
     * equation: physical_value = BZERO + BSCALE * array_value.
     *
     * @param array of pixels
     * @param bZero a floating point number representing the physical value
     * corresponding to an array value of zero.
     * @param bScale a floating point number
     * representing the coefficient of the linear term in the scaling
     * equation, the ratio of physical value to array value at zero offset
     * @return transformed array
     */
    public static double[] applyBScale(double[] array, double bZero, double bScale) {
        Objects.requireNonNull(array);

        return Arrays.stream(array).map(v -> v * bScale + bZero).toArray();
    }

    /**
     * Returns the dimension of the given array.
     * @param data array
     * @return number of dimensions, 0 if the parameter is not an array
     */
    public static int nDims(Object data) {
        Objects.requireNonNull(data);

        return 1 + data.getClass().getName().lastIndexOf('[');
    }

    /**
     * Test if the differences between all neighbouring pairs are equal.
     * @param values to test
     * @return true if all differences are the same, false otherwise
     */
    public static boolean valuesHaveSameDifference(double[] values) {
        Objects.requireNonNull(values);

        if (values.length < 2) {
            throw new IllegalArgumentException("Array must have at least two elements.");
        } else if (values.length == 2) {
            return true;
        }

        double diff = values[1] - values[0];
        for (int i = 2; i < values.length; i++) {
            if (!MathUtils.doublesEqual(diff, values[i] - values[i - 1])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the minimum and the maximum element in the array.
     * This implementation is efficient in that it loops over the array only once to find both elements.
     * @param array to search
     * @return a tuple of the minimum and maximum elements respectively
     */
    public static Pair<Double, Double> findMinMax(double[] array) {
        Objects.requireNonNull(array);

        if (array.length == 0) {
            throw new IllegalArgumentException("Array must not be empty.");
        }

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for (double value : array) {
            if (value < min) {
                min = value;
            }
            if (value > max) {
                max = value;
            }
        }

        return Tuple.of(min, max);
    }

    protected ArrayUtils() throws IllegalAccessException {
        super();
    }
}
