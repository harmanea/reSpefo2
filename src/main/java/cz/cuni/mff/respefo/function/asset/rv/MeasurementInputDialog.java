package cz.cuni.mff.respefo.function.asset.rv;

import cz.cuni.mff.respefo.component.SpefoDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import static cz.cuni.mff.respefo.util.builders.CompositeBuilder.composite;
import static cz.cuni.mff.respefo.util.builders.GridDataBuilder.gridData;
import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.builders.LabelBuilder.label;

public class MeasurementInputDialog extends SpefoDialog {

    private String category;
    private String comment;

    public MeasurementInputDialog(boolean isCorrection) {
        super("Confirm measurement");

        category = isCorrection ? "corr" : "";
        comment = "";
    }

    public String getCategory() {
        return category;
    }

    public String getComment() {
        return comment;
    }

    @Override
    protected void createDialogArea(Composite parent) {
        Composite composite = composite(parent)
                .layout(gridLayout(2, false).margins(15).marginBottom(25).horizontalSpacing(10))
                .layoutData(gridData(GridData.FILL_BOTH).widthHint(250).heightHint(80))
                .build();

        label(composite)
                .text("Category:")
                .layoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING))
                .build();

        final Text categoryText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        categoryText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        if (category.equals("corr")) {
            categoryText.setText("corr");
            categoryText.setEnabled(false);
        } else {
            categoryText.addListener(SWT.Modify, event -> {
                getButton(SWT.OK).setEnabled(!categoryText.getText().equals(""));
                category = categoryText.getText();
            });
        }

        label(composite)
                .text("Comment:")
                .layoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING))
                .build();

        final Text commentText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        commentText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        commentText.addListener(SWT.Modify, event -> comment = commentText.getText());
    }

    @Override
    protected void createButtons(Composite parent) {
        super.createButtons(parent);

        if (!category.equals("corr")) {
            getButton(SWT.OK).setEnabled(false);
        }
    }
}
