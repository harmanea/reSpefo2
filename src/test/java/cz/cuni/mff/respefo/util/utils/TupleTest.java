package cz.cuni.mff.respefo.util.utils;

import cz.cuni.mff.respefo.util.collections.tuple.Tuple;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TupleTest {
    @Test
    public void testToString() {
        assertEquals("(a, b)", Tuple.of("a", "b").toString());
        assertEquals("(1, 2, 3)", Tuple.of(1, 2, 3).toString());
        assertEquals("(0.5, test, null, true)", Tuple.of(0.5, "test", null, true).toString());
        assertEquals("(-42.0, a, false, 420, e)", Tuple.of(-42.0, 'a', false, 420L, "e").toString());
    }
}
