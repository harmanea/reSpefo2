package cz.cuni.mff.respefo.util.builders.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;

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

    public SpinnerBuilder digits(int value) {
        addProperty(s -> s.setDigits(value));
        return this;
    }

    public SpinnerBuilder increment(int increment, int pageIncrement) {
        if (increment != SWT.DEFAULT) {
            addProperty(s -> s.setIncrement(increment));
        }
        if (pageIncrement != SWT.DEFAULT) {
            addProperty(s -> s.setPageIncrement(pageIncrement));
        }
        return this;
    }

    public SpinnerBuilder bounds(int minimum, int maximum) {
        if (minimum != SWT.DEFAULT) {
            addProperty(s -> s.setMinimum(minimum));
        }
        if (maximum != SWT.DEFAULT) {
            addProperty(s -> s.setMaximum(maximum));
        }
        return this;
    }

    public SpinnerBuilder selection(int value) {
        addProperty(s -> s.setSelection(value));
        return this;
    }

    public SpinnerBuilder textLimit(int limit) {
        addProperty(s -> s.setTextLimit(limit));
        return this;
    }

    public SpinnerBuilder onModify(Listener listener) {
        return listener(SWT.Modify, listener);
    }
}
