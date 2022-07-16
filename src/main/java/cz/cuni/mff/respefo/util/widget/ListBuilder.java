package cz.cuni.mff.respefo.util.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;

// Maybe add some methods for adding and selecting items
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

    /**
     * @see List#setSelection(int)
     */
    public ListBuilder selection(int value) {
        addProperty(l -> l.setSelection(value));
        return this;
    }

    /**
     * @see List#addSelectionListener(SelectionListener)
     */
    public ListBuilder onSelection(Listener listener) {
        return listener(SWT.Selection, listener).listener(SWT.DefaultSelection, listener);
    }

    /**
     * @see List#setItems(String...)
     */
    public ListBuilder items(String... items) {
        addProperty(l -> l.setItems(items));
        return this;
    }
}
