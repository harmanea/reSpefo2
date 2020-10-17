package cz.cuni.mff.respefo.function.asset.port;

import cz.cuni.mff.respefo.component.SpefoDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import static cz.cuni.mff.respefo.util.builders.CompositeBuilder.composite;
import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.builders.LabelBuilder.label;

public class RVCorrectionDialog extends SpefoDialog {

    private double rvCorr;

    public RVCorrectionDialog() {
        super("No RV correction");
    }

    public double getRvCorr() {
        return rvCorr;
    }

    @Override
    protected void createDialogArea(Composite parent) {
        Composite composite = composite(parent)
                .layout(gridLayout(2, false).margins(10).verticalSpacing(10).horizontalSpacing(10))
                .layoutData(new GridData(GridData.FILL_BOTH))
                .build();

        label(composite)
                .text("No RV correction is defined in this file.")
                .layoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));

        label(composite)
                .text("Please insert it manually.")
                .layoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));

        label(composite)
                .text("RV Corr:")
                .layoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

        final Text text = new Text(composite, SWT.SINGLE | SWT.BORDER);
        text.setText("0.0");
        text.setLayoutData(new GridData(GridData.FILL_BOTH));
        text.addModifyListener(event -> verifyValue(text));
        text.setFocus();
    }

    private void verifyValue(Text text) {
        try {
            rvCorr = Double.parseDouble(text.getText());
            getButton(SWT.OK).setEnabled(true);
        } catch (NumberFormatException exception) {
            getButton(SWT.OK).setEnabled(false);
        }
    }
}
