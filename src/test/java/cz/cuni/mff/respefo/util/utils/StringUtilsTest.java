package cz.cuni.mff.respefo.util.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringUtilsTest {
    @Test
    public void testSubstringBefore() {
        String s = "abc\ndef";
        assertEquals("abc", StringUtils.substringBefore(s, '\n'));

        s = "abc";
        assertEquals("abc", StringUtils.substringBefore(s, 'd'));
    }
}