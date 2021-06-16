package cz.cuni.mff.respefo.format.formats;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.format.Spectrum;

public interface ImportFileFormat extends FileFormat {
    Spectrum importFrom(String fileName) throws SpefoException;
}
