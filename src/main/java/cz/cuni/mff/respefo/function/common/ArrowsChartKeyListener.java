package cz.cuni.mff.respefo.function.common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.swtchart.Chart;

public abstract class ArrowsChartKeyListener extends ChartKeyListener {

    protected ArrowsChartKeyListener(Chart chart) {
        super(chart);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.keyCode) {
            case SWT.ARROW_DOWN:
                down();
                break;
            case SWT.ARROW_LEFT:
                left();
                break;
            case SWT.ARROW_RIGHT:
                right();
                break;
            case SWT.ARROW_UP:
                up();
                break;
            default:
                super.keyPressed(e);
        }
    }

    protected abstract void down();

    protected abstract void left();

    protected abstract void right();

    protected abstract void up();
}
