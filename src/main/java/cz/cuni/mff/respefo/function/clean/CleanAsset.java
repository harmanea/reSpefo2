package cz.cuni.mff.respefo.function.clean;

import cz.cuni.mff.respefo.spectrum.asset.FunctionAsset;
import cz.cuni.mff.respefo.util.collections.DoubleArrayList;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.utils.MathUtils;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

public class CleanAsset implements FunctionAsset, Iterable<Integer> {
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

    public void clear() {
        deletedIndices.clear();
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


        int n = series.getLength();

        DoubleArrayList remainingX = new DoubleArrayList(n - deletedIndices.size());
        DoubleArrayList remainingY = new DoubleArrayList(n - deletedIndices.size());
        DoubleArrayList deletedX = new DoubleArrayList(deletedIndices.size());

        Iterator<Integer> deletedIterator = deletedIndices.iterator();
        int nextDeletedIndex = deletedIterator.next();
        for (int i = 0; i < n; i++) {
            if (i < nextDeletedIndex) {
                remainingX.add(series.getX(i));
                remainingY.add(series.getY(i));
            } else {
                deletedX.add(series.getX(i));
                nextDeletedIndex = deletedIterator.hasNext() ? deletedIterator.next() : n;
            }
        }

        double[] deletedYSeries = MathUtils.intep(remainingX.elements(), remainingY.elements(), deletedX.elements());


        double[] newYSeries = new double[n];

        deletedIterator = deletedIndices.iterator();
        nextDeletedIndex = deletedIterator.next();

        int deletedIndex = 0;

        for (int i = 0; i < n; i++) {
            if (i < nextDeletedIndex) {
                newYSeries[i] = series.getY(i);
            } else {
                newYSeries[i] = deletedYSeries[deletedIndex++];
                nextDeletedIndex = deletedIterator.hasNext() ? deletedIterator.next() : n;
            }
        }

        return new XYSeries(series.getXSeries(), newYSeries);
    }

    @Override
    public Iterator<Integer> iterator() {
        return deletedIndices.iterator();
    }
}
