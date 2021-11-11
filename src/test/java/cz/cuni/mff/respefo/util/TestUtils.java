package cz.cuni.mff.respefo.util;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class TestUtils {

    public static double[] arrayOf(double ... values) {
        return values;
    }

    public static double[] emptyArray() {
        return new double[0];
    }

    @SafeVarargs
    public static <T> List<T> listOf(T ... values) {
        return Arrays.asList(values);
    }

    public static String buildPath(String ... parts) {
        return String.join(File.separator, parts);
    }

    private TestUtils() {
        throw new IllegalStateException();
    }
}
