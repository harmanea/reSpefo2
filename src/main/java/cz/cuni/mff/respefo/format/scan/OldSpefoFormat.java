package cz.cuni.mff.respefo.format.scan;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.format.FileFormat;
import cz.cuni.mff.respefo.format.SpectrumFile;
import cz.cuni.mff.respefo.util.NotYetImplementedException;

public class OldSpefoFormat implements FileFormat {
    @Override
    public String[] fileExtensions() {
        return new String[]{"uui", "rui", "rci", "rfi"};
    }

    @Override
    public SpectrumFile importFrom(String fileName) throws SpefoException {
        throw new NotYetImplementedException();
    }

    @Override
    public void exportTo(SpectrumFile spectrumFile, String fileName) throws SpefoException {
        throw new NotYetImplementedException();
    }
}
