package cz.cuni.mff.respefo.util.utils;

import org.junit.Test;

import static cz.cuni.mff.respefo.util.utils.FormattingUtils.*;
import static org.junit.Assert.assertEquals;

public class FormattingUtilsTest {
    @Test
    public void testFormatDouble() {
        double number = 12345.12345;

        assertEquals("    12345.12345000", formatDouble(number, 8, 8));
        assertEquals(" 12345.123", formatDouble(number, 5, 3));
        assertEquals("12345.1235", formatDouble(number, 1, 4, false));
        assertEquals("12345", formatDouble(number, 1, 0, false));

        number = -123.123;

        assertEquals("-123.123", formatDouble(number, 3, 3));
        assertEquals("   -123.12", formatDouble(number, 6, 2));
        assertEquals("-123.1230", formatDouble(number, 1, 4, false));
        assertEquals("-123", formatDouble(number, 1, 0, false));

        assertEquals(formatDouble(number, 3, 3), formatDouble(number, 3, 3, true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatDoubleNegativeBefore() {
        formatDouble(42.0, -1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatDoubleZeroBefore() {
        formatDouble(42.0, 0, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatDoubleNegativeAfter() {
        formatDouble(42.0, 1, -1);
    }

    @Test
    public void testFormatInteger() {
        int number = 12345;

        assertEquals("    12345", formatInteger(number, 8));
        assertEquals("12345", formatInteger(number, 4, false));
        assertEquals(" 12345", formatInteger(number, 4, true));
        assertEquals("12345", formatInteger(number, 1, false));

        number = -123;

        assertEquals("     -123", formatInteger(number, 8));
        assertEquals("    -123", formatInteger(number, 8, false));
        assertEquals(" -123", formatInteger(number, 4, true));
        assertEquals("-123", formatInteger(number, 1, false));

        assertEquals(formatInteger(number, 8), formatInteger(number, 8, true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatIntegerNegativeDigits() {
        formatInteger(42, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatIntegerZeroDigits() {
        formatInteger(42, 0);
    }

    @Test
    public void testRound() {
        double number = 123.456;

        assertEquals("123", round(number, 0));
        assertEquals("123.5", round(number, 1));
        assertEquals("123.456", round(number, 3));
        assertEquals("123.45600", round(number, 5));

        number = -666_666.666_666;

        assertEquals("-666667", round(number, 0));
        assertEquals("-666666.667", round(number, 3));
        assertEquals("-666666.666666", round(number, 6));
        assertEquals("-666666.6666660000", round(number, 10));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRoundNegativeDigits() {
        round(42.0, -1);
    }
}