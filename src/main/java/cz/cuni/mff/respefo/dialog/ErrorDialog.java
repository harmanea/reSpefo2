package cz.cuni.mff.respefo.dialog;

import cz.cuni.mff.respefo.util.utils.ExceptionUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import static cz.cuni.mff.respefo.util.layout.GridDataBuilder.gridData;
import static cz.cuni.mff.respefo.util.layout.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.widget.CompositeBuilder.newComposite;
import static org.eclipse.swt.SWT.*;

public class ErrorDialog extends TitleAreaDialog {
    private final String message;
    private final Throwable cause;

    public ErrorDialog(String message, Throwable cause) {
        super("Error");

        this.message = message;
        this.cause = cause;
    }

    @Override
    protected void createButtons(Composite parent) {
        createButton(parent, SWT.OK, "   OK   ", true);
    }

    @Override
    protected void createDialogArea(Composite parent) {
        setMessage(message, SWT.ICON_ERROR);

        final Composite composite = newComposite()
                .layout(gridLayout().margins(5))
                .layoutData(gridData(GridData.FILL_BOTH).widthHint(600).heightHint(200).build())
                .build(parent);

        final StyledText text = new StyledText(composite, BORDER | READ_ONLY | H_SCROLL | V_SCROLL);
        text.setLayoutData(new GridData(GridData.FILL_BOTH));
        text.setMargins(5, 5, 5, 5);
        text.setText(ExceptionUtils.getStackTrace(cause));
    }
}
