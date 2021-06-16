package cz.cuni.mff.respefo.function.prepare;

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
import org.eclipse.swt.SWT;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static cz.cuni.mff.respefo.function.lst.LstFile.DATE_TIME_FORMATTER;
import static cz.cuni.mff.respefo.function.lst.LstFile.TABLE_HEADER;
import static cz.cuni.mff.respefo.util.utils.FileUtils.hasExtension;
import static cz.cuni.mff.respefo.util.utils.FormattingUtils.formatDouble;
import static cz.cuni.mff.respefo.util.utils.FormattingUtils.formatInteger;
import static java.util.stream.Collectors.toList;

@Fun(name = "Prepare")
public class PrepareProjectFunction implements ProjectFunction {

    @Override
    public void execute(List<File> files) {
        List<File> lstFiles = files.stream().filter(hasExtension("lst")).sorted().collect(toList());

        String suggestedPrefix = Project.getRootDirectory().getName();
        boolean suggestedUseLst = !lstFiles.isEmpty();
        String lstFileName = lstFiles.isEmpty() ? "" : lstFiles.get(0).getName();
        PrepareProjectDialog dialog = new PrepareProjectDialog(suggestedPrefix, suggestedUseLst, lstFileName);
        int status = dialog.open();
        if (status == SWT.CANCEL) {
            return;
        }

        String prefix = dialog.getPrefix();

        List<FitsFile> fitsFiles = files.stream()
                .filter(new FitsFileFilter()::accept)
                .map(PrepareProjectFunction::openFile)
                .filter(Objects::nonNull)
                .sorted(PrepareProjectFunction::compareDates)
                .collect(toList());

        switch (status) {
            case PrepareProjectDialog.USE_LST: {
                /* Use existing lst file */
                try {
                    Path lstFile = Paths.get(Project.getRootDirectory().getPath(), dialog.getLstFileName());
                    Files.move(lstFile, lstFile.resolveSibling(prefix + ".lst")).toFile();
                } catch (IOException exception) {
                    Message.error("Couldn't rename .lst file", exception);
                }
            }
            break;
            case PrepareProjectDialog.HEC2: {
                /* Generate input file for hec2 */
                File hec2File = Project.getRootDirectory().toPath().resolve("hec2." + prefix).toFile();
                try (PrintWriter writer = new PrintWriter(hec2File)) {
                    for (int i = 0; i < fitsFiles.size(); i++) {
                        FitsFile fits = fitsFiles.get(i);

                        writer.println(String.join(" ",
                                String.format("%05d", i + 1),
                                FitsUtils.getDateOfObservation(fits.getHeader()).format(DATE_TIME_FORMATTER),
                                Double.toString(FitsUtils.getExpTime(fits.getHeader()))));
                    }

                } catch (IOException exception) {
                    Message.error("Couldn't generate hec2 input file", exception);
                }
            }
            break;
            case PrepareProjectDialog.NEW_LST: {
                /* Generate new lst file */
                // TODO: Try to refactor this duplicate code
                String fileName = Project.getRootFileName(".lst");
                try (PrintWriter writer = new PrintWriter(fileName)) {
                    writer.print("\n\n\n\n"); // TODO: generate some relevant header
                    writer.print(TABLE_HEADER);

                    for (int i = 0; i < fitsFiles.size(); i++) {
                        FitsFile fits = fitsFiles.get(i);

                        writer.println(String.join(" ",
                                formatInteger(i + 1, 5),
                                FitsUtils.getDateOfObservation(fits.getHeader()).format(DATE_TIME_FORMATTER),
                                formatDouble(FitsUtils.getExpTime(fits.getHeader()), 5, 3, false),
                                fits.getFile().getName(),
                                formatDouble(FitsUtils.getHJD(fits.getHeader()).getRJD(), 5, 4),
                                formatDouble(FitsUtils.getRVCorrection(fits.getHeader()), 3, 2)));
                    }

                } catch (IOException exception) {
                    Message.error("Couldn't generate .lst file", exception);
                }
            }
            break;
        }

        /* Rename fits files */
        for (int i = 0; i < fitsFiles.size(); i++) {
            Path path = fitsFiles.get(i).getFile().toPath();

            try {
                String newFileName = prefix + String.format("%05d", i + 1) + "." + FileUtils.getFileExtension(path.toFile());
                Files.move(path, path.resolveSibling(newFileName));
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
