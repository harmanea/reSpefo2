package cz.cuni.mff.respefo.util.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;

import java.util.function.Consumer;

public final class SpinnerBuilder extends AbstractControlBuilder<SpinnerBuilder, Spinner> {

    private SpinnerBuilder(int style) {
        super((Composite parent) -> new Spinner(parent, style));
    }

    /**
     * @see SWT#READ_ONLY
     * @see SWT#WRAP
     */
    public static SpinnerBuilder newSpinner(int style) {
        return new SpinnerBuilder(style);
    }

    public static SpinnerBuilder newSpinner() { return new SpinnerBuilder(SWT.NONE); }

    /**
     * @see Spinner#setDigits(int)
     */
    public SpinnerBuilder digits(int value) {
        addProperty(s -> s.setDigits(value));
        return this;
    }

    /**
     * Use SWT.DEFAULT to use the default value for any of the parameters.
     *
     * @see Spinner#setIncrement(int)
     * @see Spinner#setPageIncrement(int)
     */
    public SpinnerBuilder increment(int increment, int pageIncrement) {
        if (increment != SWT.DEFAULT) {
            addProperty(s -> s.setIncrement(increment));
        }
        if (pageIncrement != SWT.DEFAULT) {
            addProperty(s -> s.setPageIncrement(pageIncrement));
        }
        return this;
    }

    /**
     * Use SWT.DEFAULT to use the default value for any of the parameters.
     *
     * @see Spinner#setMinimum(int)
     * @see Spinner#setMaximum(int)
     */
    public SpinnerBuilder bounds(int minimum, int maximum) {
        if (minimum != SWT.DEFAULT) {
            addProperty(s -> s.setMinimum(minimum));
        }
        if (maximum != SWT.DEFAULT) {
            addProperty(s -> s.setMaximum(maximum));
        }
        return this;
    }

    /**
     * @see Spinner#setSelection(int)
     */
    public SpinnerBuilder selection(int value) {
        addProperty(s -> s.setSelection(value));
        return this;
    }

    /**
     * @see Spinner#setTextLimit(int)
     */
    public SpinnerBuilder textLimit(int limit) {
        addProperty(s -> s.setTextLimit(limit));
        return this;
    }

    /**
     * @see Spinner#addModifyListener(ModifyListener)
     */
    public SpinnerBuilder onModify(Listener listener) {
        return listener(SWT.Modify, listener);
    }

    /**
     * @see Spinner#addModifyListener(ModifyListener)
     */
    public SpinnerBuilder onModifiedValue(Consumer<Integer> valueConsumer) {
        return onModify(event -> valueConsumer.accept(((Spinner) event.widget).getSelection()));
    }
}
