package cz.cuni.mff.respefo.function.scan;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.component.Project;
import cz.cuni.mff.respefo.format.Spectrum;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.ProjectFunction;
import cz.cuni.mff.respefo.function.asset.rename.MultipleLstFilesDialog;
import cz.cuni.mff.respefo.function.asset.rename.ProjectPrefixDialog;
import cz.cuni.mff.respefo.function.filter.SpefoAndLstFileFilter;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.util.Message;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

@Fun(name = "Rename", fileFilter = SpefoAndLstFileFilter.class)
public class RenameProjectFunction implements ProjectFunction {

    @Override
    public void execute(List<File> files) {
        ProjectPrefixDialog dialog = new ProjectPrefixDialog();
        if (dialog.openIsNotOk()) {
            return;
        }
        String prefix = dialog.getPrefix();

        List<File> lstFiles = files.stream().filter(endsWith("lst")).sorted().collect(toList());
        if (!lstFiles.isEmpty()) {
            File lstFile;

            if (lstFiles.size() > 1) {
                MultipleLstFilesDialog lstFilesDialog = new MultipleLstFilesDialog(lstFiles);
                if (lstFilesDialog.openIsNotOk()) {
                    return;
                }

                lstFile = lstFilesDialog.getSelectedFile();
            } else {
                lstFile = lstFiles.get(0);
            }

            try {
                lstFile = Files.move(lstFile.toPath(), lstFile.toPath().resolveSibling(prefix + ".lst")).toFile();
            } catch (IOException exception) {
                Log.error("Couldn't rename .lst file", exception);
            }

            // TODO: try and load the .lst file
        }

        List<Spectrum> spfFiles = files.stream()
                .filter(endsWith("spf"))
                .map(this::toSpectrum)
                .filter(Objects::nonNull)
                .sorted(Spectrum.hjdComparator())
                .collect(toList());

        for (int i = 0; i < spfFiles.size(); i++) {
            try {
                Path spectrumPath = spfFiles.get(i).getFile().toPath();
                Files.move(spectrumPath, spectrumPath.resolveSibling(prefix + String.format("%05d", i + 1) + ".spf"));

            } catch (IOException exception) {
                Log.error("Couldn't rename file [" + spfFiles.get(i).getFile().getName() + "]", exception);
            }
        }

        Project.refresh();
        Message.info("Project renamed successfully");
    }

    private static Predicate<File> endsWith(String suffix) {
        return file -> file.getName().endsWith(suffix);
    }

    private Spectrum toSpectrum(File file) {
        try {
            return Spectrum.open(file);
        } catch (SpefoException exception) {
            Log.error("Couldn't load spectrum [" + file.getName() + "]", exception);
            return null;
        }
    }
}
