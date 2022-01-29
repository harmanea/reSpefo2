package cz.cuni.mff.respefo.function.rectify;

import cz.cuni.mff.respefo.function.common.ArrowsChartKeyListener;
import cz.cuni.mff.respefo.util.utils.ChartUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.swtchart.Chart;

public class BlazeKeyListener extends ArrowsChartKeyListener {
    private final Runnable finishAction;

    protected BlazeKeyListener(Chart chart, Blaze blaze, Runnable update, Runnable finishAction) {
        super(chart,
                () -> down(blaze, chart, update),
                () -> left(blaze, chart, update),
                () -> right(blaze, chart, update),
                () -> up(blaze, chart, update));
        this.finishAction = finishAction;
    }

    private static void down(Blaze blaze, Chart chart, Runnable update) {
        double diff = ChartUtils.getRelativeVerticalStep(chart);
        blaze.updateScale(-diff);
        update.run();
    }

    private static void left(Blaze blaze, Chart chart, Runnable update) {
        double diff = ChartUtils.getRelativeHorizontalStep(chart);
        blaze.updateCentralWavelength(-diff);
        update.run();
    }

    private static void right(Blaze blaze, Chart chart, Runnable update) {
        double diff = ChartUtils.getRelativeHorizontalStep(chart);
        blaze.updateCentralWavelength(diff);
        update.run();
    }

    private static void up(Blaze blaze, Chart chart, Runnable update) {
        double diff = ChartUtils.getRelativeVerticalStep(chart);
        blaze.updateScale(diff);
        update.run();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.keyCode == SWT.CR) {
            finishAction.run();
        } else {
            super.keyPressed(e);
        }
    }

    @Override
    protected void adjustRange() {
        ChartUtils.makeAllSeriesEqualRange(chart);
        chart.redraw();
    }
}
