package cz.cuni.mff.respefo.component;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.function.scan.DebugFunction;
import cz.cuni.mff.respefo.function.scan.ExportFunction;
import cz.cuni.mff.respefo.function.scan.ImportFunction;
import cz.cuni.mff.respefo.function.scan.OpenFunction;
import cz.cuni.mff.respefo.logging.FancyLogListener;
import cz.cuni.mff.respefo.logging.LabelLogListener;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.resources.ImageResource;
import cz.cuni.mff.respefo.util.*;
import cz.cuni.mff.respefo.util.utils.FileUtils;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

import java.io.File;

import static cz.cuni.mff.respefo.util.builders.CompositeBuilder.composite;
import static cz.cuni.mff.respefo.util.builders.FillLayoutBuilder.fillLayout;
import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.builders.LabelBuilder.label;
import static cz.cuni.mff.respefo.util.builders.RowLayoutBuilder.rowLayout;
import static java.util.stream.Collectors.toList;
import static org.eclipse.swt.SWT.*;

public class ComponentManager extends UtilityClass {
    private static Display display;
    private static Shell shell;

    private static FileExplorer fileExplorer;
    private static Composite scene;

    public static void init() {
        display = Display.getDefault();
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
                .layout(rowLayout(VERTICAL).margins(0).spacing(0))
                .build();

        final ComponentWithBottomBar componentWithBottomBar = new ComponentWithBottomBar(shell);
        componentWithBottomBar.setWeights(new int[]{70, 30});
        componentWithBottomBar.setLayoutData(new GridData(FILL, FILL, true, true));
        componentWithBottomBar.getScene().setLayout(fillLayout().margins(0).spacing(0).build()); // TEMPORARY
        componentWithBottomBar.getBottomBar().setLayout(gridLayout().margins(0).spacings(0).build()); // TEMPORARY
        componentWithBottomBar.maximizeScene();

        final Composite rightBar = composite(shell, BORDER)
                .layoutData(new GridData(RIGHT, FILL, false, true))
                .layout(rowLayout(VERTICAL).margins(0).spacing(0))
                .build();

        final Composite bottomBar = composite(shell, BORDER)
                .layoutData(new GridData(FILL, BOTTOM, true, false, 3, 1))
                .layout(gridLayout(4, false).margins(3).spacings(3))
                .build();


        // componentWithBottomBar -> scene

        final ComponentWithSidebars componentWithSidebars = new ComponentWithSidebars(componentWithBottomBar.getScene());
        componentWithSidebars.setWeights(new int[]{20, 60, 20});
        componentWithSidebars.getLeftBar().setLayout(gridLayout().margins(0).spacings(0).build());
        componentWithSidebars.getScene().setLayout(gridLayout().margins(0).build());
        componentWithSidebars.getRightBar().setLayout(gridLayout().margins(0).spacings(0).build());
        componentWithSidebars.minimizeLeftBar();
        componentWithSidebars.minimizeRightBar();

        scene = componentWithSidebars.getScene();

        label(componentWithSidebars.getScene(), CENTER)
                .text("Welcome to reSpefo!") // TEMPORARY TEXT
                .layoutData(new GridData(CENTER, CENTER, true, true))
                .build();


        // Left Tool Bar

        final Composite leftBarComposite = composite(componentWithSidebars.getLeftBar(), BORDER)
                .layoutData(new GridData(GridData.FILL_BOTH))
                .layout(gridLayout().margins(0).spacings(0))
                .build();

        ToolBar leftToolBar = new ToolBar(leftBarComposite, leftBar, componentWithSidebars::toggleLeftBar);

        ToolBar.ToolBarTab projectTab = leftToolBar.addTab(parent -> new VerticalToggle(parent, UP),
                "Project", "Project", ImageResource.FOLDER_LARGE);

        fileExplorer = new FileExplorer(projectTab.getWindow());
        fileExplorer.setLayoutData(new GridData(GridData.FILL_BOTH));
        fileExplorer.setRootDirectory(FileUtils.getUserDirectory());

        projectTab.addTopBarButton("ChangeDirectory", ImageResource.OPENED_FOLDER, fileExplorer::changeDirectory);
        projectTab.addTopBarButton("Refresh", ImageResource.REFRESH, fileExplorer::refresh);
        projectTab.addTopBarButton("Collapse All", ImageResource.COLLAPSE, fileExplorer::collapseAll);

        projectTab.show();


        // Right Tool Bar

        final Composite rightBarComposite = composite(componentWithSidebars.getRightBar(), BORDER)
                .layoutData(new GridData(GridData.FILL_BOTH))
                .layout(gridLayout().margins(0).spacings(0))
                .build();

        ToolBar rightToolBar = new ToolBar(rightBarComposite, rightBar, componentWithSidebars::toggleRightBar);

        ToolBar.ToolBarTab toolsTab = rightToolBar.addTab(parent -> new VerticalToggle(parent, DOWN),
                "Tools", "Tools", ImageResource.TOOLS_LARGE);

        label(toolsTab.getWindow(), CENTER)
                .layoutData(new GridData(GridData.FILL_BOTH))
                .text("This toolbar is empty");


        // Bottom Tool Bar

        final Composite bottomBarComposite = composite(componentWithBottomBar.getBottomBar(), NONE)
                .layoutData(new GridData(GridData.FILL_BOTH))
                .layout(gridLayout().margins(0).spacings(0))
                .build();

        final Composite bottomBarTabsComposite = composite(bottomBar)
                .layoutData(new GridData(LEFT, FILL, false, true))
                .layout(rowLayout(HORIZONTAL).margins(0).spacing(0))
                .build();

        ToolBar bottomToolBar = new ToolBar(bottomBarComposite, bottomBarTabsComposite, componentWithBottomBar::toggleScene);

        ToolBar.ToolBarTab eventLogTab = bottomToolBar.addTab(parent -> new HorizontalToggle(parent, NONE),
                "", "Event Log", ImageResource.EVENT_LOG_LARGE);

        final FancyLogListener fancyLogListener = new FancyLogListener(eventLogTab.getWindow());
        fancyLogListener.setLayoutData(new GridData(GridData.FILL_BOTH));
        Log.registerListener(fancyLogListener);

        final Label bottomLogLabel = label(bottomBar, NONE)
                .text("")
                .layoutData(new GridData(LEFT, CENTER, false, false))
                .build();
        bottomLogLabel.addListener(MouseDown, event -> eventLogTab.show());
        Log.registerListener(new LabelLogListener(bottomLogLabel, eventLogTab::isHidden));
        eventLogTab.appendToggleAction(toggled -> {
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
            // TODO: refactor this
            java.util.List<String> fileNames = FileDialogs.openMultipleFilesDialog(FileType.COMPATIBLE_SPECTRUM_FILES);
            if (!fileNames.isEmpty()) {
                if (fileNames.size() > 1) {
                    new ImportFunction().execute(fileNames.stream().map(File::new).collect(toList()));
                } else {
                    new ImportFunction().execute(new File(fileNames.get(0)));
                }
            }
        }));

