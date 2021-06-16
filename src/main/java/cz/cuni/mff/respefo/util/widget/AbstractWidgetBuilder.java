package cz.cuni.mff.respefo.util.widget;

import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public abstract class AbstractWidgetBuilder<B extends AbstractWidgetBuilder<?, ?, ?>, W extends Widget, P extends Widget> {

    private final Function<P, W> widgetCreator;
    private final List<Consumer<W>> properties = new ArrayList<>();
    private final Map<Integer, Listener> listeners = new HashMap<>();

    protected AbstractWidgetBuilder(Function<P, W> widgetCreator) {
        this.widgetCreator = widgetCreator;
    }

    public final W build(P parent) {
        W widget = widgetCreator.apply(parent);
        properties.forEach(p -> p.accept(widget));
        listeners.forEach(widget::addListener);
        return widget;
    }

    protected final void addProperty(Consumer<W> property) {
        properties.add(property);
    }

    public B listener(int eventType, Listener listener) {
        listeners.put(eventType, listener);
        return (B) this;
    }
}
