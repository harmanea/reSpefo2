package cz.cuni.mff.respefo.util.layout;

import org.eclipse.swt.widgets.Layout;

public abstract class LayoutBuilder<L extends Layout> {
    protected L layout;

    public L build() {
        return layout;
    }
}