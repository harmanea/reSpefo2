package cz.cuni.mff.respefo.component;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.logging.LabelLogListener;
import cz.cuni.mff.respefo.logging.ListLogListener;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.resources.ImageManager;
import cz.cuni.mff.respefo.resources.ImageResource;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.UtilityClass;
import cz.cuni.mff.respefo.util.VersionInfo;
import cz.cuni.mff.respefo.util.utils.FileUtils;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
    private static ComponentWithBottomBar componentWithBottomBar;

    public static void init() {
        display = new Display();
        Display.setAppName("reSpefo");
        Display.setAppVersion(VersionInfo.getVersion());

        shell = new Shell(display, SHELL_TRIM);
        shell.setText("reSpefo (" + VersionInfo.getVersion() + ")");
    }

    public static void build() throws SpefoException {
        shell.setLayout(
                gridLayout(3, false)
                        .margins(0)
                        .spacings(0)
                        .build()
        );


        // Top level

        final Composite leftBar = composite(shell, BORDER)
                .layoutData(new GridData(LEFT, FILL, false, true))
                .layout(
                        gridLayout()
                                .margins(0)
                                .spacings(0)
                                .build()
                ).build();

        componentWithBottomBar = new ComponentWithBottomBar(shell);
        componentWithBottomBar.setLayoutData(new GridData(FILL, FILL, true, true));
        componentWithBottomBar.getScene().setLayout(new FillLayout()); // TEMPORARY
        componentWithBottomBar.getBottomBar().setLayout(new FillLayout()); // TEMPORARY
        componentWithBottomBar.maximizeScene();

        final Composite rightBar = composite(shell, BORDER)
                .layoutData(new GridData(RIGHT, FILL, false, true))
                .layout(
                        gridLayout()
                                .margins(0)
                                .spacings(0)
                                .build()
                ).build();

        final Composite bottomBar = composite(shell, BORDER)
                .layoutData(new GridData(FILL, BOTTOM, true, false, 3, 1))
                .layout(
                        gridLayout(2, false)
                                .margins(3)
                                .spacings(3)
                                .build()
                ).build();


        // componentWithBottomBar -> scene

        componentWithSidebars = new ComponentWithSidebars(componentWithBottomBar.getScene());
        componentWithSidebars.getLeftBar().setLayout(new FillLayout()); // TEMPORARY
        componentWithSidebars.getScene().setLayout(new GridLayout(1, false));
        componentWithSidebars.getRightBar().setLayout(fillLayout().margins(3).build());

        fileExplorer = new FileExplorer(componentWithSidebars.getLeftBar());
        fileExplorer.setRootDirectory(FileUtils.getUserDirectory());

        label(componentWithSidebars.getScene(), CENTER)
                .text("Welcome to reSpefo!") // TEMPORARY TEXT
                .layoutData(new GridData(CENTER, CENTER, true, true))
                .build();

        // TEMPORARY
        pushButton(componentWithSidebars.getRightBar())
                .text("Log")
                .onSelection(event -> Log.info("This is a test log " + System.currentTimeMillis()))
                .build();


        // componentWithBottomBar -> bottom bar

        // TEMPORARY
        final List logList = new List(componentWithBottomBar.getBottomBar(), SINGLE | V_SCROLL);
        Log.registerListener(new ListLogListener(logList));


        // Fill main sidebars

        final Toggle leftToggle = new Toggle(leftBar, NONE) {
            @Override
            protected void toggleAction() {
                componentWithSidebars.toggleLeftBar();
            }
        };
        leftToggle.setToggled(true);
        leftToggle.setImage(ImageManager.getImage(ImageResource.FOLDER_LARGE));
        leftToggle.setLayoutData(new GridData(FILL, TOP, true, false));


        final Toggle rightToggle = new Toggle(rightBar, NONE) {
            @Override
            protected void toggleAction() {
                componentWithSidebars.toggleRightBar();
            }
        };
        rightToggle.setToggled(true);
        rightToggle.setImage(ImageManager.getImage(ImageResource.WRENCH_LARGE));
        rightToggle.setLayoutData(new GridData(FILL, TOP, true, false));

        final Toggle bottomToggle = new Toggle(bottomBar, NONE) {
            @Override
            protected void toggleAction() {
                componentWithBottomBar.toggleScene();
            }
        };
        bottomToggle.setToggled(false);
        bottomToggle.setImage(ImageManager.getImage(ImageResource.SCROLL_LARGE));
        bottomToggle.setLayoutData(new GridData(LEFT, FILL, false, true));

        final Label bottomLogLabel = label(bottomBar, NONE)
                .text("")
                .layoutData(new GridData(LEFT, CENTER, false, false))
                .build();
        bottomLogLabel.addListener(MouseDown, event -> {
            bottomToggle.toggle();
            bottomLogLabel.setText("");
            bottomLogLabel.requestLayout();
        });

        Log.registerListener(new LabelLogListener(bottomLogLabel));


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
        Message.error("An error occurred in one of the components.", exception);
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

    public static FileExplorer getFileExplorer() {
        return fileExplorer;
    }

    protected ComponentManager() throws IllegalAccessException {
        super();
    }
}
