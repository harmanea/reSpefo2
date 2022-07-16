package cz.cuni.mff.respefo.util.widget;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.resources.ImageManager;
import cz.cuni.mff.respefo.resources.ImageResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

// The following properties were not included: alignment
public final class LabelBuilder extends AbstractControlBuilder<LabelBuilder, Label> {

    private LabelBuilder(int style) {
        super((Composite parent) -> new Label(parent, style));
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
    public static LabelBuilder newLabel(int style) {
        return new LabelBuilder(style);
    }

    public static LabelBuilder newLabel() {
        return new LabelBuilder(SWT.NONE);
    }

    /**
     * @see Label#setImage(Image)
     */
    public LabelBuilder image(Image image) {
        addProperty(l -> l.setImage(image));
        return this;
    }

    /**
     * @see Label#setImage(Image)
     */
    public LabelBuilder image(ImageResource imageResource) {
        return image(ImageManager.getImage(imageResource));
    }

    /**
     * @see Label#setText(String)
     */
    public LabelBuilder text(String text) {
        addProperty(l -> l.setText(text));
        return this;
    }

    public LabelBuilder bold() {
        addProperty(l -> {
            FontData fontData = l.getFont().getFontData()[0];
            l.setFont(new Font(ComponentManager.getDisplay(), new FontData(fontData.getName(), fontData.getHeight(), SWT.BOLD)));
            l.addDisposeListener(event -> l.getFont().dispose());
        });
        return this;
    }
}
