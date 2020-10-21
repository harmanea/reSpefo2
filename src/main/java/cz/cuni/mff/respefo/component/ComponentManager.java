package cz.cuni.mff.respefo.component;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.function.scan.DebugFunction;
import cz.cuni.mff.respefo.function.scan.ExportFunction;
import cz.cuni.mff.respefo.function.scan.ImportFunction;
import cz.cuni.mff.respefo.function.scan.OpenFunction;
import cz.cuni.mff.respefo.logging.FancyLogListener;
import cz.cuni.mff.respefo.logging.LabelLogListener;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.resources.ImageManager;
import cz.cuni.mff.respefo.resources.ImageResource;
import cz.cuni.mff.respefo.util.*;
import cz.cuni.mff.respefo.util.utils.FileUtils;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.*;

import java.io.File;

import static cz.cuni.mff.respefo.util.builders.CompositeBuilder.composite;
import static cz.cuni.mff.respefo.util.builders.FillLayoutBuilder.fillLayout;
import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.builders.LabelBuilder.label;
import static java.util.stream.Collectors.toList;
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

        // Menu

        final Menu menuBar = new Menu(shell, BAR);
        shell.setMenuBar(menuBar);

        final MenuItem fileMenuHeader = new MenuItem(menuBar, CASCADE);
        fileMenuHeader.setText("&File");

        final Menu fileMenu = new Menu(shell, DROP_DOWN);
        fileMenuHeader.setMenu(fileMenu);

        final MenuItem openMenuItem = new MenuItem(fileMenu, PUSH);
        openMenuItem.setText("Open");
        openMenuItem.addSelectionListener(new DefaultSelectionListener(event -> {
            String fileName = FileDialogs.openFileDialog(FileType.SPECTRUM);
            if (fileName != null) {
                new OpenFunction().execute(new File(fileName)); // TODO: refactor this
            }
        }));

        new MenuItem(fileMenu, SEPARATOR);

        final MenuItem importMenuItem = new MenuItem(fileMenu, PUSH);
        importMenuItem.setText("Import");
        importMenuItem.addSelectionListener(new DefaultSelectionListener(event -> {
            java.util.List<String> fileNames = FileDialogs.openMultipleFilesDialog(FileType.COMPATIBLE_SPECTRUM_FILES);
            if (!fileNames.isEmpty()) {
                new ImportFunction().execute(fileNames.stream().map(File::new).collect(toList())); // TODO: refactor this
            }
        }));

        final MenuItem exportMenuItem = new MenuItem(fileMenu, PUSH);
        exportMenuItem.setText("Export");
        exportMenuItem.addSelectionListener(new DefaultSelectionListener(event -> {
            java.util.List<String> fileNames = FileDialogs.openMultipleFilesDialog(FileType.SPECTRUM);
            if (!fileNames.isEmpty()) {
                new ExportFunction().execute(fileNames.stream().map(File::new).collect(toList())); // TODO: refactor this
            }
        }));

        new MenuItem(fileMenu, SEPARATOR);

        final MenuItem quitMenuItem = new MenuItem(fileMenu, PUSH);
        quitMenuItem.setText("Quit");
        quitMenuItem.addSelectionListener(new DefaultSelectionListener(event -> shell.close()));


        final MenuItem windowMenuHeader = new MenuItem(menuBar, CASCADE);
        windowMenuHeader.setText("&Window");

        final Menu windowMenu = new Menu(shell, DROP_DOWN);
        windowMenuHeader.setMenu(windowMenu);

        final MenuItem showSideBarsMenuItem = new MenuItem(windowMenu, PUSH);
        showSideBarsMenuItem.setText("Show Sidebars");
        showSideBarsMenuItem.addSelectionListener(new DefaultSelectionListener(event -> {
            if (componentWithSidebars.isLeftBarMinimized()) {
                componentWithSidebars.restoreLeftBar();
            }
            if (componentWithSidebars.isRightBarMinimized()) {
                componentWithSidebars.restoreRightBar();
            }
        }));

        final MenuItem hideSideBarsMenuItem = new MenuItem(windowMenu, PUSH);
        hideSideBarsMenuItem.setText("Hide Sidebars");
        hideSideBarsMenuItem.addSelectionListener(new DefaultSelectionListener(event -> {
            if (!componentWithSidebars.isLeftBarMinimized()) {
                componentWithSidebars.minimizeLeftBar();
            }
            if (!componentWithSidebars.isRightBarMinimized()) {
                componentWithSidebars.minimizeRightBar();
            }
        }));

        new MenuItem(windowMenu, SEPARATOR);

        final MenuItem clearSceneMenuItem = new MenuItem(windowMenu, PUSH);
        clearSceneMenuItem.setText("Clear Scene");
        clearSceneMenuItem.addSelectionListener(new DefaultSelectionListener(event -> clearScene()));

        final MenuItem focusSceneMenuItem = new MenuItem(windowMenu, PUSH);
        focusSceneMenuItem.setText("Focus Scene");
        focusSceneMenuItem.addSelectionListener(new DefaultSelectionListener(event -> getScene().forceFocus()));


        final MenuItem debugMenuHeader = new MenuItem(menuBar, CASCADE);
        debugMenuHeader.setText("Debug");

        final Menu debugMenu = new Menu(shell, DROP_DOWN);
        debugMenuHeader.setMenu(debugMenu);

        final MenuItem spectrumJsonMenuItem = new MenuItem(debugMenu, PUSH);
        spectrumJsonMenuItem.setText("Inspect JSON");
        spectrumJsonMenuItem.addSelectionListener(new DefaultSelectionListener(event -> {
            String fileName = FileDialogs.openFileDialog(FileType.SPECTRUM);
            if (fileName != null) {
                new DebugFunction().execute(new File(fileName)); // TODO: refactor this
            }
        }));

        final MenuItem exceptionMenuItem = new MenuItem(debugMenu, PUSH);
        exceptionMenuItem.setText("Throw an Exception");
        exceptionMenuItem.addSelectionListener(new DefaultSelectionListener(event -> {
            throw new RuntimeException("This is a debug exception");
        }));


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

        label(rightBarComposite, CENTER)
                .layoutData(new GridData(GridData.FILL_BOTH))
                .text("This toolbar is empty");

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
        Log.registerListener(new LabelLogListener(bottomLogLabel, () -> !bottomToggle.isToggled()));

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

        LabelButton leftHide = new LabelButton(leftTopBar.getToolbox(), NONE);
        leftHide.setImage(ImageManager.getImage(ImageResource.MINIMIZE));
        leftHide.onClick(leftToggle::toggle);
        leftHide.setToolTipText("Hide");

        label(leftTopBar.getToolbox(), VERTICAL | SEPARATOR)
                .layoutData(new RowData(DEFAULT, leftHide.computeSize(DEFAULT, DEFAULT).y))
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

        LabelButton rightHide = new LabelButton(rightTopBar.getToolbox(), NONE);
        rightHide.setImage(ImageManager.getImage(ImageResource.MINIMIZE));
        rightHide.onClick(rightToggle::toggle);
        rightHide.setToolTipText("Hide");

        // bottom

        LabelButton bottomHide = new LabelButton(bottomTopBar.getToolbox(), NONE);
        bottomHide.setImage(ImageManager.getImage(ImageResource.MINIMIZE));
        bottomHide.onClick(bottomToggle::toggle);
        bottomHide.setToolTipText("Hide");

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
