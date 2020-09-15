package cz.cuni.mff.respefo.format;

public class XYSeries {
    private double[] xSeries;
    private double[] ySeries;

    public XYSeries() {
        // default empty constructor
    }

    public XYSeries(double[] xSeries, double[] ySeries) {
        this.xSeries = xSeries;
        this.ySeries = ySeries;
    }

    public double[] getXSeries() {
        return xSeries;
    }

    public void setXSeries(double[] xSeries) {
        this.xSeries = xSeries;
    }

    public double[] getYSeries() {
        return ySeries;
    }

    public void setYSeries(double[] ySeries) {
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
