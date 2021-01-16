package cz.cuni.mff.respefo.function.scan;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.format.Spectrum;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.util.JulianDate;
import cz.cuni.mff.respefo.util.Message;

import java.io.File;
import java.time.LocalDateTime;

public class RepairFunction implements SingleFileFunction {
    @Override
    public void execute(File file) {
        try {
            Spectrum spectrum = Spectrum.open(file);

            if (spectrum.getDateOfObservation() == null) {
                spectrum.setDateOfObservation(LocalDateTime.MIN);
                Log.info("Replaced null date of observation");
            }

            if (spectrum.getHjd() == null) {
                spectrum.setHjd(new JulianDate());
                Log.info("Replaced null HJD");
            }

            spectrum.save();
            Message.info("Spectrum repaired successfully");

        } catch (SpefoException e) {
            Message.error("An error occurred", e);
        }
    }
}
