package cz.cuni.mff.respefo.function.asset.rectify;

import cz.cuni.mff.respefo.format.XYSeries;
import cz.cuni.mff.respefo.format.asset.FunctionAsset;
import cz.cuni.mff.respefo.util.DoubleArrayList;
import cz.cuni.mff.respefo.util.Point;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;
import cz.cuni.mff.respefo.util.utils.MathUtils;

public class RectifyAsset implements FunctionAsset {
    private DoubleArrayList xCoordinates;
    private DoubleArrayList yCoordinates;
    private transient int activeIndex = 0;

    private RectifyAsset() {
        // default empty constructor
    }

    public RectifyAsset(DoubleArrayList xCoordinates, DoubleArrayList yCoordinates) {
        this.xCoordinates = xCoordinates;
        this.yCoordinates = yCoordinates;
    }

    @Override
    public XYSeries process(XYSeries series) {
        double[] continuum = getIntepData(series.getXSeries());
        double[] newYSeries = ArrayUtils.divideArrayValues(series.getYSeries(), continuum);

        return new XYSeries(series.getXSeries(), newYSeries);
    }

    public int getActiveIndex() {
        return activeIndex;
    }

    public void setActiveIndex(int activeIndex) {
        this.activeIndex = activeIndex;
    }

    public double[] getXCoordinatesArray() {
        return xCoordinates.toArray();
    }

    public double[] getYCoordinatesArray() {
        return yCoordinates.toArray();
    }

    public void addPoint(Point point) {
        addPoint(point.getX(), point.getY());
    }

    public void addPoint(double x, double y) {
        int index = ArrayUtils.findFirstGreaterThan(getXCoordinatesArray(), x);

        xCoordinates.add(x, index);
        yCoordinates.add(y, index);

        activeIndex = index;
    }

    public boolean isEmpty() {
        return xCoordinates.isEmpty() && yCoordinates.isEmpty();
    }

    public double[] getIntepData(double[] xinter) {
        return MathUtils.intep(getXCoordinatesArray(), getYCoordinatesArray(), xinter);
    }

    public void deleteActivePoint() {
        if (xCoordinates.size() > 1) {
            xCoordinates.remove(activeIndex);
            yCoordinates.remove(activeIndex);

            if (activeIndex == xCoordinates.size()) {
                activeIndex--;
            }
        }
    }

    public XYSeries getActivePoint() {
        return new XYSeries(new double[]{xCoordinates.get(activeIndex)}, new double[]{yCoordinates.get(activeIndex)});
    }

    public void moveActivePoint(double xShift, double yShift) {
        double oldX = getActiveX();
        double oldY = getActiveY();

        xCoordinates.remove(activeIndex);
        yCoordinates.remove(activeIndex);

        addPoint(oldX + xShift, oldY + yShift);
    }

    public double getActiveX() {
        return xCoordinates.get(activeIndex);
    }

    public double getActiveY() {
        return yCoordinates.get(activeIndex);
    }

    public int getCount() {
        return xCoordinates.size();
    }

    public static RectifyAsset withDefaultPoints(XYSeries data) {
        double[] xSeries = data.getXSeries();
        double[] ySeries = data.getYSeries();

        DoubleArrayList xCoordinates = new DoubleArrayList(new double[]{xSeries[0], xSeries[xSeries.length - 1]});
        DoubleArrayList yCoordinates = new DoubleArrayList(new double[]{ySeries[0], ySeries[ySeries.length - 1]});

        return new RectifyAsset(xCoordinates, yCoordinates);
    }
}
