package cz.cuni.mff.respefo.util.utils;

import org.junit.Test;

import static cz.cuni.mff.respefo.util.utils.StringUtils.*;
import static org.junit.Assert.*;

public class StringUtilsTest {
    @Test
    public void testSubstringBefore() {
        assertEquals("abc", substringBefore("abc\ndef", '\n'));
        assertEquals("abc", substringBefore("abc", 'd'));
    }

    @Test
    public void testTrimmedOrPaddedString() {
        String str = "abc";

        assertEquals("   abc", trimmedOrPaddedString(str, 6));
        assertEquals("abc", trimmedOrPaddedString(str, 3));
        assertEquals("ab", trimmedOrPaddedString(str, 2));
    }

    @Test
    public void testRepeat() {
        assertEquals("aaa", repeat("a", 3));
        assertEquals("abcabc", repeat("abc", 2));
        assertEquals("hello", repeat("hello", 1));
        assertEquals(EMPTY_STRING, repeat("not repeated", 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDivideArrayValuesDifferentLengths() {
        repeat("", -1);
    }

    @Test
    public void testIsBlank() {
        assertTrue(isBlank(""));
        assertTrue(isBlank("   "));
        assertTrue(isBlank("\t"));
        assertTrue(isBlank(" \t \n \f \r \u000B \u001C \u001D \u001E \u001F"));

        assertFalse(isBlank("not blank"));
        assertFalse(isBlank("   not blank   "));
        assertFalse(isBlank("\t_\n"));
        assertFalse(isBlank("\u001C\u2202"));
        assertFalse(isBlank("\u00A0 \u2007 \u202F"));
    }
}