package cz.cuni.mff.respefo.format;

import cz.cuni.mff.respefo.util.collections.XYSeries;

import java.util.Arrays;
import java.util.PriorityQueue;

import static cz.cuni.mff.respefo.util.Constants.SPEED_OF_LIGHT;
import static java.lang.Double.isNaN;

public class EchelleSpectrum extends Spectrum {
    protected static final int FORMAT = 2;

    private XYSeries[] series;

    private EchelleSpectrum() {
        super();  // default empty constructor
    }

    public EchelleSpectrum(XYSeries[] series) {
        super(FORMAT);

        this.series = series;
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
        double[] xSeries = new double[series.length * series[0].getLength()];
        double[] ySeries = new double[series.length * series[0].getLength()];

        int i = 0;

        PriorityQueue<XYSeriesContainer> heap = new PriorityQueue<>(series.length);
        for (XYSeries xySeries : series) {
            heap.add(new XYSeriesContainer(xySeries));
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
        XYSeries series;
        int index;

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
