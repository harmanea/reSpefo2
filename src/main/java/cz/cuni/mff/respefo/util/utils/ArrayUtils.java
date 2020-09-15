package cz.cuni.mff.respefo.util.utils;

import cz.cuni.mff.respefo.util.UtilityClass;

import java.util.Arrays;
import java.util.function.IntToDoubleFunction;

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

        return createArray(numerators.length, i -> numerators[i] / denominators[i]);
    }

    /**
     * Divide all array entries by a given value
     * @param numerators array to be divided
     * @param denominator value to divide with
     * @return adjusted array
     */
    public static double[] divideArrayValues(double[] numerators, double denominator) {
        return Arrays.stream(numerators).map(numerator -> numerator / denominator).toArray();
    }

    /**
     * Add a value to all array entries
     * @param array
     * @param value to add
     * @return adjusted array
     */
    public static double[] addValueToArrayElements(double[] array, double value) {
        return Arrays.stream(array).map(num -> num + value).toArray();
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

    public static double[] fillArray(int size, double value) {
        if (size < 1) {
            throw new IllegalArgumentException("Size must be a positive integer");
        }

        double[] result = new double[size];
        for (int i = 0; i < size; i++) {
            result[i] = value;
        }

        return result;
    }

    public static double[] createArray(int size, IntToDoubleFunction creator) {
        if (size < 1) {
            throw new IllegalArgumentException("Size must be a positive integer");
        }

        double[] result = new double[size];
        for (int i = 0; i < size; i++) {
            result[i] = creator.applyAsDouble(i);
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

    public static boolean valuesHaveSameDifference(double[] values) {
        if (values.length < 2) {
            throw new IllegalArgumentException("Values must contain at least two values");
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

    public static double[] reverseArray(double[] array) {
        double[] newArray = new double[array.length];
        for(int i = 0; i < newArray.length; i++)
        {
            newArray[i] = array[array.length - i - 1];
        }

        return newArray;
    }

    public static int findClosest(double[] array, double target) {
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

    protected ArrayUtils() throws IllegalAccessException {
        super();
    }
}
