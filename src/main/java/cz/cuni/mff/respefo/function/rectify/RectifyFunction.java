package cz.cuni.mff.respefo.function.rectify;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.component.SpectrumExplorer;
import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.Serialize;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.common.ChartKeyListener;
import cz.cuni.mff.respefo.function.common.ZoomMouseWheelListener;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.function.open.OpenFunction;
import cz.cuni.mff.respefo.resources.ColorResource;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.spectrum.format.EchelleSpectrum;
import cz.cuni.mff.respefo.spectrum.format.SimpleSpectrum;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.widget.ChartBuilder;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static cz.cuni.mff.respefo.resources.ColorResource.GRAY;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.AxisLabel.RELATIVE_FLUX;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.AxisLabel.WAVELENGTH;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.LineSeriesBuilder.lineSeries;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.ScatterSeriesBuilder.scatterSeries;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.newChart;

@Fun(name = "Rectify", fileFilter = SpefoFormatFileFilter.class, group = "Preprocessing")
@Serialize(key = RectifyFunction.SERIALIZE_KEY, assetClass = RectifyAsset.class)
public class RectifyFunction implements SingleFileFunction {

    public static final String SERIALIZE_KEY = "rectify";
    public static final String POINTS_SERIES_NAME = "points";
    public static final String SELECTED_SERIES_NAME = "selected";
    public static final String CONTINUUM_SERIES_NAME = "continuum";

    private static RectifyAsset previousAsset;
    private static final Map<Integer, RectifyAsset> previousAssets = new HashMap<>();

    @Override
    public void execute(File file) {
        Spectrum spectrum;
        try {
            spectrum = Spectrum.open(file);
        } catch (SpefoException e) {
            Message.error("Couldn't open file", e);
            return;
        }

        switch (spectrum.getFormat()) {
            case SimpleSpectrum.FORMAT:
                rectifySimpleSpectrum((SimpleSpectrum) spectrum);
                break;
            case EchelleSpectrum.FORMAT:
                rectifyEchelleSpectrum((EchelleSpectrum) spectrum);
                break;
            default:
                Message.warning("Spectrum has an unexpected file format");
        }
    }

    private static void rectifySimpleSpectrum(SimpleSpectrum spectrum) {
        RectifyAsset asset = spectrum.getFunctionAsset(SERIALIZE_KEY, RectifyAsset.class)
                .orElse(previousAsset != null
                        ? previousAsset.adjustToNewData(spectrum.getProcessedSeries())
                        : RectifyAsset.withDefaultPoints(spectrum.getProcessedSeries()));

        XYSeries series = spectrum.getProcessedSeriesWithout(asset);

        rectify(spectrum.getFile().getName(), series, asset, UnaryOperator.identity(), a -> finishSimpleSpectrum(spectrum, a));
    }

    private static void rectifyEchelleSpectrum(EchelleSpectrum spectrum) {
        XYSeries[] series = spectrum.getOriginalSeries();
        Map<Integer, RectifyAsset> currentAssets = spectrum.getRectifyAssets();

        String[][] names = new String[series.length][3];
        for (int i = 0; i <= series.length - 1; i++) {
            XYSeries xySeries = series[i];
            names[i] = new String[]{
                    Integer.toString(i + 1),
                    Double.toString(xySeries.getX(0)),
                    Double.toString(xySeries.getLastX())
            };
        }

        boolean[] selected = new boolean[series.length];
        for (int i = 0; i < series.length; i++) {
            selected[i] = currentAssets.containsKey(i);
        }

        EchelleSelectionDialog dialog = new EchelleSelectionDialog(names, selected);
        if (dialog.openIsNotOk()) {
            return;
        }

        List<Integer> selectedIndices = dialog.getSelectedIndices();

        SortedMap<Integer, RectifyAsset> filteredAssets = currentAssets.entrySet()
                .stream()
                .filter(entry -> selectedIndices.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, TreeMap::new));

