package cz.cuni.mff.respefo.function.rename;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.component.FileExplorer;
import cz.cuni.mff.respefo.component.Project;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.ProjectFunction;
import cz.cuni.mff.respefo.function.filter.FitsFileFilter;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.collections.FitsFile;
import cz.cuni.mff.respefo.util.utils.FileUtils;
import cz.cuni.mff.respefo.util.utils.FitsUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static cz.cuni.mff.respefo.function.lst.LstFile.DATE_TIME_FORMATTER;
import static cz.cuni.mff.respefo.util.utils.FileUtils.hasExtension;
import static cz.cuni.mff.respefo.util.utils.FormattingUtils.formatInteger;
import static java.util.stream.Collectors.toList;

@Fun(name = "Prepare")
public class PrepareProjectFunction implements ProjectFunction {

    @Override
    public void execute(List<File> files) {
        List<File> lstFiles = files.stream().filter(hasExtension("lst")).sorted().collect(toList());

        String suggestedPrefix = Project.getRootDirectory().getName();
        boolean useLst = !lstFiles.isEmpty();
        String lstFileName = lstFiles.isEmpty() ? "" : lstFiles.get(0).getName();
        PrepareProjectDialog dialog = new PrepareProjectDialog(suggestedPrefix, useLst, lstFileName);
        if (dialog.openIsNotOk()) {
            return;
        }

        String prefix = dialog.getPrefix();

        List<FitsFile> fitsFiles = files.stream()
                .filter(new FitsFileFilter()::accept)
                .map(PrepareProjectFunction::openFile)
                .filter(Objects::nonNull)
                .sorted(PrepareProjectFunction::compareDates)
                .collect(toList());

        if (useLst) {
            try {
                Path lstFile = Paths.get(dialog.getLstFileName());
                Files.move(lstFile, lstFile.resolveSibling(prefix + ".lst")).toFile();
            } catch (IOException exception) {
                Log.error("Couldn't rename .lst file", exception);
            }

        } else {
            try (PrintWriter writer = new PrintWriter(Project.getRootFileName("hec2"))) {
                for (int i = 0; i < fitsFiles.size(); i++) {
                    FitsFile fits = fitsFiles.get(i);

                    writer.println(String.join(" ",
                            formatInteger(i + 1, 5),
                            FitsUtils.getDateOfObservation(fits.getHeader()).format(DATE_TIME_FORMATTER),
                            Double.toString(FitsUtils.getExpTime(fits.getHeader()))));
                }

            } catch (IOException exception) {
                Message.error("Couldn't generate hec2 input file", exception);
            }
        }

        for (int i = 0; i < fitsFiles.size(); i++) {
            Path path = fitsFiles.get(i).getFile().toPath();

            try {
                Files.move(path, path.resolveSibling(prefix + String.format("%05d", i + 1) + FileUtils.getFileExtension(path.toFile())));
            } catch (IOException exception) {
                Log.error("Couldn't rename file [" + path.toFile().getName() + "]", exception);
            }
        }

        FileExplorer.getDefault().refresh();
        Message.info("Project prepared successfully");
    }

    private static FitsFile openFile(File file) {
        try {
            return new FitsFile(file, true, false);

        } catch (SpefoException exception) {
            Log.error("Couldn't load file [" + file.toPath() + "]", exception);
            return null;
        }
    }

    private static int compareDates(FitsFile a, FitsFile b) {
        return FitsUtils.getDateOfObservation(a.getHeader())
                .compareTo(FitsUtils.getDateOfObservation(b.getHeader()));
    }
}
