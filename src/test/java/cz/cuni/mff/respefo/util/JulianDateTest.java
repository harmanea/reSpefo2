package cz.cuni.mff.respefo.util;

import cz.cuni.mff.respefo.util.collections.JulianDate;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static cz.cuni.mff.respefo.util.utils.MathUtils.DOUBLE_PRECISION;
import static org.junit.Assert.assertEquals;

public class JulianDateTest {
    @Test
    public void roundTrip() {
        LocalDate date = LocalDate.of(2000, 5, 8);

        assertEquals(date, JulianDate.fromDate(date).toDate());

        LocalDateTime dateTime = LocalDateTime.of(1985, 12, 1, 0, 12, 45);

        assertEquals(dateTime, JulianDate.fromDateTime(dateTime).toDateTime());
        assertEquals(dateTime.toLocalDate(), JulianDate.fromDateTime(dateTime).toDate());
        assertEquals(dateTime.toLocalTime(), JulianDate.fromDateTime(dateTime).toTime());
    }

    @Test
    public void testDayOfTheWeek() {
        assertEquals(4, JulianDate.fromDate(2020, 7, 9).getDayOfWeek());
        assertEquals(1, JulianDate.fromDate(2022, 2, 28).getDayOfWeek());
    }

    @Test
    public void wikiTest() {
        LocalDateTime dateTime = LocalDateTime.of(2000, 1, 1, 18, 0, 0);

        assertEquals(2451545.25, JulianDate.fromDateTime(dateTime).getJD(), DOUBLE_PRECISION);
    }
}