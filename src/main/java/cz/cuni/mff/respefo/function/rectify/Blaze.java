package cz.cuni.mff.respefo.function.rectify;

import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.util.collections.DoubleArrayList;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;
import cz.cuni.mff.respefo.util.utils.MathUtils;
import org.orangepalantir.leastsquares.Fitter;
import org.orangepalantir.leastsquares.Function;
import org.orangepalantir.leastsquares.fitters.MarquardtFitter;

import static java.lang.Math.*;
import static java.util.Arrays.stream;

public class Blaze {
    private static final double[] K_COEFFICIENTS = new double[] {5.56243514e+05,  2.72704907e+02, -2.57380895e+00,  8.24940353e-03};

    private final int order;
    private double centralWavelength;
    private double scale;

    public Blaze(int index, double[] coeffs) {
        order = 125 - index;
        centralWavelength = getCentralWavelength(order);
        scale = MathUtils.polynomial(centralWavelength, coeffs);
    }

    public static double getAlpha(double wavelength) {
        if (wavelength > 5.82178076e+03) {
            return 9.61192285e-01 + (wavelength - 5.82178076e+03) * 2.56317390e-05;
        } else {
            return 9.61192285e-01 - (wavelength - 5.82178076e+03) * -2.38535971e-06;
        }
    }

    public static double getCentralWavelength(int order) {
        return MathUtils.polynomial(order, K_COEFFICIENTS) / order;
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

    public void setCentralWavelength(double centralWavelength) {
        this.centralWavelength = centralWavelength;
    }

    public void updateCentralWavelength(double diff) {
        centralWavelength += diff;
    }

    @Deprecated
    public void fit(XYSeries series) {
        Function fun = new Function() {
            @Override
            public double evaluate(double[] values, double[] parameters) {
                return parameters[1] * r(values[0], order, parameters[0], getAlpha(values[0]));
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
        double[] xs = ArrayUtils.linspace(series.getX(0), series.getLastX(), 20);
        double[] ys = ySeries(xs);

        return new RectifyAsset(new DoubleArrayList(xs), new DoubleArrayList(ys));
    }

    public double[] ySeries(double[] xSeries) {
        return stream(xSeries)
                .map(x -> scale * r(x, order, centralWavelength, getAlpha(x)))
                .toArray();
    }

    private static double r(double lambda, int order, double centralWavelength, double alpha) {
        double x = order * (1 - centralWavelength / lambda);
        double argument = PI * alpha * x;
        return pow(sin(argument) / argument, 2);
    }
}
