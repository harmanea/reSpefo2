package cz.cuni.mff.respefo.component;

import cz.cuni.mff.respefo.resources.ImageManager;
import cz.cuni.mff.respefo.resources.ImageResource;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import java.util.function.Consumer;
import java.util.function.Function;

import static cz.cuni.mff.respefo.util.builders.CompositeBuilder.composite;
import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.builders.LabelBuilder.label;
import static cz.cuni.mff.respefo.util.builders.RowLayoutBuilder.rowLayout;
import static java.lang.Boolean.TRUE;
import static org.eclipse.swt.SWT.*;

public class ToolBar {
    private final Composite bar;
    private Composite barWindow;

    private Label topBarLabel;
    private Label topBarSeparator;
    private Composite topBarContextIconsComposite;

    private final Runnable toggleAction;

    private ToolBarTab activeTab;

    public ToolBar(Composite window, Composite bar, Runnable toggleAction) {
        this.bar = bar;
        this.toggleAction = toggleAction;

        createTopBar(window);
        createBarWindow(window);

        activeTab = null;
    }

    /**
     * This will not set any layout data for the created toggle
     */
    public ToolBarTab addTab(Function<Composite, Toggle> toggleCreator, String label, String topBarLabelText, ImageResource icon) {
        final Composite iconsComposite = composite(topBarContextIconsComposite)
                .layout(rowLayout(HORIZONTAL).margins(0).build())
                .build();

        final Composite windowComposite = composite(barWindow)
                .layout(gridLayout().margins(0).spacings(0))
                .build();

        final Toggle toggle = toggleCreator.apply(bar);
        toggle.setImage(ImageManager.getImage(icon));
        toggle.setText(label);
        toggle.setToggled(false);

        ToolBarTab tab = new ToolBarTab(iconsComposite, windowComposite, toggle, topBarLabelText);

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

    private void showTab(ToolBarTab tab) {
        if (activeTab == null) {
            toggleAction.run();
        } else {
            activeTab.toggle.setToggled(false);
        }
        activeTab = tab;
        activeTab.bringToTop();
    }

    private void createTopBar(Composite window) {
        final Composite composite = composite(window, BORDER)
                .layout(gridLayout(2, false).margins(0).spacings(0).build())
                .layoutData(new GridData(FILL, TOP, true, false))
                .build();

        final Composite labelComposite = composite(composite, NONE)
                .layout(rowLayout(HORIZONTAL).margins(3).build())
                .layoutData(new GridData(FILL, CENTER, true, true))
                .build();

        topBarLabel = label(labelComposite)
                .bold()
                .build();

        final Composite iconsComposite = composite(composite, RIGHT_TO_LEFT)
                .layout(rowLayout(HORIZONTAL).margins(0).build())
                .layoutData(new GridData(GridData.FILL_VERTICAL | GridData.HORIZONTAL_ALIGN_END))
                .build();

        final LabelButton hideButton = new LabelButton(iconsComposite, NONE);
        hideButton.setImage(ImageManager.getImage(ImageResource.MINIMIZE));
        hideButton.onClick(this::hide);
        hideButton.setToolTipText("Hide");

        topBarSeparator = label(iconsComposite, VERTICAL | SEPARATOR)
                .layoutData(new RowData(DEFAULT, hideButton.computeSize(DEFAULT, DEFAULT).y))
                .build();

        topBarContextIconsComposite = composite(iconsComposite, RIGHT_TO_LEFT)
                .layout(new StackLayout())
                .build();
    }

    public void hide() {
        if (activeTab != null) {
            toggleAction.run();
            activeTab.toggle.setToggled(false);
            activeTab = null;
        }
    }

    private void createBarWindow(Composite window) {
        barWindow = composite(window)
                .layoutData(new GridData(GridData.FILL_BOTH))
                .layout(new StackLayout())
                .build();
    }

    public class ToolBarTab {
        private final Composite iconsComposite;
        private final Composite windowComposite;
        private final Toggle toggle;

        private final String toolBarLabelText;

        private boolean hasTopBarButtons;

        private ToolBarTab(Composite iconsComposite, Composite windowComposite, Toggle toggle, String toolBarLabelText) {
            this.iconsComposite = iconsComposite;
            this.windowComposite = windowComposite;
            this.toggle = toggle;

            this.toolBarLabelText = toolBarLabelText;

            hasTopBarButtons = false;
        }

        public Composite getWindow() {
            return windowComposite;
        }

        /**
         * Filled from right to left
         */
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

        public void dispose() {
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
            topBarContextIconsComposite.layout();
            ((StackLayout) barWindow.getLayout()).topControl = windowComposite;
            barWindow.layout();
        }
    }
}
