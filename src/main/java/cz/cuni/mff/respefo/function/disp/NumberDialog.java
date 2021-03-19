package cz.cuni.mff.respefo.function.disp;

import cz.cuni.mff.respefo.dialog.SpefoDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;

import static cz.cuni.mff.respefo.util.layout.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.widget.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.widget.LabelBuilder.newLabel;
import static cz.cuni.mff.respefo.util.widget.SpinnerBuilder.newSpinner;

public class NumberDialog extends SpefoDialog {

    private final int maximum;
    private final String labelText;

    private int number;

    public NumberDialog(int maximum, String title, String labelText) {
        super(title);

        this.maximum = maximum;
        this.labelText = labelText;
    }

    public int getNumber() {
        return number;
    }

    @Override
    protected void createDialogArea(Composite parent) {
        final Composite composite = newComposite()
                .layout(gridLayout(2, false).margins(15).horizontalSpacing(10))
                .gridLayoutData(GridData.FILL_BOTH)
                .build(parent);

        newLabel().text(labelText).gridLayoutData(GridData.HORIZONTAL_ALIGN_BEGINNING).build(composite);
        newSpinner(SWT.NONE)
                .gridLayoutData(GridData.FILL_BOTH)
                .selection(1)
                .bounds(1, maximum)
                .digits(0)
                .increment(1, 1)
                .onModify(event -> number = ((Spinner) event.widget).getSelection())
                .focus()
                .build(composite);
    }
}
