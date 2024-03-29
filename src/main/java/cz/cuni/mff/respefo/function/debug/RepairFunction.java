package cz.cuni.mff.respefo.function.debug;

import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.collections.JulianDate;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

import static cz.cuni.mff.respefo.util.utils.CollectionUtils.listOf;

@Fun(name = "Repair", fileFilter = SpefoFormatFileFilter.class, group = "Debug")
public class RepairFunction implements SingleFileFunction {

    private static final List<Predicate<Spectrum>> FIX_OPERATIONS = listOf(RepairFunction::fixNullDateOfObservation,
            RepairFunction::fixNullHJD, RepairFunction::fixNaNRvCorrection);

    @Override
    public void execute(File file) {
        try {
            Spectrum spectrum = Spectrum.open(file);

            boolean fixed = false;
            for (Predicate<Spectrum> fixOperation : FIX_OPERATIONS) {
                fixed |= fixOperation.test(spectrum);
            }

            spectrum.save();

            if (fixed) {
                Message.info("Spectrum repaired successfully.");
            } else {
                Message.info("No problems found with the spectrum.");
            }

        } catch (SpefoException exception) {
            Message.error("An error occurred while loading spectrum", exception);
        }
    }

    private static boolean fixNullDateOfObservation(Spectrum spectrum) {
        if (spectrum.getDateOfObservation() == null) {
            spectrum.setDateOfObservation(LocalDateTime.MIN);
            Log.info("Replaced null date of observation");
            return true;
        }
        return false;
    }

    private static boolean fixNullHJD(Spectrum spectrum) {
        if (spectrum.getHjd() == null) {
            spectrum.setHjd(new JulianDate());
            Log.info("Replaced null HJD");
            return true;
        }
        return false;
    }

    private static boolean fixNaNRvCorrection(Spectrum spectrum) {
        if (Double.isNaN(spectrum.getRvCorrection())) {
            spectrum.setRvCorrection(0);
            Log.info("Replaced NaN rv correction");
            return true;
        }
        return false;
    }
}
