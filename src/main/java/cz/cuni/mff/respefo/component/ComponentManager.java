package cz.cuni.mff.respefo.component;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.function.FunctionInfo;
import cz.cuni.mff.respefo.function.FunctionManager;
import cz.cuni.mff.respefo.function.ProjectFunction;
import cz.cuni.mff.respefo.function.scan.InspectJSONFunction;
import cz.cuni.mff.respefo.function.scan.RepairFunction;
import cz.cuni.mff.respefo.logging.FancyLogListener;
import cz.cuni.mff.respefo.logging.LabelLogListener;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.resources.ImageResource;
import cz.cuni.mff.respefo.util.*;
import cz.cuni.mff.respefo.util.builders.widgets.CompositeBuilder;
import cz.cuni.mff.respefo.util.utils.FileUtils;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

import java.io.File;

import static cz.cuni.mff.respefo.util.builders.FillLayoutBuilder.fillLayout;
import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.builders.RowLayoutBuilder.rowLayout;
import static cz.cuni.mff.respefo.util.builders.widgets.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.builders.widgets.LabelBuilder.newLabel;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.eclipse.swt.SWT.*;

public class ComponentManager extends UtilityClass {
    private static Display display;
    private static Shell shell;

    private static FileExplorer fileExplorer;
    private static Composite scene;

    // TODO: maybe add a notification for lost work
    private static ToolBar leftToolBar;
    private static ToolBar rightToolBar;
    private static ToolBar bottomToolBar;

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

        CompositeBuilder sideBarsBuilder = newComposite(BORDER)
                .layout(rowLayout(VERTICAL).margins(0).spacing(0));

