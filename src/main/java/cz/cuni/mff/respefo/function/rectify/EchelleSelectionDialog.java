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
import static java.util.Collections.emptySortedSet;
import static java.util.Collections.unmodifiableSet;

public class EchelleSelectionDialog extends TitleAreaDialog {

    private static SortedSet<Integer> previousSelected = emptySortedSet();

    private final String[][] names;
    private final SortedSet<Integer> selected;  // zero based
    private final Set<Integer> grayed;
    private boolean printParameters;

    public EchelleSelectionDialog(String[][] names, Set<Integer> grayed, boolean printParameters) {
        super("Select echelle orders");

        this.names = names;
        this.selected = new TreeSet<>(previousSelected);
        this.grayed = unmodifiableSet(grayed);
        this.printParameters = printParameters;

        this.selected.removeAll(grayed);
    }

    @Override
    protected void buttonPressed(int returnCode) {
        if (returnCode == SWT.OK) {
            synchronized (this) {
                previousSelected = new TreeSet<>(selected);
                previousSelected.addAll(grayed);
            }
        }

        super.buttonPressed(returnCode);
    }

    public List<Integer> getSelectedIndices() {
        return new ArrayList<>(selected);
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

        final Table table = newTable(SWT.CHECK | SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL)
                .gridLayoutData(GridData.FILL_BOTH)
                .headerVisible(true)
                .linesVisible(true)
                .columns("No.", "From", "To")
                .columnWidths(75, 150, 150)
                .items(Arrays.asList(names))
                .decorate((i, item) -> {
                    if (grayed.contains(i)) {
                        item.setChecked(true);
                        item.setGrayed(true);
                        item.setForeground(ComponentManager.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
                    } else if (selected.contains(i)) {
                        item.setChecked(true);
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

                        if (grayed.contains(index)) {
                            tableItem.setChecked(true);
                            tableItem.setGrayed(!tableItem.getGrayed());
                        }

                    }
                })
                .build(composite);

        newButton(SWT.PUSH)
                .gridLayoutData(GridData.FILL_HORIZONTAL)
                .text("(De)select multiple")
                .onSelection(event -> {
                    MultipleOrdersSelectionDialog dialog = new MultipleOrdersSelectionDialog(names.length);
                    if (dialog.openIsOk()) {
                        int from = dialog.getFrom();
                        int to = dialog.getTo();
                        boolean select = dialog.isSelect();

                        for (int i = Math.min(from, to) - 1; i < Math.max(from, to); i++) {
                            if (select) {
                                selected.add(i);
                            } else {
                                selected.remove(i);
                            }

                            if (grayed.contains(i)) {
                                table.getItem(i).setGrayed(!select);
                            } else {
                                table.getItem(i).setChecked(select);
                            }
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
