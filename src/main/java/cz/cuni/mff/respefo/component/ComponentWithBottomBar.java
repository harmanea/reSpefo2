package cz.cuni.mff.respefo.component;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;

import static org.eclipse.swt.SWT.NONE;
import static org.eclipse.swt.SWT.VERTICAL;

public class ComponentWithBottomBar {
    private final SashForm sash;
    private final Composite scene;
    private final Composite bottomBar;

    public ComponentWithBottomBar(Composite parent) {
        sash = new SashForm(parent, VERTICAL);
        sash.setBackground(sash.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
        sash.setSashWidth(1);

        scene = new Composite(sash, NONE);
        bottomBar = new Composite(sash, NONE);
    }

    public Composite getScene() {
        return scene;
    }

    public Composite getBottomBar() {
        return bottomBar;
    }

    public void setLayoutData(Object layoutData) {
        sash.setLayoutData(layoutData);
    }

    public void setWeights(int[] weights) {
        sash.setWeights(weights);
    }

    public void maximizeScene() {
        sash.setMaximizedControl(scene);
    }

    public void restoreScene() {
        sash.setMaximizedControl(null);
    }

    public void toggleScene() {
        if (isSceneMaximized()) {
            restoreScene();
        } else {
            maximizeScene();
        }
    }

    public boolean isSceneMaximized() {
        return sash.getMaximizedControl() == scene;
    }
}
