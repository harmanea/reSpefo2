package cz.cuni.mff.respefo.util.layout;

import cz.cuni.mff.respefo.util.widget.DefaultSelectionListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import java.util.function.Consumer;
import java.util.function.Function;

import static org.eclipse.swt.SWT.*;

public class MenuBuilder {
    private MenuBuilder() {}

    public static void bar(Shell shell, HeaderBuilder ... headers) {
        final Menu menuBar = new Menu(shell, BAR);
        shell.setMenuBar(menuBar);

        for (HeaderBuilder headerBuilder : headers) {
            headerBuilder.build(menuBar);
        }
    }

    public static HeaderBuilder header(String text, MenuItemBuilder ... items) {
        return new HeaderBuilder(text, items);
    }

    public static MenuItemBuilder item(String text, Runnable action) {
        return new ActionMenuItemBuilder(text, action);
    }

    public static MenuItemBuilder checkItem(String text, Consumer<Boolean> checkAction) {
        return new CheckMenuItemBuilder(text, checkAction);
    }

    public static MenuItemBuilder separator() {
        return new SeparatorMenuItemBuilder();
    }

    public static MenuItemBuilder subMenu(String text, MenuItemBuilder ... items) {
        return new SubMenuMenuItemBuilder(text, items);
    }

    public static <T> MenuItemBuilder items(Iterable<T> items, Function<T, String> nameFunction, Function<T, Runnable> actionFunction) {
        return new MultiActionMenuItemBuilder<>(items, nameFunction, actionFunction);
    }

    public static class HeaderBuilder {
        private final String text;
        private final MenuItemBuilder[] items;

        private HeaderBuilder(String text, MenuItemBuilder ... items) {
            this.text = text;
            this.items = items;
        }

        private void build(Menu menu) {
            final MenuItem menuHeader = new MenuItem(menu, CASCADE);
            menuHeader.setText(text);

            final Menu dropDownMenu = new Menu(menu.getShell(), DROP_DOWN);
            menuHeader.setMenu(dropDownMenu);

            for (MenuItemBuilder itemBuilder : items) {
                itemBuilder.build(dropDownMenu);
            }
        }
    }

    public interface MenuItemBuilder {
        void build(Menu menu);
    }

    private static class ActionMenuItemBuilder implements MenuItemBuilder {
        private final String text;
        private final Runnable action;

        private ActionMenuItemBuilder(String text, Runnable action) {
            this.text = text;
            this.action = action;
        }

        @Override
        public void build(Menu menu) {
            final MenuItem menuItem = new MenuItem(menu, PUSH);
            menuItem.setText(text);
            menuItem.addSelectionListener(new DefaultSelectionListener(event -> action.run()));
        }
    }

    private static class CheckMenuItemBuilder implements MenuItemBuilder {
        private final String text;
        private final Consumer<Boolean> checkAction;

        private CheckMenuItemBuilder(String text, Consumer<Boolean> checkAction) {
            this.text = text;
            this.checkAction = checkAction;
        }

        @Override
        public void build(Menu menu) {
            final MenuItem menuItem = new MenuItem(menu, CHECK);
            menuItem.setText(text);
            menuItem.addSelectionListener(new DefaultSelectionListener(event -> checkAction.accept(menuItem.getSelection())));
        }
    }

    private static class SeparatorMenuItemBuilder implements MenuItemBuilder {
        private SeparatorMenuItemBuilder() {}

        @Override
        public void build(Menu menu) {
            new MenuItem(menu, SEPARATOR);
        }
    }

    private static class SubMenuMenuItemBuilder implements MenuItemBuilder {
        private final String text;
        private final MenuItemBuilder[] items;

        private SubMenuMenuItemBuilder(String text, MenuItemBuilder ... items) {
            this.text = text;
            this.items = items;
        }

        @Override
        public void build(Menu menu) {
            final MenuItem menuItem = new MenuItem(menu, CASCADE);
            menuItem.setText(text);

            final Menu subMenu = new Menu(menu.getShell(), DROP_DOWN | NO_RADIO_GROUP);
            menuItem.setMenu(subMenu);

            for (MenuItemBuilder itemBuilder : items) {
                itemBuilder.build(subMenu);
            }
        }
    }

    private static class MultiActionMenuItemBuilder<T> implements MenuItemBuilder {
        private final Iterable<T> items;
        private final Function<T, String> nameFunction;
        private final Function<T, Runnable> actionFunction;

        private MultiActionMenuItemBuilder(Iterable<T> items, Function<T, String> nameFunction, Function<T, Runnable> actionFunction) {
            this.items = items;
            this.nameFunction = nameFunction;
            this.actionFunction = actionFunction;
        }

        @Override
        public void build(Menu menu) {
            for (T t : items) {
                final MenuItem menuItem = new MenuItem(menu, PUSH);
                menuItem.setText(nameFunction.apply(t));
                menuItem.addSelectionListener(new DefaultSelectionListener(event -> actionFunction.apply(t).run()));
            }
        }
    }
}
