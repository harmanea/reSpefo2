package cz.cuni.mff.respefo.util.utils;

import cz.cuni.mff.respefo.util.UtilityClass;

import java.util.Locale;

import static cz.cuni.mff.respefo.util.utils.StringUtils.repeat;

public class FormattingUtils extends UtilityClass {
    /**
     * Formats a double to String and pads it with spaces from the left if necessary
     * @param number to be formated
     * @param before number of digits before decimal point
     * @param after number of digits after decimal point
     * @param sign include sign
     * @return formatted String
     */
    public static String formatDouble(double number, int before, int after, boolean sign) {
        String format = "%" + (sign ? " " : "") + before + "." + after + "f";
        return formatNumber(number, format, before + after + (sign ? 2 : 1));
    }

    /**
     * Formats a double to String and pads it with spaces from the left if necessary, including sign
     * @param number to be formated
     * @param before number of digits before decimal point
     * @param after number of digits after decimal point
     * @return formatted String
     */
    public static String formatDouble(double number, int before, int after) {
        return formatDouble(number, before, after, true);
    }

    /**
     * Formats an integer to String and pads it with spaces from the left if necessary
     * @param number to be formatted
     * @param digits number of digits to show
     * @param sign include sign
     * @return formatted String
     */
    public static String formatInteger(int number, int digits, boolean sign) {
        String format = "%" + (sign ? " " : "") + digits + "d";
        return formatNumber(number, format, digits + (sign ? 1 : 0));
    }

    /**
     * Formats an integer to String and pads it with spaces from the left if necessary, including sign
     * @param number to be formatted
     * @param digits number of digits to show
     * @return formatted String
     */
    public static String formatInteger(int number, int digits) {
        return formatInteger(number, digits, true);
    }

    private static String formatNumber(Object number, String format, int targetLength) {
        String formattedNumber = String.format(Locale.US, format, number);

        return repeat(" ", Math.max(targetLength - formattedNumber.length(), 0)) + formattedNumber;
    }

    protected FormattingUtils() throws IllegalAccessException {
        super();
    }
}
