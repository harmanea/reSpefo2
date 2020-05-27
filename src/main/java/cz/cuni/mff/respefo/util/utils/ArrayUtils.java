package cz.cuni.mff.respefo.util.utils;

import cz.cuni.mff.respefo.util.UtilityClass;

import java.util.Arrays;
import java.util.stream.IntStream;

public class ArrayUtils extends UtilityClass {

    /**
     * Find first array entry that is greater than target
     * @param array of values
     * @param target value
     * @return index of the value matching the criteria, returns array.length if all values are smaller than or equal to the target
     */
    public static int findFirstGreaterThen(double[] array, double target) {
        int index = Arrays.binarySearch(array, target);
        if (index >= 0) {
            return index + 1;
        } else {
            return -1 * (index + 1);
        }
    }

    /**
     * Divide all array entries by values in another array
     * @param numerators array to be divided
     * @param denominators array to divide with
     * @return adjusted array
     */
    public static double[] divideArrayValues(double[] numerators, double[] denominators) {
        if (numerators.length != denominators.length) {
            throw new IllegalArgumentException("Arrays must be of equal length");
        }

        return IntStream.range(0, numerators.length).mapToDouble(i -> numerators[i] / denominators[i]).toArray();
    }

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

    public static double[] applyBScale(double[] array, double bZero, double bScale) {
        for (int i = 0; i < array.length; i++) {
            array[i] = array[i] * bScale + bZero;
        }

        return array;
    }

    public static int nDims(Object data) {
        return 1 + data.getClass().getName().lastIndexOf('[');
    }

    protected ArrayUtils() throws IllegalAccessException {
        super();
    }
}
