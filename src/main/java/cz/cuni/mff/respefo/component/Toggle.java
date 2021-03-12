package cz.cuni.mff.respefo.component;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;

import java.util.function.Consumer;

public abstract class Toggle extends Composite {

    private boolean toggled;
    private Consumer<Boolean> toggleAction = t -> {};

    Toggle(Composite parent, int style) {
        super(parent, style);

        toggled = false;
        setDefaultBackground();
    }

    public abstract void setText(String text);

    public abstract void setImage(Image image);

    public void setToggleAction(Consumer<Boolean> toggleAction) {
        this.toggleAction = toggleAction;
    }

    public void appendToggleAction(Consumer<Boolean> toggleAction) {
        final Consumer<Boolean> oldToggleAction = this.toggleAction;

        this.toggleAction = t -> {
            oldToggleAction.accept(t);
            toggleAction.accept(t);
        };
    }

    public boolean isToggled() {
        return toggled;
    }

    /**
     * Unlike the function {@link #toggle()}, this will not trigger the toggle action
     */
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

    public void toggle() {
        setToggled(!toggled);
        toggleAction.accept(toggled);
    }

    protected void addListeners(Widget widget) {
        widget.addListener(SWT.MouseEnter, event -> {
            if (!toggled) {
                setHighlightedBackground();
            }
        });

        widget.addListener(SWT.MouseExit, event -> {
            if (!toggled) {
                setDefaultBackground();
            }
        });

        widget.addListener(SWT.MouseUp, event -> toggle());
    }

    private void setDefaultBackground() {
        setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
    }

    private void setHighlightedBackground() {
        setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
    }

    private void setToggledBackground() {
        setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
    }
}
