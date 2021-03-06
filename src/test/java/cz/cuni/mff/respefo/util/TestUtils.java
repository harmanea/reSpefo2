package cz.cuni.mff.respefo.util;

import java.io.File;

public class TestUtils {

    public static double[] arrayOf(double ... values) {
        return values;
    }

    public static double[] emptyArray() {
        return new double[0];
    }

    public static String buildPath(String ... parts) {
        return String.join(File.separator, parts);
    }

    private TestUtils() {
        throw new IllegalStateException();
    }
}
