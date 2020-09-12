package cz.cuni.mff.respefo.util.utils;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.util.FileType;
import cz.cuni.mff.respefo.util.UtilityClass;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileUtils extends UtilityClass {
    private static final File USER_DIRECTORY;
    private static String filterPath;
    static {
        USER_DIRECTORY = new File(System.getProperty("user.dir"));
        filterPath = USER_DIRECTORY.getPath();
    }

    public static File getUserDirectory() {
        return USER_DIRECTORY;
    }

    public static String getFilterPath() {
        return filterPath;
    }

    public static void setFilterPath(String filterPath) {
        FileUtils.filterPath = filterPath;
    }

    public static String openFileDialog(FileType fileType) {
        return openFileDialog(fileType, true);
    }

    public static String openFileDialog(FileType fileType, boolean saveFilterPath) {
        return fileDialog(fileType, saveFilterPath, SWT.OPEN, null);
    }

    public static String saveFileDialog(FileType fileType, String fileName) {
        return saveFileDialog(fileType, fileName, true);
    }

    public static String saveFileDialog(FileType fileType, String fileName, boolean saveFilterPath) {
        return fileDialog(fileType, saveFilterPath, SWT.SAVE, fileName);
    }

    private static String fileDialog(FileType fileType, boolean saveFilterPath, int style, String fileName) {
        FileDialog dialog = new FileDialog(ComponentManager.getShell(), style);

        dialog.setText("Select file");
        dialog.setFilterNames(new String[] {fileType.getFilterNames(), "All Files"});
        dialog.setFilterExtensions(new String[] {fileType.getFilterExtensions(), "*"});

        if (fileName != null) {
            dialog.setFileName(stripParent(fileName));
            dialog.setFilterPath(getParentDirectory(fileName));
        } else {
            dialog.setFilterPath(getFilterPath());
        }

        String filePath = dialog.open();

        if (saveFilterPath && filePath != null && Paths.get(filePath).getParent() != null) {
            setFilterPath(Paths.get(filePath).getParent().toString());
        }

        return filePath;
    }

    public static String directoryDialog() {
        return directoryDialog(true);
    }

    public static String directoryDialog(boolean saveFilterPath) {
        DirectoryDialog dialog = new DirectoryDialog(ComponentManager.getShell());

        dialog.setText("Select directory");
        dialog.setFilterPath(getFilterPath());

        String directoryPath = dialog.open();

        if (saveFilterPath && directoryPath != null) {
            setFilterPath(Paths.get(directoryPath).toString());
        }

        return directoryPath;
    }

    public static List<String> openMultipleFilesDialog(FileType fileType) {
        FileDialog dialog = new FileDialog(ComponentManager.getShell(), SWT.OPEN | SWT.MULTI);

        dialog.setText("Select files");
        dialog.setFilterNames(new String[] {fileType.getFilterNames(), "All Files"});
        dialog.setFilterExtensions(new String[] {fileType.getFilterExtensions(), "*"});
        dialog.setFilterPath(getFilterPath());

        dialog.open();

        return Arrays.stream(dialog.getFileNames())
                .map(fileName -> dialog.getFilterPath() + File.separator + fileName)
                .collect(Collectors.toList());
    }

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

    public static String replaceFileExtension(String fileName, String newExtension) {
        return stripFileExtension(fileName) + "." + newExtension;
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

    public static Path getRelativePath(File file) {
        return getRelativePath(file.toPath());
    }

    public static Path getRelativePath(Path path) {
        return ComponentManager.getFileExplorer().getRootDirectory().toPath().relativize(path);
    }

    protected FileUtils() throws IllegalAccessException {
        super();
    }
}
