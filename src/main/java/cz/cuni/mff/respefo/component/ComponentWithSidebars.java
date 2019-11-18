package cz.cuni.mff.respefo.component;

import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;

import static org.eclipse.swt.SWT.HORIZONTAL;
import static org.eclipse.swt.SWT.NONE;

public class ComponentWithSidebars {
    private final SashForm sash;
    private final Composite leftBar;
    private final Composite scene;
    private final Composite rightBar;

    private int leftWeight = -1;
    private int rightWeight = -1;

    public ComponentWithSidebars(Composite parent) {
        sash = new SashForm(parent, HORIZONTAL);

        leftBar = new Composite(sash, NONE);
        scene = new Composite(sash, NONE);
        rightBar = new Composite(sash, NONE);
    }

    public Composite getLeftBar() {
        return leftBar;
    }

    public Composite getScene() {
        return scene;
    }

    public Composite getRightBar() {
        return rightBar;
    }

    public void setWeights(int[] weights) {
        sash.setWeights(weights);
    }

    public void minimizeLeftBar() {
        leftWeight = minimizeBar(leftBar, 0);
    }

    public void restoreLeftBar() {
        leftWeight = restoreBar(leftBar, 0, leftWeight);
    }

    public void minimizeRightBar() {
        rightWeight = minimizeBar(rightBar, 2);
    }

    public void restoreRightBar() {
        rightWeight = restoreBar(rightBar, 2, rightWeight);
    }

    private int minimizeBar(Composite bar, int index) {
        int[] weights = sash.getWeights();

        weights[1] += weights[index];
        int weight = weights[index];
        weights[index] = 0;

        bar.setVisible(false);

        setWeights(weights);

        return weight;
    }

    private int restoreBar(Composite bar, int index, int weight) {
        int[] weights = sash.getWeights();
        int minWeight = (weights[2 - index] + weights[1] + weight) / 99;

        weights[1] -= weight;
        if (weights[1] < minWeight) {
            weights[index] = weight + weights[1] - minWeight;
            weights[1] = minWeight;
        } else {
            weights[index] = weight;
        }

        bar.setVisible(true);

        setWeights(weights);

        return -1;
    }

    public void toggleLeftBar() {
        if (isLeftBarMinimized()) {
            restoreLeftBar();
        } else {
            minimizeLeftBar();
        }
    }

    public void toggleRightBar() {
        if (isRightBarMinimized()) {
            restoreRightBar();
        } else {
            minimizeRightBar();
        }
    }

    public boolean isLeftBarMinimized() {
        return leftWeight >= 0;
    }

    public boolean isRightBarMinimized() {
        return rightWeight >= 0;
    }
}
