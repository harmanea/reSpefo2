package cz.cuni.mff.respefo.util.builders.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

// TODO: Maybe add some methods for adding and selecting items
public final class ListBuilder extends AbstractControlBuilder<ListBuilder, List> {

    private ListBuilder(int style) {
        super((Composite parent) -> new List(parent, style));
    }

    /**
     * @see SWT#SINGLE
     * @see SWT#MULTI
     */
    public static ListBuilder newList(int style) {
        return new ListBuilder(style);
    }
}
