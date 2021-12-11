package cz.cuni.mff.respefo.function.rectify;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.dialog.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static cz.cuni.mff.respefo.util.layout.GridDataBuilder.gridData;
import static cz.cuni.mff.respefo.util.layout.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.widget.ButtonBuilder.newButton;
import static cz.cuni.mff.respefo.util.widget.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.widget.TableBuilder.newTable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class EchelleSelectionDialog extends TitleAreaDialog {

    private final String[][] names;
    private final boolean[] previous;
    private final boolean[] keep;
    private final boolean[] edit;

    public EchelleSelectionDialog(String[][] names, boolean[] previous) {
        super("Select echelle orders");

        this.names = names;
        this.previous = previous;
        this.keep = Arrays.copyOf(previous, previous.length);
        this.edit = new boolean[previous.length];
    }

    public Set<Integer> getIndicesToKeep() {
        return IntStream.range(0, keep.length)
                .filter(i -> keep[i] || edit[i])
                .boxed()
                .collect(toSet());
    }

    public List<Integer> getIndicesToEdit() {
        return IntStream.range(0, edit.length)
                .filter(i -> edit[i])
                .boxed()
                .collect(toList());
    }

    @Override
    protected void createDialogArea(Composite parent) {
        setMessage("Select which echelle orders to import and rectify.", SWT.ICON_INFORMATION);

        final Composite composite = newComposite()
                .layout(gridLayout().margins(15))
                .layoutData(gridData(GridData.FILL_BOTH).widthHint(400).heightHint(400).build())
                .build(parent);

//        final Composite buttonsComposite = newComposite()
//                .layout(gridLayout().marginBottom(10))
//                .gridLayoutData(GridData.FILL_HORIZONTAL)
//                .build(composite);
//
//        newButton(SWT.CHECK)
//                .gridLayoutData(GridData.FILL_HORIZONTAL)
//                .enabled(false)
//                .text("Do not use")
//                .build(buttonsComposite);
//
//        newButton(SWT.CHECK)
//                .gridLayoutData(GridData.FILL_HORIZONTAL)
//                .enabled(false)
//                .selection(true)
//                .grayed(true)
//                .text("Keep as is")
//                .build(buttonsComposite);
//
//        newButton(SWT.CHECK)
//                .gridLayoutData(GridData.FILL_HORIZONTAL)
//                .enabled(false)
//                .selection(true)
//                .text("Edit")
//                .build(buttonsComposite);

        final Table table = newTable(SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL)
                .gridLayoutData(GridData.FILL_BOTH)
                .headerVisible(true)
                .linesVisible(true)
                .columns("", "No.", "From", "To")
                .columnWidths(25, 30, 150, 150)
                .items(Arrays.asList(names))
                .build(composite);

        FontData fontData = table.getFont().getFontData()[0];
        Font boldFont = new Font(ComponentManager.getDisplay(), new FontData(fontData.getName(), fontData.getHeight(), SWT.BOLD));
        table.addDisposeListener(event -> boldFont.dispose());

        for (int i = 0; i < previous.length; i++) {
            final int index = i;
            final TableItem item = table.getItem(i);

            if (previous[i]) {
                item.setFont(boldFont);
            }

            Button button = newButton(SWT.CHECK)
                    .gridLayoutData(GridData.FILL_BOTH)
                    .selection(previous[i])
                    .grayed(previous[i])
                    .onSelection(e -> {
                        Button b = (Button) e.widget;
                        if (b.getSelection()) {  // was not selected previously
                            if (!b.getGrayed() && previous[index]) {
                                b.setGrayed(true);
                                keep[index] = true;
                            } else {
                                edit[index] = true;
                            }
                        } else {  // was either grayed or selected previously
                            if (b.getGrayed()) {
                                b.setGrayed(false);
                                b.setSelection (true);
                                edit[index] = true;
                            } else {
                                edit[index] = false;
                            }
                            keep[index] = false;
                        }

                        if (IntStream.range(0, previous.length).anyMatch(j -> keep[j] || edit[j])) {
                            getButton(SWT.OK).setEnabled(true);
                            setMessage("Select which echelle orders to import and rectify.", SWT.ICON_INFORMATION);
                        } else {
                            getButton(SWT.OK).setEnabled(false);
                            setMessage("At least one echelle order must be selected.", SWT.ICON_WARNING);
                        }
                    })
                    .pack()
                    .build(table);

            TableEditor editor = new TableEditor(table);
            editor.minimumWidth = button.getSize().x;
            editor.grabVertical = true;
            editor.grabHorizontal = true;
            editor.verticalAlignment = SWT.CENTER;
            editor.horizontalAlignment = SWT.CENTER;
            editor.setEditor(button, item, 0);
        }
    }

    @Override
    protected void createButtons(Composite parent) {
        super.createButtons(parent);

        getButton(SWT.OK).setEnabled(IntStream.range(0, previous.length).anyMatch(i -> previous[i]));
    }
}
