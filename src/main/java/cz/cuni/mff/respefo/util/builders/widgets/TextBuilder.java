package cz.cuni.mff.respefo.util.builders.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

// The following properties were not included: doubleClickEnabled, echoChar, selection, tabs, textChars, textLimit, topIndex
public final class TextBuilder extends AbstractControlBuilder<TextBuilder, Text> {

    private TextBuilder(int style) {
        super((Composite parent) -> new Text(parent, style));
    }

    /**
     * @see SWT#SINGLE
     * @see SWT#MULTI
     * @see SWT#READ_ONLY
     * @see SWT#WRAP
     * @see SWT#LEFT
     * @see SWT#RIGHT
     * @see SWT#CENTER
     * @see SWT#PASSWORD
     * @see SWT#SEARCH
     * @see SWT#ICON_SEARCH
     * @see SWT#ICON_CANCEL
     */
    public static TextBuilder newText(int style) {
        return new TextBuilder(style);
    }

    public TextBuilder editable(boolean editable) {
        addProperty(t -> t.setEditable(editable));
        return this;
    }

    public TextBuilder message(String message) {
        addProperty(t -> t.setMessage(message));
        return this;
    }

    public TextBuilder text(String text) {
        addProperty(t -> t.setText(text));
        return this;
    }

    public TextBuilder onModify(Listener listener) {
        return listener(SWT.Modify, listener);
    }
}