package cz.cuni.mff.respefo.component;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import java.util.function.Consumer;

import static cz.cuni.mff.respefo.util.builders.FillLayoutBuilder.fillLayout;

public class Toggle extends Composite {

    private boolean toggled;
    private final CLabel label;

    private Consumer<Boolean> toggleAction = t -> {};

    protected Toggle(Composite parent, int style) {
        this(parent, style, 0);
    }

    protected Toggle(Composite parent, int style, int margin) {
        super(parent, style);
        setLayout(fillLayout().margins(margin).build());

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

    public boolean isToggled() {
        return toggled;
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

    public void setToggleAction(Consumer<Boolean> toggleAction) {
        this.toggleAction = toggleAction;
    }

    public void toggle() {
        setToggled(!toggled);
        toggleAction.accept(toggled);
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
}
