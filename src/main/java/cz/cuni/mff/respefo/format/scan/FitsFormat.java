package cz.cuni.mff.respefo.format.scan;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.format.Data;
import cz.cuni.mff.respefo.format.FileFormat;
import cz.cuni.mff.respefo.format.InvalidFileFormatException;
import cz.cuni.mff.respefo.format.SpectrumFile;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.util.NotYetImplementedException;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;
import nom.tam.fits.*;
import nom.tam.fits.header.Standard;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.stream.IntStream;

public class FitsFormat implements FileFormat {

    public static final String[] FILE_EXTENSIONS = { "fits", "fts", "fit"};

    @Override
    public String[] fileExtensions() {
        return FILE_EXTENSIONS;
    }

    @Override
    public SpectrumFile importFrom(String fileName) throws SpefoException {
        Data spectrumData;

        try (Fits f = new Fits(fileName)) {
            BasicHDU<?>[] HDUs = f.read();

            if (HDUs.length == 0) {
                throw new InvalidFileFormatException("There are no HDUs in the file");
            } else if (HDUs.length > 1) {
                Log.warning("There are more than one HDUs in the file. The first ImageHDU will be chosen.");
            }

            ImageHDU imageHdu = (ImageHDU) Arrays.stream(HDUs).filter(hdu -> hdu instanceof ImageHDU).findFirst()
                    .orElseThrow(() -> new InvalidFileFormatException("No ImageHDU in the FITS file"));

            Object data = imageHdu.getKernel();
            if (data == null || !data.getClass().isArray()) {
                throw new InvalidFileFormatException("The HDU does not contain array data");
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
                    throw new InvalidFileFormatException("The 2-D data array is too long in the first dimension");
                }

                spectrumData = getBothSeriesFromData(data, imageHdu.getBitPix());
            } else {
                throw new InvalidFileFormatException("The data array is " + nDims + "-dimensional");
            }

            double bZero = header.getDoubleValue(Standard.BZERO, 0);
            double bScale = header.getDoubleValue(Standard.BSCALE, 1);

            if (bZero != 0 || bScale != 1) {
                spectrumData.setY(ArrayUtils.applyBScale(spectrumData.getY(), bZero, bScale));
            }

            // TODO: parse header information
        } catch (IOException | FitsException exception) {
            throw new SpefoException("Error while reading file", exception);
        }

        return new SpectrumFile(spectrumData);
    }

    @Override
    public void exportTo(SpectrumFile spectrumFile, String fileName) throws SpefoException {
        throw new NotYetImplementedException();
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
        double CRPIX = header.getDoubleValue("CRPIX1", 1);
        double CDELT = header.getDoubleValue("CDELT1", 1);
        double CRVAL = header.getDoubleValue("CRVAL1", 0);

        return ArrayUtils.fillArray(size, (1 - CRPIX) * CDELT + CRVAL, CDELT);
    }

    private Data getBothSeriesFromData(Object data, int bitPix) throws SpefoException {
        double[] xSeries = getSeriesFromData(Array.get(data, 0), bitPix);
        double[] ySeries = getSeriesFromData(Array.get(data, 1), bitPix);

        return new Data(xSeries, ySeries);
    }
}
