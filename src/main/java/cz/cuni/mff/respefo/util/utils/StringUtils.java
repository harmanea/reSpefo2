package cz.cuni.mff.respefo.util.utils;

import cz.cuni.mff.respefo.util.UtilityClass;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StringUtils extends UtilityClass {

    public static final String EMPTY_STRING = "";

    /**
     * Returns the part of the string before the first occurrence of the given char.
     * If there is no such character, returns the whole string.
     * @param str string to search
     * @param ch character to search for
     * @return the (possibly) trimmed string
     */
    public static String substringBefore(String str, int ch) {
        Objects.requireNonNull(str);

        int index = str.indexOf(ch);
        if (index == -1) {
            return str;
        } else {
            return str.substring(0, index);
        }
    }

    /**
     * Trims or pads the string to a given size.
     * @param str string to adjust
     * @param targetLength length to which the string will be padded or trimmed
     * @return the string of the given size
     */
    public static String trimmedOrPaddedString(String str, int targetLength) {
        Objects.requireNonNull(str);

        String result = str.substring(0, Math.min(targetLength, str.length()));
        return repeat(" ", Math.max(0, targetLength - str.length())) + result;
    }

    /**
     * Repeats the string a given number of times.
     * @param str string to repeat
     * @param times number of times the string should be repeated, returns and empty string if it's zero
     * @return the repeated string
     */
    public static String repeat(String str, int times) {
        Objects.requireNonNull(str);

        if (times < 0) {
            throw new IllegalArgumentException("Times must be a non-negative integer");
        }

        return IntStream.range(0, times).mapToObj(i -> str).collect(Collectors.joining());
    }

    /**
     * Returns a human-readable version of the file size as defined by SI (e.g. 1k = 1.000).
     * <p>
     * Taken from a Stack Overflow answer by aioobe, CC BY-SA 4.0
     * <p>
     * <a href="https://stackoverflow.com/a/3758880">https://stackoverflow.com/a/3758880</a>
     *
     * @param bytes the number of bytes
     * @return a human-readable display value
     */
    public static String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }

    /**
     * Check whether a string is empty or only whitespace.
     *
     * @param str string to evaluate
     * @return True if the given string is empty or contains only whitespace characters, False otherwise
     * @see Character#isWhitespace
     */
    public static boolean isBlank(String str) {
        Objects.requireNonNull(str);
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Combine a list of integers into a concise string.
     * <p><p>
     * For example:
     * <p>
     * 1, 2, 3, 4, 5 => "1-5"
     * <p>
     * 3, 5, 9 => "3,5,9"
     * <p>
     * 100, 101, 102, 105 => "100-102,105"
     *
     * @param indices list of integer indices
     * @return a concise representation of the indices
     */
    public static String combineIndices(List<Integer> indices) {
        if (indices.isEmpty()) {
            return "";
        }

        List<String> fragments = new ArrayList<>();

        int low = indices.get(0);
        int high = low;

        for (int index : indices) {
            if (index > high + 1) {
                fragments.add(fragment(low, high));

                low = index;
            }

            high = index;
        }

        fragments.add(fragment(low, high));

        return String.join(",", fragments);
    }

    private static String fragment(int low, int high) {
        if (high > low) {
            return low + "-" + high;
        } else {
            return String.valueOf(low);
        }
    }

    protected StringUtils() throws IllegalAccessException {
        super();
    }
}
