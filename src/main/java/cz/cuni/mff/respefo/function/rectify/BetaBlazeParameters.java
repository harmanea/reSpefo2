package cz.cuni.mff.respefo.function.rectify;

import cz.cuni.mff.respefo.util.collections.tuple.Quintet;

import java.util.function.BiFunction;

import static java.lang.Math.PI;
import static java.lang.Math.sin;

public class BetaBlazeParameters extends ParabolaBlazeParameters {
    @Override
    public BiFunction<Double, Double, Double> getValueFunction(Blaze blaze) {
        Quintet<Double, Double, Double, Double, Double> params = PARAMETERS.get(blaze.getOrder());
        double delta = params.b;
        double epsilon = params.c;
        double theta = params.d;

        return (lambda, lambdaC) -> {
            double l = lambdaC - epsilon - lambda;
            double beta = theta * 1e-8 * l * l * l - delta * 1e-6 * l * l + blaze.getAlpha();
            double X = blaze.getOrder() * (1 - lambdaC / lambda);

            double argument = PI * beta * X;
            double fraction = sin(argument) / argument;

            return fraction * fraction;
        };
    }

    @Override
    public String label() {
        return "Beta";
    }
}
