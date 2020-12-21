package cz.cuni.mff.respefo.function.asset.port;

import cz.cuni.mff.respefo.component.SpefoDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.builders.widgets.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.builders.widgets.LabelBuilder.newLabel;
import static cz.cuni.mff.respefo.util.builders.widgets.TextBuilder.newText;

public class FileExtensionDialog extends SpefoDialog {

    private static final String DEFAULT_FILE_EXTENSION = "fits";

    private String fileExtension;

    public FileExtensionDialog() {
        super("File extension");

        fileExtension = DEFAULT_FILE_EXTENSION;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    @Override
    protected void createDialogArea(Composite parent) {
        final Composite composite = newComposite()
                .layout(gridLayout(2, false).margins(10).verticalSpacing(10).horizontalSpacing(10))
                .gridLayoutData(GridData.FILL_BOTH)
                .build(parent);

        newLabel().text("File extension:").gridLayoutData(GridData.HORIZONTAL_ALIGN_BEGINNING).build(composite);
        newText(SWT.SINGLE | SWT.BORDER)
                .gridLayoutData(GridData.FILL_BOTH)
                .onModify(event -> fileExtension = ((Text) event.widget).getText())
                .focus()
                .build(composite);
    }
}
