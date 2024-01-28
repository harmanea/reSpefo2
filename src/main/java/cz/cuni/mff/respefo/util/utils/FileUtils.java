package cz.cuni.mff.respefo.util.utils;

import cz.cuni.mff.respefo.component.Project;
import cz.cuni.mff.respefo.util.FileDialogs;
import cz.cuni.mff.respefo.util.UtilityClass;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

// TODO: Move away from File, use Path instead
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
        return getFileExtension(file.getName());
    }

    /**
     * @param path whose extension should be extracted
     * @return the file extension or an empty string if it has none
     */
    public static String getFileExtension(Path path) {
        Objects.requireNonNull(path);
        return getFileExtension(path.getFileName().toString());
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
     * @return the file name with its extension stripped or the original string if it has none
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
     * @return the file name with its extension replaced or appended if it has none
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
     * @return the file name with its parent stripped
     */
    public static String stripParent(String fileName) {
        Objects.requireNonNull(fileName);
        return Paths.get(fileName).getFileName().toString();
    }

    /**
     * @param file whose path should be turned relative
     * @return path relative to the currently opened project
     */
    public static Path getRelativePath(File file) {
        Objects.requireNonNull(file);
        return getRelativePath(file.toPath());
    }

    /**
     * @param path that should be turned relative
     * @return path relative to the currently opened project
     */
    public static Path getRelativePath(Path path) {
        Objects.requireNonNull(path);
        return Project.getRootDirectory().toPath().relativize(path);
    }

    /**
     * Deletes a file.
     * If it is a directory, it's contents will be recursively deleted first.
     * @param path to be deleted
     * @throws IOException if an I/O error occurs
     */
    public static void deleteFile(Path path) throws IOException {
        Objects.requireNonNull(path);
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
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
            Files.delete(path);
        }
    }

    /**
     * Transforms a list of filenames to a formatted string.
     * Outputs up to five filenames relative to the project root per line and, if there are any, the number of the remaining filenames.
     * @param fileNames list of filenames
     * @return formatted string
     */
    public static String fileNamesListToString(List<String> fileNames) {
        Objects.requireNonNull(fileNames);
        return filesListToString(fileNames.stream().map(File::new).collect(toList()));
    }

    /**
     * Transforms a list of files to a formatted string.
     * Outputs up to five filenames relative to the project root per line and, if there are any, the number of the remaining filenames.
     * @param files list of files
     * @return formatted string
     */
    public static String filesListToString(List<File> files) {
        Objects.requireNonNull(files);
        return (files.size() > 5 ? files.subList(0, 5)  : files)
                .stream().map(file -> FileUtils.getRelativePath(file).toString()).collect(Collectors.joining(System.lineSeparator()))
                + (files.size() > 5 ? "\n\nand " + (files.size() - 5) + " more"  : "");
    }

    /**
     * Returns a predicate that returns true if a given file has the specified extension and false otherwise.
     * This method is meant to be used as a Stream.filter argument.
     * @param extension to check files for
     * @return a File predicate
     */
    public static Predicate<File> hasExtension(String extension) {
        return file -> getFileExtension(file).equals(extension);
    }

    /**
     * Parse the file name to obtain the file index as specified by the naming convention.
     * Returns <dd>Optional.empty()</dd> if the file name does not conform to the naming convention.
     * <p>
     * The file naming convention dictates that the name should start with a (preferably 3-letter) prefix followed
     * by a 5 digit index with leading zeros if necessary. Finally, it should end with the corresponding file extension.
     * <p>
     * Examples: <dd>abc00001.spf</dd>, <dd>v2a13245.fit</dd>, <dd>foobar00007.txt</dd>
     * @param fileName to parse
     * @return <dd>Optional</dd> instance containing the extracted index or <dd>Optional.empty()</dd> if it couldn't
     * be extracted
     */
    public static Optional<Integer> getFileIndex(String fileName) {
        Objects.requireNonNull(fileName);
        try {
            String strippedFileName = stripFileExtension(fileName);
            int index = Integer.parseInt(strippedFileName.substring(strippedFileName.length() - 5));
            return Optional.of(index);

        } catch (NumberFormatException | IndexOutOfBoundsException exception) {
            // File name does not conform to the naming convention
            return Optional.empty();
        }
    }

    /**
     * Reads given resource file as a string.
     * <p>
     * Taken from a <a href="https://stackoverflow.com/a/46613809/">StackOverflow answer </a>.
     *
     * @param fileName path to the resource file
     * @return the file's contents
     * @throws IOException if read fails for any reason
     */
    public static String getResourceFileAsString(String fileName) throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(fileName)) {
            if (is == null) return null;
            try (InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader reader = new BufferedReader(isr)) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }
    }

    protected FileUtils() throws IllegalAccessException {
        super();
    }
}
