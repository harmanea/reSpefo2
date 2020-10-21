package cz.cuni.mff.respefo.function.asset.port;

import cz.cuni.mff.respefo.component.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;

import static cz.cuni.mff.respefo.util.builders.CompositeBuilder.composite;
import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.builders.LabelBuilder.label;

public class ChironDialog extends TitleAreaDialog {

    private final int max;

    private int from;
    private int to;

    public ChironDialog(int max) {
        super("Select echels to import");

        this.max = max;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    @Override
    protected void createDialogArea(Composite parent) {
        setMessage("Select which echels to import.", SWT.ICON_INFORMATION);

        Composite composite = composite(parent)
                .layout(gridLayout(2, false).margins(15).horizontalSpacing(10))
                .layoutData(new GridData(GridData.FILL_BOTH))
                .build();

        label(composite)
                .text("From:")
                .layoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING))
                .build();

        Spinner fromSpinner = new Spinner(composite, SWT.NONE);
        fromSpinner.setValues(1, 1, max, 0, 1, 1);
        fromSpinner.setLayoutData(new GridData(GridData.FILL_BOTH));

        label(composite)
                .text("To:")
                .layoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING))
                .build();

        Spinner toSpinner = new Spinner(composite, SWT.NONE);
        toSpinner.setValues(1, 1, max, 0, 1, 1);
        toSpinner.setLayoutData(new GridData(GridData.FILL_BOTH));

        fromSpinner.addModifyListener(event -> {
            from = fromSpinner.getSelection();
            if (from > to) {
                toSpinner.setSelection(from);
            }
        });
        toSpinner.addModifyListener(event -> {
            to = toSpinner.getSelection();
            if (from > to) {
                fromSpinner.setSelection(to);
            }
        });
    }
}
