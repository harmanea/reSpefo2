package cz.cuni.mff.respefo.function.asset.clean;

import cz.cuni.mff.respefo.format.XYSeries;
import cz.cuni.mff.respefo.format.asset.FunctionAsset;
import cz.cuni.mff.respefo.util.utils.MathUtils;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.IntStream;

public class CleanAsset implements FunctionAsset {
    private final SortedSet<Integer> deletedIndexes;
    private transient int activeIndex = 0;

    public CleanAsset() {
        deletedIndexes = new TreeSet<>();
    }

    public int getActiveIndex() {
        return activeIndex;
    }

    public void setActiveIndex(int value) {
        activeIndex = value;
    }

    public void addActiveIndex() {
        deletedIndexes.add(activeIndex);
    }

    public void removeActiveIndex() {
        deletedIndexes.remove(activeIndex);
    }

    public boolean isActiveIndexDeleted() {
        return deletedIndexes.contains(activeIndex);
    }

    public boolean isEmpty() {
        return deletedIndexes.isEmpty();
    }

    public XYSeries mapDeletedIndexesToValues(XYSeries data) {
        double[] xSeries = deletedIndexes.stream().mapToDouble(data::getX).toArray();
        double[] ySeries = deletedIndexes.stream().mapToDouble(data::getY).toArray();

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
                .filter(index -> !deletedIndexes.contains(index))
                .mapToDouble(series::getX)
                .toArray();

        double[] remainingYSeries = IntStream.range(0, series.getLength())
                .filter(index -> !deletedIndexes.contains(index))
                .mapToDouble(series::getY)
                .toArray();

        double[] newYSeries = MathUtils.intep(remainingXSeries, remainingYSeries, series.getXSeries());

        return new XYSeries(series.getXSeries(), newYSeries);
    }
}
