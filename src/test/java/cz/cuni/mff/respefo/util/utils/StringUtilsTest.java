package cz.cuni.mff.respefo.util.utils;

import org.junit.Test;

import static cz.cuni.mff.respefo.util.TestUtils.listOf;
import static cz.cuni.mff.respefo.util.utils.StringUtils.*;
import static java.util.Collections.emptyList;
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

    @Test
    public void testCombineIndices() {
        assertEquals("1,3,5", combineIndices(listOf(1, 3, 5)));
        assertEquals("1-10", combineIndices(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
        assertEquals("10-11,13-15,17-18,20", combineIndices(listOf(10, 11, 13, 14, 15, 17, 18, 20)));
        assertEquals("42", combineIndices(listOf(42)));
        assertEquals("", combineIndices(emptyList()));
    }
}