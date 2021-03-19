package cz.cuni.mff.respefo.function.port;

import cz.cuni.mff.respefo.dialog.SpefoDialog;
import cz.cuni.mff.respefo.util.widget.LabelBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import static cz.cuni.mff.respefo.util.layout.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.widget.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.widget.LabelBuilder.newLabel;
import static cz.cuni.mff.respefo.util.widget.TextBuilder.newText;

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
        final Composite composite = newComposite()
                .layout(gridLayout(2, false).margins(10).verticalSpacing(10).horizontalSpacing(10))
                .gridLayoutData(GridData.FILL_BOTH)
                .build(parent);

        LabelBuilder labelBuilder = newLabel().gridLayoutData(SWT.FILL, SWT.TOP, true, false, 2, 1);

        labelBuilder.text("No RV correction is defined in this file.").build(composite);
        labelBuilder.text("Please insert it manually.").build(composite);

        newLabel().gridLayoutData(GridData.HORIZONTAL_ALIGN_BEGINNING).text("RV Corr:").build(composite);
        newText(SWT.SINGLE | SWT.BORDER)
                .gridLayoutData(GridData.FILL_BOTH)
                .text("0.0")
                .onModify(event -> verifyValue((Text) event.widget))
                .focus()
                .build(composite);
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
