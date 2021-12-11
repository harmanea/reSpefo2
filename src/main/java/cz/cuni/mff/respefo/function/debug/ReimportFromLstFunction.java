package cz.cuni.mff.respefo.function.debug;

import cz.cuni.mff.respefo.component.Project;
import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.MultiFileFunction;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.function.lst.LstFile;
import cz.cuni.mff.respefo.function.port.ImportFunction;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.spectrum.origin.BaseOrigin;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.Progress;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

@Fun(name = "Reimport from .lst", fileFilter = SpefoFormatFileFilter.class, group = "Debug")
public class ReimportFromLstFunction implements SingleFileFunction, MultiFileFunction {

    @Override
    public void execute(File file) {
        update(lst -> {
            if (updateFile(lst, file)) {
                Message.info("Update finished successfully.");
            } else {
                Message.warning("An error occurred while updating file.");
            }
        });
    }

    @Override
    public void execute(List<File> files) {
        update(lst -> Progress.withProgressTracking(
                p -> {
                    p.refresh("Reimporting files", files.size());

                    boolean successful = true;
                    for (File file : files) {
                        successful &= updateFile(lst, file);
                        p.step();
                    }
                    return successful;
                }, successful -> {
                    if (Boolean.TRUE.equals(successful)) {
                        Message.info("Update finished successfully.");
                    } else {
                        Message.warning("Some errors occurred while updating files.");
                    }
                }
        ));
    }

    private static void update(Consumer<LstFile> updater) {
        File[] lstFiles = Project.getRootDirectory().listFiles((dir, name) -> name.endsWith(".lst"));
        if (lstFiles == null || lstFiles.length == 0) {
            Message.warning("No .lst file found in the project directory");
            return;
        }

        try {
            LstFile lst = new LstFile(lstFiles[0]);
            updater.accept(lst);

        } catch (SpefoException exception) {
            Message.error("Couldn't load .lst file", exception);
        }
    }

    private static boolean updateFile(LstFile lst, File file) {
        try {
            Spectrum spectrum = Spectrum.open(file);
            ImportFunction.updateSpectrumUsingLstFile(spectrum, lst, ((BaseOrigin) spectrum.getOrigin()).getFileName());
            spectrum.save();
            return true;

        } catch (SpefoException exception) {
            Log.error("An error occurred while updating file " + file.getPath(), exception);
            return false;
        }
    }
}
