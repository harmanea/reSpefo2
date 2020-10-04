package cz.cuni.mff.respefo.function.asset.ew;

import cz.cuni.mff.respefo.component.SpefoDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import java.util.Arrays;

import static cz.cuni.mff.respefo.util.builders.CompositeBuilder.composite;
import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.builders.LabelBuilder.label;

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
        Composite composite = composite(parent)
                .layout(gridLayout(2, false).margins(15).horizontalSpacing(10))
                .layoutData(new GridData(GridData.FILL_BOTH))
                .build();

        label(composite)
                .text("Category:")
                .layoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING))
                .build();

        Combo combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
        combo.setItems(Arrays.stream(MeasureEWResultPointCategory.values()).map(MeasureEWResultPointCategory::name).toArray(String[]::new));
        combo.addModifyListener(event -> {
            getButton(SWT.OK).setEnabled(!combo.getText().equals(""));
            category = combo.getText();
        });
        combo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                String ch = String.valueOf(e.character).toLowerCase();
                for (MeasureEWResultPointCategory pointCategory : MeasureEWResultPointCategory.values()) {
                    if (pointCategory.name().toLowerCase().startsWith(ch)) {
                        combo.setText(pointCategory.name());
                        break;
                    }
                }
            }
        });
    }

    @Override
    protected void createButtons(Composite parent) {
        super.createButtons(parent);

        getButton(SWT.OK).setEnabled(false);
    }
}
