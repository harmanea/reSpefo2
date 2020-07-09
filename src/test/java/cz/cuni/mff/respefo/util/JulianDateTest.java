package cz.cuni.mff.respefo.util;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;

public class JulianDateTest {
    @Test
    public void roundTrip() {
        LocalDate date = LocalDate.of(2000, 5, 8);

        assertEquals(date, JulianDate.fromDate(date).toDate());
    }

    @Test
    public void testDayOfTheWeek() {
        JulianDate date = JulianDate.fromDate(2020, 7, 9);

        assertEquals(4, date.getDayOfWeek());
    }
}