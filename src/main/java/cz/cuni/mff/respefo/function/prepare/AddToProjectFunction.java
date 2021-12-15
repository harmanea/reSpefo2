package cz.cuni.mff.respefo.function.prepare;

import cz.cuni.mff.respefo.component.FileExplorer;
import cz.cuni.mff.respefo.component.Project;
import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.filter.FitsFileFilter;
import cz.cuni.mff.respefo.function.lst.LstFile;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.spectrum.port.fits.ImportFitsFormat;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.collections.FitsFile;
import cz.cuni.mff.respefo.util.utils.FileUtils;
import org.eclipse.swt.SWT;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static cz.cuni.mff.respefo.function.lst.LstFile.DATE_TIME_FORMATTER;
import static cz.cuni.mff.respefo.util.utils.FileUtils.hasExtension;
import static java.util.stream.Collectors.toList;

@Fun(name = "Add Files")
public class AddToProjectFunction extends PrepareProjectFunction {

    @Override
    public void execute(List<File> files) {
        List<File> lstFiles = files.stream().filter(hasExtension("lst")).collect(toList());

        ProjectDialog dialog = projectDialog(lstFiles, false);  // TODO: Can we suggest the prefix in a more clever way?
        int status = dialog.open();
        if (status == SWT.CANCEL) {
            return;
        }
        String prefix = dialog.getPrefix();

        ImportFitsFormat format = importFormat();
        if (format == null) {
            return;
        }

        // Files to be added
        // TODO: Make this editable
        List<FitsFile> fitsFiles = files.stream()
                .filter(new FitsFileFilter()::accept)
                .filter(AddToProjectFunction::nonStandardName)
                .map(AddToProjectFunction::openFile)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(f -> format.getDateOfObservation(f.getHeader())))
                .collect(toList());

        // Find the index of the last file in the project
        int lastFileIndex = files.stream()
                .map(File::getName)
                .filter(fileName -> fileName.startsWith(prefix))
                .filter(fileName -> fileName.endsWith(".spf"))
                .map(FileUtils::getFileIndex)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .max(Integer::compareTo) // TODO: What happens if the indices are not ascending with 1 increments?
                .orElse(0);

        switch (status) {
            case ProjectDialog.USE_LST: {
                /* Use existing lst file */
                try {
                    LstFile lstFile = new LstFile(new File(dialog.getLstFileName()));
                    for (int i = 0; i < fitsFiles.size(); i++) {
                        FitsFile fits = fitsFiles.get(i);
                        lstFile.addRecord(new LstFile.Record(lastFileIndex + i + 1,
                                format.getDateOfObservation(fits.getHeader()),
                                format.getExpTime(fits.getHeader()),
                                fits.getFile().getName(),
                                format.getHJD(fits.getHeader()),
                                format.getRVCorrection(fits.getHeader())));
                    }
                    lstFile.save();
                } catch (SpefoException exception) {
                    Message.error("Couldn't append to .lst file", exception);
                }
            }
            break;
            case ProjectDialog.HEC2: {
                /* Generate input file for hec2 */
                File hec2File = Project.getRootDirectory().toPath().resolve("hec2." + prefix).toFile();
                try (PrintWriter writer = new PrintWriter(hec2File)) {
                    for (int i = 0; i < fitsFiles.size(); i++) {
                        FitsFile fits = fitsFiles.get(i);
                        writer.println(String.join(" ",
                                String.format("%05d", lastFileIndex + i + 1),
                                format.getDateOfObservation(fits.getHeader()).format(DATE_TIME_FORMATTER),
                                Double.toString(format.getExpTime(fits.getHeader()))));
                    }

                } catch (IOException exception) {
                    Message.error("Couldn't generate hec2 input file", exception);
                }
            }
            break;
        }

        /* Rename fits files */
        for (int i = 0; i < fitsFiles.size(); i++) {
            Path path = fitsFiles.get(i).getFile().toPath();

            try {
                String newFileName = prefix + String.format("%05d", lastFileIndex + i + 1) + "." + FileUtils.getFileExtension(path.toFile());
                Files.move(path, path.resolveSibling(newFileName));
            } catch (IOException exception) {
                Log.error("Couldn't rename file [" + path.toFile().getName() + "]", exception);
            }
        }

        FileExplorer.getDefault().refresh();
        Message.info("Files added to project successfully");
    }

    private static boolean nonStandardName(File file) {
        return !FileUtils.getFileIndex(file.getName()).isPresent();
    }
}
