package cz.cuni.mff.respefo.util.utils;

import cz.cuni.mff.respefo.util.UtilityClass;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StringUtils extends UtilityClass {

    public static final String EMPTY_STRING = "";

    public static String substringBefore(String str, int ch) {
        int index = str.indexOf(ch);
        if (index == -1) {
            return str;
        } else {
            return str.substring(0, index);
        }
    }

    public static String trimmedOrPaddedString(String str, int targetLength) {
        String result = str.substring(0, Math.min(targetLength, str.length()));
        return repeat(" ", targetLength - str.length()) + result;
    }

    public static String repeat(String str, int times) {
        return IntStream.range(0, times).mapToObj(i -> str).collect(Collectors.joining());
    }

    protected StringUtils() throws IllegalAccessException {
        super();
    }
}
