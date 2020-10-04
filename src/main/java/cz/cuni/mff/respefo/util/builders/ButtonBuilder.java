package cz.cuni.mff.respefo.util.builders;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;

public class ButtonBuilder extends ControlBuilder<Button, ButtonBuilder> {
    private ButtonBuilder(Composite parent, int style) {
        control = new Button(parent, style);
    }

    /**
     * @see SWT#ARROW
     * @see SWT#CHECK
     * @see SWT#PUSH
     * @see SWT#RADIO
     * @see SWT#TOGGLE
     * @see SWT#FLAT
     * @see SWT#UP
     * @see SWT#DOWN
     * @see SWT#LEFT
     * @see SWT#RIGHT
     * @see SWT#CENTER
     */
    public static ButtonBuilder button(Composite parent, int style) {
        return new ButtonBuilder(parent, style);
    }

    public static ButtonBuilder pushButton(Composite parent) {
        return new ButtonBuilder(parent, SWT.PUSH);
    }

    public static ButtonBuilder radioButton(Composite parent) {
        return new ButtonBuilder(parent, SWT.RADIO);
    }

    public ButtonBuilder text(String text) {
        control.setText(text);

        return this;
    }

    public ButtonBuilder selection(boolean selected) {
        control.setSelection(selected);

        return this;
    }

    public ButtonBuilder image(Image image) {
        control.setImage(image);

        return this;
    }

    public ButtonBuilder onSelection(Listener listener) {
        control.addListener(SWT.Selection, listener);

        return this;
    }
}
