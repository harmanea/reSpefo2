package cz.cuni.mff.respefo.util.utils;

import cz.cuni.mff.respefo.util.UtilityClass;

public class ArrayUtils extends UtilityClass {

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
