package cz.cuni.mff.respefo.function.rectify;

import cz.cuni.mff.respefo.util.collections.DoubleArrayList;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;
import cz.cuni.mff.respefo.util.utils.MathUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.DoubleUnaryOperator;

import static cz.cuni.mff.respefo.util.Constants.SPEED_OF_LIGHT;
import static cz.cuni.mff.respefo.util.utils.MathUtils.doublesEqual;
import static java.lang.Math.*;
import static java.util.Arrays.stream;

public class Blaze {
    private static final double[] K_COEFFICIENTS = new double[] {5.53283427e+05, 3.55298869e+02, -3.35894502e+00, 1.07610824e-02};

    private final int order;
    private double centralWavelength;  // shifted by the rv correction
    private double scale;
    private double alpha;
    private final double rvCorrection;

    public Blaze(int index, DoubleUnaryOperator scaleFunction, double rvCorrection) {
        order = indexToOrder(index);
        centralWavelength = orderToCentralWavelength(order);
        centralWavelength += rvCorrection * (centralWavelength / SPEED_OF_LIGHT);
        scale = scaleFunction.applyAsDouble(centralWavelength);
        alpha = wavelengthToAlpha(centralWavelength);
        this.rvCorrection = rvCorrection;
    }


    public static double wavelengthToAlpha(double wavelength) {
        if (wavelength > 5.82178076e+03) {
            return 9.61192285e-01 + (wavelength - 5.82178076e+03) * 2.56317390e-05;
        } else {
            return 9.61192285e-01 - (wavelength - 5.82178076e+03) * -2.38535971e-06;
        }
    }

    public static double orderToCentralWavelength(int order) {
        return MathUtils.polynomial(order, K_COEFFICIENTS) / order;
    }

    public static int indexToOrder(int index) {
        return 125 - index;
    }


    public int getOrder() {
        return order;
    }

    public double getScale() {
        return scale;
    }

    public void updateScale(double diff) {
        scale += diff;
    }

    public double getCentralWavelength() {
        return centralWavelength;
    }

    public void updateCentralWavelength(double diff) {
        centralWavelength += diff;
    }

    public double getK() {
        return order * centralWavelength;
    }

    public double getAlpha() {
        return alpha;
    }

    public int getSpinnerAlpha(int digits) {
        BigDecimal bd = new BigDecimal(alpha * Math.pow(10, digits)).setScale(0, RoundingMode.HALF_EVEN);
        return bd.intValue();
    }

    public void setAlpha(double newValue) {
        alpha = newValue;
    }

    public void updateFromAsset(BlazeAsset asset) {
        centralWavelength = asset.getCentralWavelength(order);
        scale = asset.getScale(order);
    }

    public void saveToAsset(BlazeAsset asset) {
        asset.setParameters(order, centralWavelength, scale);
    }

    public boolean isUnchanged(BlazeAsset asset) {
        return asset.hasParameters(order)
                && doublesEqual(centralWavelength, asset.getCentralWavelength(order))
                && doublesEqual(scale, asset.getScale(order));
    }


    public RectifyAsset toRectifyAsset(XYSeries series) {
        double[] xs = ArrayUtils.linspace(series.getX(0), series.getLastX(), 20);
        double[] ys = ySeries(xs);

        return new RectifyAsset(new DoubleArrayList(xs), new DoubleArrayList(ys));
    }


    public double[] ySeries(double[] xSeries) {
        double correctedCentralWavelength = centralWavelength - rvCorrection * (centralWavelength / SPEED_OF_LIGHT);

        return stream(xSeries)
                .map(x -> x - rvCorrection * (x / SPEED_OF_LIGHT))
                .map(x -> scale * r(x, order, correctedCentralWavelength, alpha))
                .toArray();
    }

    private static double r(double lambda, int order, double centralWavelength, double alpha) {
        double x = order * (1 - centralWavelength / lambda);
        double argument = PI * alpha * x;
        return pow(sin(argument) / argument, 2);
    }
}
