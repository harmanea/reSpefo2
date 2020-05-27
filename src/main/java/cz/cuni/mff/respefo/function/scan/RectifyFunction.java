package cz.cuni.mff.respefo.function.scan;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.format.Data;
import cz.cuni.mff.respefo.format.Spectrum;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.Serialize;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.asset.rectify.RectifyAsset;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.resources.ColorResource;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.Point;
import cz.cuni.mff.respefo.util.utils.ChartUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Rectangle;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;
import org.swtchart.Range;

import java.io.File;

import static cz.cuni.mff.respefo.util.builders.ChartBuilder.LineSeriesBuilder.lineSeries;
import static cz.cuni.mff.respefo.util.builders.ChartBuilder.ScatterSeriesBuilder.scatterSeries;
import static cz.cuni.mff.respefo.util.builders.ChartBuilder.chart;

@Fun(name = "Rectify", fileFilter = SpefoFormatFileFilter.class)
@Serialize(key = RectifyFunction.SERIALIZE_KEY, assetClass = RectifyAsset.class)
public class RectifyFunction implements SingleFileFunction {

    static final String SERIALIZE_KEY = "rectify";

    @Override
    public void execute(File file) {
        Spectrum spectrum;
        try {
            spectrum = new Spectrum(file);
        } catch (SpefoException e) {
            Message.error("Couldn't open file", e);
            return;
        }

        RectifyAsset asset = (RectifyAsset) spectrum.getSpectrumFile()
                .getFunctionAssets()
                .get(SERIALIZE_KEY);

        if (asset == null) {
            Data data = spectrum.getProcessedData();

            double[] xSeries = data.getX();
            double[] ySeries = data.getY();

            asset = RectifyAsset.withDefaultPoints(xSeries[0], ySeries[0], xSeries[xSeries.length - 1], ySeries[ySeries.length - 1]);
            spectrum.getSpectrumFile().getFunctionAssets().put(SERIALIZE_KEY, asset);
        }

        Data data = spectrum.getProcessedDataBefore(asset);

        chart(ComponentManager.clearAndGetScene())
                .title(file.getName())
                .xAxisLabel("wavelength (Å)")
                .yAxisLabel("relative flux")
                .series(lineSeries()
                        .name("original")
                        .color(ColorResource.GREEN)
                        .data(data))
                .series(lineSeries()
                        .name("continuum")
                        .color(ColorResource.YELLOW)
                        .xSeries(data.getX())
                        .ySeries(asset.getIntepData(data.getX())))
                .series(scatterSeries()
                        .name("points")
                        .color(ColorResource.WHITE)
                        .symbolSize(3)
                        .xSeries(asset.getXCoordinatesArray())
                        .ySeries(asset.getYCoordinatesArray()))
                .series(scatterSeries()
                        .name("selected")
                        .color(ColorResource.RED)
                        .symbolSize(3)
                        .data(asset.getActivePoint()))
                .mouseListener(chart -> mouseListener(chart, spectrum, data))
                .mouseMoveListener(chart -> mouseMoveListener(chart, spectrum))
                .keyListener(chart -> keyListener(spectrum))
                .makeAllSeriesEqualRange()
                .forceFocus()
                .build();
    }

    private static MouseListener mouseListener(Chart chart, Spectrum spectrum, Data data) {
        return new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent event) {
                if (event.button == 1 || event.button == 3) {
                    RectifyAsset rectifyAsset = ((RectifyAsset) spectrum.getSpectrumFile().getFunctionAssets().get(SERIALIZE_KEY));

                    if (event.button == 1) {
                        Point point = ChartUtils.getRealValuesFromEventPosition(chart, event.x, event.y);
                        rectifyAsset.addPoint(point);

                    } else {
                        rectifyAsset.deleteActivePoint();
                    }

                    updateYSeries(chart, "continuum", rectifyAsset.getIntepData(data.getX()));
                    updateLineSeries(chart, "points", rectifyAsset.getXCoordinatesArray(), rectifyAsset.getYCoordinatesArray());
                    updateLineSeries(chart, "selected", rectifyAsset.getActivePoint().getX(), rectifyAsset.getActivePoint().getY());

                    chart.redraw();
                    chart.forceFocus();

                }
            }
        };
    }

    private static MouseMoveListener mouseMoveListener(Chart chart, Spectrum spectrum) {
        return event -> {
            ILineSeries series = (ILineSeries) chart.getSeriesSet().getSeries("points");

            double[] xSeries = series.getXSeries();
            double[] ySeries = series.getYSeries();

            Rectangle bounds = chart.getPlotArea().getBounds();

            Range xRange = chart.getAxisSet().getXAxis(0).getRange();
            Range yRange = chart.getAxisSet().getYAxis(0).getRange();

            int index = -1;
            int closest = Integer.MAX_VALUE;

            for (int i = 0; i < xSeries.length; i++) {
                if (xSeries[i] >= xRange.lower && xSeries[i] <= xRange.upper && ySeries[i] >= yRange.lower && ySeries[i] <= yRange.upper) {
                    double x = (xSeries[i] - xRange.lower) / (xRange.upper - xRange.lower) * bounds.width;
                    double y = (1 - (ySeries[i] - yRange.lower) / (yRange.upper - yRange.lower)) * bounds.height;

                    int distance = (int) Math.sqrt(Math.pow(x - event.x, 2) + Math.pow(y - event.y, 2));

                    if (distance < closest) {
                        index = i;
                        closest = distance;
                    }
                }
            }

            RectifyAsset rectifyAsset = ((RectifyAsset) spectrum.getSpectrumFile().getFunctionAssets().get(SERIALIZE_KEY));
            if (index >= 0 && rectifyAsset.getActiveIndex() != index) {
                rectifyAsset.setActiveIndex(index);
                updateLineSeries(chart, "selected", rectifyAsset.getActivePoint().getX(), rectifyAsset.getActivePoint().getY());
                chart.redraw();
            }
        };
    }

    private static KeyListener keyListener(Spectrum spectrum) {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == SWT.CR) {
                    try {
                        spectrum.save();
                        Message.info("Rectified spectrum saved successfully");
                    } catch (SpefoException exception) {
                        Message.error("Couldn't save file", exception);
                    }
                }
            }
        };
    }

    private static void updateLineSeries(Chart chart, String name, double[] xSeries, double[] ySeries) {
        ILineSeries series = (ILineSeries) chart.getSeriesSet().getSeries(name);

        series.setXSeries(xSeries);
        series.setYSeries(ySeries);
    }

    private static void updateYSeries(Chart chart, String name, double[] ySeries) {
        ILineSeries series = (ILineSeries) chart.getSeriesSet().getSeries(name);

        series.setYSeries(ySeries);
    }
}
