package cz.cuni.mff.respefo.component;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import static cz.cuni.mff.respefo.util.builders.FillLayoutBuilder.fillLayout;

public class HorizontalToggle extends Toggle {

    private final CLabel label;

    protected HorizontalToggle(Composite parent, int style) {
        this(parent, style, 0);
    }

    protected HorizontalToggle(Composite parent, int style, int margin) {
        super(parent, style);

        setBackgroundMode(SWT.INHERIT_FORCE);
        setLayout(fillLayout().margins(margin).build());

        label = new CLabel(this, style);
        addListeners(label);
    }

    @Override
    public void setText(String text) {
        label.setText(text);
    }

    @Override
    public void setImage(Image image) {
        label.setImage(image);
    }

    @Override
    public void setToolTipText(String tooltipText) {
        label.setToolTipText(tooltipText);
    }

}
