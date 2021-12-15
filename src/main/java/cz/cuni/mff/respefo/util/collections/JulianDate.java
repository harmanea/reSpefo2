package cz.cuni.mff.respefo.util.collections;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * A collection for storing and working with Julian dates.
 * Has methods for converting to and from <code>LocalDate</code> and <code>LocalDateTime</code>.
 * <p>
 * Algorithms taken from <a href='https://en.wikipedia.org/wiki/Julian_day'>Wikipedia</a>
 */
public class JulianDate implements Comparable<JulianDate> {
    private static final double REDUCTION = 2_400_000;

    private final double jd;

    public JulianDate() {
        jd = Double.NaN;
    }

    public JulianDate(double jd) {
        this.jd = jd;
    }

    /**
     * @param rjd reduced Julian date
     */
    public static JulianDate fromRJD(double rjd) {
        return new JulianDate(rjd + REDUCTION);
    }

    public static JulianDate fromDate(LocalDate date) {
        Objects.requireNonNull(date);
        return fromDate(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    }

    public static JulianDate fromDateTime(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime);
        return fromDateTime(dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth(),
                dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond());
    }

    /**
     * @param year  using the astronomical year numbering, thus 1 BC is 0, 2 BC is −1, and 4713 BC is −4712
     * @param month numbered January to December from 1 to 12
     * @param day   day of month
     */
    public static JulianDate fromDate(int year, int month, int day) {
        int jdn = (1461 * (year + 4800 + (month - 14) / 12)) / 4
                + (367 * (month - 2 - 12 * ((month - 14) / 12))) / 12
                - (3 * ((year + 4900 + (month - 14) / 12) / 100)) / 4
                + day - 32075;

        return new JulianDate(jdn);
    }

    /**
     * @param year   using the astronomical year numbering, thus 1 BC is 0, 2 BC is −1, and 4713 BC is −4712
     * @param month  numbered January to December from 1 to 12
     * @param day    day of month
     * @param hour   UT hour of the day
     * @param minute UT minute of the day
     * @param second UT second of the day
     */
    public static JulianDate fromDateTime(int year, int month, int day, int hour, int minute, int second) {
        int jdn = (1461 * (year + 4800 + (month - 14) / 12)) / 4
                + (367 * (month - 2 - 12 * ((month - 14) / 12))) / 12
                - (3 * ((year + 4900 + (month - 14) / 12) / 100)) / 4
                + day - 32075;

        double jd = jdn + (hour - 12) / 24.0 + minute / 1440.0 + second / 86400.0;

        return new JulianDate(jd);
    }

    public LocalDate toDate() {
        int f = ((int) jd + 1401 + (((4 * (int) jd + 274277) / 146097) * 3) / 4 - 38);
        int e = 4 * f + 3;
        int g = (e % 1461) / 4;
        int h = 5 * g + 2;

        int day = (h % 153) / 5 + 1;
        int month = (h / 153 + 2) % 12 + 1;
        int year = e / 1461 - 4716 + (12 + 2 - month) / 12;

        return LocalDate.of(year, month, day).plusDays(jd % 1 > 0.5 ? 1 : 0);
    }

    public LocalTime toTime() {
        double fractionalPart = (jd + 0.5) % 1;
        long secondOfDay = Math.round(fractionalPart * 86400);

        return LocalTime.ofSecondOfDay(secondOfDay);
    }

    public LocalDateTime toDateTime() {
        return LocalDateTime.of(toDate(), toTime());
    }

    public double getJD() {
        return jd;
    }

    /**
     * @return reduced Julian date
     */
    public double getRJD() {
        return jd - REDUCTION;
    }

    /**
     * @return ISO day of the week, numbered Monday to Sunday from 1 to 7
     */
    public int getDayOfWeek() {
        return (int) (jd + 0.5) % 7 + 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JulianDate that = (JulianDate) o;
        return Double.compare(that.jd, jd) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(jd);
    }

    @Override
    public int compareTo(JulianDate other) {
        Objects.requireNonNull(other);
        return Double.compare(jd, other.jd);
    }
}
