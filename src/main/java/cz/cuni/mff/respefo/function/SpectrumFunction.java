package cz.cuni.mff.respefo.function;

import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.util.Message;

import java.io.File;

public abstract class SpectrumFunction implements SingleFileFunction {
    @Override
    public void execute(File file) {
        try {
            Spectrum spectrum = Spectrum.open(file);
            execute(spectrum);
        } catch (SpefoException e) {
            Message.error("Couldn't open file", e);
        }
    }

    protected abstract void execute(Spectrum spectrum);
}
