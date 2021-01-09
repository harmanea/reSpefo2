package cz.cuni.mff.respefo.function.asset.port;

import cz.cuni.mff.respefo.component.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import java.util.Arrays;
import java.util.stream.IntStream;

import static cz.cuni.mff.respefo.util.builders.FillLayoutBuilder.fillLayout;
import static cz.cuni.mff.respefo.util.builders.GridDataBuilder.gridData;
import static cz.cuni.mff.respefo.util.builders.widgets.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.builders.widgets.TableBuilder.newTable;

public class InteractiveChironSelectionDialog extends TitleAreaDialog {

    private final String[][] names;
    private final boolean[] selected;

    public InteractiveChironSelectionDialog(String[][] names) {
        super("Select echels");

        this.names = names;
        this.selected = new boolean[names.length];
    }

    public boolean[] getSelected() {
        return selected;
    }

    @Override
    protected void createDialogArea(Composite parent) {
        setMessage("Select which echels to import and rectify.", SWT.ICON_INFORMATION);

        final Composite composite = newComposite()
                .layout(fillLayout().margins(15))
                .layoutData(gridData(GridData.FILL_BOTH).widthHint(400).heightHint(400).build())
                .build(parent);

        newTable(SWT.CHECK | SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL)
                .headerVisible(true)
                .linesVisible(true)
                .columns("No.", "From", "To")
                .columnWidths(100, 150, 150)
                .items(Arrays.asList(names))
                .onSelection(event -> {
                    if (event.detail == SWT.CHECK) {
                        TableItem tableItem = (TableItem) event.item;
                        selected[((Table) event.widget).indexOf(tableItem)] = tableItem.getChecked();

                        if (IntStream.range(0, selected.length).anyMatch(i -> selected[i])) {
                            getButton(SWT.OK).setEnabled(true);
                            setMessage("Select which echels to import and rectify.", SWT.ICON_INFORMATION);
                        } else {
                            getButton(SWT.OK).setEnabled(false);
                            setMessage("At least one echel must be selected.", SWT.ICON_WARNING);
                        }
                    }
                })
                .build(composite);
    }

    @Override
    protected void createButtons(Composite parent) {
        super.createButtons(parent);

        getButton(SWT.OK).setEnabled(false);
    }
}
