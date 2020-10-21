package cz.cuni.mff.respefo.util;

import cz.cuni.mff.respefo.component.ComponentManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static cz.cuni.mff.respefo.util.utils.FileUtils.getParentDirectory;
import static cz.cuni.mff.respefo.util.utils.FileUtils.stripParent;

// TODO: add javadoc
public class FileDialogs extends UtilityClass {

    private static String filterPath;

    public static String getFilterPath() {
        return filterPath;
    }

    public static void setFilterPath(String filterPath) {
        FileDialogs.filterPath = Objects.requireNonNull(filterPath);
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
        Objects.requireNonNull(fileType);

        FileDialog dialog = new FileDialog(ComponentManager.getShell(), style);

        dialog.setText("Select file");
        dialog.setFilterNames(new String[] {fileType.getFilterNames(), "All Files"});
        dialog.setFilterExtensions(new String[] {fileType.getFilterExtensions(), "*.*"});

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
        return openMultipleFilesDialog(fileType, true);
    }

    public static List<String> openMultipleFilesDialog(FileType fileType, boolean saveFilterPath) {
        Objects.requireNonNull(fileType);

        FileDialog dialog = new FileDialog(ComponentManager.getShell(), SWT.OPEN | SWT.MULTI);

        dialog.setText("Select files");
        dialog.setFilterNames(new String[] {fileType.getFilterNames(), "All Files"});
        dialog.setFilterExtensions(new String[] {fileType.getFilterExtensions(), "*.*"});
        dialog.setFilterPath(getFilterPath());

        String filePath = dialog.open();

        if (saveFilterPath && filePath != null && Paths.get(filePath).getParent() != null) {
            setFilterPath(Paths.get(filePath).getParent().toString());
        }

        return Arrays.stream(dialog.getFileNames())
                .map(fileName -> dialog.getFilterPath() + File.separator + fileName)
                .collect(Collectors.toList());
    }

    protected FileDialogs() throws IllegalAccessException {
        super();
    }
}