        final MenuItem exportMenuItem = new MenuItem(fileMenu, PUSH);
        exportMenuItem.setText("Export");
        exportMenuItem.addSelectionListener(new DefaultSelectionListener(event -> {
            // TODO: refactor this
            java.util.List<String> fileNames = FileDialogs.openMultipleFilesDialog(FileType.SPECTRUM);
            if (!fileNames.isEmpty()) {
                if (fileNames.size() > 1) {
                    new ExportFunction().execute(fileNames.stream().map(File::new).collect(toList()));
                } else {
                    new ExportFunction().execute(new File(fileNames.get(0)));
                }
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

        final MenuItem hideSideBarsMenuItem = new MenuItem(windowMenu, PUSH);
        hideSideBarsMenuItem.setText("Hide All Toolbars");
        hideSideBarsMenuItem.addSelectionListener(new DefaultSelectionListener(event -> {
            leftToolBar.hide();
            rightToolBar.hide();
            bottomToolBar.hide();
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

        // TODO: this sometimes freezes, investigate
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

//        final MenuItem placeHolderMenuItem = new MenuItem(debugMenu, PUSH);
//        placeHolderMenuItem.setText("Placeholder text");
//        placeHolderMenuItem.addSelectionListener(new DefaultSelectionListener(event -> {
//            // Placeholder action
//        }));
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
        return scene;
    }

    public static void clearScene() {
        for (Control control : scene.getChildren()) {
            control.dispose();
        }
    }

    public static Composite clearAndGetScene() {
        clearScene();
        return scene;
    }

    public static FileExplorer getFileExplorer() {
        return fileExplorer;
    }

    protected ComponentManager() throws IllegalAccessException {
        super();
    }
}
