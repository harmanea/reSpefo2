package cz.cuni.mff.respefo.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils extends UtilityClass {
    private static File userDirectory;

    public static String getFileExtension(File file) {
        return getFileExtension(file.getPath());
    }

    public static String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index >= 0) {
            return fileName.substring(index + 1);
        } else {
            return "";
        }
    }

    public static String stripFileExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index >= 0) {
            return fileName.substring(0, index);
        } else {
            return fileName;
        }
    }

    public static String getParentDirectory(String fileName) {
        Path parent = Paths.get(fileName).getParent();
        if (parent != null) {
            return parent.toString();
        } else {
            return "";
        }
    }

    public static String stripParent(String fileName) {
        return Paths.get(fileName).getFileName().toString();
    }

    public static File getUserDirectory() {
        if (userDirectory == null) {
            userDirectory = new File(System.getProperty("user.dir"));
        }

        return userDirectory;
    }

    protected FileUtils() throws IllegalAccessException {
        super();
    }
}
