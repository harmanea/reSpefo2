package cz.cuni.mff.respefo.function.scan;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.SingleOrMultiFileFunction;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.Progress;
import cz.cuni.mff.respefo.util.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Fun(name = "Delete")
public class DeleteFunction implements SingleOrMultiFileFunction {
    @Override
    public void execute(List<File> files) {
        if (!Message.question("Are you sure you want to delete the following files?\n\n" + filesListToString(files))) {
            return;
        }

        Progress.withProgressTracking(progress -> {
            progress.refresh("Deleting files", files.size() - 1);

            List<File> failedFiles = new ArrayList<>();
            for (File file : files) {
                try {
                    deleteFile(file);
                } catch (IOException exception) {
                    progress.asyncExec(() -> Log.error("Couldn't delete file [" + file.getPath() + "]", exception));
                    failedFiles.add(FileUtils.getRelativePath(file).toFile());
                } finally {
                    progress.step();
                }
            }
            return failedFiles;

        }, failedFiles -> {
            if (!failedFiles.isEmpty()) {
                Message.warning("Some files couldn't be deleted:\n\n" + filesListToString(failedFiles) + "\n\nSee log for more details.");
            } else {
                Message.info("Files deleted successfully");
            }

            ComponentManager.getFileExplorer().refresh();
        });
    }

    private String filesListToString(List<File> files) {
        return (files.size() > 5 ? files.subList(0, 5)  : files)
                .stream().map(file -> FileUtils.getRelativePath(file).toString()).collect(Collectors.joining("\n"))
                + (files.size() > 5 ? "\n\nand " + (files.size() - 5) + " more"  : "");
    }

    @Override
    public void execute(File file) {
        if (!Message.question("Are you sure you want to delete following file?\n\n" + FileUtils.getRelativePath(file).toString())) {
            return;
        }

        try {
            deleteFile(file);

        } catch (IOException exception) {
            Message.errorWithDetails("Couldn't delete file", exception);
            return;
        }

        Message.info("File deleted successfully");

        ComponentManager.getFileExplorer().refresh();
    }

    private void deleteFile(File file) throws IOException {
        if (file.isDirectory()) {
            Files.walkFileTree(file.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });

        } else {
            Files.delete(file.toPath());
        }
    }
}
