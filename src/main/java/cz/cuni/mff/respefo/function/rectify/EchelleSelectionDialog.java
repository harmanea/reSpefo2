package cz.cuni.mff.respefo.function.rectify;

import cz.cuni.mff.respefo.dialog.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static cz.cuni.mff.respefo.util.layout.FillLayoutBuilder.fillLayout;
import static cz.cuni.mff.respefo.util.layout.GridDataBuilder.gridData;
import static cz.cuni.mff.respefo.util.widget.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.widget.TableBuilder.newTable;

public class EchelleSelectionDialog extends TitleAreaDialog {

    private final String[][] names;
    private final Set<Integer> selected;  // zero based

    public EchelleSelectionDialog(String[][] names) {
        super("Select echelle orders");

        this.names = names;
        this.selected = new HashSet<>();
    }

    public Set<Integer> getSelectedIndices() {
        return selected;
    }

    @Override
    protected void createDialogArea(Composite parent) {
        setMessage("Select which echelle orders to rectify manually.", SWT.ICON_INFORMATION);

        final Composite composite = newComposite()
                .layout(fillLayout().margins(15))
                .layoutData(gridData(GridData.FILL_BOTH).widthHint(400).heightHint(400).build())
                .build(parent);

        newTable(SWT.CHECK | SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL)
                .headerVisible(true)
                .linesVisible(true)
                .columns("No.", "From", "To")
                .columnWidths(50, 150, 150)
                .items(Arrays.asList(names))
                .onSelection(event -> {
                    if (event.detail == SWT.CHECK) {
                        TableItem tableItem = (TableItem) event.item;
                        int index = ((Table) event.widget).indexOf(tableItem);
                        if (tableItem.getChecked()) {
                            selected.add(index);
                        } else {
                            selected.remove(index);
                        }
                    }
                })
                .build(composite);
    }
}
