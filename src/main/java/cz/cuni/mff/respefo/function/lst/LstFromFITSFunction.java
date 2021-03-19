package cz.cuni.mff.respefo.function.lst;

import cz.cuni.mff.respefo.format.InvalidFileFormatException;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.filter.FitsFileFilter;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.util.collections.JulianDate;
import cz.cuni.mff.respefo.util.utils.FitsUtils;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.Header;
import nom.tam.fits.ImageHDU;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;

@Fun(name = "Generate .lst File", fileFilter = FitsFileFilter.class)
public class LstFromFITSFunction extends AbstractLstFunction<LstFromFITSFunction.FileAndHeader> {

    @Override
    protected FileAndHeader openFile(File file) throws Exception {
        try (Fits f = new Fits(file)) {
            BasicHDU<?>[] hdus = f.read();

            if (hdus.length == 0) {
                throw new InvalidFileFormatException("There are no HDUs in the file");
            } else if (hdus.length > 1) {
                Log.warning("There are more than one HDUs in the file. The first ImageHDU will be chosen.");
            }

            ImageHDU imageHdu = (ImageHDU) Arrays.stream(hdus).filter(hdu -> hdu instanceof ImageHDU).findFirst()
                    .orElseThrow(() -> new InvalidFileFormatException("No ImageHDU in the FITS file"));
            return new FileAndHeader(file, imageHdu.getHeader());
        }
    }

    @Override
    protected LocalDateTime getDateOfObservation(FileAndHeader fileAndHeader) {
        return FitsUtils.getDateOfObservation(fileAndHeader.header);
    }

    @Override
    protected File getFile(FileAndHeader fileAndHeader) {
        return fileAndHeader.file;
    }

    @Override
    protected double getExpTime(FileAndHeader fileAndHeader) {
        return FitsUtils.getExpTime(fileAndHeader.header);
    }

    @Override
    protected JulianDate getHJD(FileAndHeader fileAndHeader) {
        return FitsUtils.getHJD(fileAndHeader.header);
    }

    @Override
    protected double getRvCorrection(FileAndHeader fileAndHeader) {
        return FitsUtils.getRVCorrection(fileAndHeader.header);
    }

    static class FileAndHeader {
        private final File file;
        private final Header header;

        public FileAndHeader(File file, Header header) {
            this.file = file;
            this.header = header;
        }
    }
}
