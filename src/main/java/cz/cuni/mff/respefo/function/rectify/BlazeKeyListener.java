package cz.cuni.mff.respefo.function.rectify;

import cz.cuni.mff.respefo.function.common.ArrowsChartKeyListener;
import cz.cuni.mff.respefo.util.utils.ChartUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.swtchart.Chart;

public class BlazeKeyListener extends ArrowsChartKeyListener {
    private final Blaze blaze;
    private final Runnable update;
    private final Runnable finishAction;

    protected BlazeKeyListener(Chart chart, Blaze blaze, Runnable update, Runnable finishAction) {
        super(chart);
        this.blaze = blaze;
        this.update = update;
        this.finishAction = finishAction;
    }

    @Override
    protected void down() {
        double diff = ChartUtils.getRelativeVerticalStep(chart);
        blaze.updateScale(-diff);
        update.run();
    }

    @Override
    protected void left() {
        double diff = ChartUtils.getRelativeHorizontalStep(chart);
        blaze.updateCentralWavelength(-diff);
        update.run();
    }

    @Override
    protected void right() {
        double diff = ChartUtils.getRelativeHorizontalStep(chart);
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
        if (e.keyCode == SWT.CR || e.keyCode == SWT.END) {
            finishAction.run();
        } else {
            super.keyPressed(e);
        }
    }
}
