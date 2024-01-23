package cz.cuni.mff.respefo.function.rectify;

import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.util.collections.tuple.Quintet;
import cz.cuni.mff.respefo.util.collections.tuple.Tuple;
import cz.cuni.mff.respefo.util.utils.FileUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.BiFunction;

import static java.lang.Math.PI;
import static java.lang.Math.sin;

public class ParabolaBlazeParameters implements BlazeParameters {
    protected static final Map<Integer, Quintet<Double, Double, Double, Double, Double>> PARAMETERS;
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

                PARAMETERS.put(Blaze.indexToOrder(index), Tuple.of(alpha, delta, epsilon, theta, lambdaC));
            }

        } catch (Exception exception) {
            Log.error("An error occurred while reading blaze parameters", exception);
        }
    }

    @Override
    public double getCentralWavelength(int order) {
        return PARAMETERS.get(order).e;
    }

    @Override
    public double getAlpha(int order) {
        return PARAMETERS.get(order).a;
    }

    @Override
    public BiFunction<Double, Double, Double> getValueFunction(Blaze blaze) {
        Quintet<Double, Double, Double, Double, Double> params = PARAMETERS.get(blaze.getOrder());
        double delta = params.b;
        double epsilon = params.c;

        return (lambda, lambdaC) -> {
            double l = lambdaC - epsilon - lambda;
            double beta = -delta * 1e-6 * l * l + blaze.getAlpha();
            double X = blaze.getOrder() * (1 - lambdaC / lambda);

            double argument = PI * beta * X;
            double fraction = sin(argument) / argument;

            return fraction * fraction;
        };
    }

    @Override
    public String label() {
        return "Parabola";
    }
}
