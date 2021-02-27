package cz.cuni.mff.respefo.function.asset.rv;

import cz.cuni.mff.respefo.function.asset.common.LeftRightChartKeyListener;
import cz.cuni.mff.respefo.util.utils.ChartUtils;
import org.eclipse.swt.events.KeyEvent;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxisSet;

import static org.eclipse.swt.SWT.KEYPAD_4;
import static org.eclipse.swt.SWT.KEYPAD_6;

public class MeasureRVKeyListener extends LeftRightChartKeyListener {

    private final String mirroredSeriesName;
    private final double l0;
    private final Runnable updateRelativeStep;

    public MeasureRVKeyListener(Chart chart, String mirroredSeriesName, double l0, Runnable updateRelativeStep, Runnable moveLeft, Runnable moveRight) {
        super(chart, moveLeft, moveRight);
        this.mirroredSeriesName = mirroredSeriesName;
        this.l0 = l0;
        this.updateRelativeStep = updateRelativeStep;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.keyCode) {
            case '+':
            case 16777259:
            case 49:
                applyToAxisSet(IAxisSet::zoomIn);
                updateRelativeStep.run();
                break;
            case '-':
            case 16777261:
            case 47:
                applyToAxisSet(IAxisSet::zoomOut);
                updateRelativeStep.run();
                break;

            case '4':
            case KEYPAD_4:
                applyToAxes(IAxisSet::getXAxes, IAxis::zoomOut);
                updateRelativeStep.run();
                break;
            case '6':
            case KEYPAD_6:
                applyToAxes(IAxisSet::getXAxes, IAxis::zoomIn);
                updateRelativeStep.run();
                break;

            default:
                super.keyPressed(e);
        }
    }

    @Override
    protected void adjustRange() {
        ChartUtils.centerAroundSeriesAndMidpoint(chart, mirroredSeriesName, l0);
        chart.getAxisSet().zoomOut();
        chart.redraw();
        updateRelativeStep.run();
    }
}
