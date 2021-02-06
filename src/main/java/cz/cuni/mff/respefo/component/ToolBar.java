package cz.cuni.mff.respefo.component;

import cz.cuni.mff.respefo.resources.ImageManager;
import cz.cuni.mff.respefo.resources.ImageResource;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.builders.RowLayoutBuilder.rowLayout;
import static cz.cuni.mff.respefo.util.builders.widgets.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.builders.widgets.LabelBuilder.newLabel;
import static java.lang.Boolean.TRUE;
import static org.eclipse.swt.SWT.*;

public class ToolBar {
    private final Composite bar;
    private Composite barWindow;

    private Label topBarLabel;
    private Label topBarSeparator;
    private Composite topBarContextIconsComposite;

    private final Runnable toggleAction;

    private final List<Tab> tabs;
    private Tab activeTab;

    public ToolBar(Composite window, Composite bar, Runnable toggleAction) {
        this.bar = bar;
        this.toggleAction = toggleAction;

        createTopBar(window);
        createBarWindow(window);

        tabs = new ArrayList<>();
        activeTab = null;
    }

    /**
     * This will not set any layout data for the created toggle
     */
    public Tab addTab(Function<Composite, Toggle> toggleCreator, String label, String topBarLabelText, ImageResource icon) {
        final Composite iconsComposite = newComposite()
                .layout(rowLayout(HORIZONTAL).margins(0).build())
                .build(topBarContextIconsComposite);

        final Composite windowComposite = newComposite()
                .layout(gridLayout().margins(0).spacings(0))
                .build(barWindow);

        final Toggle toggle = toggleCreator.apply(bar);
        toggle.setImage(ImageManager.getImage(icon));
        toggle.setText(label);
        toggle.setToggled(false);

        Tab tab = new Tab(iconsComposite, windowComposite, toggle, topBarLabelText);
        tabs.add(tab);

        toggle.setToggleAction(toggled -> {
            if (TRUE.equals(toggled)) {
                showTab(tab);
            } else {
                hide();
            }
        });

        bar.layout();
        return tab;
    }

    private void showTab(Tab tab) {
        if (activeTab == null) {
            toggleAction.run();
        } else {
            activeTab.toggle.setToggled(false);
        }
        activeTab = tab;
        activeTab.bringToTop();
    }

    public void disposeTabs() {
        for (Tab tab : tabs) {
            tab.dispose();
        }
        tabs.clear();
    }

    private void createTopBar(Composite window) {
        final Composite composite = newComposite(BORDER)
                .layout(gridLayout(2, false).margins(0).spacings(0).build())
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING)
                .build(window);

        final Composite labelComposite = newComposite()
                .layout(rowLayout(HORIZONTAL).margins(3).build())
                .gridLayoutData(FILL, CENTER, true, true)
                .build(composite);

        topBarLabel = newLabel().bold().build(labelComposite);

        final Composite iconsComposite = newComposite()
                .layout(rowLayout(HORIZONTAL).margins(0).build())
                .gridLayoutData(GridData.FILL_VERTICAL | GridData.HORIZONTAL_ALIGN_END)
                .build(composite);

        topBarContextIconsComposite = newComposite()
                .layout(new StackLayout())
                .build(iconsComposite);

        topBarSeparator = newLabel(VERTICAL | SEPARATOR)
                .build(iconsComposite);

        final LabelButton hideButton = new LabelButton(iconsComposite, NONE);
        hideButton.setImage(ImageManager.getImage(ImageResource.MINIMIZE));
        hideButton.onClick(this::hide);
        hideButton.setToolTipText("Hide");

        topBarSeparator.setLayoutData(new RowData(DEFAULT, hideButton.computeSize(DEFAULT, DEFAULT).y));
    }

    public void hide() {
        if (activeTab != null) {
            toggleAction.run();
            activeTab.toggle.setToggled(false);
            activeTab = null;
        }
    }

    private void createBarWindow(Composite window) {
        barWindow = newComposite()
                .gridLayoutData(GridData.FILL_BOTH)
                .layout(new StackLayout())
                .build(window);
    }

    public class Tab {
        private final Composite iconsComposite;
        private final Composite windowComposite;
        private final Toggle toggle;

        private final String toolBarLabelText;

        private boolean hasTopBarButtons;

        private Tab(Composite iconsComposite, Composite windowComposite, Toggle toggle, String toolBarLabelText) {
            this.iconsComposite = iconsComposite;
            this.windowComposite = windowComposite;
            this.toggle = toggle;

            this.toolBarLabelText = toolBarLabelText;

            hasTopBarButtons = false;
        }

        public Composite getWindow() {
            return windowComposite;
        }

        public void addTopBarButton(String toolTip, ImageResource icon, Runnable onClickAction) {
            final LabelButton topBarButton = new LabelButton(iconsComposite, NONE);
            topBarButton.setImage(ImageManager.getImage(icon));
            topBarButton.onClick(onClickAction);
            topBarButton.setToolTipText(toolTip);

            hasTopBarButtons = true;
        }

        public void appendToggleAction(Consumer<Boolean> toggleAction) {
            toggle.appendToggleAction(toggleAction);
        }

        private void dispose() {
            if (activeTab == this) {
                hide();
            }

            iconsComposite.dispose();
            windowComposite.dispose();
            toggle.dispose();
        }

        public void show() {
            if (!toggle.isToggled()) {
                toggle.toggle();
            }
        }

        public boolean isHidden() {
            return activeTab != this;
        }

        private void bringToTop() {
            topBarLabel.setText(toolBarLabelText);
            topBarLabel.getParent().layout();
            topBarSeparator.setVisible(hasTopBarButtons);

            ((StackLayout) topBarContextIconsComposite.getLayout()).topControl = iconsComposite;
            topBarContextIconsComposite.setLayoutData(new RowData(iconsComposite.computeSize(DEFAULT, DEFAULT)));
            topBarContextIconsComposite.getParent().getParent().layout();

            ((StackLayout) barWindow.getLayout()).topControl = windowComposite;
            barWindow.layout();
        }
    }
}
