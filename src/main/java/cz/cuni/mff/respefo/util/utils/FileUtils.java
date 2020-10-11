package cz.cuni.mff.respefo.util.utils;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.util.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

public class FileUtils extends UtilityClass {
    private static final File USER_DIRECTORY;
    static {
        USER_DIRECTORY = new File(System.getProperty("user.dir"));
        FileDialogs.setFilterPath(USER_DIRECTORY.getPath());
    }

    /**
     * @return the directory the application was launched from
     */
    public static File getUserDirectory() {
        return USER_DIRECTORY;
    }

    /**
     * @param file whose extension should be extracted
     * @return the file extension or an empty string if it has none
     */
    public static String getFileExtension(File file) {
        Objects.requireNonNull(file);
        return getFileExtension(file.getPath());
    }

    /**
     * @param fileName of the file whose extension should be extracted
     * @return the file extension or an empty string if it has none
     */
    public static String getFileExtension(String fileName) {
        Objects.requireNonNull(fileName);

        int index = fileName.lastIndexOf('.');
        if (index >= 0) {
            return fileName.substring(index + 1);
        } else {
            return "";
        }
    }

    /**
     * @param fileName whose extension should be stripped
     * @return the file name with it's extension stripped or the original string if it has none
     */
    public static String stripFileExtension(String fileName) {
        Objects.requireNonNull(fileName);

        int index = fileName.lastIndexOf('.');
        if (index >= 0) {
            return fileName.substring(0, index);
        } else {
            return fileName;
        }
    }

    /**
     * @param fileName whose extension should be replaced
     * @param newExtension to replace the current extension
     * @return the file name with it's extension replaced or appended if it has none
     */
    public static String replaceFileExtension(String fileName, String newExtension) {
        Objects.requireNonNull(fileName);
        Objects.requireNonNull(newExtension);

        return stripFileExtension(fileName) + "." + newExtension;
    }

    /**
     * @param fileName whose parent directory should be extracted
     * @return the parent directory or an empty string if it has none
     */
    public static String getParentDirectory(String fileName) {
        Objects.requireNonNull(fileName);

        Path parent = Paths.get(fileName).getParent();
        if (parent != null) {
            return parent.toString();
        } else {
            return "";
        }
    }

    /**
     * @param fileName whose parent directory should be stripped
     * @return the file name with it's parent stripped
     */
    public static String stripParent(String fileName) {
        Objects.requireNonNull(fileName);
        return Paths.get(fileName).getFileName().toString();
    }

    /**
     * @param file whose path should be relativized
     * @return path relative to the currently opened project
     */
    public static Path getRelativePath(File file) {
        Objects.requireNonNull(file);
        return getRelativePath(file.toPath());
    }

    /**
     * @param path that should be relativized
     * @return path relative to the currently opened project
     */
    public static Path getRelativePath(Path path) {
        Objects.requireNonNull(path);
        return ComponentManager.getFileExplorer().getRootDirectory().toPath().relativize(path);
    }

    /**
     * Deletes a file.
     * If it is a directory, it's contents will be recursively deleted first.
     * @param file to be deleted
     * @throws IOException if an I/O error occurs
     */
    public static void deleteFile(File file) throws IOException {
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

    protected FileUtils() throws IllegalAccessException {
        super();
    }
}
