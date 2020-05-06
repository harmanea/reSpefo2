package cz.cuni.mff.respefo.util;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.util.utils.ExceptionUtils;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import static org.eclipse.core.runtime.IStatus.ERROR;
import static org.eclipse.swt.SWT.*;

public class Message extends UtilityClass {

    public static void info(String message) {
        info(ComponentManager.getShell(), message);
    }

    public static void info(Shell shell, String message) {
        openMessageBox(shell, message, ICON_INFORMATION | OK);
    }

    public static void warning(String message) {
        warning(ComponentManager.getShell(), message);
    }

    public static void warning(Shell shell, String message) {
        openMessageBox(shell, message, ICON_WARNING | OK);
    }

    public static void error(String message, Throwable cause) {
        error(ComponentManager.getShell(), message, cause);
    }

    public static void error(Shell shell, String message, Throwable cause) {
        Log.error(message, cause);
        String displayMessage = String.format("%s%n%n%s: %s%n(See log for more details)", message, cause.getClass().toString(), cause.getMessage());
        openMessageBox(shell, displayMessage, ICON_ERROR | OK);
    }

    public static void errorWithDetails(String message, Throwable cause) {
        Status status = new Status(ERROR, "cz.cuni.mff.respefo", ExceptionUtils.getStackTrace(cause));
        MultiStatus multiStatus = new MultiStatus("cz.cuni.mff.respefo", ERROR, new Status[]{status}, cause.toString(), cause);

        ErrorDialog.openError(ComponentManager.getShell(), "Error", message, multiStatus);
        Log.error("An error occured in one of the components", cause);
    }

    public static boolean question(String message) {
        return question(ComponentManager.getShell(), message);
    }

    public static boolean question(Shell shell, String message) {
        return openMessageBox(shell, message, ICON_QUESTION | YES | NO) == YES;
    }

    private static int openMessageBox(Shell shell, String message, int style) {
        MessageBox messageBox = new MessageBox(shell, style);

        messageBox.setMessage(message);
        return messageBox.open();
    }

    protected Message() throws IllegalAccessException {
        super();
    }
}
