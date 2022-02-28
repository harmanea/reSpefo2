package cz.cuni.mff.respefo.function.clean;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.Serialize;
import cz.cuni.mff.respefo.function.SpectrumFunction;
import cz.cuni.mff.respefo.function.common.ChartKeyListener;
import cz.cuni.mff.respefo.function.common.DragMouseListener;
import cz.cuni.mff.respefo.function.common.NearestPointMouseMoveListener;
import cz.cuni.mff.respefo.function.common.ZoomMouseWheelListener;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.function.open.OpenFunction;
import cz.cuni.mff.respefo.resources.ColorManager;
import cz.cuni.mff.respefo.resources.ColorResource;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;

import static cz.cuni.mff.respefo.util.widget.ChartBuilder.AxisLabel.RELATIVE_FLUX;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.AxisLabel.WAVELENGTH;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.ScatterSeriesBuilder.scatterSeries;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.newChart;

@Fun(name = "Clean", fileFilter = SpefoFormatFileFilter.class, group = "Preprocessing")
@Serialize(key = CleanFunction.SERIALIZE_KEY, assetClass = CleanAsset.class)
public class CleanFunction extends SpectrumFunction {

    public static final String SERIALIZE_KEY = "clean";
    public static final String POINTS_SERIES_NAME = "points";
    public static final String SELECTED_SERIES_NAME = "selected";
    public static final String DELETED_SERIES_NAME = "deleted";

    @Override
    public void execute(Spectrum spectrum) {
        CleanAsset asset = spectrum.getFunctionAsset(SERIALIZE_KEY, CleanAsset.class).orElse(new CleanAsset());

        XYSeries data = spectrum.getProcessedSeriesWithout(asset);

        newChart()
                .title(spectrum.getFile().getName())
                .xAxisLabel(WAVELENGTH)
                .yAxisLabel(RELATIVE_FLUX)
                .series(scatterSeries()
                        .name(DELETED_SERIES_NAME)
                        .color(ColorResource.GRAY)
                        .symbolSize(2)
                        .series(asset.mapDeletedIndicesToValues(data)))
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
                .keyListener(ch -> KeyListener.keyPressedAdapter(e -> {
                    switch (e.keyCode) {
                        case SWT.DEL:
                            asset.addActiveIndex();
                            updateSeries(ch, data, asset);
                            updateActivePoint(ch, asset, asset.getActiveIndex() + (asset.getActiveIndex() < data.getLength() ? 1 : 0));
                            break;
                        case SWT.INSERT:
                            asset.removeActiveIndex();
                            updateSeries(ch, data, asset);
                            updateActivePoint(ch, asset, asset.getActiveIndex() + (asset.getActiveIndex() < data.getLength() ? 1 : 0));
                            break;
                        case 'n':
                            if (asset.getActiveIndex() > 0) {
                                updateActivePoint(ch, asset, asset.getActiveIndex() - 1);
                            }
                            break;
                        case 'm':
                            if (asset.getActiveIndex() < data.getLength() - 1) {
                                updateActivePoint(ch, asset, asset.getActiveIndex() + 1);
                            }
                            break;
                        case SWT.CR:
                            if (asset.isEmpty()) {
                                spectrum.removeFunctionAsset(SERIALIZE_KEY);
                            } else {
                                spectrum.putFunctionAsset(SERIALIZE_KEY, asset);
                            }

                            try {
                                spectrum.save();
                                OpenFunction.displaySpectrum(spectrum);
                                Message.info("Cleaned spectrum saved successfully.");

                            } catch (Exception exception) {
                                Message.error("Spectrum file couldn't be saved.", exception);
                            }
                            break;
                    }
                }))
                .mouseAndMouseMoveListener(DragMouseListener::new)
                .mouseMoveListener(ch -> new NearestPointMouseMoveListener(ch, POINTS_SERIES_NAME, index -> {
                    if (asset.getActiveIndex() != index) {
                        updateActivePoint(ch, asset, index);
                    }
                }))
                .mouseWheelListener(ZoomMouseWheelListener::new)
                .makeAllSeriesEqualRange()
                .forceFocus()
                .build(ComponentManager.clearAndGetScene());
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
        XYSeries deletedSeriesData = asset.mapDeletedIndicesToValues(data);

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
