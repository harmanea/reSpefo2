package cz.cuni.mff.respefo.component;

import cz.cuni.mff.respefo.resources.ColorManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import static cz.cuni.mff.respefo.resources.ColorResource.LIGHT_GRAY;
import static cz.cuni.mff.respefo.resources.ColorResource.WHITE;
import static cz.cuni.mff.respefo.util.builders.CompositeBuilder.composite;
import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.builders.LabelBuilder.label;

public abstract class TitleAreaDialog extends SpefoDialog {
    private final String title;

    private Label messageLabel;
    private Label imageLabel;

    protected TitleAreaDialog(String title) {
        super("");
        this.title = title;
    }

    protected void setMessage(String text, int icon) {
        messageLabel.setText(text);
        imageLabel.setImage(ComponentManager.getDisplay().getSystemImage(icon));

        imageLabel.getParent().redraw();
    }

    @Override
    void createContents(Shell shell) {
        final Composite composite = composite(shell)
                .layout(gridLayout().margins(0).verticalSpacing(0))
                .gridLayoutData(GridData.FILL_BOTH)
                .build();

        createTitleArea(composite);
        createDialogArea(composite);
        createButtonsArea(composite);
    }

    private void createTitleArea(Composite parent) {
        final Composite composite = composite(parent)
                .layoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false))
                .layout(gridLayout(2, false).margins(15))
                .build();
        composite.addPaintListener(e -> {
            e.gc.setBackground(ColorManager.getColor(WHITE));
            e.gc.fillRectangle(0, 0, e.width, e.height);

            e.gc.setForeground(ColorManager.getColor(WHITE));
            e.gc.setBackground(ColorManager.getColor(LIGHT_GRAY));
            e.gc.fillGradientRectangle(e.width / 2, 0, e.width, e.height, false);
        });

        label(composite)
                .text(title)
                .bold()
                .layoutData(new GridData(GridData.FILL_BOTH))
                .build();

        imageLabel = label(composite)
                .layoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, true, 1, 2))
                .build();

        messageLabel = label(composite)
                .layoutData(new GridData(GridData.FILL_BOTH))
                .build();
    }
}
