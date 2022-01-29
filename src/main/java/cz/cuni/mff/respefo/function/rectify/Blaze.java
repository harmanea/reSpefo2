package cz.cuni.mff.respefo.function.rectify;

import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.util.collections.DoubleArrayList;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.utils.MathUtils;
import org.orangepalantir.leastsquares.Fitter;
import org.orangepalantir.leastsquares.Function;
import org.orangepalantir.leastsquares.fitters.MarquardtFitter;

import static cz.cuni.mff.respefo.util.utils.MathUtils.linearInterpolation;
import static java.lang.Math.*;
import static java.util.Arrays.stream;

public class Blaze {
    public static final double K = 565754; // More like 565660.213... by my calculations
    private static final double[] ALPHA_COEFFICIENTS = new double[]
            {-1.52982125e+01, 1.27961723e-02, -3.97132440e-06, 6.06949396e-10, -4.56757646e-14, 1.35599569e-18};

    private final int order;
    private double centralWavelength;
    private double scale;

    public Blaze(int order, double centralWavelength, double scale) {
        this.order = order;
        this.centralWavelength = centralWavelength;
        this.scale = scale;
    }

    public Blaze(int index, double[] coeffs) {
        order = 125 - index;
        centralWavelength = K / order;
        scale = MathUtils.polynomial(centralWavelength, coeffs);
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

    public void setScale(double value) {
        scale = value;
    }

    public double getCentralWavelength() {
        return centralWavelength;
    }

    public void updateCentralWavelength(double diff) {
        centralWavelength += diff;
    }

    public void setCentralWavelength(double value) {
        centralWavelength = value;
    }

    public void fit(XYSeries series) {
        Function fun = new Function() {
            @Override
            public double evaluate(double[] values, double[] parameters) {
                return parameters[1] * r(values[0], order, parameters[0], MathUtils.polynomial(values[0], ALPHA_COEFFICIENTS));
            }

            @Override
            public int getNParameters() {
                return 2; // central wavelength, scale
            }

            @Override
            public int getNInputs() {
                return 1; // wavelength
            }
        };

        Fitter fit = new MarquardtFitter(fun);
        fit.setData(
                stream(series.getXSeries()).mapToObj(x -> new double[]{x}).toArray(double[][]::new),
                series.getYSeries()
        );
        fit.setParameters(new double[]{centralWavelength, scale});
        fit.fitData();

        double[] parameters = fit.getParameters();
        if (Double.isNaN(parameters[0]) || Double.isNaN(parameters[1])) {
            Log.warning("Least squares fit failed for order " + order);
        } else {
            centralWavelength = parameters[0];
            scale = parameters[1];
        }
    }

    public RectifyAsset toRectifyAsset(XYSeries series) {
        DoubleArrayList xCoordinates = new DoubleArrayList(20);
        DoubleArrayList yCoordinates = new DoubleArrayList(20);

        double low = series.getX(0);
        double high = series.getLastX();

        for (int i = 0; i < 20; i++) {
            double x = linearInterpolation(0, low, 19, high, i);
            double y = scale * r(x, order, centralWavelength, MathUtils.polynomial(x, ALPHA_COEFFICIENTS));

            xCoordinates.add(x);
            yCoordinates.add(y);
        }

        return new RectifyAsset(xCoordinates, yCoordinates);
    }

    public double[] ySeries(double[] xSeries) {
        return stream(xSeries)
                .map(x -> scale * r(x, order, centralWavelength, MathUtils.polynomial(x, ALPHA_COEFFICIENTS)))
                .toArray();
    }

    private static double r(double lambda, int order, double centralWavelength, double alpha) {
        double x = order * (1 - centralWavelength / lambda);
        double argument = PI * alpha * x;
        return pow(sin(argument) / argument, 2);
    }
}
