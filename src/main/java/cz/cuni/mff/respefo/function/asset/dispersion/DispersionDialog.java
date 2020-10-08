package cz.cuni.mff.respefo.function.asset.dispersion;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.component.SpefoDialog;
import cz.cuni.mff.respefo.util.FileType;
import cz.cuni.mff.respefo.util.utils.FileDialogs;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import static cz.cuni.mff.respefo.util.builders.CompositeBuilder.composite;
import static cz.cuni.mff.respefo.util.builders.GridDataBuilder.gridData;
import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.builders.LabelBuilder.label;

public class DispersionDialog extends SpefoDialog {

    private String labFileNameA;
    private String labFileNameB;
    private String cmpFileName;

    public DispersionDialog() {
        super("Derive dispersion");
    }

    public String getLabFileNameA() {
        return labFileNameA;
    }

    public String getLabFileNameB() {
        return labFileNameB;
    }

    public String getCmpFileName() {
        return cmpFileName;
    }

    @Override
    protected void createDialogArea(Composite parent) {
        Composite composite = composite(parent)
                .layout(gridLayout(2, false).margins(15).horizontalSpacing(10))
                .layoutData(gridData(GridData.FILL_BOTH).widthHint(500))
                .build();

        label(composite)
                .layoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 2, 1))
                .text("File A:");

        Text aText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        aText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        aText.addListener(SWT.Modify, event -> labFileNameA = aText.getText());

        Button aButton = new Button(composite, SWT.PUSH);
        aButton.setText("Browse");
        aButton.addListener(SWT.Selection, event -> {
            String fileName = FileDialogs.openFileDialog(FileType.FITS);
            if (fileName != null) {
                aText.setText(fileName);
            }
            parent.getShell().moveAbove(ComponentManager.getShell());
        });

        label(composite)
                .layoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 2, 1))
                .text("File B:");

        Text bText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        bText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        bText.addListener(SWT.Modify, event -> labFileNameB = bText.getText());

        Button bButton = new Button(composite, SWT.PUSH);
        bButton.setText("Browse");
        bButton.addListener(SWT.Selection, event -> {
            String fileName = FileDialogs.openFileDialog(FileType.FITS);
            if (fileName != null) {
                bText.setText(fileName);
            }
            parent.getShell().moveAbove(ComponentManager.getShell());
        });

        label(composite)
                .layoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 2, 1))
                .text("CMP File:");

        Text cText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        cText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        cText.addListener(SWT.Modify, event -> cmpFileName = cText.getText());

        Button cButton = new Button(composite, SWT.PUSH);
        cButton.setText("Browse");
        cButton.addListener(SWT.Selection, event -> {
            String fileName = FileDialogs.openFileDialog(FileType.CMP);
            if (fileName != null) {
                cText.setText(fileName);
            }
            parent.getShell().moveAbove(ComponentManager.getShell());
        });
    }
}
