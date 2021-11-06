package cz.cuni.mff.respefo.function.chiron;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.function.common.ChartKeyListener;
import cz.cuni.mff.respefo.function.common.ZoomMouseWheelListener;
import cz.cuni.mff.respefo.function.rectify.RectifyAsset;
import cz.cuni.mff.respefo.function.rectify.RectifyKeyListener;
import cz.cuni.mff.respefo.function.rectify.RectifyMouseListener;
import cz.cuni.mff.respefo.resources.ColorResource;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.widget.ChartBuilder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static cz.cuni.mff.respefo.function.rectify.RectifyFunction.*;
import static cz.cuni.mff.respefo.resources.ColorResource.GRAY;
import static cz.cuni.mff.respefo.resources.ColorResource.GREEN;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.AxisLabel.RELATIVE_FLUX;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.AxisLabel.WAVELENGTH;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.LineSeriesBuilder.lineSeries;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.ScatterSeriesBuilder.scatterSeries;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.newChart;

public class InteractiveChironController {

    private static final Map<Integer, RectifyAsset> previousAssets = new HashMap<>();

    private final XYSeries[] series;
    private final Map<Integer, RectifyAsset> assets;

    private final Iterator<Integer> indicesIterator;
    private int currentIndex;

    private Consumer<Map<Integer, RectifyAsset>> callback;

    public InteractiveChironController(XYSeries[] series, Map<Integer, RectifyAsset> assets, List<Integer> selectedIndices) {
        this.series = series;
        this.assets = assets;

        this.indicesIterator = selectedIndices.iterator();
    }

    public void rectify(Consumer<Map<Integer, RectifyAsset>> callback) {
        this.callback = callback;

        rectifyNext();
    }

    private void rectifyNext() {
        if (indicesIterator.hasNext()) {
            currentIndex = indicesIterator.next();
            ComponentManager.getDisplay().asyncExec(this::rectifySingle);

        } else {
            callback.accept(assets);
        }
    }

    private void rectifySingle() {
        XYSeries current = series[currentIndex];

        RectifyAsset asset = assets.containsKey(currentIndex)
                ? assets.get(currentIndex)
                : (previousAssets.containsKey(currentIndex)
                    ? previousAssets.get(currentIndex).adjustToNewData(current)
                    : RectifyAsset.withDefaultPoints(current));

        ChartBuilder chartBuilder = newChart()
                .title("#" + (currentIndex + 1))
                .xAxisLabel(WAVELENGTH)
                .yAxisLabel(RELATIVE_FLUX)
                .series(lineSeries()
                        .name("data")
                        .series(current)
                        .color(GREEN))
                .series(lineSeries()
                        .name(CONTINUUM_SERIES_NAME)
                        .color(ColorResource.YELLOW)
                        .xSeries(current.getXSeries())
                        .ySeries(asset.getIntepData(current.getXSeries())))
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
                        .series(asset.getActivePoint()));

        // draw neighbors
        int lowerBound = currentIndex == 0 ? 1 : Math.max(currentIndex - 2, 0);
        int upperBound = currentIndex == series.length - 1 ? series.length - 2 : Math.min(currentIndex + 2, series.length - 1);
        for (int i = lowerBound; i <= upperBound; i++) {
            if (i == currentIndex) {
                continue;
            }

            chartBuilder.series(lineSeries()
                    .series(series[i])
                    .color(GRAY));
        }

        chartBuilder
                .keyListener(ChartKeyListener::makeAllSeriesEqualRange)
                .keyListener(ch -> new RectifyKeyListener(ch, asset,
                        () -> updateAllSeries(ch, asset, current),
                        newIndex -> updateActivePoint(ch, asset, newIndex),
                        () -> {
                            assets.put(currentIndex, asset);
                            previousAssets.put(currentIndex, asset);
                            rectifyNext();
                        }))
                .mouseAndMouseMoveListener(ch -> new RectifyMouseListener(ch,
                        POINTS_SERIES_NAME,
                        newIndex -> {
                            if (asset.getActiveIndex() != newIndex) {
                                updateActivePoint(ch, asset, newIndex);
                            }
                        },
                        point -> {
                            asset.moveActivePoint(point.getX(), point.getY());
                            updateAllSeries(ch, asset, current);
                        },
                        point -> {
                            asset.addPoint(point);
                            updateAllSeries(ch, asset, current);
                        },
                        () -> {
                            asset.deleteActivePoint();
                            updateAllSeries(ch, asset, current);
                        }))
                .mouseWheelListener(ZoomMouseWheelListener::new)
                .makeAllSeriesEqualRange()
                .forceFocus()
                .build(ComponentManager.clearAndGetScene());
    }
}
