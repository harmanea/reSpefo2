package cz.cuni.mff.respefo.component;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.logging.FancyLogListener;
import cz.cuni.mff.respefo.logging.LabelLogListener;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.resources.ImageManager;
import cz.cuni.mff.respefo.resources.ImageResource;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.Progress;
import cz.cuni.mff.respefo.util.UtilityClass;
import cz.cuni.mff.respefo.util.VersionInfo;
import cz.cuni.mff.respefo.util.utils.FileUtils;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.*;

import static cz.cuni.mff.respefo.util.builders.ButtonBuilder.pushButton;
import static cz.cuni.mff.respefo.util.builders.CompositeBuilder.composite;
import static cz.cuni.mff.respefo.util.builders.FillLayoutBuilder.fillLayout;
import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.builders.LabelBuilder.label;
import static org.eclipse.swt.SWT.*;

public class ComponentManager extends UtilityClass {
    private static Display display;
    private static Shell shell;

    private static FileExplorer fileExplorer;
    private static ComponentWithSidebars componentWithSidebars;

    public static void init() {
        display = new Display();
        Display.setAppName("reSpefo");
        Display.setAppVersion(VersionInfo.getVersion());

        shell = new Shell(display, SHELL_TRIM);
        shell.setText("reSpefo (" + VersionInfo.getVersion() + ")");
    }

