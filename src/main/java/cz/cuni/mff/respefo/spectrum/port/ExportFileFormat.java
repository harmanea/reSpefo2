package cz.cuni.mff.respefo.spectrum.port;

import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.spectrum.Spectrum;

public interface ExportFileFormat extends FileFormat {
    void exportTo(Spectrum spectrum, String fileName) throws SpefoException;
}
