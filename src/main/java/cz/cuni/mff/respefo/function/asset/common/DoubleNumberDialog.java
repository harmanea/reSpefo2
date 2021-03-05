package cz.cuni.mff.respefo.function.asset.common;


import cz.cuni.mff.respefo.component.SpefoDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import static cz.cuni.mff.respefo.util.builders.GridDataBuilder.gridData;
import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.builders.widgets.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.builders.widgets.LabelBuilder.newLabel;
import static cz.cuni.mff.respefo.util.builders.widgets.TextBuilder.newText;

public class DoubleNumberDialog extends SpefoDialog {

    private final String label;
    private double value;

    public DoubleNumberDialog(String label, double value) {
        super("Select number");

        this.label = label;
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    protected void createDialogArea(Composite parent) {
        final Composite composite = newComposite()
                .layout(gridLayout(2, false).margins(15).horizontalSpacing(10))
                .layoutData(gridData(GridData.FILL_BOTH).widthHint(250).build())
                .build(parent);

        newLabel().text(label).gridLayoutData(GridData.HORIZONTAL_ALIGN_BEGINNING).build(composite);

        newText(SWT.SINGLE | SWT.BORDER)
                .text(Double.toString(value))
                .gridLayoutData(GridData.FILL_HORIZONTAL)
                .onModify(event -> {
                    try {
                        value = Double.parseDouble(((Text) event.widget).getText());
                        if (Double.isNaN(value) || Double.isInfinite(value)) {
                            throw new NumberFormatException();
                        }
                        getButton(SWT.OK).setEnabled(true);

                    } catch (NumberFormatException e) {
                        getButton(SWT.OK).setEnabled(false);
                    }
                })
                .build(composite);
    }
}
