package cz.cuni.mff.respefo.util.utils;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.format.InvalidFileFormatException;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.util.UtilityClass;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.ImageHDU;

import java.io.IOException;
import java.util.Arrays;

public class FitsUtils extends UtilityClass {

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

    protected FitsUtils() throws IllegalAccessException {
        super();
    }
}
