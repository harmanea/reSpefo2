package cz.cuni.mff.respefo.function.rectify;

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
    private static final double K = 565754; // More like 565660.213... by my calculations
    private static final double[] COEFFICIENTS = new double[]
            {-3.0817976563120606, 0.21780270894619688, -0.004371189338350063, 4.139193926208102e-05, -1.8545313075173e-07, 3.12485701485604e-10};

    private final XYSeries series;
    private final int order;
    private final double alpha;
    private double centralWavelength;
    private double scale;

    public Blaze(XYSeries series, int order) {
        this(series, order, K / order, MathUtils.intep(series.getXSeries(), series.getYSeries(), K / order));
        fit();
    }

    public Blaze(XYSeries series, int order, double centralWavelength, double scale) {
        this.series = series;
        this.order = order;
        this.centralWavelength = centralWavelength;
        this.scale = scale;

        alpha = MathUtils.polynomial(order, COEFFICIENTS);
    }

    private void fit() {
        Function fun = new Function() {
            @Override
            public double evaluate(double[] values, double[] parameters) {
                return parameters[1] * r(values[0], order, parameters[0], alpha);
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
        fit.setParameters(new double[] {centralWavelength, scale});
        fit.fitData();

        double[] parameters = fit.getParameters();
        centralWavelength = parameters[0];
        scale = parameters[1];
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

    public RectifyAsset toRectifyAsset() {
        DoubleArrayList xCoordinates = new DoubleArrayList(20);
        DoubleArrayList yCoordinates = new DoubleArrayList(20);

        double low = series.getX(0);
        double high = series.getLastX();

        for (int i = 0; i < 20; i++) {
            double x = linearInterpolation(0, low, 19, high, i);
            double y = scale * r(x, order, centralWavelength, alpha);

            xCoordinates.add(x);
            yCoordinates.add(y);
        }

        return new RectifyAsset(xCoordinates, yCoordinates);
    }

    public XYSeries series() {
        double[] xSeries = series.getXSeries();
        double[] ySeries = stream(xSeries)
                .map(x -> scale * r(x, order, centralWavelength, alpha))
                .toArray();

        return new XYSeries(xSeries, ySeries);
    }

    private static double r(double lambda, int order, double centralWavelength, double alpha) {
        double x = order * (1 - centralWavelength / lambda);
        double argument = PI * alpha * x;
        return pow(sin(argument) / argument, 2);
    }
}
