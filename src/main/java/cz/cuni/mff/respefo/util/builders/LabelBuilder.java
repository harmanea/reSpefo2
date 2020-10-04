package cz.cuni.mff.respefo.util.builders;

import cz.cuni.mff.respefo.component.ComponentManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class LabelBuilder extends ControlBuilder<Label, LabelBuilder> {
    private LabelBuilder(Composite parent, int style) {
        control = new Label(parent, style);
    }

    /**
     * @see SWT#SEPARATOR
     * @see SWT#HORIZONTAL
     * @see SWT#VERTICAL
     * @see SWT#SHADOW_IN
     * @see SWT#SHADOW_OUT
     * @see SWT#SHADOW_NONE
     * @see SWT#CENTER
     * @see SWT#LEFT
     * @see SWT#RIGHT
     * @see SWT#WRAP
     */
    public static LabelBuilder label(Composite parent, int style) {
        return new LabelBuilder(parent, style);
    }

    public static LabelBuilder label(Composite parent) {
        return new LabelBuilder(parent, SWT.NONE);
    }

    public LabelBuilder text(String text) {
        control.setText(text);

        return this;
    }

    public LabelBuilder image(Image image) {
        control.setImage(image);

        return this;
    }

    public LabelBuilder bold() {
        FontData fontData = control.getFont().getFontData()[0];
        control.setFont(new Font(ComponentManager.getDisplay(), new FontData(fontData.getName(), fontData.getHeight(), SWT.BOLD)));
        control.addDisposeListener(event -> control.getFont().dispose());

        return this;
    }
}
