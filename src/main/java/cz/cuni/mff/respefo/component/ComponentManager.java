package cz.cuni.mff.respefo.component;

import cz.cuni.mff.respefo.function.*;
import cz.cuni.mff.respefo.function.debug.InspectJSONFunction;
import cz.cuni.mff.respefo.function.debug.RepairFunction;
import cz.cuni.mff.respefo.function.open.OpenFunction;
import cz.cuni.mff.respefo.function.port.ExportFunction;
import cz.cuni.mff.respefo.function.port.ImportFunction;
import cz.cuni.mff.respefo.logging.FancyLogListener;
import cz.cuni.mff.respefo.logging.LabelLogListener;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.logging.LogLevel;
import cz.cuni.mff.respefo.resources.ImageResource;
import cz.cuni.mff.respefo.util.*;
import cz.cuni.mff.respefo.util.info.VersionInfo;
import cz.cuni.mff.respefo.util.utils.FileUtils;
import cz.cuni.mff.respefo.util.widget.CompositeBuilder;
import cz.cuni.mff.respefo.util.widget.DefaultSelectionListener;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

import java.io.File;
import java.net.URL;
import java.util.Arrays;

import static cz.cuni.mff.respefo.util.layout.FillLayoutBuilder.fillLayout;
import static cz.cuni.mff.respefo.util.layout.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.layout.MenuBuilder.*;
import static cz.cuni.mff.respefo.util.layout.RowLayoutBuilder.rowLayout;
import static cz.cuni.mff.respefo.util.widget.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.widget.LabelBuilder.newLabel;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.eclipse.swt.SWT.*;

public class ComponentManager extends UtilityClass {
    private static Display display;
    private static Shell shell;

    private static Composite scene;

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

    public static void build() {
        shell.setLayout(gridLayout(3, false).margins(0).spacings(0).build());
        shell.addListener(Close, event -> event.doit = Message.question("Are you sure you want to quit?"));
        shell.addShellListener(new ShellAdapter() {
            @Override
            public void shellActivated(ShellEvent e) {
                if (Project.getRootDirectory() == null) {
                    Project.setRootDirectory(FileUtils.getUserDirectory());
                }
            }
        });

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
                .layout(gridLayout(4, false).margins(1).spacings(3))
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

        CompositeBuilder toolBarCompositeBuilder = newComposite()
                .gridLayoutData(GridData.FILL_BOTH)
                .layout(gridLayout().margins(0).spacings(0));

        final Composite leftBarComposite = toolBarCompositeBuilder.build(componentWithSidebars.getLeftBar());

        leftToolBar = new ToolBar(leftBarComposite, leftBar, componentWithSidebars::toggleLeftBar);

        ToolBar.Tab projectTab = leftToolBar.addTab(parent -> new VerticalToggle(parent, UP),
                "Project", "Project", ImageResource.FOLDER_LARGE);

        final FileExplorer fileExplorer = new FileExplorer(projectTab.getWindow());
        fileExplorer.setLayoutData(new GridData(GridData.FILL_BOTH));
        FileExplorer.setDefaultInstance(fileExplorer);

        projectTab.addTopBarButton("Change Directory", ImageResource.OPENED_FOLDER, Project::changeRootDirectory);
        projectTab.addTopBarButton("Refresh", ImageResource.REFRESH, fileExplorer::refresh);
        projectTab.addTopBarButton("Collapse All", ImageResource.COLLAPSE, fileExplorer::collapseAll);

        projectTab.show();

        ToolBar.Tab spectraTab = leftToolBar.addTab(parent -> new VerticalToggle(parent, UP),
                "Spectra", "Spectra", ImageResource.SPECTRA_LARGE);

        SpectrumExplorer spectrumExplorer = new SpectrumExplorer(spectraTab.getWindow());
        spectrumExplorer.setLayoutData(new GridData(GridData.FILL_BOTH));
        SpectrumExplorer.setDefaultInstance(spectrumExplorer);

        spectraTab.addTopBarButton("Change Directory", ImageResource.OPENED_FOLDER, Project::changeRootDirectory);
        spectraTab.addTopBarButton("Refresh", ImageResource.REFRESH, spectrumExplorer::refresh);

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
        Log.registerListener(fancyLogListener, LogLevel.INFO);
        Log.registerActionListener(fancyLogListener);

        final Menu menu = new Menu(shell, POP_UP);
        for (LogLevel level : LogLevel.values()) {
            final MenuItem item = new MenuItem(menu, PUSH);
            item.setText(level.name());
            item.addSelectionListener(new DefaultSelectionListener(event -> fancyLogListener.setMinimumLevel(level)));
        }
        eventLogTab.addTopBarMenuButton("Minimum Level", ImageResource.FILTER, menu);

        eventLogTab.addTopBarToggleButton("Scroll to End", ImageResource.SCROLL_TO_END, fancyLogListener::setScrollToEnd);

        eventLogTab.show();
        bottomToolBar.hide();

        final Label bottomLogLabel = newLabel()
                .text("")
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_CENTER)
                .listener(MouseDown, event -> eventLogTab.show())
                .build(bottomBar);
        Log.registerListener(new LabelLogListener(bottomLogLabel, eventLogTab::isHidden), LogLevel.INFO);
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
                .layout(fillLayout().marginWidth(10).marginHeight(0))
                .build(bottomBar);

        final ProgressBar progressBar = new ProgressBar(progressBarComposite, SMOOTH);
        progressBar.setVisible(false);

