package cz.cuni.mff.respefo.function.scan;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.component.Project;
import cz.cuni.mff.respefo.format.Spectrum;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.MultiFileFunction;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.Progress;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static cz.cuni.mff.respefo.util.utils.FormattingUtils.formatDouble;
import static cz.cuni.mff.respefo.util.utils.FormattingUtils.formatInteger;

@Fun(name = "Generate .lst File", fileFilter = SpefoFormatFileFilter.class)
public class CreateListFunction implements MultiFileFunction {

    private static final String TABLE_HEADER =
            "==============================================================================\n" +
            "   N.  Date & UT start       exp[s]      Filename       J.D.hel.  RVcorr\n" +
            "==============================================================================\n";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy MM dd HH mm ss");

    @Override
    public void execute(List<File> files) {
        Progress.withProgressTracking(p -> {
            p.refresh("Opening files", files.size());

            List<Spectrum> spectra = new ArrayList<>();
            for (File file : files) {
                try {
                    Spectrum spectrum = Spectrum.open(file);
                    spectra.add(spectrum);

                } catch (SpefoException exception) {
                    Log.error("An error occurred while opening file " + file.getPath(), exception);
                } finally {
                    p.step();
                }
            }

            return spectra;
        }, spectra -> {
            String fileName = Project.getRootFileName(".lst");
            try (PrintWriter writer = new PrintWriter(fileName)) {
                writer.print("\n\n\n\n"); // TODO: generate some relevant header
                writer.print(TABLE_HEADER);

                for (int i = 0; i < spectra.size(); i++) {
                    Spectrum spectrum = spectra.get(i);

                    writer.println(String.join(" ",
                            formatInteger(i + 1, 5),
                            spectrum.getDateOfObservation().format(DATE_TIME_FORMATTER),
                            formatDouble(spectrum.getExpTime(), 5, 3, false),
                            spectrum.getFile().getName(),
                            formatDouble(spectrum.getHjd().getRJD(), 5, 4),
                            formatDouble(spectrum.getRvCorrection(), 3, 2)));
                }

                Message.info("File created successfully");
                Project.refresh();
            } catch (FileNotFoundException exception) {
                Message.error("Couldn't generate .lst file", exception);
            }
        });
    }
}
