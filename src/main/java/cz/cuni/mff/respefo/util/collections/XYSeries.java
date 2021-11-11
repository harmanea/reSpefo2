package cz.cuni.mff.respefo.util.collections;

import java.util.Objects;
import java.util.PriorityQueue;

/**
 * A collection for storing a 2-D sequence of real numbers. The underlying arrays cannot be null and are guaranteed
 * to have the same size. It is intended to store spectrum data points.
 */
public class XYSeries {
    private double[] xSeries;
    private double[] ySeries;

    public XYSeries() {
        this(new double[0], new double[0]);
    }

    public XYSeries(double[] xSeries, double[] ySeries) {
        Objects.requireNonNull(xSeries);
        Objects.requireNonNull(ySeries);

        if (xSeries.length != ySeries.length) {
            throw new IllegalArgumentException("Both series must have equal length");
        }

        this.xSeries = xSeries;
        this.ySeries = ySeries;
    }

    public int getLength() {
        return xSeries.length;
    }

    public double[] getXSeries() {
        return xSeries;
    }

    public void updateXSeries(double[] xSeries) {
        Objects.requireNonNull(xSeries);

        if (xSeries.length != this.xSeries.length) {
            throw new IllegalArgumentException("New series must have the same length");
        }

        this.xSeries = xSeries;
    }

    public double[] getYSeries() {
        return ySeries;
    }

    public void updateYSeries(double[] ySeries) {
        Objects.requireNonNull(ySeries);

        if (ySeries.length != this.ySeries.length) {
            throw new IllegalArgumentException("New series must have the same length");
        }

        this.ySeries = ySeries;
    }

    public double getX(int index) {
        if (index < 0 || index > xSeries.length - 1) {
            throw new IndexOutOfBoundsException("Trying to access xSeries at index " + index + " but length is " + xSeries.length);
        }

        return xSeries[index];
    }

    public double getLastX() {
        if (xSeries.length == 0) {
            throw new IndexOutOfBoundsException("xSeries are empty");
        }

        return xSeries[xSeries.length - 1];
    }

    public double getY(int index) {
        if (index < 0 || index > ySeries.length - 1) {
            throw new IndexOutOfBoundsException("Trying to access ySeries at index " + index + " but length is " + ySeries.length);
        }

        return ySeries[index];
    }

    public double getLastY() {
        if (ySeries.length == 0) {
            throw new IndexOutOfBoundsException("ySeries are empty");
        }

        return ySeries[ySeries.length - 1];
    }

    /**
     * K-way merge of x-sorted XYSeries
     * @param xySeries any number of x-sorted series
     * @return a new x-sorted series containing all the values from the original collections
     */
    public static XYSeries merge(XYSeries ... xySeries) {
        PriorityQueue<Container> heap = new PriorityQueue<>(xySeries.length);

        int n = 0;
        for (XYSeries series : xySeries) {
            heap.add(new Container(series));
            n += series.getLength();
        }

        double[] xSeries = new double[n];
        double[] ySeries = new double[n];

        int i = 0;
        while (!heap.isEmpty()) {
            Container container = heap.poll();
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

    private static class Container implements Comparable<Container> {
        private final XYSeries series;
        private int index;

        Container(XYSeries series) {
            this.series = Objects.requireNonNull(series);
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
        public int compareTo(Container other) {
            return Double.compare(getX(), other.getX());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Container container = (Container) o;

            if (index != container.index) return false;
            return series.equals(container.series);
        }

        @Override
        public int hashCode() {
            int result = series.hashCode();
            result = 31 * result + index;
            return result;
        }
    }
}
