package cz.cuni.mff.respefo.util.collections;

import java.util.Objects;

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
}
