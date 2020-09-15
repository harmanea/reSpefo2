package cz.cuni.mff.respefo.format.formats;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.format.Spectrum;

public interface ExportFileFormat extends FileFormat {
    void exportTo(Spectrum spectrum, String fileName) throws SpefoException;
}
