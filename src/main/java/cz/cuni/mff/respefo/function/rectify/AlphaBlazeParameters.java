package cz.cuni.mff.respefo.function.rectify;

import cz.cuni.mff.respefo.util.utils.MathUtils;

import java.util.function.BiFunction;

import static java.lang.Math.PI;
import static java.lang.Math.sin;

public class AlphaBlazeParameters implements BlazeParameters {
    private static final double[] K_COEFFICIENTS = new double[] {5.53283427e+05, 3.55298869e+02, -3.35894502e+00, 1.07610824e-02};

    @Override
    public double getCentralWavelength(int order) {
        return MathUtils.polynomial(order, K_COEFFICIENTS) / order;
    }

    @Override
    public double getAlpha(int order) {
        double wavelength = getCentralWavelength(order);

        if (wavelength > 5.82178076e+03) {
            return 9.61192285e-01 + (wavelength - 5.82178076e+03) * 2.56317390e-05;
        } else {
            return 9.61192285e-01 - (wavelength - 5.82178076e+03) * -2.38535971e-06;
        }
    }

    @Override
    public BiFunction<Double, Double, Double> getValueFunction(Blaze blaze) {
        return (lambda, lambdaC) -> {
            double X = blaze.getOrder() * (1 - lambdaC / lambda);
            double argument = PI * blaze.getAlpha() * X;
            double fraction = sin(argument) / argument;

            return fraction * fraction;
        };
    }

    @Override
    public String label() {
        return "Alpha";
    }
}
