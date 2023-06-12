package cz.cuni.mff.respefo.function.rectify;

import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.util.collections.DoubleArrayList;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.collections.tuple.Quintet;
import cz.cuni.mff.respefo.util.collections.tuple.Tuple;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;
import cz.cuni.mff.respefo.util.utils.FileUtils;
import cz.cuni.mff.respefo.util.utils.MathUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.BiFunction;
import java.util.function.DoubleUnaryOperator;

import static cz.cuni.mff.respefo.util.Constants.SPEED_OF_LIGHT;
import static cz.cuni.mff.respefo.util.utils.MathUtils.doublesEqual;
import static java.lang.Math.PI;
import static java.lang.Math.sin;
import static java.util.Arrays.stream;

public class Blaze {
    private static final double[] K_COEFFICIENTS = new double[] {5.53283427e+05, 3.55298869e+02, -3.35894502e+00, 1.07610824e-02};

    private static final Map<Integer, Quintet<Double, Double, Double, Double, Double>> PARAMETERS;
    static {
        PARAMETERS = new HashMap<>(62);

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

                PARAMETERS.put(indexToOrder(index), Tuple.of(alpha, delta, epsilon, theta, lambdaC));
            }

        } catch (Exception exception) {
            Log.error("An error occurred while reading blaze parameters", exception);
        }
    }


    private final int order;
    private double alpha;
    private final double delta;
    private final double epsilon;
    private final double theta;
    private double lambdaC;
    private double A;
    private final double rvCorrection;
    private Mode mode;

    public Blaze(int index, DoubleUnaryOperator scaleFunction, double rvCorrection, Mode mode) {
        order = indexToOrder(index);

        Quintet<Double, Double, Double, Double, Double> params = PARAMETERS.get(order);
        delta = params.b;
        epsilon = params.c;
        theta = params.d;

        switch (mode) {
            case ALPHA:
                lambdaC = orderToCentralWavelength(order, Mode.ALPHA);
                alpha = wavelengthToAlpha(lambdaC);
                break;
            case BETA:
            case PARABOLA:
                alpha = params.a;
                lambdaC = params.e;
                break;
        }

        lambdaC += rvCorrection * (lambdaC / SPEED_OF_LIGHT);

        A = scaleFunction.applyAsDouble(lambdaC);

        this.rvCorrection = rvCorrection;
        this.mode = mode;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        switch (mode) {
            case ALPHA:
                alpha = wavelengthToAlpha(lambdaC);
                break;
            case BETA:
            case PARABOLA:
                alpha = PARAMETERS.get(order).a;
                break;
        }

        this.mode = mode;
    }

    public static int indexToOrder(int index) {
        return 125 - index;
    }

    public static int orderToIndex(int order) {
        return 125 - order;
    }

    public static double orderToCentralWavelength(int order, Mode mode) {
        switch (mode) {
            case BETA:
            case PARABOLA:
                return PARAMETERS.get(order).e;
            case ALPHA:
                return MathUtils.polynomial(order, K_COEFFICIENTS) / order;
            default:
                throw new IllegalArgumentException("Unknown mode");
        }
    }

    public static double wavelengthToAlpha(double wavelength) {
        if (wavelength > 5.82178076e+03) {
            return 9.61192285e-01 + (wavelength - 5.82178076e+03) * 2.56317390e-05;
        } else {
            return 9.61192285e-01 - (wavelength - 5.82178076e+03) * -2.38535971e-06;
        }
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

    public double getK() {
        return order * lambdaC;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double newValue) {
        alpha = newValue;
    }

    public int getSpinnerAlpha(int digits) {
        return MathUtils.roundForSpinner(alpha, digits);
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

        BiFunction<Double, Double, Double> valueFunction;
        if (mode == Mode.ALPHA) {
            valueFunction = this::alphaValue;
        } else if (mode == Mode.BETA) {
            valueFunction = this::betaValue;
        } else {
            valueFunction = this::parabolaValue;
        }

        return stream(xSeries)
                .map(x -> x - rvCorrection * (x / SPEED_OF_LIGHT))
                .map(x -> valueFunction.apply(x, correctedLambdaC))
                .toArray();
    }

    private double alphaValue(double lambda, double lambdaC) {
        double X = order * (1 - lambdaC / lambda);
        double argument = PI * alpha * X;
        double fraction = sin(argument) / argument;

        return A * fraction * fraction;
    }

    private double betaValue(double lambda, double lambdaC) {
        double l = lambdaC - epsilon - lambda;
        double beta = theta * 1e-8 * l * l * l - delta * 1e-6 * l * l + alpha;
        double X = order * (1 - lambdaC / lambda);

        double argument = PI * beta * X;
        double fraction = sin(argument) / argument;

        return A * fraction * fraction;
    }

    private double parabolaValue(double lambda, double lambdaC) {
        double l = lambdaC - epsilon - lambda;
        double beta = -delta * 1e-6 * l * l + alpha;
        double X = order * (1 - lambdaC / lambda);

        double argument = PI * beta * X;
        double fraction = sin(argument) / argument;

        return A * fraction * fraction;
    }

    enum Mode {
        ALPHA, BETA, PARABOLA
    }
}
