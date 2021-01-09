package cz.cuni.mff.respefo.function.asset.ew;


import cz.cuni.mff.respefo.component.TitleAreaDialog;
import cz.cuni.mff.respefo.util.FileDialogs;
import cz.cuni.mff.respefo.util.FileType;
import cz.cuni.mff.respefo.util.builders.widgets.ButtonBuilder;
import cz.cuni.mff.respefo.util.builders.widgets.ListBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

import static cz.cuni.mff.respefo.util.builders.GridDataBuilder.gridData;
import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.builders.widgets.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.builders.widgets.LabelBuilder.newLabel;

public class MeasureEWDialog extends TitleAreaDialog {

    private String[] items;

    public MeasureEWDialog() {
        super("Measure EW");
    }

    public String[] getItems() {
        return items;
    }

    @Override
    protected void createDialogArea(Composite parent) {
        setMessage("Measure equivalent width and other spectrophotometric quantities", SWT.ICON_INFORMATION);

        final Composite topComposite = newComposite()
                .layout(gridLayout(2, false).margins(15).horizontalSpacing(10))
                .layoutData(gridData(GridData.FILL_BOTH).widthHint(500).heightHint(300).build())
                .build(parent);

        newLabel()
                .gridLayoutData(SWT.FILL, SWT.TOP, true, false, 2, 1)
                .text("Select .lst files with measurements:")
                .build(topComposite);

        final List list = ListBuilder.newList(SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL)
                .gridLayoutData(GridData.FILL_BOTH)
                .listener(SWT.KeyDown, e -> {
                    if (e.keyCode == SWT.DEL) {
                        removeStlFile((List) e.widget);
                    } else if (e.keyCode == SWT.INSERT) {
                        addStlFile((List) e.widget);
                    }
                }).build(topComposite);

        final Composite buttonsComposite = newComposite()
                .layout(gridLayout().margins(0))
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_BEGINNING)
                .build(topComposite);

        ButtonBuilder buttonBuilder = ButtonBuilder.newButton(SWT.PUSH).gridLayoutData(GridData.FILL_BOTH);

        buttonBuilder.text("Add").onSelection(event -> addStlFile(list)).build(buttonsComposite);
        buttonBuilder.text("Remove").onSelection(event -> removeStlFile(list)).build(buttonsComposite);
    }

    @Override
    protected void createButtons(Composite parent) {
        super.createButtons(parent);

        getButton(SWT.OK).setEnabled(false);
    }

    private void addStlFile(List list) {
        String fileName = FileDialogs.openFileDialog(FileType.STL, false);
        if (fileName != null) {
            list.add(fileName);

            items = list.getItems();

            if (items.length == 1) {
                setMessage("Measure equivalent width and other spectrophotometric quantities", SWT.ICON_INFORMATION);
                getButton(SWT.OK).setEnabled(true);
            }
        }
    }

    private void removeStlFile(List list) {
        if (list.getSelectionIndex() != -1) {
            list.remove(list.getSelectionIndex());

            items = list.getItems();

            if (items.length == 0) {
                setMessage("Select at least one .stl file", SWT.ICON_WARNING);
                getButton(SWT.OK).setEnabled(false);
            }
        }
    }
}