        Progress.setControls(progressBar, progressLabel);

        // Menu

        bar(shell,
                header("&File",
                        item("Open", function(new OpenFunction())),
                        separator(),
                        item("Import", multipleFilesFunction(FileType.COMPATIBLE_SPECTRUM_FILES, new ImportFunction())),
                        item("Export", multipleFilesFunction(FileType.SPECTRUM, new ExportFunction())),
                        separator(),
                        item("Quit", () -> shell.close())
                ),
                header("&Project",
                        item("Change Directory", Project::changeRootDirectory),
                        separator(),
                        subMenuItems(projectFunctionGroups(), ComponentManager::projectFunctionsForGroup, FunctionInfo::getName, ComponentManager::projectFunction),
                        separator(),
                        items(projectFunctionsWithoutGroup(), FunctionInfo::getName, ComponentManager::projectFunction)
                ),
                header("&Window",
                        item("Hide All Toolbars", ComponentManager::hideToolbars),
                        separator(),
                        item("Clear Scene", () -> clearScene(true)),
                        item("Focus Scene", () -> getScene().forceFocus())
                ),
                header("Debug",
                        separator(),
                        item("Inspect JSON", function(new InspectJSONFunction())),
                        item("Repair File", function(new RepairFunction())),
                        separator(),
                        item("Long Running Task", longTask()),
                        item("Throw an Exception", () -> { throw new RuntimeException("This is a debug exception"); }),
                        subMenu("Log",
                                item("Error", () -> Log.error("Test error log", new RuntimeException("This is a debug exception"))),
                                item("Warning", () -> Log.warning("Test warning log")),
                                item("Info", () -> Log.info("Test info log")),
                                item("Debug", () -> Log.debug("Test debug log")),
                                item("Trace", () -> Log.trace("Test trace log")),
                                separator(),
                                item("Action", () -> Log.action("Test action log ", "Action", () -> Message.info("Test action!"), false))
                        )
                ),
                header("Help",
                        item("Documentation", ComponentManager::showDocumentation)
                )
        );
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

    public static ToolBar getLeftToolBar() {
        return leftToolBar;
    }

    public static ToolBar getRightToolBar() {
        return rightToolBar;
    }

    public static ToolBar getBottomToolBar() {
        return bottomToolBar;
    }

    public static void hideToolbars() {
        leftToolBar.hide();
        bottomToolBar.hide();
        rightToolBar.hide();
    }

    private static Runnable function(SingleFileFunction function) {
        return () -> {
            String fileName = FileDialogs.openFileDialog(FileType.SPECTRUM);
            if (fileName != null) {
                function.execute(new File(fileName));
            }
        };
    }

    private static <T extends SingleFileFunction & MultiFileFunction> Runnable multipleFilesFunction(FileType fileType, T function) {
        return () -> {
            java.util.List<String> fileNames = FileDialogs.openMultipleFilesDialog(fileType);
            if (!fileNames.isEmpty()) {
                if (fileNames.size() > 1) {
                    function.execute(fileNames.stream().map(File::new).collect(toList()));
                } else {
                    function.execute(new File(fileNames.get(0)));
                }
            }
        };
    }

    private static Runnable projectFunction(FunctionInfo<ProjectFunction> functionInfo) {
        return () -> {
            File[] files = Project.getRootDirectory().listFiles(functionInfo.getFileFilter());
            if (files != null) {
                Arrays.sort(files);
                functionInfo.getInstance().execute(asList(files));
            }
        };
    }

    private static Iterable<String> projectFunctionGroups() {
        return FunctionManager.getProjectFunctions().stream()
                .filter(functionInfo -> functionInfo.getGroup().isPresent())
                .map(functionInfo -> functionInfo.getGroup().get())
                .distinct()
                .sorted()
                .collect(toList());
    }

    private static Iterable<FunctionInfo<ProjectFunction>> projectFunctionsWithoutGroup() {
        return FunctionManager.getProjectFunctions().stream()
                .filter(functionInfo -> !functionInfo.getGroup().isPresent())
                .collect(toList());
    }

    private static Iterable<FunctionInfo<ProjectFunction>> projectFunctionsForGroup(String groupName) {
        return FunctionManager.getProjectFunctions().stream()
                .filter(functionInfo -> groupName.equals(functionInfo.getGroup().orElse(null)))
                .collect(toList());
    }

    private static Runnable longTask() {
        return () -> Progress.withProgressTracking(
                p -> {
                    p.refresh("Long task", 25);
                    for (int i = 0; i < 25; i++) {
                        try {
                            Thread.sleep(500);
                            p.step();
                        } catch (InterruptedException e) {
                            Log.debug("Thread interrupted");
                            Thread.currentThread().interrupt();
                        }
                    }

                    return null;
                }, n -> {}
        );
    }

    private static void showDocumentation() {
        try {
            final Browser browser = new Browser(clearAndGetScene(), NONE);
            browser.setLayoutData(new GridData(GridData.FILL_BOTH));
            browser.addProgressListener(Progress.progressListener());
            URL url = ComponentManager.class.getClassLoader().getResource("docs/documentation.html");
            browser.setUrl(url.toString());
            browser.requestLayout();

        } catch (SWTError error) {
            Message.error("Couldn't instantiate browser", error);
        }
    }

    protected ComponentManager() throws IllegalAccessException {
        super();
    }
}
