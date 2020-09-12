package cz.cuni.mff.respefo.util.utils;

import org.junit.Test;

import static cz.cuni.mff.respefo.util.utils.FormattingUtils.formatDouble;
import static cz.cuni.mff.respefo.util.utils.FormattingUtils.formatInteger;
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
}