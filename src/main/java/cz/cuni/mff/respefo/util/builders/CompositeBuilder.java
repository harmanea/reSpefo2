package cz.cuni.mff.respefo.util.builders;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

public class CompositeBuilder extends ControlBuilder<Composite, CompositeBuilder> {
    private CompositeBuilder(Composite parent, int style) {
        control = new Composite(parent, style);
    }

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
}
