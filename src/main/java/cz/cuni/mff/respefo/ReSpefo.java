package cz.cuni.mff.respefo;

import cz.cuni.mff.respefo.function.FunctionManager;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.VersionInfo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import static org.eclipse.swt.SWT.SHELL_TRIM;

public class ReSpefo {

    private static Shell shell;

    public static void main(String[] args) {

        // TODO: OS detection and handling

        // TODO: Scan for annotations -> fill function managers
        FunctionManager.scan();

        // TODO: Create Display & Shell -> init resource managers
        final Display display = createDisplay();
        shell = createShell(display);

        // TODO: Build components -> register log listeners

        // TODO: Open Shell + set size?
        shell.open();

        // Main loop
        while (!shell.isDisposed()) {
            try {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            } catch (Exception exception) {
                handleException(exception);
            }
        }
        display.dispose();

        // TODO: Cleanup
    }

    public static Shell getShell() {
        return shell;
    }

    private static Display createDisplay() {
        final Display display = new Display();

        Display.setAppName("reSpefo");
        Display.setAppVersion(VersionInfo.getVersion());

        return display;
    }

    private static Shell createShell(Display display) {
        final Shell shell = new Shell(display, SHELL_TRIM);

        shell.setText("reSpefo (" + VersionInfo.getVersion() + ")");

        return shell;
    }

    private static void handleException(Exception exception) {
        Message.error("An error occurred in one of the components.", exception);
    }
}