        final Composite leftBar = sideBarsBuilder
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.FILL_VERTICAL)
                .build(shell);

        final ComponentWithBottomBar componentWithBottomBar = new ComponentWithBottomBar(shell);
        componentWithBottomBar.setWeights(new int[]{70, 30});
        componentWithBottomBar.setLayoutData(new GridData(FILL, FILL, true, true));
        componentWithBottomBar.getScene().setLayout(fillLayout().margins(0).spacing(0).build());
        componentWithBottomBar.getBottomBar().setLayout(gridLayout().margins(0).spacings(0).build());
        componentWithBottomBar.maximizeScene();

        final Composite rightBar = sideBarsBuilder
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_END | GridData.FILL_VERTICAL)
                .build(shell);

        final Composite bottomBar = newComposite(BORDER)
                .gridLayoutData(FILL, BOTTOM, true, false, 3, 1)
                .layout(gridLayout(4, false).margins(3).spacings(3))
                .build(shell);


        // componentWithBottomBar -> scene

        final ComponentWithSidebars componentWithSidebars = new ComponentWithSidebars(componentWithBottomBar.getScene());
        componentWithSidebars.setWeights(new int[]{20, 60, 20});
        componentWithSidebars.getLeftBar().setLayout(gridLayout().margins(0).spacings(0).build());
        componentWithSidebars.getScene().setLayout(gridLayout().margins(0).build());
        componentWithSidebars.getRightBar().setLayout(gridLayout().margins(0).spacings(0).build());
        componentWithSidebars.minimizeLeftBar();
        componentWithSidebars.minimizeRightBar();

        scene = componentWithSidebars.getScene();

        newLabel(CENTER)
                .text("Welcome to reSpefo!")
                .gridLayoutData(CENTER, CENTER, true, true)
                .build(componentWithSidebars.getScene());


        // Left Tool Bar

        CompositeBuilder toolBarCompositeBuilder = newComposite(BORDER)
                .gridLayoutData(GridData.FILL_BOTH)
                .layout(gridLayout().margins(0).spacings(0));

        final Composite leftBarComposite = toolBarCompositeBuilder.build(componentWithSidebars.getLeftBar());

        leftToolBar = new ToolBar(leftBarComposite, leftBar, componentWithSidebars::toggleLeftBar);

        ToolBar.Tab projectTab = leftToolBar.addTab(parent -> new VerticalToggle(parent, UP),
                "Project", "Project", ImageResource.FOLDER_LARGE);

        fileExplorer = new FileExplorer(projectTab.getWindow());
        fileExplorer.setLayoutData(new GridData(GridData.FILL_BOTH));
        fileExplorer.setRootDirectory(FileUtils.getUserDirectory());

        projectTab.addTopBarButton("ChangeDirectory", ImageResource.OPENED_FOLDER, fileExplorer::changeDirectory);
        projectTab.addTopBarButton("Refresh", ImageResource.REFRESH, fileExplorer::refresh);
        projectTab.addTopBarButton("Collapse All", ImageResource.COLLAPSE, fileExplorer::collapseAll);

        projectTab.show();


        // Right Tool Bar

        final Composite rightBarComposite = toolBarCompositeBuilder.build(componentWithSidebars.getRightBar());

        rightToolBar = new ToolBar(rightBarComposite, rightBar, componentWithSidebars::toggleRightBar);

        // Make sure the right bar is visible even if there are no tabs
        ((GridData) rightBar.getLayoutData()).widthHint = leftBar.computeSize(DEFAULT, DEFAULT).x;

        // Bottom Tool Bar

        final Composite bottomBarComposite = toolBarCompositeBuilder.build(componentWithBottomBar.getBottomBar());

        final Composite bottomBarTabsComposite = newComposite()
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.FILL_VERTICAL)
                .layout(rowLayout(HORIZONTAL).margins(0).spacing(0))
                .build(bottomBar);

        bottomToolBar = new ToolBar(bottomBarComposite, bottomBarTabsComposite, componentWithBottomBar::toggleScene);

        ToolBar.Tab eventLogTab = bottomToolBar.addTab(parent -> new HorizontalToggle(parent, NONE),
                "", "Event Log", ImageResource.EVENT_LOG_LARGE);

        final FancyLogListener fancyLogListener = new FancyLogListener(eventLogTab.getWindow());
        fancyLogListener.setLayoutData(new GridData(GridData.FILL_BOTH));
        Log.registerListener(fancyLogListener);

        final Label bottomLogLabel = newLabel()
                .text("")
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_CENTER)
                .listener(MouseDown, event -> eventLogTab.show())
                .build(bottomBar);
        Log.registerListener(new LabelLogListener(bottomLogLabel, eventLogTab::isHidden));
        eventLogTab.appendToggleAction(toggled -> {
            bottomLogLabel.setText("");
            bottomLogLabel.requestLayout();
        });

        final Label progressLabel = newLabel(RIGHT)
                .gridLayoutData(RIGHT, CENTER, true, true)
                .visible(false)
                .build(bottomBar);

        final Composite progressBarComposite = newComposite()
                .gridLayoutData(RIGHT, CENTER, false, true)
                .layout(fillLayout().marginWidth(10).marginHeight(0).build())
                .build(bottomBar);

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
                FunctionManager.getSingleFileFunctionByName("Open").execute(new File(fileName));
            }
        }));

        new MenuItem(fileMenu, SEPARATOR);

        final MenuItem importMenuItem = new MenuItem(fileMenu, PUSH);
        importMenuItem.setText("Import");
        importMenuItem.addSelectionListener(new DefaultSelectionListener(event -> {
            java.util.List<String> fileNames = FileDialogs.openMultipleFilesDialog(FileType.COMPATIBLE_SPECTRUM_FILES);
            if (!fileNames.isEmpty()) {
                if (fileNames.size() > 1) {
                    FunctionManager.getMultiFileFunctionByName("Import").execute(fileNames.stream().map(File::new).collect(toList()));
                } else {
                    FunctionManager.getSingleFileFunctionByName("Import").execute(new File(fileNames.get(0)));
                }
            }
        }));

        final MenuItem exportMenuItem = new MenuItem(fileMenu, PUSH);
        exportMenuItem.setText("Export");
        exportMenuItem.addSelectionListener(new DefaultSelectionListener(event -> {
            java.util.List<String> fileNames = FileDialogs.openMultipleFilesDialog(FileType.SPECTRUM);
            if (!fileNames.isEmpty()) {
                if (fileNames.size() > 1) {
                    FunctionManager.getMultiFileFunctionByName("Export").execute(fileNames.stream().map(File::new).collect(toList()));
                } else {
                    FunctionManager.getSingleFileFunctionByName("Export").execute(new File(fileNames.get(0)));
                }
            }
        }));

        new MenuItem(fileMenu, SEPARATOR);

        final MenuItem quitMenuItem = new MenuItem(fileMenu, PUSH);
        quitMenuItem.setText("Quit");
        quitMenuItem.addSelectionListener(new DefaultSelectionListener(event -> shell.close()));


        final MenuItem projectMenuHeader = new MenuItem(menuBar, CASCADE);
        projectMenuHeader.setText("&Project");

        final Menu projectMenu = new Menu(shell, DROP_DOWN);
        projectMenuHeader.setMenu(projectMenu);

        for (FunctionInfo<ProjectFunction> functionInfo : FunctionManager.getProjectFunctions()) {
            final MenuItem menuItem = new MenuItem(projectMenu, PUSH);
            menuItem.setText(functionInfo.getName());
            menuItem.addSelectionListener(new DefaultSelectionListener(event -> {
                File[] files = getFileExplorer().getRootDirectory().listFiles(functionInfo.getFileFilter());
                if (files != null) {
                    functionInfo.getInstance().execute(asList(files));
                }
            }));
        }

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
        clearSceneMenuItem.addSelectionListener(new DefaultSelectionListener(event -> clearScene(true)));

        final MenuItem focusSceneMenuItem = new MenuItem(windowMenu, PUSH);
        focusSceneMenuItem.setText("Focus Scene");
        focusSceneMenuItem.addSelectionListener(new DefaultSelectionListener(event -> getScene().forceFocus()));


        final MenuItem debugMenuHeader = new MenuItem(menuBar, CASCADE);
        debugMenuHeader.setText("Debug");

        final Menu debugMenu = new Menu(shell, DROP_DOWN);
        debugMenuHeader.setMenu(debugMenu);

        final InspectJSONFunction debugFunction = new InspectJSONFunction();
        final MenuItem spectrumJsonMenuItem = new MenuItem(debugMenu, PUSH);
        spectrumJsonMenuItem.setText("Inspect JSON");
        spectrumJsonMenuItem.addSelectionListener(new DefaultSelectionListener(event -> {
            String fileName = FileDialogs.openFileDialog(FileType.SPECTRUM);
            if (fileName != null) {
                debugFunction.execute(new File(fileName));
            }
        }));

        final MenuItem exceptionMenuItem = new MenuItem(debugMenu, PUSH);
        exceptionMenuItem.setText("Throw an Exception");
        exceptionMenuItem.addSelectionListener(new DefaultSelectionListener(event -> {
            throw new RuntimeException("This is a debug exception");
        }));

        final RepairFunction repairFunction = new RepairFunction();
        final MenuItem repairMenuItem = new MenuItem(debugMenu, PUSH);
        repairMenuItem.setText("Repair file");
        repairMenuItem.addSelectionListener(new DefaultSelectionListener(event -> {
            String fileName = FileDialogs.openFileDialog(FileType.SPECTRUM);
            if (fileName != null) {
                repairFunction.execute(new File(fileName));
            }
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

    public static void clearScene(boolean clearToolsTab) {
        for (Control control : scene.getChildren()) {
            control.dispose();
        }

        if (clearToolsTab) {
            rightToolBar.disposeTabs();
        }
    }

    public static Composite clearAndGetScene() {
        return clearAndGetScene(true);
    }

    public static Composite clearAndGetScene(boolean clearToolsTab) {
        clearScene(clearToolsTab);
        return scene;
    }

    public static FileExplorer getFileExplorer() {
        return fileExplorer;
    }

    public static ToolBar getLeftToolBar() {
        return leftToolBar;
    }

    public static ToolBar getRightToolBar() {
        return rightToolBar;
    }

    public static ToolBar getBottomToolBar() {
        return bottomToolBar;
    }

    protected ComponentManager() throws IllegalAccessException {
        super();
    }
}
