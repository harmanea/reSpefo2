package cz.cuni.mff.respefo.format.formats.fits;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.format.InvalidFileFormatException;
import cz.cuni.mff.respefo.format.Spectrum;
import cz.cuni.mff.respefo.format.formats.ImportFileFormat;
import cz.cuni.mff.respefo.util.collections.FitsFile;
import cz.cuni.mff.respefo.util.collections.JulianDate;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;
import cz.cuni.mff.respefo.util.utils.FitsUtils;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Header;
import nom.tam.fits.header.Standard;
import nom.tam.util.Cursor;

import java.io.File;
import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class ImportFitsFormat extends FitsFormat implements ImportFileFormat {

    @Override
    public Spectrum importFrom(String fileName) throws SpefoException {
        FitsFile fits = new FitsFile(new File(fileName));

        XYSeries series = parseData(fits);

        Header header = fits.getHeader();
        double bZero = header.getDoubleValue(Standard.BZERO, 0);
        double bScale = header.getDoubleValue(Standard.BSCALE, 1);
        if (bZero != 0 || bScale != 1) {
            series.updateYSeries(ArrayUtils.applyBScale(series.getYSeries(), bZero, bScale));
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
        spectrum.setExpTime(getExpTime(header));

        return spectrum;
    }

    protected XYSeries parseData(FitsFile fits) throws SpefoException {
        Object data = fits.getData();
        if (data == null || !data.getClass().isArray()) {
            throw new InvalidFileFormatException("The HDU does not contain array data");
        }

        int nDims = ArrayUtils.nDims(data);
        if (nDims == 1) {
            double[] ySeries = getSeriesFromData(data, fits.getBitPix());
            double[] xSeries = getSeriesFromCData(fits.getHeader(), ySeries.length);

            return new XYSeries(xSeries, ySeries);
        } else if (nDims == 2) {
            int length = Array.getLength(data);
            if (length != 2) {
                throw new InvalidFileFormatException("The 2-D data array is too long in the first dimension");
            }

            return getBothSeriesFromData(data, fits.getBitPix());
        } else {
            throw new InvalidFileFormatException("The data array is " + nDims + "-dimensional");
        }
    }

    protected double[] getSeriesFromData(Object data, int bitPix) throws SpefoException {
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

    protected double[] getSeriesFromCData(Header header, int size) {
        double crpix = header.getDoubleValue(Standard.CRPIXn.n(1), 1);
        double cdelt = header.getDoubleValue(Standard.CDELTn.n(1), 1);
        double crval = header.getDoubleValue(Standard.CRVALn.n(1), 0);

        return ArrayUtils.fillArray(size, (1 - crpix) * cdelt + crval, cdelt);
    }

    protected XYSeries getBothSeriesFromData(Object data, int bitPix) throws SpefoException {
        double[] xSeries = getSeriesFromData(Array.get(data, 0), bitPix);
        double[] ySeries = getSeriesFromData(Array.get(data, 1), bitPix);

        return new XYSeries(xSeries, ySeries);
    }

    protected JulianDate getHJD(Header header) {
        return FitsUtils.getHJD(header);
    }

    protected LocalDateTime getDateOfObservation(Header header) {
        return FitsUtils.getDateOfObservation(header);
    }

    protected double getRVCorrection(Header header) {
        return FitsUtils.getRVCorrection(header);
    }

    protected double getExpTime(Header header) {
        return FitsUtils.getExpTime(header);
    }
}
