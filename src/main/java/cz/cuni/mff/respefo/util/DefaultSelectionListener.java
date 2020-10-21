package cz.cuni.mff.respefo.util;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import java.util.function.Consumer;

/**
 * A utility class that routes both the {@link SelectionListener#widgetDefaultSelected}
 * and {@link SelectionListener#widgetSelected} methods to the provided consumer.
 */
public class DefaultSelectionListener implements SelectionListener {

    private final Consumer<SelectionEvent> selectionEventConsumer;

    public DefaultSelectionListener(Consumer<SelectionEvent> selectionEventConsumer) {
        this.selectionEventConsumer = selectionEventConsumer;
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        selectionEventConsumer.accept(e);
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        selectionEventConsumer.accept(e);
    }
}
