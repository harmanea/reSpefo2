package cz.cuni.mff.respefo.util.widget;

import cz.cuni.mff.respefo.util.layout.LayoutBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

// The following properties were not included: backgroundMode, layoutDeferred, tabList
// Maybe add a better way of selecting layouts
public final class CompositeBuilder extends AbstractControlBuilder<CompositeBuilder, Composite> {

    protected CompositeBuilder(int style) {
        super((Composite parent) -> new Composite(parent, style));
    }

    /**
     * @see SWT#NO_BACKGROUND
     * @see SWT#NO_FOCUS
     * @see SWT#NO_MERGE_PAINTS
     * @see SWT#NO_REDRAW_RESIZE
     * @see SWT#NO_RADIO_GROUP
     * @see SWT#EMBEDDED
     * @see SWT#DOUBLE_BUFFERED
     */
    public static CompositeBuilder newComposite(int style) {
        return new CompositeBuilder(style);
    }

    public static CompositeBuilder newComposite() {
        return new CompositeBuilder(SWT.NONE);
    }

    public CompositeBuilder layout(Layout layout) {
        addProperty(c -> c.setLayout(layout));
        return this;
    }

    public CompositeBuilder layout(LayoutBuilder<?> layoutBuilder) {
        addProperty(c -> c.setLayout(layoutBuilder.build()));
        return this;
    }
}
