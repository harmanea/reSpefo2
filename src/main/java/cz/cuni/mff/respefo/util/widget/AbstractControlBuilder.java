package cz.cuni.mff.respefo.util.widget;

import cz.cuni.mff.respefo.resources.ColorManager;
import cz.cuni.mff.respefo.resources.ColorResource;
import cz.cuni.mff.respefo.resources.ImageManager;
import cz.cuni.mff.respefo.resources.ImageResource;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import java.util.function.Function;
import java.util.function.Supplier;


// The following subclasses don't have a builder: Link, *ProgressBar(1)*, Sash, *SashForm(2)*, Scale, Slider, Browser, *Canvas(1)*, *ExpandBar(1)*, Form, *Group(3)*, ToolBar, *Tree(2)*, *Combo(2)*
// The following properties were not included: bounds, capture, cursor, dragDetect, font, location, menu, orientation, parent, redraw, region, size, textDirection, touchEnabled
@SuppressWarnings("unchecked")
public abstract class AbstractControlBuilder<B extends AbstractControlBuilder<?, ?>, C extends Control>
        extends AbstractWidgetBuilder<B, C, Composite> {

    protected AbstractControlBuilder(Function<Composite, C> widgetCreator) {
        super(widgetCreator);
    }

    /**
     * @see Control#setBackground(Color)
     */
    public B background(Color color) {
        addProperty(c -> c.setBackground(color));
        return (B) this;
    }

    /**
     * @see Control#setBackground(Color)
     */
    public B background(ColorResource colorResource) {
        return background(ColorManager.getColor(colorResource));
    }

    /**
     * @see Control#setBackgroundImage(Image)
     */
    public B backgroundImage(Image image) {
        addProperty(c -> c.setBackgroundImage(image));
        return (B) this;
    }

    /**
     * @see Control#setBackgroundImage(Image)
     */
    public B backgroundImage(ImageResource imageResource) {
        return backgroundImage(ImageManager.getImage(imageResource));
    }

    /**
     * @see Control#setEnabled(boolean)
     */
    public B enabled(boolean enabled) {
        addProperty(c -> c.setEnabled(enabled));
        return (B) this;
    }

    /**
     * @see Control#setForeground(Color)
     */
    public B foreground(Color color) {
        addProperty(c -> c.setForeground(color));
        return (B) this;
    }

    /**
     * @see Control#setForeground(Color)
     */
    public B foreground(ColorResource colorResource) {
        return foreground(ColorManager.getColor(colorResource));
    }

    /**
     * @see Control#setLayoutData(Object)
     */
    public B layoutData(Supplier<Object> layoutDataSupplier) {
        addProperty(c -> c.setLayoutData(layoutDataSupplier.get()));
        return (B) this;
    }

    /**
     * @see Control#setLayoutData(Object)
     */
    public B layoutData(Object layoutData) {
        addProperty(c -> c.setLayoutData(layoutData));
        return (B) this;
    }

    /**
     * @see Control#setLayoutData(Object)
     * @see GridData#GridData(int)
     */
    public B gridLayoutData(int style) {
        return layoutData(() -> new GridData(style));
    }

    /**
     * @see Control#setLayoutData(Object)
     * @see GridData#GridData(int, int, boolean, boolean)
     */
    public B gridLayoutData(int horizontalAlignment, int verticalAlignment, boolean grabHorizontal, boolean grabVertical) {
        return layoutData(() -> new GridData(horizontalAlignment, verticalAlignment, grabHorizontal, grabVertical));
    }

    /**
     * @see Control#setLayoutData(Object)
     * @see GridData#GridData(int, int, boolean, boolean, int, int)
     */
    public B gridLayoutData(int horizontalAlignment, int verticalAlignment, boolean grabHorizontal, boolean grabVertical, int horizontalSpan, int verticalSpan) {
        return layoutData(() -> new GridData(horizontalAlignment, verticalAlignment, grabHorizontal, grabVertical, horizontalSpan, verticalSpan));
    }

    /**
     * @see Control#setToolTipText(String)
     */
    public B toolTip(String toolTipText) {
        addProperty(c -> c.setToolTipText(toolTipText));
        return (B) this;
    }

    /**
     * @see Control#setVisible(boolean)
     */
    public B visible(boolean visible) {
        addProperty(c -> c.setVisible(visible));
        return (B) this;
    }

    /**
     * @see Control#setFocus()
     */
    public B focus() {
        addProperty(Control::setFocus);
        return (B) this;
    }

    /**
     * @see Control#pack()
     */
    public B pack() {
        addProperty(Control::pack);
        return (B) this;
    }
}
