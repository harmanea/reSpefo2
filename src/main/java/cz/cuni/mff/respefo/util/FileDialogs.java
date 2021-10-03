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

public class FileDialogs extends UtilityClass {

    private static String filterPath;

    /**
     * Returns the static filter path currently used for dialogs in this class.
     * @return filter path
     */
    public static String getFilterPath() {
        return filterPath;
    }

    /**
     * Sets the static filter path to be used by dialogs in this class.
     * @param filterPath new value
     */
    public static void setFilterPath(String filterPath) {
        FileDialogs.filterPath = Objects.requireNonNull(filterPath);
    }

    /**
     * Open a standard file selection dialog for the selected file type.
     * <br>
     * This automatically sets the filter path to the parent of the selected file.
     *
     * @param fileType type of files that the selection dialog will filter
     * @return path to the selected file or null if the dialog was cancelled
     * @see FileDialogs#setFilterPath(String)
     */
    public static String openFileDialog(FileType fileType) {
        return openFileDialog(fileType, true);
    }

    /**
     * Open a standard file selection dialog for the selected file type.
     *
     * @param fileType type of files that the selection dialog will filter
     * @param saveFilterPath if this flag is true, set the filter path to the parent of the selected file
     * @return path to the selected file or null if the dialog was cancelled
     * @see FileDialogs#setFilterPath(String)
     */
    public static String openFileDialog(FileType fileType, boolean saveFilterPath) {
        return fileDialog(fileType, saveFilterPath, SWT.OPEN, null);
    }

    /**
     * Open a standard file save dialog for the selected file type.
     * <br>
     * This automatically sets the filter path to the parent of the selected file.
     *
     * @param fileType type of files that the selection dialog will filter
     * @param fileName default value for the filename
     * @return path to the selected file or null if the dialog was cancelled
     * @see FileDialogs#setFilterPath(String)
     */
    public static String saveFileDialog(FileType fileType, String fileName) {
        return saveFileDialog(fileType, fileName, true);
    }

    /**
     * Open a standard file save dialog for the selected file type.
     *
     * @param fileType type of files that the selection dialog will filter
     * @param fileName default value for the filename
     * @param saveFilterPath if this flag is true, set the filter path to the parent of the selected file
     * @return path to the selected file or null if the dialog was cancelled
     * @see FileDialogs#setFilterPath(String)
     */
    public static String saveFileDialog(FileType fileType, String fileName, boolean saveFilterPath) {
        return fileDialog(fileType, saveFilterPath, SWT.SAVE, fileName);
    }

    private static String fileDialog(FileType fileType, boolean saveFilterPath, int style, String fileName) {
        Objects.requireNonNull(fileType);

        FileDialog dialog = new FileDialog(ComponentManager.getShell(), style);

        dialog.setText("Select file");
        dialog.setFilterNames(fileType.getFilterNames());
        dialog.setFilterExtensions(fileType.getFilterExtensions());

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

    /**
     * Open a standard directory selection dialog.
     * <br>
     * This automatically sets the filter path to the parent of the selected file.
     *
     * @return path to the selected directory or null if the dialog was cancelled
     * @see FileDialogs#setFilterPath(String)
     */
    public static String directoryDialog() {
        return directoryDialog(true);
    }

    /**
     * Open a standard directory selection dialog.
     *
     * @param saveFilterPath if this flag is true, set the filter path to the parent of the selected file
     * @return path to the selected directory or null if the dialog was cancelled
     * @see FileDialogs#setFilterPath(String)
     */
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

    /**
     * Open a standard files selection dialog for the selected file type.
     * <br>
     * This automatically sets the filter path to the parent of the selected file.
     *
     * @param fileType type of files that the selection dialog will filter
     * @return list of paths to the selected files, will be empty if the dialog is cancelled
     * @see FileDialogs#setFilterPath(String)
     */
    public static List<String> openMultipleFilesDialog(FileType fileType) {
        return openMultipleFilesDialog(fileType, true);
    }

    /**
     * Open a standard files selection dialog for the selected file type.
     *
     * @param fileType type of files that the selection dialog will filter
     * @param saveFilterPath if this flag is true, set the filter path to the parent of the selected file
     * @return list of paths to the selected files, will be empty if the dialog is cancelled
     * @see FileDialogs#setFilterPath(String)
     */
    public static List<String> openMultipleFilesDialog(FileType fileType, boolean saveFilterPath) {
        Objects.requireNonNull(fileType);

        FileDialog dialog = new FileDialog(ComponentManager.getShell(), SWT.OPEN | SWT.MULTI);

        dialog.setText("Select files");
        dialog.setFilterNames(fileType.getFilterNames());
        dialog.setFilterExtensions(fileType.getFilterExtensions());
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
