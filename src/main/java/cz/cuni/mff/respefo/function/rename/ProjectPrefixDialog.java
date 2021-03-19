package cz.cuni.mff.respefo.function.rename;

import cz.cuni.mff.respefo.dialog.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import static cz.cuni.mff.respefo.util.layout.GridDataBuilder.gridData;
import static cz.cuni.mff.respefo.util.layout.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.widget.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.widget.LabelBuilder.newLabel;
import static cz.cuni.mff.respefo.util.widget.TextBuilder.newText;

public class ProjectPrefixDialog extends TitleAreaDialog {

    private String prefix;

    public ProjectPrefixDialog() {
        super("Rename project");

        prefix = "";
    }

    public String getPrefix() {
        return prefix;
    }

    @Override
    protected void createDialogArea(Composite parent) {
        setMessage("Select a prefix for the project", SWT.ICON_INFORMATION);

        final Composite composite = newComposite()
                .layout(gridLayout(2, false).margins(15).horizontalSpacing(10))
                .layoutData(gridData(GridData.FILL_BOTH).widthHint(400).build())
                .build(parent);

        newLabel().text("Prefix:").gridLayoutData(GridData.HORIZONTAL_ALIGN_BEGINNING).build(composite);

        newText(SWT.SINGLE | SWT.BORDER)
                .text("")
                .gridLayoutData(GridData.FILL_HORIZONTAL)
                .onModify(event -> {
                    prefix = ((Text) event.widget).getText();
                    if (prefix.length() == 0) {
                        setMessage("The prefix cannot be blank", SWT.ICON_WARNING);
                        getButton(SWT.OK).setEnabled(false);
                    } else {
                        if (prefix.length() == 3) {
                            setMessage("Select a prefix for the project", SWT.ICON_INFORMATION);
                        } else {
                            setMessage("The recommended prefix length is three", SWT.ICON_WARNING);
                        }
                        getButton(SWT.OK).setEnabled(true);
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
