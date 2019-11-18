package cz.cuni.mff.respefo.component;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

public abstract class Toggle extends Composite {

    protected Toggle(Composite parent, int style) {
        this(parent, style, 0);
    }

    protected Toggle(Composite parent, int style, int margin) {
        super(parent, style);

        FillLayout layout = new FillLayout();
        layout.marginHeight = margin;
        layout.marginWidth = margin;
        setLayout(layout);

        toggled = false;
        label = new CLabel(this, style);

        label.addListener(SWT.MouseEnter, event -> {
            if (!toggled) {
                setHighlightedBackground();
            }
        });

        label.addListener(SWT.MouseExit, event -> {
            if (!toggled) {
                setDefaultBackground();
            }
        });

        label.addListener(SWT.MouseUp, event -> toggle());
    }

    public boolean isToggled() {
        return toggled;
    }

    public final void setToggled(boolean toggled) {
        if (toggled != this.toggled) {
            if (toggled) {
                setToggledBackground();
            } else {
                setDefaultBackground();
            }

            this.toggled = toggled;
        }
    }

    public void setText(String text) {
        label.setText(text);
    }

    public void setImage(Image image) {
        label.setImage(image);
    }

    public void setTooltipText(String tooltipText) {
        label.setToolTipText(tooltipText);
    }

    public void toggle() {
        toggleAction();
        setToggled(!toggled);
    }

    private void setDefaultBackground() {
        label.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
    }

    private void setHighlightedBackground() {
        label.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
    }

    private void setToggledBackground() {
        label.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
    }

    protected abstract void toggleAction();

    private boolean toggled;
    private final CLabel label;
}
