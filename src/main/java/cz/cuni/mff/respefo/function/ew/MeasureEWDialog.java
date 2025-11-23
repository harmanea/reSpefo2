package cz.cuni.mff.respefo.function.ew;

import cz.cuni.mff.respefo.dialog.TitleAreaDialog;
import cz.cuni.mff.respefo.util.FileDialogs;
import cz.cuni.mff.respefo.util.FileType;
import cz.cuni.mff.respefo.util.widget.ButtonBuilder;
import cz.cuni.mff.respefo.util.widget.ListBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

import static cz.cuni.mff.respefo.util.layout.GridDataBuilder.gridData;
import static cz.cuni.mff.respefo.util.layout.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.widget.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.widget.LabelBuilder.newLabel;

public class MeasureEWDialog extends TitleAreaDialog {

    private static String[] previousFileNames = {};

    private String[] fileNames;

    public MeasureEWDialog() {
        super("Measure EW");

        fileNames = previousFileNames;
    }

    public String[] getFileNames() {
        return fileNames;
    }

    @Override
    protected void createDialogArea(Composite parent) {
        setMessage("Measure equivalent width and other spectrophotometric quantities", SWT.ICON_INFORMATION);

        final Composite topComposite = newComposite()
                .layout(gridLayout(2, false).margins(15).horizontalSpacing(10))
                .layoutData(gridData(GridData.FILL_BOTH).widthHint(500).heightHint(300))
                .build(parent);

        newLabel()
                .gridLayoutData(SWT.FILL, SWT.TOP, true, false, 2, 1)
                .text("Select .stl files with measurements:")
                .build(topComposite);

        final List list = ListBuilder.newList(SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL)
                .gridLayoutData(GridData.FILL_BOTH)
                .listener(SWT.KeyDown, e -> {
                    if (e.keyCode == SWT.DEL) {
                        removeStlFile((List) e.widget);
                    } else if (e.keyCode == SWT.INSERT || e.keyCode == SWT.HELP || (e.keyCode == 'i' && e.stateMask == SWT.COMMAND)) {
                        addStlFile((List) e.widget);
                    }
                })
                .items(fileNames)
                .build(topComposite);

        final Composite buttonsComposite = newComposite()
                .layout(gridLayout().margins(0))
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_BEGINNING)
                .build(topComposite);

        ButtonBuilder buttonBuilder = ButtonBuilder.newPushButton().gridLayoutData(GridData.FILL_BOTH);

        buttonBuilder.text("Add").onSelection(event -> addStlFile(list)).build(buttonsComposite);
        buttonBuilder.text("Remove").onSelection(event -> removeStlFile(list)).build(buttonsComposite);
    }

    @Override
    protected void createButtons(Composite parent) {
        super.createButtons(parent);

        getButton(SWT.OK).setEnabled(fileNames.length > 0);
    }

    private void addStlFile(List list) {
        FileDialogs.openFileDialog(FileType.STL, false)
                .ifPresent(fileName -> {
                    list.add(fileName);

                    fileNames = list.getItems();
                    if (fileNames.length == 1) {
                        setMessage("Measure equivalent width and other spectrophotometric quantities", SWT.ICON_INFORMATION);
                        getButton(SWT.OK).setEnabled(true);
                    }
                });
    }

    private void removeStlFile(List list) {
        if (list.getSelectionIndex() != -1) {
            list.remove(list.getSelectionIndex());

            fileNames = list.getItems();

            if (fileNames.length == 0) {
                setMessage("Select at least one .stl file", SWT.ICON_WARNING);
                getButton(SWT.OK).setEnabled(false);
            }
        }
    }

    @Override
    protected void buttonPressed(int returnCode) {
        if (returnCode == SWT.OK) {
            synchronized (this) {
                previousFileNames = fileNames;
            }
        }

        super.buttonPressed(returnCode);
    }
}
