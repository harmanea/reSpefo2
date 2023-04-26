package cz.cuni.mff.respefo.function.rectify;

import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.util.collections.DoubleArrayList;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.collections.tuple.Quintet;
import cz.cuni.mff.respefo.util.collections.tuple.Tuple;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;
import cz.cuni.mff.respefo.util.utils.FileUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.DoubleUnaryOperator;

import static cz.cuni.mff.respefo.util.Constants.SPEED_OF_LIGHT;
import static cz.cuni.mff.respefo.util.utils.MathUtils.doublesEqual;
import static java.lang.Math.PI;
import static java.lang.Math.sin;
import static java.util.Arrays.stream;

public class Blaze {

    private static final Map<Integer, Quintet<Double, Double, Double, Double, Double>> parameters;
    static {
        parameters = new HashMap<>(62);

        try {
            String csvText = FileUtils.getResourceFileAsString("blaze.csv");
            Scanner lineScanner = new Scanner(Objects.requireNonNull(csvText));

            lineScanner.nextLine(); // Skip header

            while (lineScanner.hasNext()) {
                String line = lineScanner.nextLine();
                String[] tokens = line.split(",");

                int index = Integer.parseInt(tokens[0]) - 1;
                double alpha = Double.parseDouble(tokens[1]);
                double delta = Double.parseDouble(tokens[2]);
                double epsilon = Double.parseDouble(tokens[3]);
                double theta = Double.parseDouble(tokens[4]);
                double lambdaC = Double.parseDouble(tokens[5]);

                parameters.put(indexToOrder(index), Tuple.of(alpha, delta, epsilon, theta, lambdaC));
            }

        } catch (Exception exception) {
            Log.error("An error occurred while reading blaze parameters", exception);
        }
    }


    private final int order;
    private final double alpha;
    private final double delta;
    private final double epsilon;
    private final double theta;
    private double lambdaC;
    private double A;
    private final double rvCorrection;

    public Blaze(int index, DoubleUnaryOperator scaleFunction, double rvCorrection) {
        order = indexToOrder(index);

        Quintet<Double, Double, Double, Double, Double> params = parameters.get(order);
        alpha = params.a;
        delta = params.b;
        epsilon = params.c;
        theta = params.d;

        lambdaC = params.e;
        lambdaC += rvCorrection * (lambdaC / SPEED_OF_LIGHT);

        A = scaleFunction.applyAsDouble(lambdaC);

        this.rvCorrection = rvCorrection;
    }

    public static int indexToOrder(int index) {
        return 125 - index;
    }

    public static int orderToIndex(int order) {
        return 125 - order;
    }

    public static double orderToCentralWavelength(int order) {
        return parameters.get(order).e;
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

    public void updateCentralWavelength(double diff) {
        lambdaC += diff;
    }

    public void updateFromAsset(BlazeAsset asset) {
        lambdaC = asset.getCentralWavelength(order);
        A = asset.getScale(order);
    }

    public void saveToAsset(BlazeAsset asset) {
        int index = orderToIndex(order);
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
                .map(x -> r(A, x, correctedLambdaC, alpha, delta, epsilon, theta, order))
                .toArray();
    }

    private static double r(double A, double lambda, double lambdaC, double alpha, double delta, double epsilon, double theta, int order) {
        double l = lambdaC - epsilon - lambda;
        double beta = theta * 1e-8 * l * l * l - delta * 1e-6 * l * l + alpha;
        double X = order * (1 - lambdaC / lambda);

        double argument = PI * beta * X;
        double fraction = sin(argument) / argument;

        return A * fraction * fraction;
    }
}
