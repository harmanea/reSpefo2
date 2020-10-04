package cz.cuni.mff.respefo.function.asset.rv;

import cz.cuni.mff.respefo.component.TitleAreaDialog;
import cz.cuni.mff.respefo.util.FileType;
import cz.cuni.mff.respefo.util.utils.FileUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import java.util.Arrays;

import static cz.cuni.mff.respefo.util.builders.ButtonBuilder.pushButton;
import static cz.cuni.mff.respefo.util.builders.CompositeBuilder.composite;
import static cz.cuni.mff.respefo.util.builders.GridDataBuilder.gridData;
import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.builders.LabelBuilder.label;

public class MeasureRVDialog extends TitleAreaDialog {

    private String[] items;
    private Boolean[] correctionFlags;

    public MeasureRVDialog() {
        super("Measure RV");
    }

    public String[] getItems() {
        return items;
    }

    public Boolean[] getCorrectionFlags() {
        return correctionFlags;
    }

    @Override
    protected void createDialogArea(Composite parent) {
        setMessage("Measure radial velocities", SWT.ICON_INFORMATION);

        final Composite topComposite = composite(parent)
                .layout(gridLayout(2, false).margins(15).horizontalSpacing(10))
                .layoutData(gridData(GridData.FILL_BOTH).widthHint(500).heightHint(300))
                .build();

        label(topComposite)
                .layoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1))
                .text("Select .lst files with measurements:")
                .build();

        final Table table = new Table(topComposite, SWT.CHECK | SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        table.addListener(SWT.Selection, event -> {
            if (event.detail == SWT.CHECK) {
                TableItem tableItem = (TableItem) event.item;
                correctionFlags[table.indexOf(tableItem)] = tableItem.getChecked();
            }
        });

        final Composite buttonsComposite = composite(topComposite)
                .layout(gridLayout().margins(0))
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_BEGINNING)
                .build();

        pushButton(buttonsComposite)
                .text("Add")
                .gridLayoutData(GridData.FILL_BOTH)
                .onSelection(event -> addStlFile(table))
                .build();

        pushButton(buttonsComposite)
                .text("Remove")
                .gridLayoutData(GridData.FILL_BOTH)
                .onSelection(event -> removeStlFile(table))
                .build();
    }

    @Override
    protected void createButtons(Composite parent) {
        super.createButtons(parent);

        getButton(SWT.OK).setEnabled(false);
    }

    private void addStlFile(Table table) {
        String fileName = FileUtils.openFileDialog(FileType.STL, false);
        if (fileName != null) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(fileName);

            items = Arrays.stream(table.getItems()).map(TableItem::getText).toArray(String[]::new);
            correctionFlags = Arrays.stream(table.getItems()).map(TableItem::getChecked).toArray(Boolean[]::new);

            if (items.length == 1) {
                setMessage("Measure radial velocities", SWT.ICON_INFORMATION);
                getButton(SWT.OK).setEnabled(true);
            }
        }
    }

    private void removeStlFile(Table table) {
        if (table.getSelectionIndex() != -1) {
            table.remove(table.getSelectionIndex());

            items = Arrays.stream(table.getItems()).map(TableItem::getText).toArray(String[]::new);
            correctionFlags = Arrays.stream(table.getItems()).map(TableItem::getChecked).toArray(Boolean[]::new);

            if (items.length == 0) {
                setMessage("Select at least one .stl file", SWT.ICON_WARNING);
                getButton(SWT.OK).setEnabled(false);
            }
        }
    }
}
