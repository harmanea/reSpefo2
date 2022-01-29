package cz.cuni.mff.respefo.function.common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.swtchart.Chart;

public abstract class ArrowsChartKeyListener extends ChartKeyListener {
    private final Runnable downAction;
    private final Runnable leftAction;
    private final Runnable rightAction;
    private final Runnable upAction;

    protected ArrowsChartKeyListener(Chart chart, Runnable downAction, Runnable leftAction, Runnable rightAction, Runnable upAction) {
        super(chart);

        this.downAction = downAction;
        this.leftAction = leftAction;
        this.rightAction = rightAction;
        this.upAction = upAction;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.keyCode) {
            case SWT.ARROW_DOWN:
                downAction.run();
                break;
            case SWT.ARROW_LEFT:
                leftAction.run();
                break;
            case SWT.ARROW_RIGHT:
                rightAction.run();
                break;
            case SWT.ARROW_UP:
                upAction.run();
                break;
            default:
                super.keyPressed(e);
        }
    }
}
