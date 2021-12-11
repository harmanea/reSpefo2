package cz.cuni.mff.respefo.function.rectify;

import cz.cuni.mff.respefo.spectrum.asset.FunctionAsset;
import cz.cuni.mff.respefo.util.collections.DoubleArrayList;
import cz.cuni.mff.respefo.util.collections.Point;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;
import cz.cuni.mff.respefo.util.utils.MathUtils;

import java.util.Arrays;

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
        addPoint(point.x, point.y);
    }

    public void addPoint(double x, double y) {
        int index = ArrayUtils.indexOfFirstGreaterThan(getXCoordinatesArray(), x);

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

    public RectifyAsset adjustToNewData(XYSeries data) {
        double[] newXSeries = xCoordinates.stream().filter(x -> x >= data.getX(0) && x <= data.getLastX()).toArray();

        double[] newYSeries = Arrays.stream(newXSeries)
                .map(x -> {
                    int index = ArrayUtils.indexOfClosest(data.getXSeries(), x);
                    return Arrays.stream(data.getYSeries(), Math.max(0, index - 2), Math.min(data.getLength() - 1, index + 2)).average().getAsDouble();
                }).toArray();

        return new RectifyAsset(new DoubleArrayList(newXSeries), new DoubleArrayList(newYSeries));
    }

    public static RectifyAsset withDefaultPoints(XYSeries data) {
        DoubleArrayList xCoordinates = new DoubleArrayList(new double[]{data.getX(0), data.getLastX()});
        DoubleArrayList yCoordinates = new DoubleArrayList(new double[]{data.getY(0), data.getLastY()});

        return new RectifyAsset(xCoordinates, yCoordinates);
    }
}
