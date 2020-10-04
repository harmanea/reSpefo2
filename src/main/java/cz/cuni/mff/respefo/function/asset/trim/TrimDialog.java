package cz.cuni.mff.respefo.function.asset.trim;

import cz.cuni.mff.respefo.component.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import static cz.cuni.mff.respefo.util.builders.GridDataBuilder.gridData;
import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;
import static java.lang.Double.isNaN;

public class TrimDialog extends TitleAreaDialog {
    private double min = Double.NEGATIVE_INFINITY;
    private double max = Double.POSITIVE_INFINITY;

    private Text minText;
    private Text maxText;

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

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(gridLayout(2, false).margins(7).build());
        composite.setLayoutData(gridData(GridData.FILL_BOTH).widthHint(500).heightHint(80).build());

        final Label minLabel = new Label(composite, SWT.NONE);
        minLabel.setText("Min:");
        minLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

        minText = new Text(composite, SWT.BORDER | SWT.SINGLE);
        minText.setMessage("No limit");
        if (min != Double.NEGATIVE_INFINITY) {
            minText.setText(Double.toString(min));
        }
        minText.addListener(SWT.Modify, event -> validateMin());
        minText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        final Label maxLabel = new Label(composite, SWT.NONE);
        maxLabel.setText("Max:");
        maxLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

        maxText = new Text(composite, SWT.BORDER | SWT.SINGLE);
        maxText.setMessage("No limit");
        if (max != Double.POSITIVE_INFINITY) {
            maxText.setText(Double.toString(max));
        }
        maxText.addListener(SWT.Modify, event -> validateMax());
        maxText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    private void validateMin() {
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

    private void validateMax() {
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
