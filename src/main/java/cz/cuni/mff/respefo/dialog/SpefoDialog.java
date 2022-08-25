package cz.cuni.mff.respefo.dialog;

import cz.cuni.mff.respefo.component.ComponentManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.util.HashMap;
import java.util.Map;

import static cz.cuni.mff.respefo.util.layout.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.widget.ButtonBuilder.newButton;
import static cz.cuni.mff.respefo.util.widget.CompositeBuilder.newComposite;

public abstract class SpefoDialog {
    private final String title;
    private final Map<Integer, Button> buttons;

    private Shell shell;
    private int returnCode = SWT.OK;

    protected SpefoDialog(String title) {
        this.title = title;

        buttons = new HashMap<>();
    }

    Shell createShell() {
        Shell newShell = new Shell(ComponentManager.getShell(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        newShell.setLayout(gridLayout().margins(0).build());
        newShell.setText(title);
        newShell.addShellListener(new ShellAdapter() {
            @Override
            public void shellClosed(ShellEvent e) {
                e.doit = false;
                returnCode = SWT.CANCEL;
                newShell.dispose();
            }
        });
        return newShell;
    }

    void createContents(Shell shell) {
        final Composite composite = newComposite()
                .layout(gridLayout().margins(0).verticalSpacing(0))
                .gridLayoutData(GridData.FILL_BOTH)
                .build(shell);

        createDialogArea(composite);
        createButtonsArea(composite);
    }

    /**
     * Implement this method to create the main dialog area.
     * @param parent parent Composite for the contents of the dialog area
     */
    protected abstract void createDialogArea(Composite parent);

    void createButtonsArea(Composite parent) {
        final Composite composite = newComposite()
                .layout(gridLayout(0, true).margins(10).horizontalSpacing(10))
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_END)
                .build(parent);

        createButtons(composite);
    }

    /**
     * Override this method to customize the button area.
     * By default, this creates an OK and a Cancel button.
     * @param parent parent Composite for the buttons in the buttons area
     */
    protected void createButtons(Composite parent) {
        createButton(parent, SWT.OK, "OK", true);
        createButton(parent, SWT.CANCEL, "Cancel", false);
    }

    protected final void createButton(Composite parent, int returnCode, String label, boolean defaultButton) {
        ((GridLayout) parent.getLayout()).numColumns++;

        final Button button = newButton(SWT.PUSH)
                .text(label)
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_FILL)
                .onSelection(event -> buttonPressed(returnCode))
                .build(parent);

        if (defaultButton) {
            shell.setDefaultButton(button);
        }

        buttons.put(returnCode, button);
    }

    protected final Button getButton(int returnCode) {
        return buttons.get(returnCode);
    }

    /**
     * Override this method to implement custom actions on button press.
     * By default, this sets the return code and disposes the shell.
     * @param returnCode code assigned to the pressed button
     */
    protected void buttonPressed(int returnCode) {
        this.returnCode = returnCode;
        shell.dispose();
    }

    public final int open() {
        shell = createShell();
        createContents(shell);

        shell.pack();
        shell.open();
        Display display = ComponentManager.getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }

        return returnCode;
    }

    public final boolean openIsOk() {
        return open() == SWT.OK;
    }

    public final boolean openIsNotOk() {
        return open() != SWT.OK;
    }
}
