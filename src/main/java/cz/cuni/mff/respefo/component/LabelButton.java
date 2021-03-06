package cz.cuni.mff.respefo.component;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import static cz.cuni.mff.respefo.util.layout.FillLayoutBuilder.fillLayout;

public class LabelButton extends Composite {
    private final CLabel label;

    public LabelButton(Composite parent, int style) {
        super(parent, style);

        setBackgroundMode(SWT.INHERIT_FORCE);
        setLayout(fillLayout().margins(0).build());

        label = new CLabel(this, style);

        label.addListener(SWT.MouseEnter, event -> setHighlightedBackground());
        label.addListener(SWT.MouseExit, event -> setDefaultBackground());
    }

    public void setText(String text) {
        label.setText(text);
    }

    public void setImage(Image image) {
        label.setImage(image);
    }

    public void onClick(Runnable action) {
        label.addListener(SWT.MouseUp, event -> action.run());
    }

    private void setDefaultBackground() {
        setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
    }

    private void setHighlightedBackground() {
        setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
    }

    @Override
    public void setToolTipText(String string) {
        label.setToolTipText(string);
    }
}
