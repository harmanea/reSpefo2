package cz.cuni.mff.respefo.util.builders;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Control;

@SuppressWarnings("unchecked")
public abstract class ControlBuilder<C extends Control, B extends ControlBuilder<C, B>> {
    protected C control;

    public B background(Color color) {
        control.setBackground(color);

        return (B) this;
    }

    public B foreground(Color color) {
        control.setForeground(color);

        return (B) this;
    }

    public B backgroundImage(Image image) {
        control.setBackgroundImage(image);

        return (B) this;
    }

    public B enabled(boolean enabled) {
        control.setEnabled(enabled);

        return (B) this;
    }

    public B visible(boolean visible) {
        control.setVisible(visible);

        return (B) this;
    }

    public B layoutData(GridDataBuilder dataBuilder) {
        control.setLayoutData(dataBuilder.build());

        return (B) this;
    }

    public B layoutData(Object layoutData) {
        control.setLayoutData(layoutData);

        return (B) this;
    }

    public B gridLayoutData(int style) {
        control.setLayoutData(new GridData(style));

        return (B) this;
    }

    public C build() {
        return control;
    }
}
