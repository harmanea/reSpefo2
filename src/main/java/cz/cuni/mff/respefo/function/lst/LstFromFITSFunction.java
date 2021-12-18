package cz.cuni.mff.respefo.function.lst;

import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.filter.FitsFileFilter;
import cz.cuni.mff.respefo.util.collections.FitsFile;
import cz.cuni.mff.respefo.util.collections.JulianDate;
import cz.cuni.mff.respefo.util.utils.FitsUtils;

import java.io.File;
import java.time.LocalDateTime;

@Fun(name = "Generate .lst File", fileFilter = FitsFileFilter.class)
public class LstFromFITSFunction extends AbstractLstFunction<FitsFile> {

    @Override
    protected FitsFile openFile(File file) throws SpefoException {
        return new FitsFile(file, true, false);
    }

    @Override
    protected LocalDateTime getDateOfObservation(FitsFile fits) {
        return FitsUtils.getDateOfObservation(fits.getHeader());
    }

    @Override
    protected File getFile(FitsFile fits) {
        return fits.getFile();
    }

    @Override
    protected double getExpTime(FitsFile fits) {
        return FitsUtils.getExpTime(fits.getHeader());
    }

    @Override
    protected JulianDate getHJD(FitsFile fits) {
        return FitsUtils.getHJD(fits.getHeader());
    }

    @Override
    protected double getRvCorrection(FitsFile fits) {
        return FitsUtils.getRVCorrection(fits.getHeader());
    }
}
