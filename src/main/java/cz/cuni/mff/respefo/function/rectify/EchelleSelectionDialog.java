package cz.cuni.mff.respefo.function.rectify;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.dialog.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import java.util.*;

import static cz.cuni.mff.respefo.util.layout.GridDataBuilder.gridData;
import static cz.cuni.mff.respefo.util.layout.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.widget.ButtonBuilder.newButton;
import static cz.cuni.mff.respefo.util.widget.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.widget.TableBuilder.newTable;

public class EchelleSelectionDialog extends TitleAreaDialog {

    private final String[][] names;
    private final SortedSet<Integer> selected;  // zero based
    private final Set<Integer> disabled;
    private boolean printParameters;

    public EchelleSelectionDialog(String[][] names, Set<Integer> disabled, boolean printParameters) {
        super("Select echelle orders");

        this.names = names;
        this.selected = new TreeSet<>();
        this.disabled = disabled;
        this.printParameters = printParameters;
    }

    public Iterator<Integer> getSelectedIndices() {
        return selected.iterator();
    }

    public boolean printParameters() {
        return printParameters;
    }

    @Override
    protected void createDialogArea(Composite parent) {
        setMessage("Select which echelle orders to rectify manually.", SWT.ICON_INFORMATION);

        final Composite composite = newComposite()
                .layout(gridLayout().margins(15).verticalSpacing(10))
                .layoutData(gridData(GridData.FILL_BOTH).widthHint(400).heightHint(450))
                .build(parent);

        newTable(SWT.CHECK | SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL)
                .gridLayoutData(GridData.FILL_BOTH)
                .headerVisible(true)
                .linesVisible(true)
                .columns("No.", "From", "To")
                .columnWidths(75, 150, 150)
                .items(Arrays.asList(names))
                .decorate((i, item) -> {
                    if (disabled.contains(i)) {
                        item.setChecked(true);
                        item.setGrayed(true);
                        item.setForeground(ComponentManager.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
                    }
                })
                .onSelection(event -> {
                    if (event.detail == SWT.CHECK) {
                        TableItem tableItem = (TableItem) event.item;
                        int index = ((Table) event.widget).indexOf(tableItem);

                        if (tableItem.getGrayed() || tableItem.getChecked()) {
                            selected.add(index);
                        } else {
                            selected.remove(index);
                        }

                        if (disabled.contains(index)) {
                            tableItem.setChecked(true);
                            tableItem.setGrayed(!tableItem.getGrayed());
                        }

                    }
                })
                .build(composite);

        newButton(SWT.CHECK)
                .gridLayoutData(GridData.FILL_HORIZONTAL)
                .text("Print parameters to file")
                .selection(printParameters)
                .onSelection(event -> printParameters = ((Button) event.widget).getSelection())
                .build(composite);
    }
}
