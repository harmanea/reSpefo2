package cz.cuni.mff.respefo.function.project;

import cz.cuni.mff.respefo.dialog.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import static cz.cuni.mff.respefo.util.layout.GridDataBuilder.gridData;
import static cz.cuni.mff.respefo.util.layout.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.widget.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.widget.LabelBuilder.newLabel;
import static cz.cuni.mff.respefo.util.widget.TextBuilder.newText;

public class ProjectPrefixDialog extends TitleAreaDialog {

    private String prefix;
    protected Composite dialogAreaComposite;

    public ProjectPrefixDialog(String title, String suggestedPrefix) {
        super(title);
        this.prefix = suggestedPrefix;
    }

    public String getPrefix() {
        return prefix;
    }

    @Override
    protected void createDialogArea(Composite parent) {
        setMessage("Select a prefix for the project", SWT.ICON_INFORMATION);

        dialogAreaComposite = newComposite()
                .layout(gridLayout(2, false).margins(15).verticalSpacing(20).horizontalSpacing(10))
                .layoutData(gridData(GridData.FILL_BOTH).widthHint(450))
                .build(parent);

        newLabel().text("Prefix:").gridLayoutData(GridData.HORIZONTAL_ALIGN_BEGINNING).build(dialogAreaComposite);

        newText(SWT.SINGLE | SWT.BORDER)
                .text(prefix)
                .gridLayoutData(GridData.FILL_HORIZONTAL)
                .onModifiedValue(value -> {
                    if (value.isEmpty()) {
                        setMessage("The prefix cannot be blank", SWT.ICON_WARNING);
                        getButton(SWT.OK).setEnabled(false);
                    } else {
                        if (value.length() == 3) {
                            setMessage("Select a prefix for the project", SWT.ICON_INFORMATION);
                        } else {
                            setMessage("The recommended prefix length is three", SWT.ICON_WARNING);
                        }

                        prefix = value;
                        getButton(SWT.OK).setEnabled(true);
                    }
                })
                .selectText()
                .build(dialogAreaComposite);
    }
}
