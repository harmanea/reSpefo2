package cz.cuni.mff.respefo.function.disp;

import cz.cuni.mff.respefo.util.utils.MathUtils;

public class ComparisonLineMeasurement {
    private final double laboratoryValue;
    private double xUp;
    private double xDown;

    public ComparisonLineMeasurement(double laboratoryValue) {
        this.laboratoryValue = laboratoryValue;

        xUp = Double.NaN;
        xDown = Double.NaN;
    }

    public double getLaboratoryValue() {
        return laboratoryValue;
    }

    public void setXUp(double xUp) {
        this.xUp = xUp;
    }

    public void setXDown(double xDown) {
        this.xDown = xDown;
    }

    public double getXUp() {
        return xUp;
    }

    public double getXDown() {
        return xDown;
    }

    public double getX() {
        if (Double.isNaN(xUp)) {
            if (Double.isNaN(xDown)) {
                return Double.NaN;
            } else {
                return xDown;
            }
        } else {
            if (Double.isNaN(xDown)) {
                return xUp;
            } else {
                return (xUp + xDown) / 2;
            }
        }
    }

    public boolean isMeasured() {
        return MathUtils.isNotNaN(getX());
    }
}
