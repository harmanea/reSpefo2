package cz.cuni.mff.respefo.format.scan;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.format.Data;
import cz.cuni.mff.respefo.format.FileFormat;
import cz.cuni.mff.respefo.format.SpectrumFile;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.Header;
import nom.tam.fits.ImageHDU;
import nom.tam.fits.header.Standard;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.stream.IntStream;

public class FitsFormat implements FileFormat {
    @Override
    public String[] fileExtensions() {
        return new String[] { "fits", "fts", "fit"};
    }

    @Override
    public SpectrumFile importFrom(String fileName) throws SpefoException {
        Data spectrumData;

        try (Fits f = new Fits(fileName)) {
            BasicHDU<?>[] HDUs = f.read();

            if (HDUs.length == 0) {
                throw new SpefoException("There are no HDUs in the file.");
            } else if (HDUs.length > 1) {
                Log.info("There are more than one HDUs in the file. The first ImageHDU will be chosen.");
            }

            ImageHDU imageHdu = (ImageHDU) Arrays.stream(HDUs).filter(hdu -> hdu instanceof ImageHDU).findFirst()
                    .orElseThrow(() -> new SpefoException("No ImageHDU in the FITS file."));

            Object data = imageHdu.getKernel();
            if (data == null || !data.getClass().isArray()) {
                throw new SpefoException("The HDU does not contain array data.");
            }

            Header header = imageHdu.getHeader();

            int nDims = ArrayUtils.nDims(data);
            if (nDims == 1) {
                double[] ySeries = getSeriesFromData(data, imageHdu.getBitPix());
                double[] xSeries = getSeriesFromCData(header, ySeries.length);

                spectrumData = new Data(xSeries, ySeries);
            } else if (nDims == 2) {
                int length = Array.getLength(data);
                if (length != 2) {
                    throw new SpefoException("The 2-D data array is too long in the first dimension.");
                }

                spectrumData = getBothSeriesFromData(data, imageHdu.getBitPix());
            } else {
                throw new SpefoException("The data array is " + nDims + "-dimensional.");
            }

            double bZero = header.getDoubleValue(Standard.BZERO, 0);
            double bScale = header.getDoubleValue(Standard.BSCALE, 1);

            if (bZero != 0 || bScale != 1) {
                spectrumData.setY(ArrayUtils.applyBScale(spectrumData.getY(), bZero, bScale));
            }

            parseDate(header); // TODO: use this info
        } catch (Exception exception) {
            throw new SpefoException("Error while reading file", exception);
        }

        return new SpectrumFile(spectrumData);
    }

    @Override
    public void exportTo(SpectrumFile spectrumFile, String fileName) throws SpefoException {

    }

    private double[] getSeriesFromData(Object data, int bitPix) throws SpefoException {
        switch (bitPix) {
            case BasicHDU.BITPIX_DOUBLE:
                return (double[]) data;
            case BasicHDU.BITPIX_FLOAT:
                return IntStream.range(0, ((float[]) data).length).mapToDouble(j -> ((float[]) data)[j]).toArray();
            case BasicHDU.BITPIX_INT:
                return IntStream.range(0, ((int[]) data).length).mapToDouble(j -> ((int[]) data)[j]).toArray();
            case BasicHDU.BITPIX_SHORT:
                return IntStream.range(0, ((short[]) data).length).mapToDouble(j -> ((short[]) data)[j]).toArray();
            case BasicHDU.BITPIX_LONG:
                return IntStream.range(0, ((long[]) data).length).mapToDouble(j -> ((long[]) data)[j]).toArray();
            case BasicHDU.BITPIX_BYTE:
                return IntStream.range(0, ((byte[]) data).length).mapToDouble(j -> ((byte[]) data)[j]).toArray();
            default:
                throw new SpefoException("Data is not of a valid value type.");
        }
    }

    private double[] getSeriesFromCData(Header header, int size) {
        double CRPIX = header.getDoubleValue("CRPIX1", 1);
        double CDELT = header.getDoubleValue("CDELT1", 1);
        double CRVAL = header.getDoubleValue("CRVAL1", 0);

        return ArrayUtils.fillArray(size, (1 - CRPIX) * CDELT + CRVAL, CDELT);
    }

    private Data getBothSeriesFromData(Object data, int bitPix) throws SpefoException {
        double[] xSeries;
        double[] ySeries;

        switch (bitPix) {
            case BasicHDU.BITPIX_DOUBLE:
                xSeries = getSeriesFromData(((double[][]) data)[0], bitPix);
                ySeries = getSeriesFromData(((double[][]) data)[1], bitPix);
                break;
            case BasicHDU.BITPIX_FLOAT:
                xSeries = getSeriesFromData(((float[][]) data)[0], bitPix);
                ySeries = getSeriesFromData(((float[][]) data)[1], bitPix);
                break;
            case BasicHDU.BITPIX_INT:
                xSeries = getSeriesFromData(((int[][]) data)[0], bitPix);
                ySeries = getSeriesFromData(((int[][]) data)[1], bitPix);
                break;
            case BasicHDU.BITPIX_SHORT:
                xSeries = getSeriesFromData(((short[][]) data)[0], bitPix);
                ySeries = getSeriesFromData(((short[][]) data)[1], bitPix);
                break;
            case BasicHDU.BITPIX_LONG:
                xSeries = getSeriesFromData(((long[][]) data)[0], bitPix);
                ySeries = getSeriesFromData(((long[][]) data)[1], bitPix);
                break;
            case BasicHDU.BITPIX_BYTE:
                xSeries = getSeriesFromData(((byte[][]) data)[0], bitPix);
                ySeries = getSeriesFromData(((byte[][]) data)[1], bitPix);
                break;
            default:
                throw new SpefoException("Data is not of a valid value type.");
        }

        return new Data(xSeries, ySeries);
    }

    private LocalDateTime parseDate(Header header) {
        String dateValue = header.getStringValue(Standard.DATE_OBS);

        LocalDateTime dateTime = parseDateTime(dateValue);
        if (dateTime == null) {
            String timeValue = header.getStringValue("UT");
            dateTime = parseDateAndTime(dateValue, timeValue);
        }
        if (dateTime == null) {
            long tmStart = (long) header.getDoubleValue("TM-START", 0);
            dateTime = parseDateAndTmStart(dateValue, tmStart);
        }
        if (dateTime == null) {
            dateTime = LocalDateTime.MIN;
        }

        return dateTime;
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
}
