package cz.cuni.mff.respefo.function.asset.common;

import org.eclipse.swt.events.KeyEvent;
import org.swtchart.Chart;

import static org.eclipse.swt.SWT.ARROW_LEFT;
import static org.eclipse.swt.SWT.ARROW_RIGHT;

public abstract class LeftRightChartKeyListener extends ChartKeyListener {

    private final Runnable moveLeft;
    private final Runnable moveRight;

    protected LeftRightChartKeyListener(Chart chart, Runnable moveLeft, Runnable moveRight) {
        super(chart);
        this.moveLeft = moveLeft;
        this.moveRight = moveRight;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.keyCode) {
            case 'a':
            case ARROW_LEFT:
                moveLeft.run();
                break;
            case 'd':
            case ARROW_RIGHT:
                moveRight.run();
                break;

            default:
                super.keyPressed(e);
        }
    }
}
