package cz.cuni.mff.respefo.function.lst;

import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.util.collections.JulianDate;

import java.io.File;
import java.time.LocalDateTime;

@Fun(name = "Generate .lst File", fileFilter = SpefoFormatFileFilter.class)
public class LstFromSpfFunction extends AbstractLstFunction<Spectrum> {

    @Override
    protected Spectrum openFile(File file) throws SpefoException {
        return Spectrum.open(file);
    }

    @Override
    protected LocalDateTime getDateOfObservation(Spectrum spectrum) {
        return spectrum.getDateOfObservation();
    }

    @Override
    protected File getFile(Spectrum spectrum) {
        return spectrum.getFile();
    }

    @Override
    protected double getExpTime(Spectrum spectrum) {
        return spectrum.getExpTime();
    }

    @Override
    protected JulianDate getHJD(Spectrum spectrum) {
        return spectrum.getHjd();
    }

    @Override
    protected double getRvCorrection(Spectrum spectrum) {
        return spectrum.getRvCorrection();
    }
}
