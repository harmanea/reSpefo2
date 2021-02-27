package cz.cuni.mff.respefo.function.asset.dispersion;

import cz.cuni.mff.respefo.function.asset.common.ChartKeyListener;
import cz.cuni.mff.respefo.function.asset.common.LeftRightChartKeyListener;
import org.swtchart.Chart;

public class DispersionKeyListener extends LeftRightChartKeyListener {

    private final String mirroredSeriesName;
    private final double l0;

    public DispersionKeyListener(Chart chart, String mirroredSeriesName, double l0, Runnable moveLeft, Runnable moveRight) {
        super(chart, moveLeft, moveRight);
        this.l0 = l0;
        this.mirroredSeriesName = mirroredSeriesName;
    }

    @Override
    protected void adjustRange() {
        ChartKeyListener.centerAroundSeries(chart, mirroredSeriesName);  // TODO: change this to stay on center
        chart.getAxisSet().zoomOut();
    }
}
