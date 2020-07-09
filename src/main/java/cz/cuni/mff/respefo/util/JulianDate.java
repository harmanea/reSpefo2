package cz.cuni.mff.respefo.util;

import java.time.LocalDate;

/**
 * Algorithms taken from https://en.wikipedia.org/wiki/Julian_day
 */
public class JulianDate {
    private final int jdn;

    public JulianDate(int jdn) {
        this.jdn = jdn;
    }

    public static JulianDate fromDate(LocalDate date) {
        return fromDate(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    }

    /**
     * @param year using the astronomical year numbering, thus 1 BC is 0, 2 BC is −1, and 4713 BC is −4712
     * @param month numbered January to December from 1 to 12
     * @param day day of month
     */
    public static JulianDate fromDate(int year, int month, int day) {
        int jdn = (1461 * (year + 4800 + (month - 14) / 12)) / 4
                + (367 * (month - 2 - 12 * ((month - 14) / 12))) / 12
                - (3 * ((year + 4900 + (month - 14) / 12) / 100)) / 4
                + day - 32075;

        return new JulianDate(jdn);
    }

    public LocalDate toDate() {
        int f = jdn + 1401 + (((4 * jdn + 274277) / 146097) * 3) / 4 - 38;
        int e = 4 * f + 3;
        int g = (e % 1461) / 4;
        int h = 5 * g + 2;

        int day = (h % 153) / 5 + 1;
        int month = (h / 153 + 2) % 12 + 1;
        int year = e / 1461 - 4716 + (12 + 2 - month) / 12;

        return LocalDate.of(year, month, day);
    }

    public int getJD() {
        return jdn;
    }

    /**
     * @return reduced Julian date
     */
    public int getRJD() {
        return jdn - 2400000;
    }

    /**
     * @return ISO day of the week, numbered Monday to Sunday from 1 to 7
     */
    public int getDayOfWeek() {
        return jdn % 7 + 1;
    }
}
