package cz.cuni.mff.respefo.util.utils;

import cz.cuni.mff.respefo.util.UtilityClass;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntToDoubleFunction;
import java.util.stream.IntStream;

public class ArrayUtils extends UtilityClass {

    /**
     * Find first array entry that is greater than target. If there are multiple such values, the first one is returned.
     * @param array to search, must be sorted in ascending order
     * @param target to search for
     * @return index of the value matching the criteria, returns array.length if all values are smaller than or equal to the target
     */
    public static int findFirstGreaterThan(double[] array, double target) {
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
    public static int findClosest(double[] array, double target) {
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
     * Divide all array entries by values in another array.
     * @param numerators array to be divided
     * @param denominators array to divide with
     * @return adjusted array
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
     * Divide all array entries by a given value.
     * @param numerators array to be divided
     * @param denominator value to divide with
     * @return adjusted array
     */
    public static double[] divideArrayValues(double[] numerators, double denominator) {
        Objects.requireNonNull(numerators);

        return Arrays.stream(numerators).map(numerator -> numerator / denominator).toArray();
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
     *
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
            throw new IllegalArgumentException("Array must not be empty.");
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

    protected ArrayUtils() throws IllegalAccessException {
        super();
    }
}
