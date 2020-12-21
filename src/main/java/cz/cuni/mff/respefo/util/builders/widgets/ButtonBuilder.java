package cz.cuni.mff.respefo.util.builders.widgets;

import cz.cuni.mff.respefo.resources.ImageManager;
import cz.cuni.mff.respefo.resources.ImageResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;

// The following properties were not included: alignment, grayed
// TODO: maybe add defaults for push (and other type) buttons
public final class ButtonBuilder extends AbstractControlBuilder<ButtonBuilder, Button> {

    private ButtonBuilder(int style) {
        super((Composite parent) -> new Button(parent, style));
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
    public static ButtonBuilder newButton(int style) {
        return new ButtonBuilder(style);
    }

    public ButtonBuilder image(Image image) {
        addProperty(b -> b.setImage(image));
        return this;
    }

    public ButtonBuilder image(ImageResource imageResource) {
        return image(ImageManager.getImage(imageResource));
    }

    public ButtonBuilder selection(boolean selected) {
        addProperty(b -> b.setSelection(selected));
        return this;
    }

    public ButtonBuilder text(String text) {
        addProperty(b -> b.setText(text));
        return this;
    }

    public ButtonBuilder onSelection(Listener listener) {
        return listener(SWT.Selection, listener);
    }
}
