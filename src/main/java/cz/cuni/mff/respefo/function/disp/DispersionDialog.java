package cz.cuni.mff.respefo.function.disp;

import cz.cuni.mff.respefo.dialog.SpefoDialog;
import cz.cuni.mff.respefo.util.FileDialogs;
import cz.cuni.mff.respefo.util.FileType;
import cz.cuni.mff.respefo.util.widget.ButtonBuilder;
import cz.cuni.mff.respefo.util.widget.LabelBuilder;
import cz.cuni.mff.respefo.util.widget.TextBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import static cz.cuni.mff.respefo.util.layout.GridDataBuilder.gridData;
import static cz.cuni.mff.respefo.util.layout.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.widget.ButtonBuilder.newButton;
import static cz.cuni.mff.respefo.util.widget.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.widget.LabelBuilder.newLabel;
import static cz.cuni.mff.respefo.util.widget.TextBuilder.newText;

// TODO: Make this more user friendly
// TODO: Add advanced import functions like ImportDialog
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
        final Composite composite = newComposite()
                .layout(gridLayout(2, false).margins(15).horizontalSpacing(10))
                .layoutData(gridData(GridData.FILL_BOTH).widthHint(500))
                .build(parent);

        LabelBuilder labelBuilder = newLabel()
                .gridLayoutData(SWT.LEFT, SWT.CENTER, true, true, 2, 1);

        TextBuilder textBuilder = newText(SWT.SINGLE | SWT.BORDER)
                .gridLayoutData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER);

        ButtonBuilder buttonBuilder = newButton(SWT.PUSH)
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_CENTER)
                .text("Browse");


        labelBuilder.text("File A:").build(composite);
        final Text aText = textBuilder.onModify(event -> labFileNameA = ((Text) event.widget).getText()).build(composite);
        buttonBuilder
                .onSelection(event -> {
                    String fileName = FileDialogs.openFileDialog(FileType.FITS);
                    if (fileName != null) {
                        aText.setText(fileName);
                    }
                }).build(composite);

        labelBuilder.text("File B:").build(composite);
        final Text bText = textBuilder.onModify(event -> labFileNameB = ((Text) event.widget).getText()).build(composite);
        buttonBuilder
                .onSelection(event -> {
                    String fileName = FileDialogs.openFileDialog(FileType.FITS);
                    if (fileName != null) {
                        bText.setText(fileName);
                    }
                }).build(composite);

        labelBuilder.text("CMP File:").build(composite);
        final Text cText = textBuilder.onModify(event -> cmpFileName = ((Text) event.widget).getText()).build(composite);
        buttonBuilder
                .onSelection(event -> {
                    String fileName = FileDialogs.openFileDialog(FileType.CMP);
                    if (fileName != null) {
                        cText.setText(fileName);
                    }
                }).build(composite);
    }
}