    public static void build() throws SpefoException {
        shell.setLayout(gridLayout(3, false).margins(0).spacings(0).build());
        shell.addListener(Close, event -> event.doit = Message.question("Are you sure you want to quit?"));

        // Top level

        final Composite leftBar = composite(shell, BORDER)
                .layoutData(new GridData(LEFT, FILL, false, true))
                .layout(gridLayout().margins(0).spacings(0).build())
                .build();

        final ComponentWithBottomBar componentWithBottomBar = new ComponentWithBottomBar(shell);
        componentWithBottomBar.setLayoutData(new GridData(FILL, FILL, true, true));
        componentWithBottomBar.getScene().setLayout(fillLayout().margins(0).spacing(0).build()); // TEMPORARY
        componentWithBottomBar.getBottomBar().setLayout(gridLayout().margins(0).spacings(0).build()); // TEMPORARY
        componentWithBottomBar.maximizeScene();

        final Composite rightBar = composite(shell, BORDER)
                .layoutData(new GridData(RIGHT, FILL, false, true))
                .layout(gridLayout().margins(0).spacings(0).build())
                .build();

        final Composite bottomBar = composite(shell, BORDER)
                .layoutData(new GridData(FILL, BOTTOM, true, false, 3, 1))
                .layout(gridLayout(4, false).margins(3).spacings(3).build())
                .build();


        // componentWithBottomBar -> scene

        componentWithSidebars = new ComponentWithSidebars(componentWithBottomBar.getScene());
        componentWithSidebars.getLeftBar().setLayout(gridLayout().margins(0).spacings(0).build());
        componentWithSidebars.getScene().setLayout(gridLayout().margins(0).build());
        componentWithSidebars.getRightBar().setLayout(gridLayout().margins(0).spacings(0).build());
        componentWithSidebars.minimizeRightBar();

        TopBar leftTopBar = new TopBar(componentWithSidebars.getLeftBar(), "Project");

        fileExplorer = new FileExplorer(componentWithSidebars.getLeftBar());
        fileExplorer.setLayoutData(new GridData(GridData.FILL_BOTH));
        fileExplorer.setRootDirectory(FileUtils.getUserDirectory());

        label(componentWithSidebars.getScene(), CENTER)
                .text("Welcome to reSpefo!") // TEMPORARY TEXT
                .layoutData(new GridData(CENTER, CENTER, true, true))
                .build();

        TopBar rightTopBar = new TopBar(componentWithSidebars.getRightBar(), "Tools");

        Composite rightBarComposite = composite(componentWithSidebars.getRightBar(), BORDER)
                .layoutData(new GridData(GridData.FILL_BOTH))
                .layout(new GridLayout())
                .build();

        pushButton(rightBarComposite)
                .layoutData(new GridData(CENTER, TOP, true, false))
                .text("Clear scene")
                .onSelection(event -> ComponentManager.clearScene());

        pushButton(rightBarComposite)
                .layoutData(new GridData(CENTER, TOP, true, false))
                .text("Focus scene")
                .onSelection(event -> ComponentManager.getScene().forceFocus());

        // componentWithBottomBar -> bottom bar

        TopBar bottomTopBar = new TopBar(componentWithBottomBar.getBottomBar(), "Event Log");

        final FancyLogListener fancyLogListener = new FancyLogListener(componentWithBottomBar.getBottomBar());
        fancyLogListener.setLayoutData(new GridData(GridData.FILL_BOTH));
        Log.registerListener(fancyLogListener);


        // Fill main sidebars

        final VerticalToggle leftToggle = new VerticalToggle(leftBar, UP);
        leftToggle.setLayoutData(new GridData(FILL, TOP, true, false));
        leftToggle.setImage(ImageManager.getImage(ImageResource.FOLDER_LARGE));
        leftToggle.setText("Project");
        leftToggle.setToggleAction(toggled -> componentWithSidebars.toggleLeftBar());
        leftToggle.setToggled(true);

        final VerticalToggle rightToggle = new VerticalToggle(rightBar, DOWN);
        rightToggle.setLayoutData(new GridData(FILL, TOP, true, false));
        rightToggle.setImage(ImageManager.getImage(ImageResource.TOOLS_LARGE));
        rightToggle.setText("Tools");
        rightToggle.setToggleAction(toggled -> componentWithSidebars.toggleRightBar());
        rightToggle.setToggled(false);

        final Toggle bottomToggle = new Toggle(bottomBar, NONE);
        bottomToggle.setToggled(false);
        bottomToggle.setImage(ImageManager.getImage(ImageResource.EVENT_LOG_LARGE));
        bottomToggle.setLayoutData(new GridData(LEFT, FILL, false, true));
        bottomToggle.setTooltipText("Event Log");

        final Label bottomLogLabel = label(bottomBar, NONE)
                .text("")
                .layoutData(new GridData(LEFT, CENTER, false, false))
                .build();
        bottomLogLabel.addListener(MouseDown, event -> bottomToggle.toggle());
        Log.registerListener(new LabelLogListener(bottomLogLabel));

        bottomToggle.setToggleAction(toggled -> {
            componentWithBottomBar.toggleScene();
            bottomLogLabel.setText("");
            bottomLogLabel.requestLayout();
        });

        final Label progressLabel = label(bottomBar, RIGHT)
                .layoutData(new GridData(RIGHT, CENTER, true, true))
                .visible(false)
                .build();

        final Composite progressBarComposite = composite(bottomBar)
                .layoutData(new GridData(RIGHT, CENTER, false, true))
                .layout(fillLayout().marginWidth(10).marginHeight(0).build())
                .build();

        final ProgressBar progressBar = new ProgressBar(progressBarComposite, SMOOTH);
        progressBar.setVisible(false);

        Progress.init(progressBar, progressLabel);

        // Top bars

        // left

        LabelButton leftMinimize = new LabelButton(leftTopBar.getToolbox(), NONE);
        leftMinimize.setImage(ImageManager.getImage(ImageResource.MINIMIZE));
        leftMinimize.onClick(leftToggle::toggle);
        leftMinimize.setToolTipText("Minimize");

        label(leftTopBar.getToolbox(), VERTICAL | SEPARATOR)
                .layoutData(new RowData(DEFAULT, leftMinimize.computeSize(DEFAULT, DEFAULT).y))
                .build();

        LabelButton collapse = new LabelButton(leftTopBar.getToolbox(), NONE);
        collapse.setImage(ImageManager.getImage(ImageResource.COLLAPSE));
        collapse.onClick(fileExplorer::collapseAll);
        collapse.setToolTipText("Collapse All");

        LabelButton refresh = new LabelButton(leftTopBar.getToolbox(), NONE);
        refresh.setImage(ImageManager.getImage(ImageResource.REFRESH));
        refresh.onClick(fileExplorer::refresh);
        refresh.setToolTipText("Refresh");

        LabelButton changeDirectory = new LabelButton(leftTopBar.getToolbox(), NONE);
        changeDirectory.setImage(ImageManager.getImage(ImageResource.OPENED_FOLDER));
        changeDirectory.onClick(fileExplorer::changeDirectory);
        changeDirectory.setToolTipText("Change Directory");

        // right

        LabelButton rightMinimize = new LabelButton(rightTopBar.getToolbox(), NONE);
        rightMinimize.setImage(ImageManager.getImage(ImageResource.MINIMIZE));
        rightMinimize.onClick(rightToggle::toggle);
        rightMinimize.setToolTipText("Minimize");

        // bottom

        LabelButton bottomMinimize = new LabelButton(bottomTopBar.getToolbox(), NONE);
        bottomMinimize.setImage(ImageManager.getImage(ImageResource.MINIMIZE));
        bottomMinimize.onClick(bottomToggle::toggle);
        bottomMinimize.setToolTipText("Minimize");

        // Final stuff

        componentWithBottomBar.setWeights(new int[]{70, 30});
        componentWithSidebars.setWeights(new int[]{15, 70, 15});
    }

    public static void open() {
        shell.open();
        while (!shell.isDisposed()) {
            try {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            } catch (Exception exception) {
                handleException(exception);
            }
        }
        display.dispose();
    }

    private static void handleException(Exception exception) {
        Message.errorWithDetails("An error occurred in one of the components", exception);
    }

    public static Display getDisplay() {
        return display;
    }

    public static Shell getShell() {
        return shell;
    }

    public static Composite getScene() {
        return componentWithSidebars.getScene();
    }

    public static void clearScene() {
        for (Control control : getScene().getChildren()) {
            control.dispose();
        }
    }

    public static Composite clearAndGetScene() {
        clearScene();
        return getScene();
    }

    public static FileExplorer getFileExplorer() {
        return fileExplorer;
    }

    protected ComponentManager() throws IllegalAccessException {
        super();
    }
}
