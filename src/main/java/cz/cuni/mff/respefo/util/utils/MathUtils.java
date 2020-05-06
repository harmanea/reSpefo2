package cz.cuni.mff.respefo.util.utils;

import cz.cuni.mff.respefo.util.UtilityClass;

public class MathUtils extends UtilityClass {
    public static final double DOUBLE_PRECISION = 0.0000001;

    public static boolean isNotNaN(double value) {
        return !Double.isNaN(value);
    }

    protected MathUtils() throws IllegalAccessException {
        super();
    }
}
