package cz.cuni.mff.respefo.util.widget;

import cz.cuni.mff.respefo.resources.ImageManager;
import cz.cuni.mff.respefo.resources.ImageResource;
import cz.cuni.mff.respefo.util.FileDialogs;
import cz.cuni.mff.respefo.util.FileType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;

import java.util.function.Consumer;

// The following properties were not included: alignment
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

    public static ButtonBuilder newPushButton() {
        return new ButtonBuilder(SWT.PUSH);
    }

    public static ButtonBuilder newCheckButton() {
        return new ButtonBuilder(SWT.CHECK);
    }

    public static ButtonBuilder newRadioButton() {
        return new ButtonBuilder(SWT.RADIO);
    }

    public static ButtonBuilder newBrowseButton(FileType fileType, Consumer<String> fileNameConsumer) {
        return newPushButton()
                .text("Browse")
                .onSelection(event -> FileDialogs.openFileDialog(fileType).ifPresent(fileNameConsumer::accept));
    }

    /**
     * @see Button#setImage(Image)
     */
    public ButtonBuilder image(Image image) {
        addProperty(b -> b.setImage(image));
        return this;
    }

    /**
     * @see Button#setImage(Image)
     */
    public ButtonBuilder image(ImageResource imageResource) {
        return image(ImageManager.getImage(imageResource));
    }

    /**
     * @see Button#setSelection(boolean)
     */
    public ButtonBuilder selection(boolean selected) {
        addProperty(b -> b.setSelection(selected));
        return this;
    }

    /**
     * @see Button#setGrayed(boolean)
     */
    public ButtonBuilder grayed(boolean grayed) {
        addProperty(b -> b.setGrayed(grayed));
        return this;
    }

    /**
     * @see Button#setText(String)
     */
    public ButtonBuilder text(String text) {
        addProperty(b -> b.setText(text));
        return this;
    }

    /**
     * @see Button#addSelectionListener(SelectionListener)
     */
    public ButtonBuilder onSelection(Runnable callback) {
        return onSelection(event -> callback.run());
    }

    /**
     * @see Button#addSelectionListener(SelectionListener)
     */
    public ButtonBuilder onSelection(Listener listener) {
        return listener(SWT.Selection, listener).listener(SWT.DefaultSelection, listener);
    }

    /**
     * @see Button#addSelectionListener(SelectionListener)
     */
    public ButtonBuilder onSelectedValue(Consumer<Boolean> valueConsumer) {
        return onSelection(event -> valueConsumer.accept(((Button) event.widget).getSelection()));
    }
}
