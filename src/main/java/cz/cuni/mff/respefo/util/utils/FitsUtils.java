package cz.cuni.mff.respefo.util.utils;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.format.InvalidFileFormatException;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.util.UtilityClass;
import cz.cuni.mff.respefo.util.collections.JulianDate;
import nom.tam.fits.*;
import nom.tam.fits.header.Standard;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;

public class FitsUtils extends UtilityClass {

    // TODO: There is a whole lot of useable header cards in nom.tam.fits.header(.extra), maybe use some of those as well

    private static final String[] JULIAN_DATE_ALIASES = {"HJD", "HCJD", "MID-HJD"};
    private static final String[] RV_CORR_ALIASES = {"VHELIO", "HCRV", "SUN_COR"};
    private static final String[] EXP_TIME_ALIASES = {"EXPTIME", "CTIME", "ITIME", "DARKTIME"}; // ObservationDurationDescription.EXPOSURE, ObservationDurationDescription.EXPTIME, SBFitsExt.DARKTIME

    public static Object extractData(String fileName) throws SpefoException {
        try (Fits f = new Fits(fileName)) {
            BasicHDU<?>[] hdus = f.read();

            if (hdus.length == 0) {
                throw new InvalidFileFormatException("There are no HDUs in the file");
            } else if (hdus.length > 1) {
                Log.warning("There are more than one HDUs in the file. The first ImageHDU will be chosen.");
            }

            ImageHDU imageHDU = (ImageHDU) Arrays.stream(hdus).filter(hdu -> hdu instanceof ImageHDU).findFirst()
                    .orElseThrow(() -> new InvalidFileFormatException("No ImageHDU in the FITS file"));

            return imageHDU.getKernel();

        } catch (FitsException | IOException | ClassCastException exception) {
            throw new SpefoException("Error while reading file", exception);
        }
    }

    public static JulianDate getHJD(Header header) {
        for (String alias : JULIAN_DATE_ALIASES) {
            if (header.containsKey(alias)) {
                return new JulianDate(header.getDoubleValue(alias));
            }
        }

        return new JulianDate();
    }

    public static LocalDateTime getDateOfObservation(Header header) {
        String dateValue = header.getStringValue(Standard.DATE_OBS);
        LocalDateTime dateTime = parseDateTime(dateValue);
        if (dateTime != null) {
            return dateTime;
        }

        String timeValue = header.getStringValue("UT");
        dateTime = parseDateAndTime(dateValue, timeValue);
        if (dateTime != null) {
            return dateTime;
        }

        timeValue = header.getStringValue("UT-OBS");
        dateTime = parseDateAndTime(dateValue, timeValue);
        if (dateTime != null) {
            return dateTime;
        }

        long tmStart = (long) header.getDoubleValue("TM-START", 0);
        dateTime = parseDateAndTmStart(dateValue, tmStart);
        if (dateTime != null) {
            return dateTime;
        }

        return LocalDateTime.MIN;
    }

    private static LocalDateTime parseDateTime(String dateTimeValue) {
        try {
            return LocalDateTime.parse(dateTimeValue);

        } catch (Exception exception) {
            return null;
        }
    }

    private static LocalDateTime parseDateAndTime(String dateValue, String timeValue) {
        try {
            LocalDate localDate = LocalDate.parse(dateValue);
            LocalTime localTime = LocalTime.parse(timeValue);
            return localDate.atTime(localTime);

        } catch (Exception exception) {
            return null;
        }
    }

    private static LocalDateTime parseDateAndTmStart(String dateValue, long tmStart) {
        try {
            LocalDate localDate = LocalDate.parse(dateValue);
            LocalTime localTime = LocalTime.ofSecondOfDay(tmStart);
            return localDate.atTime(localTime);

        } catch (Exception exception) {
            return null;
        }
    }

    public static double getRVCorrection(Header header) {
        for (String alias : RV_CORR_ALIASES) {
            if (header.containsKey(alias)) {
                return header.getDoubleValue(alias);
            }
        }

        return Double.NaN;
    }

    public static double getExpTime(Header header) {
        for (String alias : EXP_TIME_ALIASES) {
            if (header.containsKey(alias)) {
                return header.getBigDecimalValue(alias).doubleValue();
            }
        }

        return Double.NaN;
    }

    protected FitsUtils() throws IllegalAccessException {
        super();
    }
}
