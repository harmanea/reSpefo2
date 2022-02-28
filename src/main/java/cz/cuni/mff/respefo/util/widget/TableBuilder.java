package cz.cuni.mff.respefo.util.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

// The following properties were not included: columnOrder, headerBackground, headerForeground, itemCount, selection, sortColumn, sortDirection, topIndex
public final class TableBuilder extends AbstractControlBuilder<TableBuilder, Table> {

    private TableBuilder(int style) {
        super((Composite parent) -> new Table(parent, style));
    }

    /**
     * @see SWT#SINGLE
     * @see SWT#MULTI
     * @see SWT#CHECK
     * @see SWT#FULL_SELECTION
     * @see SWT#HIDE_SELECTION
     * @see SWT#VIRTUAL
     * @see SWT#NO_SCROLL
     */
    public static TableBuilder newTable(int style) {
        return new TableBuilder(style);
    }

    public TableBuilder headerVisible(boolean show) {
        addProperty(t -> t.setHeaderVisible(show));
        return this;
    }

    public TableBuilder linesVisible(boolean show) {
        addProperty(t -> t.setLinesVisible(show));
        return this;
    }

    public TableBuilder onSelection(Listener listener) {
        return listener(SWT.Selection, listener).listener(SWT.DefaultSelection, listener);
    }

    public TableBuilder unselectable() {
        addProperty(t -> t.addSelectionListener(new DefaultSelectionListener(event -> t.deselectAll())));
        return this;
    }

    public TableBuilder columns(String... columnNames) {
        addProperty(t -> {
            for (String columnName : columnNames) {
                final TableColumn tableColumn = new TableColumn(t, SWT.NONE);
                tableColumn.setText(columnName);
            }
        });
        return this;
    }

    public TableBuilder columns(int count) {
        addProperty(t -> {
            for (int i = 0; i < count; i++) {
                new TableColumn(t, SWT.NONE);
            }
        });
        return this;
    }

    public TableBuilder columnWidths(int... columnWidths) {
        addProperty(t -> {
            for (int i = 0; i < columnWidths.length; i++) {
                t.getColumn(i).setWidth(columnWidths[i]);
            }
        });
        return this;
    }

    public TableBuilder items(Iterable<String[]> items) {
        addProperty(t -> items.forEach(texts -> {
            final TableItem tableItem = new TableItem(t, SWT.NONE);
            tableItem.setText(texts);
        }));
        return this;
    }

    public <T> TableBuilder items(Iterable<T> iterable, Function<T, String[]> transformer) {
        addProperty(t -> iterable.forEach(item -> {
            final TableItem tableItem = new TableItem(t, SWT.NONE);
            tableItem.setText(transformer.apply(item));
        }));
        return this;
    }

    public <T> TableBuilder items(Iterable<T> iterable, Function<T, String[]> transformer, BiConsumer<T, TableItem> decorator) {
        addProperty(t -> iterable.forEach(item -> {
            final TableItem tableItem = new TableItem(t, SWT.NONE);
            tableItem.setText(transformer.apply(item));
            decorator.accept(item, tableItem);
        }));
        return this;
    }

    public TableBuilder item(Consumer<Table> producer) {
        addProperty(producer);
        return this;
    }

    public TableBuilder packColumns() {
        addProperty(t -> {
            for (TableColumn column : t.getColumns()) {
                column.pack();
            }
        });
        return this;
    }

    public TableBuilder fixedAspectColumns(int... weights) {
        addProperty(t -> t.getParent().addControlListener(ControlListener.controlResizedAdapter(e -> {
            Rectangle area = t.getParent().getClientArea();
            ScrollBar vBar = t.getVerticalBar();
            int width = area.width - t.computeTrim(0, 0, 0, 0).width - vBar.getSize().x;
            if (t.computeSize(SWT.DEFAULT, SWT.DEFAULT).y > area.height + t.getHeaderHeight()) {
                // Subtract the scrollbar width from the total column width
                Point vBarSize = vBar.getSize();
                width -= vBarSize.x;
            }

            int sum = Arrays.stream(weights).sum();

            if (t.getSize().x > area.width) {
                // Table is shrinking
                for (int i = 0; i < weights.length; i++) {
                    t.getColumn(i).setWidth(width * weights[i] / sum);
                }
                t.setSize(area.width, area.height);

            } else {
                // Table is expanding
                t.setSize(area.width, area.height);
                for (int i = 0; i < weights.length; i++) {
                    t.getColumn(i).setWidth(width * weights[i] / sum);
                }
            }
        })));
        return this;
    }

    public TableBuilder decorate(BiConsumer<Integer, TableItem> decorator) {
        addProperty(t -> {
            for (int i = 0; i < t.getItemCount(); i++) {
                decorator.accept(i, t.getItem(i));
            }
        });
        return this;
    }
}
