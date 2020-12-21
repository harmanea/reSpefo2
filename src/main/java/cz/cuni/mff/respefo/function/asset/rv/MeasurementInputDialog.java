package cz.cuni.mff.respefo.function.asset.rv;

import cz.cuni.mff.respefo.component.SpefoDialog;
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
        final Composite composite = newComposite()
                .layout(gridLayout(2, false).margins(15).marginBottom(25).horizontalSpacing(10))
                .layoutData(gridData(GridData.FILL_BOTH).widthHint(250).heightHint(80).build())
                .build(parent);

        LabelBuilder labelBuilder = newLabel().gridLayoutData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        TextBuilder textBuilder = newText(SWT.SINGLE | SWT.BORDER).gridLayoutData(GridData.FILL_HORIZONTAL);

        labelBuilder.text("Category:").build(composite);
        final Text categoryText = textBuilder.build(composite);
        if (category.equals("corr")) {
            categoryText.setText("corr");
            categoryText.setEnabled(false);
        } else {
            categoryText.addListener(SWT.Modify, event -> {
                getButton(SWT.OK).setEnabled(!categoryText.getText().equals(""));
                category = categoryText.getText();
            });
        }

        labelBuilder.text("Comment:").build(composite);
        textBuilder.onModify(event -> comment = ((Text) event.widget).getText()).build(composite);
    }

    @Override
    protected void createButtons(Composite parent) {
        super.createButtons(parent);

        if (!category.equals("corr")) {
            getButton(SWT.OK).setEnabled(false);
        }
    }
}
