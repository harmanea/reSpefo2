package cz.cuni.mff.respefo.util.utils;

import org.junit.Test;

import static cz.cuni.mff.respefo.util.utils.StringUtils.*;
import static org.junit.Assert.assertEquals;

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
}