package cz.cuni.mff.respefo.function.project;

import cz.cuni.mff.respefo.component.FileExplorer;
import cz.cuni.mff.respefo.component.Project;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.ProjectFunction;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.spectrum.port.ascii.AsciiFormat;
import cz.cuni.mff.respefo.util.Message;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static cz.cuni.mff.respefo.util.utils.FileUtils.getFileExtension;
import static java.util.stream.Collectors.toList;

@Fun(name = "Rename Files")  // TODO: maybe indicate that only ASCII files will be renamed
public class RenameFilesFunction implements ProjectFunction {
    @Override
    public void execute(List<File> files) {
        String suggestedPrefix = Project.getRootDirectory().getName();
        ProjectPrefixDialog dialog = new ProjectPrefixDialog("Rename project files", suggestedPrefix);
        if (dialog.openIsNotOk()) {
            return;
        }
        String prefix = dialog.getPrefix();

        List<File> asciiFiles = files.stream()
                .filter(file -> !file.isDirectory())
                .filter(file -> AsciiFormat.FILE_EXTENSIONS.contains(getFileExtension(file)))
                .sorted().collect(toList());

        for (int i = 0; i < asciiFiles.size(); i++) {
            Path path = asciiFiles.get(i).toPath();

            try {
                String newFileName = prefix + String.format("%05d", i + 1);

                String fileExtension = getFileExtension(path);
                if (!fileExtension.isEmpty()) {
                    newFileName = newFileName + "." + fileExtension;
                }

                Files.move(path, path.resolveSibling(newFileName));
            } catch (IOException exception) {
                Log.error("Couldn't rename file [" + path.getFileName().toString() + "]", exception);
            }
        }

        FileExplorer.getDefault().refresh();
        Message.info("Files renamed successfully");
    }
}
