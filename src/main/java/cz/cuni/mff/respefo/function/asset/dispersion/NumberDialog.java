package cz.cuni.mff.respefo.function.asset.dispersion;

import cz.cuni.mff.respefo.component.SpefoDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;

import static cz.cuni.mff.respefo.util.builders.CompositeBuilder.composite;
import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.builders.LabelBuilder.label;

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
        Composite composite = composite(parent)
                .layout(gridLayout(2, false).margins(15).horizontalSpacing(10))
                .layoutData(new GridData(GridData.FILL_BOTH))
                .build();

        label(composite)
                .text(labelText)
                .layoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING))
                .build();

        Spinner spinner = new Spinner(composite, SWT.NONE);
        spinner.setValues(1, 1, maximum, 0, 1, 1);
        spinner.setLayoutData(new GridData(GridData.FILL_BOTH));
        spinner.addModifyListener(event -> number = spinner.getSelection());
        spinner.forceFocus();
    }
}
