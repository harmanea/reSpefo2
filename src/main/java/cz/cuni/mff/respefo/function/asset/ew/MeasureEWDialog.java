package cz.cuni.mff.respefo.function.asset.ew;


import cz.cuni.mff.respefo.component.TitleAreaDialog;
import cz.cuni.mff.respefo.util.FileType;
import cz.cuni.mff.respefo.util.utils.FileDialogs;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

import static cz.cuni.mff.respefo.util.builders.ButtonBuilder.pushButton;
import static cz.cuni.mff.respefo.util.builders.CompositeBuilder.composite;
import static cz.cuni.mff.respefo.util.builders.GridDataBuilder.gridData;
import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.builders.LabelBuilder.label;

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

        final Composite topComposite = composite(parent)
                .layout(gridLayout(2, false).margins(15).horizontalSpacing(10))
                .layoutData(gridData(GridData.FILL_BOTH).widthHint(500).heightHint(300))
                .build();

        label(topComposite)
                .layoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1))
                .text("Select .lst files with measurements:")
                .build();

        final List list = new List(topComposite, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
        list.setLayoutData(new GridData(GridData.FILL_BOTH));
        list.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == SWT.DEL) {
                    removeStlFile(list);
                } else if (e.keyCode == SWT.INSERT) {
                    addStlFile(list);
                }
            }
        });

        final Composite buttonsComposite = composite(topComposite)
                .layout(gridLayout().margins(0))
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_BEGINNING)
                .build();

        pushButton(buttonsComposite)
                .text("Add")
                .gridLayoutData(GridData.FILL_BOTH)
                .onSelection(event -> addStlFile(list))
                .build();

        pushButton(buttonsComposite)
                .text("Remove")
                .gridLayoutData(GridData.FILL_BOTH)
                .onSelection(event -> removeStlFile(list))
                .build();
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
