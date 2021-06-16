package cz.cuni.mff.respefo.util;

import cz.cuni.mff.respefo.dialog.OverwriteDialog;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.util.utils.FileUtils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static cz.cuni.mff.respefo.dialog.OverwriteDialog.*;
import static org.eclipse.swt.SWT.CANCEL;

public class FileCopy {

    private final Path targetDir;

    private boolean cancel;
    private boolean applyToAll;

    private boolean replaceAll;
    private boolean mergeAll;
    private boolean skipAll;

    /**
     * @return a list of failed filenames
     */
    public static List<String> copyTo(Path targetDir, String ... fileNames) {
        return new FileCopy(targetDir).copy(fileNames);
    }

    private FileCopy(Path targetDir) {
        this.targetDir = targetDir;
    }

    private List<String> copy(String ... fileNames) {
        cancel = false;
        applyToAll = false;

        replaceAll = false;
        mergeAll = false;
        skipAll = false;

        List<String> failedFiles = new ArrayList<>();
        for (String fileName : fileNames){
            try {
                Path source = Paths.get(fileName);

                if (Files.isDirectory(source)) {
                    copyDirectory(source, targetDir.resolve(source.getFileName()));
                } else {
                    copyFile(source, targetDir.resolve(source.getFileName()));
                }
                if (cancel) {
                    break;
                }

            } catch (IOException exception) {
                failedFiles.add(fileName);
                Log.error("An error occurred while copying file " + fileName, exception);
            }
        }

        return failedFiles;
    }

    private FileVisitResult copyDirectory(Path sourceDirectory, Path targetDirectory, CopyOption... options) throws IOException {
        Files.walkFileTree(sourceDirectory, EnumSet.noneOf(FileVisitOption.class), 1, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (targetDirectory.toFile().exists()) {
                    if (applyToAll) {
                        if (!targetDirectory.toFile().isDirectory() && replaceAll) {
                            FileUtils.deleteFile(targetDirectory.toFile());
                            Files.copy(dir, targetDirectory, options);
                            return FileVisitResult.CONTINUE;

                        } else if (targetDirectory.toFile().isDirectory() && mergeAll) {
                            return FileVisitResult.CONTINUE;

                        } else if (skipAll) {
                            return FileVisitResult.TERMINATE;
                        }
                    }

                    OverwriteDialog dialog = new OverwriteDialog(targetDirectory.toFile(), sourceDirectory.toFile());
                    int result = dialog.open();
                    if (result == MERGE) {
                        if (dialog.applyToAll()) {
                            applyToAll = true;
                            mergeAll = true;
                        }
                        return FileVisitResult.CONTINUE;

                    } else if (result == REPLACE) {
                        if (dialog.applyToAll()) {
                            applyToAll = true;
                            replaceAll = true;
                        }
                        FileUtils.deleteFile(targetDirectory.toFile());

                    } else if (result == RENAME) {
                        copyDirectory(sourceDirectory, targetDirectory.resolveSibling(dialog.getNewName()), options);
                        return FileVisitResult.TERMINATE;

                    } else if (result == CANCEL) {
                        cancel = true;
                        return FileVisitResult.TERMINATE;

                    } else /* (result == SKIP) */ {
                        if (dialog.applyToAll()) {
                            applyToAll = true;
                            skipAll = true;
                        }
                        return FileVisitResult.TERMINATE;
                    }

                }

                Files.copy(dir, targetDirectory, options);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toFile().isDirectory()) {
                    return copyDirectory(file, targetDirectory.resolve(sourceDirectory.relativize(file)), options);
                } else {
                    return copyFile(file, targetDirectory.resolve(sourceDirectory.relativize(file)), options);
                }
            }
        });

        if (cancel) {
            return FileVisitResult.TERMINATE;
        } else {
            return FileVisitResult.CONTINUE;
        }
    }

    private FileVisitResult copyFile(Path sourceFile, Path targetFile, CopyOption ... options) throws IOException {
        try {
            Files.copy(sourceFile, targetFile, options);
            return FileVisitResult.CONTINUE;

        } catch (FileAlreadyExistsException alreadyExistsException) {
            if (applyToAll) {
                if (replaceAll) {
                    return copyWithReplace(sourceFile, targetFile, options);
                } else if (skipAll) {
                    return FileVisitResult.CONTINUE;
                }
            }

            OverwriteDialog dialog = new OverwriteDialog(targetFile.toFile(), sourceFile.toFile());
            int result = dialog.open();
            if (result == REPLACE) {
                if (dialog.applyToAll()) {
                    applyToAll = true;
                    replaceAll = true;
                }
                return copyWithReplace(sourceFile, targetFile, options);

            } else if (result == RENAME) {
                return copyFile(sourceFile, targetFile.resolveSibling(dialog.getNewName()), options);

            } else if (result == CANCEL) {
                cancel = true;
                return FileVisitResult.TERMINATE;

            } else /* (result == SKIP) */ {
                if (dialog.applyToAll()) {
                    applyToAll = true;
                    skipAll = true;
                }
                return FileVisitResult.CONTINUE;
            }
        }
    }

    private FileVisitResult copyWithReplace(Path source, Path target, CopyOption ... options) throws IOException {
        if (target.toFile().isDirectory()) {
            FileUtils.deleteFile(target.toFile());
            return copyFile(source, target, options);
        }

        CopyOption[] optionsWithReplace = Arrays.copyOf(options, options.length + 1);
        optionsWithReplace[options.length] = StandardCopyOption.REPLACE_EXISTING;
        return copyFile(source, target, optionsWithReplace);
    }
}
