package cz.cuni.mff.respefo.util;

import org.junit.Test;

import static cz.cuni.mff.respefo.util.utils.MathUtils.DOUBLE_PRECISION;
import static org.junit.Assert.assertEquals;

public class SanityTests {
    @Test
    public void scientificNotation() {
        assertEquals(0.000001, 1e-6, DOUBLE_PRECISION);
    }
}
