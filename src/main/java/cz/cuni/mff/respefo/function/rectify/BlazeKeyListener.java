package cz.cuni.mff.respefo.function.rectify;

import cz.cuni.mff.respefo.function.common.ArrowsChartKeyListener;
import cz.cuni.mff.respefo.util.utils.ChartUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.swtchart.Chart;

public class BlazeKeyListener extends ArrowsChartKeyListener {
    private final Blaze blaze;
    private final Runnable update;
    private final Runnable next;
    private final Runnable previous;

    protected BlazeKeyListener(Chart chart, Blaze blaze, Runnable update, Runnable next, Runnable previous) {
        super(chart);
        this.blaze = blaze;
        this.update = update;
        this.next = next;
        this.previous = previous;
    }

    @Override
    protected void down() {
        double diff = ChartUtils.getRelativeVerticalStep(chart);
        blaze.updateScale(-diff);
        update.run();
    }

    @Override
    protected void left() {
        double diff = ChartUtils.getRelativeHorizontalStep(chart) / 2;
        blaze.updateCentralWavelength(-diff);
        update.run();
    }

    @Override
    protected void right() {
        double diff = ChartUtils.getRelativeHorizontalStep(chart) / 2;
        blaze.updateCentralWavelength(diff);
        update.run();
    }

    @Override
    protected void up() {
        double diff = ChartUtils.getRelativeVerticalStep(chart);
        blaze.updateScale(diff);
        update.run();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.keyCode) {
            case SWT.CR:
            case SWT.END:
                next.run();
                break;
            case SWT.BS:
                previous.run();
                break;
            default:
                super.keyPressed(e);
        }
    }
}
