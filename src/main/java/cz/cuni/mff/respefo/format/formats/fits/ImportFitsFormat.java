package cz.cuni.mff.respefo.format.formats.fits;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.format.InvalidFileFormatException;
import cz.cuni.mff.respefo.format.Spectrum;
import cz.cuni.mff.respefo.format.XYSeries;
import cz.cuni.mff.respefo.format.formats.ImportFileFormat;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.util.JulianDate;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;
import nom.tam.fits.*;
import nom.tam.fits.header.Standard;
import nom.tam.util.Cursor;

import java.io.IOException;
import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class ImportFitsFormat extends FitsFormat implements ImportFileFormat {

    private static final String[] JULIAN_DATE_ALIASES = {"HJD", "HCJD", "MID-HJD"};
    private static final String[] RV_CORR_ALIASES = {"VHELIO", "HCRV", "SUN_COR"};

    @Override
    public Spectrum importFrom(String fileName) throws SpefoException {
        try (Fits f = new Fits(fileName)) {
            BasicHDU<?>[] hdus = f.read();

            if (hdus.length == 0) {
                throw new InvalidFileFormatException("There are no HDUs in the file");
            } else if (hdus.length > 1) {
                Log.warning("There are more than one HDUs in the file. The first ImageHDU will be chosen.");
            }

            ImageHDU imageHdu = (ImageHDU) Arrays.stream(hdus).filter(hdu -> hdu instanceof ImageHDU).findFirst()
                    .orElseThrow(() -> new InvalidFileFormatException("No ImageHDU in the FITS file"));

            XYSeries series = parseData(imageHdu);

            Header header = imageHdu.getHeader();
            double bZero = header.getDoubleValue(Standard.BZERO, 0);
            double bScale = header.getDoubleValue(Standard.BSCALE, 1);
            if (bZero != 0 || bScale != 1) {
                series.setYSeries(ArrayUtils.applyBScale(series.getYSeries(), bZero, bScale));
            }

            List<HeaderCard> headerCards = new ArrayList<>();
            for (Cursor<String, nom.tam.fits.HeaderCard> it = header.iterator(); it.hasNext(); ) {
                nom.tam.fits.HeaderCard card = it.next();
                headerCards.add(new HeaderCard(card.getComment(), card.getKey(), card.getValue()));
            }

            Spectrum spectrum = new Spectrum(series);
            spectrum.setOrigin(new FitsOrigin(fileName, headerCards));
            spectrum.setHjd(getHJD(header));
            spectrum.setDateOfObservation(getDateOfObservation(header));
            spectrum.setRvCorrection(getRVCorrection(header));

            return spectrum;

        } catch (IOException | FitsException exception) {
            throw new SpefoException("Error while reading file", exception);
        }
    }

    protected XYSeries parseData(ImageHDU imageHdu) throws SpefoException, FitsException {
        Object data = imageHdu.getKernel();
        if (data == null || !data.getClass().isArray()) {
            throw new InvalidFileFormatException("The HDU does not contain array data");
        }

        int nDims = ArrayUtils.nDims(data);
        if (nDims == 1) {
            double[] ySeries = getSeriesFromData(data, imageHdu.getBitPix());
            double[] xSeries = getSeriesFromCData(imageHdu.getHeader(), ySeries.length);

            return new XYSeries(xSeries, ySeries);
        } else if (nDims == 2) {
            int length = Array.getLength(data);
            if (length != 2) {
                throw new InvalidFileFormatException("The 2-D data array is too long in the first dimension");
            }

            return getBothSeriesFromData(data, imageHdu.getBitPix());
        } else {
            throw new InvalidFileFormatException("The data array is " + nDims + "-dimensional");
        }
    }

    private double[] getSeriesFromData(Object data, int bitPix) throws SpefoException {
        switch (bitPix) {
            case BasicHDU.BITPIX_DOUBLE:
                return (double[]) data;
            case BasicHDU.BITPIX_FLOAT:
            case BasicHDU.BITPIX_INT:
            case BasicHDU.BITPIX_SHORT:
            case BasicHDU.BITPIX_LONG:
            case BasicHDU.BITPIX_BYTE:
                return IntStream.range(0, Array.getLength(data)).mapToDouble(j ->  Array.getDouble(data, j)).toArray();
            default:
                throw new InvalidFileFormatException("Data is not of a valid value type");
        }
    }

    private double[] getSeriesFromCData(Header header, int size) {
        double crpix = header.getDoubleValue(Standard.CRPIXn.n(1), 1);
        double cdelt = header.getDoubleValue(Standard.CDELTn.n(1), 1);
        double crval = header.getDoubleValue(Standard.CRVALn.n(1), 0);

        return ArrayUtils.fillArray(size, (1 - crpix) * cdelt + crval, cdelt);
    }

    private XYSeries getBothSeriesFromData(Object data, int bitPix) throws SpefoException {
        double[] xSeries = getSeriesFromData(Array.get(data, 0), bitPix);
        double[] ySeries = getSeriesFromData(Array.get(data, 1), bitPix);

        return new XYSeries(xSeries, ySeries);
    }

    private JulianDate getHJD(Header header) {
        for (String alias : JULIAN_DATE_ALIASES) {
            if (header.containsKey(alias)) {
                return new JulianDate(header.getDoubleValue(alias));
            }
        }

        return null;
    }

    private LocalDateTime getDateOfObservation(Header header) {
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

        return null;
    }

    private LocalDateTime parseDateTime(String dateTimeValue) {
        try {
            return LocalDateTime.parse(dateTimeValue);

        } catch (Exception exception) {
            return null;
        }
    }

    private LocalDateTime parseDateAndTime(String dateValue, String timeValue) {
        try {
            LocalDate localDate = LocalDate.parse(dateValue);
            LocalTime localTime = LocalTime.parse(timeValue);
            return localDate.atTime(localTime);

        } catch (Exception exception) {
            return null;
        }
    }

    private LocalDateTime parseDateAndTmStart(String dateValue, long tmStart) {
        try {
            LocalDate localDate = LocalDate.parse(dateValue);
            LocalTime localTime = LocalTime.ofSecondOfDay(tmStart);
            return localDate.atTime(localTime);

        } catch (Exception exception) {
            return null;
        }
    }

    protected double getRVCorrection(Header header) {
        for (String alias : RV_CORR_ALIASES) {
            if (header.containsKey(alias)) {
                return header.getDoubleValue(alias);
            }
        }

        return Double.NaN;
    }
}
