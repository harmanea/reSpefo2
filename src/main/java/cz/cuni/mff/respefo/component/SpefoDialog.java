package cz.cuni.mff.respefo.component;

import cz.cuni.mff.respefo.util.utils.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import static cz.cuni.mff.respefo.util.builders.ButtonBuilder.pushButton;
import static cz.cuni.mff.respefo.util.builders.CompositeBuilder.composite;
import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;

public abstract class SpefoDialog {
    private Shell shell;
    private int returnCode = SWT.OK;

    private Shell createShell() {
        Shell newShell = new Shell(ComponentManager.getShell(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        newShell.setLayout(gridLayout().margins(0).build());
        newShell.setText(getTitle());
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

    /**
     * Override this method to give the dialog a custom title
     * @return the dialog title
     */
    protected String getTitle() {
        return StringUtils.EMPTY_STRING;
    }

    private void createContents(Shell shell) {
        final Composite composite = composite(shell).layout(
                gridLayout().margins(0).verticalSpacing(0).build()
        ).layoutData(new GridData(GridData.FILL_BOTH)).build();

        createDialogArea(composite);
        createButtonsArea(composite);
    }

    /**
     * Implement this method to create the main dialog area.
     * @param parent parent Composite for the contents of the dialog area
     */
    protected abstract void createDialogArea(Composite parent);

    private void createButtonsArea(Composite parent) {
        final Composite composite = composite(parent)
                .layout(
                        gridLayout(0, true).margins(5).horizontalSpacing(5).build()
                ).layoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_CENTER))
                .build();

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

    protected Button createButton(Composite parent, int returnCode, String label, boolean defaultButton) {
        ((GridLayout) parent.getLayout()).numColumns++;

        Button button = pushButton(parent)
                .text(label)
                .layoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL))
                .onSelection(event -> buttonPressed(returnCode))
                .build();

        if (defaultButton) {
            shell.setDefaultButton(button);
        }

        return button;
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
        Display display = Display.getCurrent();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }

        return returnCode;
    }
}
