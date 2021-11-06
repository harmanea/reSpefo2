package cz.cuni.mff.respefo.spectrum.format;

import cz.cuni.mff.respefo.function.rectify.RectifyAsset;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PriorityQueue;

import static cz.cuni.mff.respefo.util.Constants.SPEED_OF_LIGHT;
import static java.lang.Double.isNaN;

public class EchelleSpectrum extends Spectrum {
    public static final int FORMAT = 2;

    private XYSeries[] series;

    private Map<Integer, RectifyAsset> rectifyAssets;

    private EchelleSpectrum() {
        super();  // default empty constructor
    }

    public EchelleSpectrum(XYSeries[] series) {
        super(FORMAT);

        this.series = series;
        rectifyAssets = new LinkedHashMap<>();
    }

    public XYSeries[] getOriginalSeries() {
        return series;
    }

    public Map<Integer, RectifyAsset> getRectifyAssets() {
        return rectifyAssets;
    }

    public void setRectifyAssets(Map<Integer, RectifyAsset> rectifyAssets) {
        this.rectifyAssets = rectifyAssets;
    }

    @Override
    public void updateRvCorrection(double newRvCorrection) {
        double diff = newRvCorrection - (isNaN(rvCorrection) ? 0 : rvCorrection);

        for (XYSeries xySeries : series) {
            double[] updatedXSeries = Arrays.stream(xySeries.getXSeries())
                    .map(value -> value + diff * (value / SPEED_OF_LIGHT))
                    .toArray();
            xySeries.updateXSeries(updatedXSeries);
        }

        setRvCorrection(newRvCorrection);
    }

    @Override
    public XYSeries getSeries() {
        // k-way mergesort, could be cached if necessary

        double[] xSeries;
        double[] ySeries;

        int i = 0;

        PriorityQueue<XYSeriesContainer> heap = new PriorityQueue<>(series.length);
        if (rectifyAssets.isEmpty()) {
            for (XYSeries xySeries : series) {
                heap.add(new XYSeriesContainer(xySeries));
            }

            xSeries = new double[series.length * series[0].getLength()];
            ySeries = new double[series.length * series[0].getLength()];

        } else {
            for (Map.Entry<Integer, RectifyAsset> entry : rectifyAssets.entrySet()) {
                XYSeries xySeries = series[entry.getKey()];

                double[] rectifiedSeries = ArrayUtils.divideArrayValues(xySeries.getYSeries(),
                        entry.getValue().getIntepData(xySeries.getXSeries()));

                heap.add(new XYSeriesContainer(new XYSeries(xySeries.getXSeries(), rectifiedSeries)));
            }

            xSeries = new double[rectifyAssets.size() * series[0].getLength()];
            ySeries = new double[rectifyAssets.size() * series[0].getLength()];
        }

        while (!heap.isEmpty()) {
            XYSeriesContainer container = heap.poll();
            xSeries[i] = container.getX();
            ySeries[i] = container.getY();
            i++;

            if (container.isNotEmpty()) {
                container.increment();
                heap.add(container);
            }
        }

        return new XYSeries(xSeries, ySeries);
    }

    private static class XYSeriesContainer implements Comparable<XYSeriesContainer> {
        private final XYSeries series;
        private int index;

        XYSeriesContainer(XYSeries series) {
            this.series = series;
            index = 0;
        }

        double getX() {
            return series.getX(index);
        }

        double getY() {
            return series.getY(index);
        }

        void increment() {
            index++;
        }

        boolean isNotEmpty() {
            return index < series.getLength() - 1;
        }

        @Override
        public int compareTo(XYSeriesContainer other) {
            return Double.compare(getX(), other.getX());
        }
    }
}