        ComponentManager.getDisplay().asyncExec(() -> rectifySingleEchelle(spectrum, selectedIndices.iterator(), series, filteredAssets));
    }

    private static void rectifySingleEchelle(EchelleSpectrum spectrum, Iterator<Integer> indicesIterator,
                                             XYSeries[] series, SortedMap<Integer, RectifyAsset> assets) {
        if (!indicesIterator.hasNext()) {
            finishEchelleSpectrum(spectrum, assets);
            return;
        }

        int currentIndex = indicesIterator.next();
        XYSeries currentSeries = series[currentIndex];
        RectifyAsset currentAsset = assets.containsKey(currentIndex)
                ? assets.get(currentIndex)
                : (previousAssets.containsKey(currentIndex)
                    ? previousAssets.get(currentIndex).adjustToNewData(currentSeries)
                    : RectifyAsset.withDefaultPoints(currentSeries));

        rectify("#" + (currentIndex + 1), currentSeries, currentAsset,
                builder -> {
                    int lowerBound = currentIndex == 0 ? 1 : Math.max(currentIndex - 2, 0);
                    int upperBound = currentIndex == series.length - 1 ? series.length - 2 : Math.min(currentIndex + 2, series.length - 1);
                    for (int i = lowerBound; i <= upperBound; i++) {
                        if (i == currentIndex) {
                            continue;
                        }

                        builder.series(lineSeries()
                                .series(series[i])
                                .color(GRAY));
                    }
                    return builder;
                },
                asset -> {
                    assets.put(currentIndex, asset);
                    previousAssets.put(currentIndex, asset);

                    if (indicesIterator.hasNext()) {
                        ComponentManager.getDisplay().asyncExec(() -> rectifySingleEchelle(spectrum, indicesIterator, series, assets));
                    } else {
                        finishEchelleSpectrum(spectrum, assets);
                    }
                });
    }

    private static void rectify(String title, XYSeries series, RectifyAsset asset,
                                                     UnaryOperator<ChartBuilder> extraSeries,
                                                     Consumer<RectifyAsset> finish) {
        newChart()
                .title(title)
                .xAxisLabel(WAVELENGTH)
                .yAxisLabel(RELATIVE_FLUX)
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
                .apply(extraSeries)
                .keyListener(ChartKeyListener::makeAllSeriesEqualRange)
                .keyListener(ch -> new RectifyKeyListener(ch, asset,
                        () -> updateAllSeries(ch, asset, series),
                        newIndex -> updateActivePoint(ch, asset, newIndex),
                        () -> finish.accept(asset)))
                .mouseAndMouseMoveListener(ch -> new RectifyMouseListener(ch,
                        POINTS_SERIES_NAME,
                        index -> {
                            if (asset.getActiveIndex() != index) {
                                updateActivePoint(ch, asset, index);
                            }
                        },
                        point -> {
                            asset.moveActivePoint(point.getX(), point.getY());
                            updateAllSeries(ch, asset, series);
                        },
                        point -> {
                            asset.addPoint(point);
                            updateAllSeries(ch, asset, series);
                        },
                        () -> {
                            asset.deleteActivePoint();
                            updateAllSeries(ch, asset, series);
                        }))
                .mouseWheelListener(ZoomMouseWheelListener::new)
                .makeAllSeriesEqualRange()
                .forceFocus()
                .build(ComponentManager.clearAndGetScene());
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

    private static void finishSimpleSpectrum(SimpleSpectrum spectrum, RectifyAsset asset) {
        if (asset.isEmpty()) { // This can never occur
            spectrum.removeFunctionAsset(SERIALIZE_KEY);
        } else {
            spectrum.putFunctionAsset(SERIALIZE_KEY, asset);
            previousAsset = asset;
        }

        try {
            spectrum.save();
            SpectrumExplorer.getDefault().refresh();
            OpenFunction.displaySpectrum(spectrum);
            Message.info("Rectified spectrum saved successfully.");

        } catch (SpefoException exception) {
            Message.error("Spectrum file couldn't be saved.", exception);
        }
    }

    private static void finishEchelleSpectrum(EchelleSpectrum spectrum, SortedMap<Integer, RectifyAsset> assets) {
        spectrum.setRectifyAssets(assets);

        try {
            spectrum.save();
            SpectrumExplorer.getDefault().refresh();
            OpenFunction.displaySpectrum(spectrum);
            Message.info("Rectified spectrum saved successfully.");

        } catch (SpefoException exception) {
            Message.error("Spectrum file couldn't be saved.", exception);
        }
    }
}
