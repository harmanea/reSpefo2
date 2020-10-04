package cz.cuni.mff.respefo.function.asset.common;

public class Measurement {
    private double l0;
    private double radius;
    private String name;
    private boolean isCorrection;

    private Measurement() {
        // default empty constructor
    }

    public Measurement(double l0, double radius, String name, boolean isCorrection) {
        this.l0 = l0;
        this.radius = radius;
        this.name = name;
        this.isCorrection = isCorrection;
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

    public boolean isCorrection() {
        return isCorrection;
    }

    public double getLowerBound() {
        return l0 - radius;
    }

    public double getUpperBound() {
        return l0 + radius;
    }

    public void increaseRadius() {
        radius *= 1.5;
    }

    public void decreaseRadius() {
        radius /= 1.5;
    }
}
