package cz.cuni.mff.respefo.function.rectify;

import cz.cuni.mff.respefo.util.utils.ChartUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.Range;

import java.util.function.Consumer;

public class RectifyKeyListener extends KeyAdapter {

    private final Chart chart;
    private final RectifyAsset asset;
    private final Runnable updateAllSeries;
    private final Consumer<Integer> updateActivePoint;
    private final Runnable finish;

    public RectifyKeyListener(Chart chart, RectifyAsset asset, Runnable updateAllSeries, Consumer<Integer> updateActivePoint, Runnable finish) {
        this.chart = chart;
        this.asset = asset;
        this.updateAllSeries = updateAllSeries;
        this.updateActivePoint = updateActivePoint;
        this.finish = finish;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.keyCode) {
            case SWT.INSERT:
                Range xRange = chart.getAxisSet().getXAxis(0).getRange();
                Range yRange = chart.getAxisSet().getYAxis(0).getRange();

                asset.addPoint((xRange.upper + xRange.lower) / 2, (yRange.upper + yRange.lower) / 2);

                updateAllSeries.run();
                break;
            case SWT.DEL:
                asset.deleteActivePoint();

                updateAllSeries.run();
                break;
            case 'n':
                if (asset.getActiveIndex() > 0) {
                    updateActivePoint.accept(asset.getActiveIndex() - 1);
                }
                break;
            case 'm':
                if (asset.getActiveIndex() < asset.getCount() - 1) {
                    updateActivePoint.accept(asset.getActiveIndex() + 1);
                }
                break;
            case 'i':
                asset.moveActivePoint(0, ChartUtils.getRelativeVerticalStep(chart));
                updateAllSeries.run();
                break;
            case 'j':
                asset.moveActivePoint(-ChartUtils.getRelativeHorizontalStep(chart), 0);
                updateAllSeries.run();
                break;
            case 'k':
                asset.moveActivePoint(0, -ChartUtils.getRelativeVerticalStep(chart));
                updateAllSeries.run();
                break;
            case 'l':
                asset.moveActivePoint(ChartUtils.getRelativeHorizontalStep(chart), 0);
                updateAllSeries.run();
                break;
            case 'p':
                double x = asset.getActiveX();
                Range chartXRange = chart.getAxisSet().getXAxis(0).getRange();
                Range newXRange = new Range(x - (chartXRange.upper - chartXRange.lower) / 2, x + (chartXRange.upper - chartXRange.lower) / 2);

                for (IAxis axis : chart.getAxisSet().getXAxes()) {
                    axis.setRange(newXRange);
                }

                double y = asset.getActiveY();
                Range chartYRange = chart.getAxisSet().getYAxis(0).getRange();
                Range newYRange = new Range(y - (chartYRange.upper - chartYRange.lower) / 2, y + (chartYRange.upper - chartYRange.lower) / 2);

                for (IAxis axis : chart.getAxisSet().getYAxes()) {
                    axis.setRange(newYRange);
                }

                chart.redraw();
                break;
            case SWT.CR:
                finish.run();
                break;
        }
    }
}
