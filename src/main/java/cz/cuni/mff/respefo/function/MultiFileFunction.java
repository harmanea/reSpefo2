package cz.cuni.mff.respefo.function;

import java.io.File;

public interface MultiFileFunction {
    void execute(File spectrumFileA, File spectrumFileB, File ... spectrumFiles);
}
