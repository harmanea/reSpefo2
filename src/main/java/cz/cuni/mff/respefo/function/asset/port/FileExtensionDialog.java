package cz.cuni.mff.respefo.function.asset.port;

import cz.cuni.mff.respefo.component.SpefoDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import static cz.cuni.mff.respefo.util.builders.CompositeBuilder.composite;
import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.builders.LabelBuilder.label;

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
        Composite composite = composite(parent)
                .layout(gridLayout(2, false).margins(10).verticalSpacing(10).horizontalSpacing(10))
                .layoutData(new GridData(GridData.FILL_BOTH))
                .build();

        label(composite)
                .text("File extension:")
                .layoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

        final Text text = new Text(composite, SWT.SINGLE | SWT.BORDER);
        text.setText(DEFAULT_FILE_EXTENSION);
        text.setLayoutData(new GridData(GridData.FILL_BOTH));
        text.addModifyListener(event -> fileExtension = text.getText());
        text.setFocus();
    }
}
