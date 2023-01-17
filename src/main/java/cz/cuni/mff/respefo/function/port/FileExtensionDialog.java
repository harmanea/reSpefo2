package cz.cuni.mff.respefo.function.port;

import cz.cuni.mff.respefo.dialog.SpefoDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import static cz.cuni.mff.respefo.util.layout.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.widget.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.widget.LabelBuilder.newLabel;
import static cz.cuni.mff.respefo.util.widget.TextBuilder.newText;

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
                .onModifiedValue(value -> fileExtension = value)
                .focus()
                .build(composite);
    }
}
