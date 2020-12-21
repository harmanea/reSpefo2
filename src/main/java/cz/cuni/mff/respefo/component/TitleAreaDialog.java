package cz.cuni.mff.respefo.component;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.builders.widgets.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.builders.widgets.LabelBuilder.newLabel;

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
        final Composite composite = newComposite()
                .layout(gridLayout().margins(0).verticalSpacing(0))
                .gridLayoutData(GridData.FILL_BOTH)
                .build(shell);

        createTitleArea(composite);
        createDialogArea(composite);
        createButtonsArea(composite);
    }

    private void createTitleArea(Composite parent) {
        final Composite composite = newComposite()
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING)
                .layout(gridLayout(2, false).margins(15))
                .build(parent);
        composite.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        composite.setBackgroundMode(SWT.INHERIT_FORCE);

        newLabel()
                .bold()
                .text(title)
                .gridLayoutData(GridData.FILL_BOTH)
                .build(composite);

        imageLabel = newLabel()
                .gridLayoutData(SWT.RIGHT, SWT.CENTER, false, true, 1, 2)
                .build(composite);

        messageLabel = newLabel()
                .gridLayoutData(GridData.FILL_BOTH)
                .build(composite);
    }
}
