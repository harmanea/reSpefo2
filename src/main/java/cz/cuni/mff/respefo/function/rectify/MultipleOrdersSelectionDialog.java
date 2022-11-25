package cz.cuni.mff.respefo.function.rectify;

import cz.cuni.mff.respefo.dialog.SpefoDialog;
import cz.cuni.mff.respefo.util.widget.ButtonBuilder;
import cz.cuni.mff.respefo.util.widget.LabelBuilder;
import cz.cuni.mff.respefo.util.widget.SpinnerBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;

import static cz.cuni.mff.respefo.util.layout.GridDataBuilder.gridData;
import static cz.cuni.mff.respefo.util.layout.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.widget.ButtonBuilder.newButton;
import static cz.cuni.mff.respefo.util.widget.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.widget.LabelBuilder.newLabel;
import static cz.cuni.mff.respefo.util.widget.SpinnerBuilder.newSpinner;

public class MultipleOrdersSelectionDialog extends SpefoDialog {

    private final int maximum;

    private int from;
    private int to;
    private boolean select;

    protected MultipleOrdersSelectionDialog(int maximum) {
        super("(De)select multiple orders");

        this.maximum = maximum;

        this.from = 1;
        this.to = maximum;
        this.select = true;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public boolean isSelect() {
        return select;
    }

    @Override
    protected void createDialogArea(Composite parent) {
        final Composite composite = newComposite()
                .layout(gridLayout(2, true).margins(15).horizontalSpacing(0))
                .layoutData(gridData(GridData.FILL_BOTH).widthHint(300))
                .build(parent);

        LabelBuilder labelBuilder = newLabel().gridLayoutData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        SpinnerBuilder spinnerBuilder = newSpinner(SWT.NONE)
                .gridLayoutData(GridData.FILL_BOTH)
                .bounds(1, maximum)
                .digits(0)
                .increment(1, 10);
        ButtonBuilder buttonBuilder = newButton(SWT.TOGGLE)
                .gridLayoutData(GridData.FILL_BOTH);

        labelBuilder.text("From:").build(composite);
        spinnerBuilder.selection(1)
                .onModify(event -> from = ((Spinner) event.widget).getSelection())
                .focus()
                .build(composite);

        labelBuilder.text("To:").build(composite);
        spinnerBuilder.selection(maximum)
                .onModify(event -> to = ((Spinner) event.widget).getSelection())
                .build(composite);

        final Composite buttonsComposite = newComposite()
                .layout(gridLayout(2, true).margins(15).horizontalSpacing(0))
                .layoutData(gridData(GridData.FILL_BOTH).horizontalSpan(2))
                .build(parent);

        final Button[] buttons = new Button[2];

        buttons[0] = buttonBuilder.text("Deselect").selection(false)
                .onSelection(event -> {
                    select = !((Button) event.widget).getSelection();
                    buttons[1].setSelection(select);
                    buttons[1].setEnabled(!select);
                    buttons[0].setEnabled(select);
                })
                .build(buttonsComposite);

        buttons[1] = buttonBuilder.text("Select").selection(true).enabled(false)
                .onSelection(event -> {
                    select = ((Button) event.widget).getSelection();
                    buttons[0].setSelection(!select);
                    buttons[0].setEnabled(select);
                    buttons[1].setEnabled(!select);
                })
                .build(buttonsComposite);
    }
}
