package cz.cuni.mff.respefo.function.ew;

import cz.cuni.mff.respefo.dialog.SpefoDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import java.util.Arrays;

import static cz.cuni.mff.respefo.util.layout.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.widget.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.widget.LabelBuilder.newLabel;

public class MeasureEWCategoryDialog extends SpefoDialog {

    private String category;

    public MeasureEWResultPointCategory getCategory() {
        return MeasureEWResultPointCategory.valueOf(category);
    }

    public MeasureEWCategoryDialog() {
        super("Select category");
    }

    @Override
    protected void createDialogArea(Composite parent) {
        final Composite composite = newComposite()
                .layout(gridLayout(2, false).margins(15).horizontalSpacing(10))
                .gridLayoutData(GridData.FILL_BOTH)
                .build(parent);

        newLabel().text("Category:").gridLayoutData(GridData.HORIZONTAL_ALIGN_BEGINNING).build(composite);

        final Combo combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
        combo.setItems(Arrays.stream(MeasureEWResultPointCategory.values()).map(MeasureEWResultPointCategory::name).toArray(String[]::new));
        combo.addModifyListener(event -> {
            getButton(SWT.OK).setEnabled(!combo.getText().equals(""));
            category = combo.getText();
        });
        combo.addKeyListener(KeyListener.keyPressedAdapter(e -> {
            String ch = String.valueOf(e.character).toLowerCase();
            for (MeasureEWResultPointCategory pointCategory : MeasureEWResultPointCategory.values()) {
                if (pointCategory.name().toLowerCase().startsWith(ch)) {
                    combo.setText(pointCategory.name());
                    break;
                }
            }
        }));
    }

    @Override
    protected void createButtons(Composite parent) {
        super.createButtons(parent);

        getButton(SWT.OK).setEnabled(false);
    }
}
