package cz.cuni.mff.respefo.util;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.component.ErrorDialog;
import cz.cuni.mff.respefo.logging.Log;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import static org.eclipse.swt.SWT.*;

public class Message extends UtilityClass {

    public static void info(String message) {
        openMessageBox(ComponentManager.getShell(), message, ICON_INFORMATION | OK);
    }

    public static void warning(String message) {
        openMessageBox(ComponentManager.getShell(), message, ICON_WARNING | OK);
    }

    public static void error(String message, Throwable cause) {
        String displayMessage = String.format("%s%n%n%s: %s%n(See log for more details)", message, cause.getClass().toString(), cause.getMessage());
        openMessageBox(ComponentManager.getShell(), displayMessage, ICON_ERROR | OK);
        Log.error(message, cause);  // Do we want to do this by default?
    }

    public static void errorWithDetails(String message, Throwable cause) {
        new ErrorDialog(message, cause).open();
        Log.error(message, cause);  // Do we want to do this by default?
    }

    public static boolean question(String message) {
        return openMessageBox(ComponentManager.getShell(), message, ICON_QUESTION | YES | NO) == YES;
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
