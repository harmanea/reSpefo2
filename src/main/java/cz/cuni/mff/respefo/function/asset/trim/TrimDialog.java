package cz.cuni.mff.respefo.function.asset.trim;

import cz.cuni.mff.respefo.component.TitleAreaDialog;
import cz.cuni.mff.respefo.util.builders.widgets.LabelBuilder;
import cz.cuni.mff.respefo.util.builders.widgets.TextBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import static cz.cuni.mff.respefo.util.builders.GridDataBuilder.gridData;
import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.builders.widgets.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.builders.widgets.LabelBuilder.newLabel;
import static cz.cuni.mff.respefo.util.builders.widgets.TextBuilder.newText;
import static java.lang.Double.isNaN;

public class TrimDialog extends TitleAreaDialog {
    private double min = Double.NEGATIVE_INFINITY;
    private double max = Double.POSITIVE_INFINITY;

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public TrimDialog() {
        super("Trim");
    }

    @Override
    protected void createDialogArea(Composite parent) {
        setMessage("Trim the spectrum x-values", SWT.ICON_INFORMATION);

        final Composite composite = newComposite()
                .layout(gridLayout(2, false).margins(7))
                .layoutData(gridData(GridData.FILL_BOTH).widthHint(500).heightHint(80).build())
                .build(parent);

        LabelBuilder labelBuilder = newLabel().gridLayoutData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        TextBuilder textBuilder = newText(SWT.BORDER | SWT.SINGLE).message("No limit").gridLayoutData(GridData.FILL_HORIZONTAL);

        labelBuilder.text("Min:").build(composite);

        final Text minText = textBuilder.onModify(event -> validateMin((Text) event.widget)).build(composite);
        if (min != Double.NEGATIVE_INFINITY) {
            minText.setText(Double.toString(min));
        }

        labelBuilder.text("Max:").build(composite);

        final Text maxText = textBuilder.onModify(event -> validateMax((Text) event.widget)).build(composite);
        if (max != Double.POSITIVE_INFINITY) {
            maxText.setText(Double.toString(max));
        }
    }

    private void validateMin(Text minText) {
        if (minText.getText().equals("")) {
            min = Double.NEGATIVE_INFINITY;
        } else {
            try {
                min = Double.parseDouble(minText.getText());
            } catch (NumberFormatException exception) {
                min = Double.NaN;
            }
        }

        validate();
    }

    private void validateMax(Text maxText) {
        if (maxText.getText().equals("")) {
            max = Double.POSITIVE_INFINITY;
        } else {
            try {
                max = Double.parseDouble(maxText.getText());
            } catch (NumberFormatException exception) {
                max = Double.NaN;
            }
        }

        validate();
    }

    private void validate() {
        if (isNaN(min) || isNaN(max)) {
            setMessage("Values must be valid numbers", SWT.ICON_WARNING);
            getButton(SWT.OK).setEnabled(false);
        } else {
            setMessage("Trim the spectrum x-values", SWT.ICON_INFORMATION);
            getButton(SWT.OK).setEnabled(true);
        }
    }
}
