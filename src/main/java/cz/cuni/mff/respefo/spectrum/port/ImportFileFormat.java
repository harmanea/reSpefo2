package cz.cuni.mff.respefo.spectrum.port;

import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.spectrum.Spectrum;

public interface ImportFileFormat extends FileFormat {
    Spectrum importFrom(String fileName) throws SpefoException;
}
