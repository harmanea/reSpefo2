package cz.cuni.mff.respefo.function.asset.common;

public class Measurement {
    private double l0;
    private double radius;
    private String name;

    public Measurement(double l0, double radius, String name) {
        this.l0 = l0;
        this.radius = radius;
        this.name = name;
    }

    public double getL0() {
        return l0;
    }

    public double getRadius() {
        return radius;
    }

    public String getName() {
        return name;
    }

    public double getLowerBound() {
        return l0 - radius;
    }

    public double getUpperBound() {
        return l0 + radius;
    }
}
