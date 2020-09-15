package cz.cuni.mff.respefo.function.asset.dispersion;

import cz.cuni.mff.respefo.component.SpefoDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;

import static cz.cuni.mff.respefo.util.builders.CompositeBuilder.composite;
import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.builders.LabelBuilder.label;

public class LineNumberDialog extends SpefoDialog {

    private final int maximum;

    private int lineNumber;

    public LineNumberDialog(int maximum) {
        super();

        this.maximum = maximum;
    }

    public int getLineNumber() {
        return lineNumber - 1;
    }

    @Override
    protected String getTitle() {
        return "Select line number";
    }

    @Override
    protected void createDialogArea(Composite parent) {
        Composite composite = composite(parent)
                .layout(gridLayout(2, false).margins(15).horizontalSpacing(10))
                .layoutData(new GridData(GridData.FILL_BOTH))
                .build();

        label(composite)
                .text("Line number:")
                .layoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING))
                .build();

        Spinner spinner = new Spinner(composite, SWT.NONE);
        spinner.setValues(1, 1, maximum, 0, 1, 1);
        spinner.setLayoutData(new GridData(GridData.FILL_BOTH));
        spinner.forceFocus();
        spinner.addModifyListener(event -> lineNumber = spinner.getSelection());
    }
}
