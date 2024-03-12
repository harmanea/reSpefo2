package cz.cuni.mff.respefo.function.rectify;

import cz.cuni.mff.respefo.util.collections.DoubleArrayList;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;
import cz.cuni.mff.respefo.util.utils.MathUtils;

import java.util.function.BiFunction;
import java.util.function.DoubleUnaryOperator;

import static cz.cuni.mff.respefo.util.Constants.SPEED_OF_LIGHT;
import static cz.cuni.mff.respefo.util.utils.MathUtils.doublesEqual;
import static java.util.Arrays.stream;

public class Blaze {
    private final int order;
    private double alpha;
    private double lambdaC;
    private double A;
    private final double rvCorrection;
    private BiFunction<Double, Double, Double> valueFunction;

    public Blaze(int order, DoubleUnaryOperator scaleFunction, double rvCorrection, BlazeParameters parameters) {
        this.order = order;
        alpha = parameters.getAlpha(order);
        lambdaC = parameters.getCentralWavelength(order);
        A = scaleFunction.applyAsDouble(lambdaC);
        this.rvCorrection = rvCorrection;
        valueFunction = parameters.getValueFunction(this);

        lambdaC += rvCorrection * (lambdaC / SPEED_OF_LIGHT);
    }

    // TODO: Probably should remember the alpha for each model
    public void update(BlazeParameters parameters) {
        this.valueFunction = parameters.getValueFunction(this);
    }

    public int getOrder() {
        return order;
    }

    public double getScale() {
        return A;
    }

    public void updateScale(double diff) {
        A += diff;
    }

    public double getCentralWavelength() {
        return lambdaC;
    }

    public void setCentralWavelength(double value) {
        lambdaC = value;
    }

    public void updateCentralWavelength(double diff) {
        lambdaC += diff;
    }

    public double getK() {
        return order * lambdaC;
    }

    public double getAlpha() {
        return alpha;
    }

    public int getSpinnerAlpha(int digits) {
        return MathUtils.roundForSpinner(alpha, digits);
    }

    public void setAlpha(double value) {
        alpha = value;
    }

    public void updateFromAsset(BlazeAsset asset) {
        lambdaC = asset.getCentralWavelength(order);
        A = asset.getScale(order);
    }

    public void saveToAsset(BlazeAsset asset, int index) {
        asset.setXCoordinate(index, lambdaC);
        asset.setYCoordinate(index, A);
        asset.setParameters(order, lambdaC, A);
    }

    public boolean isUnchanged(BlazeAsset asset) {
        return asset.hasParameters(order)
                && doublesEqual(lambdaC, asset.getCentralWavelength(order))
                && doublesEqual(A, asset.getScale(order));
    }

    public RectifyAsset toRectifyAsset(XYSeries series) {
        double[] xs = ArrayUtils.linspace(series.getX(0), series.getLastX(), 20);
        double[] ys = ySeries(xs);

        return new RectifyAsset(new DoubleArrayList(xs), new DoubleArrayList(ys));
    }

    public double[] ySeries(double[] xSeries) {
        double correctedLambdaC = lambdaC - rvCorrection * (lambdaC / SPEED_OF_LIGHT);

        return stream(xSeries)
                .map(x -> x - rvCorrection * (x / SPEED_OF_LIGHT))
                .map(x -> A * valueFunction.apply(x, correctedLambdaC))
                .toArray();
    }
}
