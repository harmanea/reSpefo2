package cz.cuni.mff.respefo.function.disp;

import cz.cuni.mff.respefo.dialog.SpefoDialog;
import cz.cuni.mff.respefo.util.FileType;
import cz.cuni.mff.respefo.util.widget.LabelBuilder;
import cz.cuni.mff.respefo.util.widget.TextBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import static cz.cuni.mff.respefo.util.layout.GridDataBuilder.gridData;
import static cz.cuni.mff.respefo.util.layout.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.widget.ButtonBuilder.newBrowseButton;
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

        labelBuilder.text("File A:").build(composite);
        final Text aText = textBuilder.onModifiedValue(value -> labFileNameA = value).build(composite);
        newBrowseButton(FileType.FITS, aText::setText)
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_CENTER)
                .build(composite);

        labelBuilder.text("File B:").build(composite);
        final Text bText = textBuilder.onModifiedValue(value -> labFileNameB = value).build(composite);
        newBrowseButton(FileType.FITS, bText::setText)
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_CENTER)
                .build(composite);

        labelBuilder.text("CMP File:").build(composite);
        final Text cText = textBuilder.onModifiedValue(value -> cmpFileName = value).build(composite);
        newBrowseButton(FileType.CMP, cText::setText)
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_CENTER)
                .build(composite);
    }
}
