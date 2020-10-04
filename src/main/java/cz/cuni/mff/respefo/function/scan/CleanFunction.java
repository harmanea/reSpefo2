package cz.cuni.mff.respefo.function.scan;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.format.Spectrum;
import cz.cuni.mff.respefo.format.XYSeries;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.Serialize;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.asset.clean.CleanAsset;
import cz.cuni.mff.respefo.function.asset.common.ChartKeyListener;
import cz.cuni.mff.respefo.function.asset.common.DragMouseListener;
import cz.cuni.mff.respefo.function.asset.common.NearestPointMouseMoveListener;
import cz.cuni.mff.respefo.function.asset.common.ZoomMouseWheelListener;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.resources.ColorManager;
import cz.cuni.mff.respefo.resources.ColorResource;
import cz.cuni.mff.respefo.util.Message;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;

import java.io.File;

import static cz.cuni.mff.respefo.util.builders.ChartBuilder.ScatterSeriesBuilder.scatterSeries;
import static cz.cuni.mff.respefo.util.builders.ChartBuilder.chart;

@Fun(name = "Clean", fileFilter = SpefoFormatFileFilter.class, group = "Preprocessing")
@Serialize(key = CleanFunction.SERIALIZE_KEY, assetClass = CleanAsset.class)
public class CleanFunction implements SingleFileFunction {

    public static final String SERIALIZE_KEY = "clean";
    public static final String POINTS_SERIES_NAME = "points";
    public static final String SELECTED_SERIES_NAME = "selected";
    public static final String DELETED_SERIES_NAME = "deleted";

    @Override
    public void execute(File file) {
        Spectrum spectrum;
        try {
            spectrum = Spectrum.open(file);
        } catch (SpefoException e) {
            Message.error("Couldn't open file", e);
            return;
        }

        CleanAsset asset = (CleanAsset) spectrum.getFunctionAssets().getOrDefault(SERIALIZE_KEY, new CleanAsset());

        XYSeries data = spectrum.getProcessedSeriesWithout(asset);

        Chart chart = chart(ComponentManager.clearAndGetScene())
                .title(file.getName())
                .xAxisLabel("wavelength (Å)")
                .yAxisLabel("relative flux I(λ)")
                .series(scatterSeries()
                        .name(DELETED_SERIES_NAME)
                        .color(ColorResource.GRAY)
                        .symbolSize(2)
                        .series(asset.mapDeletedIndexesToValues(data)))
                .series(scatterSeries()
                        .name(POINTS_SERIES_NAME)
                        .color(ColorResource.GREEN)
                        .symbolSize(2)
                        .series(asset.process(data)))
                .series(scatterSeries()
                        .name(SELECTED_SERIES_NAME)
                        .color(asset.isActiveIndexDeleted() ? ColorResource.ORANGE : ColorResource.RED)
                        .symbolSize(3)
                        .series(asset.mapActiveIndexToValues(data)))
                .keyListener(ChartKeyListener::makeAllSeriesEqualRange)
                .mouseAndMouseMoveListener(DragMouseListener::new)
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
                    case SWT.DEL: {
                        asset.addActiveIndex();
                        updateSeries(chart, data, asset);
                        updateActivePoint(chart, asset, asset.getActiveIndex() + (asset.getActiveIndex() < data.getXSeries().length ? 1 : 0));
                        break;
                    }
                    case SWT.INSERT: {
                        asset.removeActiveIndex();
                        updateSeries(chart, data, asset);
                        updateActivePoint(chart, asset, asset.getActiveIndex() + (asset.getActiveIndex() < data.getXSeries().length ? 1 : 0));
                        break;
                    }
                    case 'n': {
                        if (asset.getActiveIndex() > 0) {
                            updateActivePoint(chart, asset, asset.getActiveIndex() - 1);
                        }
                        break;
                    }
                    case 'm': {
                        if (asset.getActiveIndex() < data.getXSeries().length - 1) {
                            updateActivePoint(chart, asset, asset.getActiveIndex() + 1);
                        }
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

                            Message.info("Cleaned spectrum saved successfully.");
                            OpenFunction.displaySpectrum(spectrum);

                        } catch (Exception exception) {
                            Message.error("Spectrum file couldn't be saved.", exception);
                        }
                        break;
                    }
                }
            }
        });
    }

    private static void updateActivePoint(Chart chart, CleanAsset asset, int newIndex) {
        asset.setActiveIndex(newIndex);

        ILineSeries series = (ILineSeries) chart.getSeriesSet().getSeries(SELECTED_SERIES_NAME);
        series.setSymbolColor(ColorManager.getColor(asset.isActiveIndexDeleted() ? ColorResource.ORANGE : ColorResource.RED));
        ILineSeries pointSeries = (ILineSeries) chart.getSeriesSet().getSeries(POINTS_SERIES_NAME);
        series.setXSeries(new double[]{pointSeries.getXSeries()[newIndex]});
        series.setYSeries(new double[]{pointSeries.getYSeries()[newIndex]});

        chart.redraw();
    }

    private static void updateSeries(Chart chart, XYSeries data, CleanAsset asset) {
        XYSeries deletedSeriesData = asset.mapDeletedIndexesToValues(data);

        ILineSeries series = (ILineSeries) chart.getSeriesSet().getSeries(DELETED_SERIES_NAME);
        series.setXSeries(deletedSeriesData.getXSeries());
        series.setYSeries(deletedSeriesData.getYSeries());

        XYSeries pointsSeriesData = asset.process(data);

        series = (ILineSeries) chart.getSeriesSet().getSeries(POINTS_SERIES_NAME);
        series.setXSeries(pointsSeriesData.getXSeries());
        series.setYSeries(pointsSeriesData.getYSeries());

        chart.redraw();
    }
}
