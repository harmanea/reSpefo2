package cz.cuni.mff.respefo.component;

import cz.cuni.mff.respefo.util.FileDialogs;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.UtilityClass;
import org.eclipse.swt.widgets.DirectoryDialog;

import java.io.File;

public class Project extends UtilityClass {
    private static File rootDirectory;

    public static void setRootDirectory(File directory) {
        if (directory == null) {
            throw new IllegalArgumentException("File is null");
        } else if (!directory.isDirectory()) {
            throw new IllegalArgumentException("File is not a directory");
        } else if (!directory.exists()) {
            throw new IllegalArgumentException("File doesn't exist");
        }

        rootDirectory = directory;

        FileExplorer.getDefault().setRootDirectory(directory);
    }

    public static File getRootDirectory() {
        return rootDirectory;
    }

    public static String getRootFileName(String suffix) {
        return rootDirectory.getPath() + File.separator + rootDirectory.getName() + suffix;
    }

    public static void changeRootDirectory() {
        DirectoryDialog dialog = new DirectoryDialog(ComponentManager.getShell());

        dialog.setText("Choose directory");
        dialog.setFilterPath(rootDirectory.getPath());

        String directoryName = dialog.open();

        if (directoryName != null) {
            try {
                setRootDirectory(new File(directoryName));
                FileDialogs.setFilterPath(directoryName);
            } catch (Exception exception) {
                Message.error("Couldn't change project directory", exception);
            }
        }
    }

    public static void refresh() {
        FileExplorer.getDefault().refresh();
    }

    private Project() throws IllegalAccessException {
        super();
    }
}
