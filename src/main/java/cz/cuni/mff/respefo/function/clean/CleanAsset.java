package cz.cuni.mff.respefo.function.clean;

import cz.cuni.mff.respefo.spectrum.asset.FunctionAsset;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.utils.MathUtils;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.IntStream;

public class CleanAsset implements FunctionAsset {
    private final SortedSet<Integer> deletedIndices;
    private transient int activeIndex = 0;

    public CleanAsset() {
        deletedIndices = new TreeSet<>();
    }

    public int getActiveIndex() {
        return activeIndex;
    }

    public void setActiveIndex(int value) {
        activeIndex = value;
    }

    public void addActiveIndex() {
        deletedIndices.add(activeIndex);
    }

    public void removeActiveIndex() {
        deletedIndices.remove(activeIndex);
    }

    public boolean isActiveIndexDeleted() {
        return deletedIndices.contains(activeIndex);
    }

    public boolean isEmpty() {
        return deletedIndices.isEmpty();
    }

    public XYSeries mapDeletedIndicesToValues(XYSeries data) {
        double[] xSeries = deletedIndices.stream().mapToDouble(data::getX).toArray();
        double[] ySeries = deletedIndices.stream().mapToDouble(data::getY).toArray();

        return new XYSeries(xSeries, ySeries);
    }

    public XYSeries mapActiveIndexToValues(XYSeries data) {
        return new XYSeries(
                new double[]{data.getX(activeIndex)},
                new double[]{data.getY(activeIndex)}
        );
    }

    @Override
    public XYSeries process(XYSeries series) {
        if (isEmpty()) {
            return series;
        }

        double[] remainingXSeries = IntStream.range(0, series.getLength())
                .filter(index -> !deletedIndices.contains(index))
                .mapToDouble(series::getX)
                .toArray();

        double[] remainingYSeries = IntStream.range(0, series.getLength())
                .filter(index -> !deletedIndices.contains(index))
                .mapToDouble(series::getY)
                .toArray();

        double[] newYSeries = MathUtils.intep(remainingXSeries, remainingYSeries, series.getXSeries());

        return new XYSeries(series.getXSeries(), newYSeries);
    }
}
