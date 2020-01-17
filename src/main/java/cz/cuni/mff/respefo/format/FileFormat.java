package cz.cuni.mff.respefo.format;

import cz.cuni.mff.respefo.SpefoException;

public interface FileFormat {
    String[] fileExtensions();
    SpectrumFile importFrom(String fileName) throws SpefoException;
    void exportTo(SpectrumFile spectrumFile, String fileName) throws SpefoException;
}
