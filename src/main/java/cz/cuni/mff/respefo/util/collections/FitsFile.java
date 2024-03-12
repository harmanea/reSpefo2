package cz.cuni.mff.respefo.util.collections;

import cz.cuni.mff.respefo.exception.InvalidFileFormatException;
import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.logging.Log;
import nom.tam.fits.*;
import nom.tam.fits.header.Bitpix;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class FitsFile {
    private final File file;
    private final Header header;
    private final Object data;
    private final Bitpix bitpix;

    public FitsFile(File file) throws SpefoException {
        this(file, true, true);
    }

    public FitsFile(File file, boolean loadHeader, boolean loadData) throws SpefoException {
        this.file = file;

        try (Fits f = new Fits(file)) {
            BasicHDU<?>[] hdus = f.read();

            if (hdus.length == 0) {
                throw new InvalidFileFormatException("There are no HDUs in the file");
            } else if (hdus.length > 1) {
                Log.warning("There are more than one HDUs in the file. The first ImageHDU will be chosen.");
            }

            ImageHDU imageHdu = (ImageHDU) Arrays.stream(hdus)
                    .filter(ImageHDU.class::isInstance)
                    .findFirst()
                    .orElseThrow(() -> new InvalidFileFormatException("No ImageHDU in the FITS file"));

            header = loadHeader ? imageHdu.getHeader() : null;

            data = loadData ? imageHdu.getKernel() : null;
            bitpix = loadData ? imageHdu.getBitpix() : null;

        } catch (IOException | FitsException exception) {
            throw new SpefoException("Error while reading file", exception);
        }
    }

    public File getFile() {
        return file;
    }

    public Header getHeader() {
        return header;
    }

    public Object getData() {
        return data;
    }

    public Bitpix getBitpix() {
        return bitpix;
    }
}
