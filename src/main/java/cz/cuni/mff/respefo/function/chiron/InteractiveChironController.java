package cz.cuni.mff.respefo.function.chiron;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.function.common.ChartKeyListener;
import cz.cuni.mff.respefo.function.common.ZoomMouseWheelListener;
import cz.cuni.mff.respefo.function.rectify.RectifyAsset;
import cz.cuni.mff.respefo.function.rectify.RectifyKeyListener;
import cz.cuni.mff.respefo.function.rectify.RectifyMouseListener;
import cz.cuni.mff.respefo.resources.ColorResource;
import cz.cuni.mff.respefo.util.collections.DoubleArrayList;
import cz.cuni.mff.respefo.util.collections.Point;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;
import cz.cuni.mff.respefo.util.widget.ChartBuilder;

import java.util.*;
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

    private final float[][][] data;
    private final boolean[] selected;

    private List<XYSeries> rectifiedData;
    private Consumer<XYSeries> callback;

    private int currentIndex;

    public InteractiveChironController(float[][][] data, boolean[] selected) {
        this.data = data;
        this.selected = selected;
    }

    public void rectify(Consumer<XYSeries> callback) {
        rectifiedData = new ArrayList<>();
        this.callback = callback;

        currentIndex = 0;
        rectifySingle();
    }

    private void rectifySingle() {
        if (currentIndex >= data.length) {
            finish();
            return;
        } else if (!selected[currentIndex]) {
            currentIndex++;
            ComponentManager.getDisplay().asyncExec(this::rectifySingle);
            return;
        }

        XYSeries current = dataToSeries(currentIndex);
        List<XYSeries> neighbours = dataToSeries(currentIndex -2, currentIndex -1, currentIndex +1, currentIndex +2);

        RectifyAsset asset = previousAssets.containsKey(currentIndex)
                ? previousAssets.get(currentIndex).adjustToNewData(current)
                : RectifyAsset.withDefaultPoints(current);

        int middle = current.getLength() / 2;
        asset.addPoint(current.getX(middle), current.getY(middle));

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

        for (XYSeries series : neighbours) {
            chartBuilder.series(lineSeries()
                    .series(series)
                    .color(GRAY));
        }

        chartBuilder
                .keyListener(ChartKeyListener::makeAllSeriesEqualRange)
                .keyListener(ch -> new RectifyKeyListener(ch, asset,
                        () -> updateAllSeries(ch, asset, current),
                        newIndex -> updateActivePoint(ch, asset, newIndex),
                        () -> {
                            double[] newYSeries = ArrayUtils.divideArrayValues(current.getYSeries(), asset.getIntepData(current.getXSeries()));
                            rectifiedData.add(new XYSeries(current.getXSeries(), newYSeries));
                            previousAssets.put(currentIndex, asset);

                            currentIndex++;
                            ComponentManager.getDisplay().asyncExec(this::rectifySingle);
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

    private List<XYSeries> dataToSeries(int ... indices) {
        List<XYSeries> xySeries = new ArrayList<>(indices.length);

        for (int index : indices) {
            if (index < 0 || index >= data.length) {
                continue;
            }

            xySeries.add(dataToSeries(index));
        }

        return xySeries;
    }

    private XYSeries dataToSeries(int index) {
        DoubleArrayList xList = new DoubleArrayList(data[0].length);
        DoubleArrayList yList = new DoubleArrayList(data[0].length);

        float[][] matrix = data[index];
        for (float[] row : matrix) {
            xList.add(row[0]);
            yList.add(row[1]);
        }

        return new XYSeries(xList.toArray(), yList.toArray());
    }

    private void finish() {
        SortedSet<Point> points = new TreeSet<>();
        for (XYSeries series : rectifiedData) {
            for (int i = 0; i < series.getLength(); i++) {
                points.add(new Point(series.getX(i), series.getY(i)));
            }
        }

        double[] xSeries = new double[points.size()];
        double[] ySeries = new double[points.size()];

        int i = 0;
        for (Point point : points) {
            xSeries[i] = point.getX();
            ySeries[i] = point.getY();
            i++;
        }

        callback.accept(new XYSeries(xSeries, ySeries));
    }
}
