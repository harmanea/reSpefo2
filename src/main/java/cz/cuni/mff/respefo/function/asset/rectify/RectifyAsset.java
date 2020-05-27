package cz.cuni.mff.respefo.function.asset.rectify;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.cuni.mff.respefo.format.Data;
import cz.cuni.mff.respefo.format.FunctionAsset;
import cz.cuni.mff.respefo.util.JsonNoAutoDetect;
import cz.cuni.mff.respefo.util.Point;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;
import cz.cuni.mff.respefo.util.utils.MathUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@JsonNoAutoDetect
public class RectifyAsset implements FunctionAsset {
    @JsonProperty
    private List<Double> xCoordinates;
    @JsonProperty
    private List<Double> yCoordinates;
    private int activeIndex = 0;

    public List<Double> getxCoordinates() {
        return xCoordinates;
    }

    public void setxCoordinates(List<Double> xCoordinates) {
        this.xCoordinates = xCoordinates;
    }

    public List<Double> getyCoordinates() {
        return yCoordinates;
    }

    public void setyCoordinates(List<Double> yCoordinates) {
        this.yCoordinates = yCoordinates;
    }

    public int getActiveIndex() {
        return activeIndex;
    }

    public void setActiveIndex(int activeIndex) {
        this.activeIndex = activeIndex;
    }

    public double[] getXCoordinatesArray() {
        return xCoordinates.stream().mapToDouble(Double::doubleValue).toArray();
    }

    public double[] getYCoordinatesArray() {
        return yCoordinates.stream().mapToDouble(Double::doubleValue).toArray();
    }

    public void addPoint(Point point) {
        addPoint(point.getX(), point.getY());
    }

    public void addPoint(double x, double y) {
        int index = ArrayUtils.findFirstGreaterThen(getXCoordinatesArray(), x);

        xCoordinates.add(index, x);
        yCoordinates.add(index, y);
    }

    public RectifyAsset() {
        xCoordinates = new ArrayList<>();
        yCoordinates = new ArrayList<>();
    }

    @Override
    public Data process(Data data) {
        double[] continuum = getIntepData(data.getX());
        double[] newYSeries = ArrayUtils.divideArrayValues(data.getY(), continuum);

        data.setY(newYSeries);

        return data;
    }

    public double[] getIntepData(double[] xinter) {
        return MathUtils.intep(getXCoordinatesArray(), getYCoordinatesArray(), xinter);
    }

    public static RectifyAsset withDefaultPoints(double x1, double y1, double x2, double y2) {
        RectifyAsset rectifyAsset = new RectifyAsset();

        List<Double> xCoordinates = new ArrayList<>(Arrays.asList(x1, x2));
        List<Double> yCoordinates = new ArrayList<>(Arrays.asList(y1, y2));

        rectifyAsset.setxCoordinates(xCoordinates);
        rectifyAsset.setyCoordinates(yCoordinates);

        return rectifyAsset;
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

    public Data getActivePoint() {
        return new Data(new double[] {xCoordinates.get(activeIndex)}, new double[] {yCoordinates.get(activeIndex)});
    }
}
