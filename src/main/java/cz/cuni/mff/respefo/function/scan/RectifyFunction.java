package cz.cuni.mff.respefo.function.scan;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.format.Spectrum;
import cz.cuni.mff.respefo.format.XYSeries;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.Serialize;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.asset.common.ChartKeyListener;
import cz.cuni.mff.respefo.function.asset.common.NearestPointMouseMoveListener;
import cz.cuni.mff.respefo.function.asset.common.ZoomMouseWheelListener;
import cz.cuni.mff.respefo.function.asset.rectify.RectifyAsset;
import cz.cuni.mff.respefo.function.asset.rectify.RectifyMouseListener;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.resources.ColorResource;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.Point;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.ILineSeries;
import org.swtchart.Range;

import java.io.File;

import static cz.cuni.mff.respefo.util.builders.ChartBuilder.LineSeriesBuilder.lineSeries;
import static cz.cuni.mff.respefo.util.builders.ChartBuilder.ScatterSeriesBuilder.scatterSeries;
import static cz.cuni.mff.respefo.util.builders.ChartBuilder.chart;

@Fun(name = "Rectify", fileFilter = SpefoFormatFileFilter.class)
@Serialize(key = RectifyFunction.SERIALIZE_KEY, assetClass = RectifyAsset.class)
public class RectifyFunction implements SingleFileFunction {

    public static final String SERIALIZE_KEY = "rectify";
    public static final String POINTS_SERIES_NAME = "points";
    public static final String SELECTED_SERIES_NAME = "selected";
    public static final String CONTINUUM_SERIES_NAME = "continuum";

    @Override
    public void execute(File file) {
        Spectrum spectrum;
        try {
            spectrum = Spectrum.open(file);
        } catch (SpefoException e) {
            Message.error("Couldn't open file", e);
            return;
        }

        RectifyAsset asset = (RectifyAsset) spectrum.getFunctionAssets()
                .getOrDefault(SERIALIZE_KEY, RectifyAsset.withDefaultPoints(spectrum.getProcessedSeries()));

        XYSeries series = spectrum.getProcessedSeriesBefore(asset);

        Chart chart = chart(ComponentManager.clearAndGetScene())
                .title(file.getName())
                .xAxisLabel("wavelength (Å)")
                .yAxisLabel("relative flux I(λ)")
                .series(lineSeries()
                        .name("original")
                        .color(ColorResource.GREEN)
                        .series(series))
                .series(lineSeries()
                        .name(CONTINUUM_SERIES_NAME)
                        .color(ColorResource.YELLOW)
                        .xSeries(series.getXSeries())
                        .ySeries(asset.getIntepData(series.getXSeries())))
                .series(scatterSeries()
                        .name(POINTS_SERIES_NAME)
                        .color(ColorResource.WHITE)
                        .symbolSize(3)
                        .xSeries(asset.getXCoordinatesArray())
                        .ySeries(asset.getYCoordinatesArray()))
                .series(scatterSeries()
                        .name(SELECTED_SERIES_NAME)
                        .color(ColorResource.RED)
                        .symbolSize(3)
                        .series(asset.getActivePoint()))
                .keyListener(ChartKeyListener::makeAllSeriesEqualRange)
                .mouseAndMouseMoveListener(ch -> new RectifyMouseListener(ch,
                        point -> {
                            asset.addPoint(point);
                            updateAllSeries(ch, asset, series);
                        },
                        () -> {
                            asset.deleteActivePoint();
                            updateAllSeries(ch, asset, series);
                        }))
                .mouseMoveListener(ch -> new NearestPointMouseMoveListener(ch, POINTS_SERIES_NAME, index -> {
                    if (asset.getActiveIndex() != index) {
                        updateActivePoint(ch, asset, index);
                    }
                }))
                .mouseWheelListener(ZoomMouseWheelListener::new)
                .makeAllSeriesEqualRange()
                .forceFocus()
                .build();

        chart.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.keyCode) {
                    case SWT.INSERT: {
                        Range xRange = chart.getAxisSet().getXAxis(0).getRange();
                        Range yRange = chart.getAxisSet().getYAxis(0).getRange();

                        asset.addPoint(new Point((xRange.upper + xRange.lower) / 2, (yRange.upper + yRange.lower) / 2));

                        updateAllSeries(chart, asset, series);
                        break;
                    }
                    case SWT.DEL: {
                        asset.deleteActivePoint();

                        updateAllSeries(chart, asset, series);
                        break;
                    }
                    case 'n': {
                        if (asset.getActiveIndex() > 0) {
                            updateActivePoint(chart, asset, asset.getActiveIndex() - 1);
                        }
                        break;
                    }
                    case 'm': {
                        if (asset.getActiveIndex() < asset.getCount() - 1) {
                            updateActivePoint(chart, asset, asset.getActiveIndex() + 1);
                        }
                        break;
                    }
                    case 'p': {
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
                    }
                    case SWT.CR: {
                        if (asset.isEmpty()) {
                            spectrum.getFunctionAssets().remove(SERIALIZE_KEY);
                        } else {
                            spectrum.getFunctionAssets().put(SERIALIZE_KEY, asset);
                        }

                        try {
                            spectrum.save();

                            Message.info("Rectified spectrum saved successfully.");
                            OpenFunction.displaySpectrum(spectrum);

                        } catch (SpefoException exception) {
                            Message.error("Couldn't save file", exception);
                        }
                        break;
                    }
                }
            }
        });
    }

    private static void updateActivePoint(Chart chart, RectifyAsset asset, int newIndex) {
        asset.setActiveIndex(newIndex);

        ILineSeries series = (ILineSeries) chart.getSeriesSet().getSeries(SELECTED_SERIES_NAME);

        series.setXSeries(asset.getActivePoint().getXSeries());
        series.setYSeries(asset.getActivePoint().getYSeries());

        chart.redraw();
    }

    private static void updateAllSeries(Chart chart, RectifyAsset asset, XYSeries xySeries) {
        ILineSeries lineSeries = (ILineSeries) chart.getSeriesSet().getSeries(POINTS_SERIES_NAME);
        lineSeries.setXSeries(asset.getXCoordinatesArray());
        lineSeries.setYSeries(asset.getYCoordinatesArray());

        lineSeries = (ILineSeries) chart.getSeriesSet().getSeries(SELECTED_SERIES_NAME);
        lineSeries.setXSeries(asset.getActivePoint().getXSeries());
        lineSeries.setYSeries(asset.getActivePoint().getYSeries());

        lineSeries = (ILineSeries) chart.getSeriesSet().getSeries(CONTINUUM_SERIES_NAME);
        lineSeries.setYSeries(asset.getIntepData(xySeries.getXSeries()));

        chart.redraw();
    }
}
