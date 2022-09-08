package cz.cuni.mff.respefo.function.ew;

import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.utils.MathUtils;

import java.util.ArrayList;
import java.util.List;

public class MeasureEWResult {
    private double l0;
    private double radius;
    private String name;
    private int[] bounds;
    private List<Integer> points;
    private List<MeasureEWResultPointCategory> categories;

    private MeasureEWResult() {
        // default empty constructor
    }

    public MeasureEWResult(double l0, double radius, String name, int left, int right) {
        this.l0 = l0;
        this.radius = radius;
        this.name = name;

        bounds = new int[]{left, right};
        points = new ArrayList<>();
        categories = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public double getL0() {
        return l0;
    }

    public double getRadius() {
        return radius;
    }

    public int pointsCount() {
        return points.size();
    }

    public int getLowerBound() {
        return Math.min(bounds[0], bounds[1]);
    }

    public int getUpperBound() {
        return Math.max(bounds[0], bounds[1]);
    }

    public int getBound(int index) {
        return bounds[index];
    }

    public void setBound(int index, int value) {
        bounds[index] = value;
    }

    public int getPoint(int index) {
        return points.get(index);
    }

    public void setPoint(int index, int value) {
        points.set(index, value);
    }

    public int getPointForCategory(MeasureEWResultPointCategory category) {
        return points.get(categories.indexOf(category));
    }

    public MeasureEWResultPointCategory getCategory(int index) {
        return categories.get(index);
    }

    public boolean containsCategory(MeasureEWResultPointCategory category) {
        return categories.contains(category);
    }

    public void add(int point, MeasureEWResultPointCategory category) {
        points.add(point);
        categories.add(category);
    }

    public void remove(int index) {
        points.remove(index);
        categories.remove(index);
    }

    public double getEW(XYSeries series) {
        int lowerBound = getLowerBound();
        int upperBound = getUpperBound();

        double ew = series.getX(upperBound) - series.getX(lowerBound);
        for (int index = lowerBound; index < upperBound; index++) {
            ew -= area(series, index, index + 1);
        }

        return ew;
    }

    private double area(XYSeries series, int left, int right) {
        double leftBase = series.getY(left);
        double rightBase = series.getY(right);
        double height = series.getX(right) - series.getX(left);

        return (leftBase + rightBase) * height / 2;
    }

    public double getFWHM(XYSeries series) {
        int point = getPointForCategory(MeasureEWResultPointCategory.Ic);
        double halfIntensity = (1 + series.getY(point)) / 2;

        int leftIndex = -1;
        for (int i = point; i > getLowerBound(); i--) {
            if (series.getY(i) > halfIntensity) {
                leftIndex = i;
                break;
            }
        }

        int rightIndex = -1;
        for (int i = point; i < getUpperBound(); i++) {
            if (series.getY(i) > halfIntensity) {
                rightIndex = i;
                break;
            }
        }

        if (leftIndex >= 0 && rightIndex >= 0) {
            double leftValue = MathUtils.linearInterpolation(series.getY(leftIndex), series.getX(leftIndex),
                    series.getY(leftIndex + 1), series.getX(leftIndex + 1), halfIntensity);
            double rightValue = MathUtils.linearInterpolation(series.getY(rightIndex), series.getX(rightIndex),
                    series.getY(rightIndex - 1), series.getX(rightIndex - 1), halfIntensity);

            return rightValue - leftValue;
        } else {
            return Double.NaN;
        }
    }

    public double getVToR(XYSeries series) {
        double v = series.getY(getPointForCategory(MeasureEWResultPointCategory.V));
        double r = series.getY(getPointForCategory(MeasureEWResultPointCategory.R));

        return v / r;
    }

    public double getVRAvg(XYSeries series) {
        double v = series.getY(getPointForCategory(MeasureEWResultPointCategory.V));
        double r = series.getY(getPointForCategory(MeasureEWResultPointCategory.R));

        return (v + r) / 2;
    }

    public boolean isRepeated(MeasureEWResult other) {
        return this != other
                && MathUtils.doublesEqual(this.l0, other.l0)
                && this.name.equals(other.name);
    }
}
