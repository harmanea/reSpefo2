package cz.cuni.mff.respefo.util.builders;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

public class CompositeBuilder extends ControlBuilder<Composite, CompositeBuilder> {
    private CompositeBuilder(Composite parent, int style) {
        control = new Composite(parent, style);
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
    public static CompositeBuilder composite(Composite parent, int style) {
        return new CompositeBuilder(parent, style);
    }

    public static CompositeBuilder composite(Composite parent) {
        return new CompositeBuilder(parent, SWT.NONE);
    }

    public CompositeBuilder layout(Layout layout) {
        control.setLayout(layout);

        return this;
    }

    public CompositeBuilder layout(LayoutBuilder<?> layoutBuilder) {
        control.setLayout(layoutBuilder.build());

        return this;
    }
}
