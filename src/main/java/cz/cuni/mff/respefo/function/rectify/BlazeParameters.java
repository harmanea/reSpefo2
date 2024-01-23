package cz.cuni.mff.respefo.function.rectify;

import java.util.function.BiFunction;

public interface BlazeParameters {
    double getCentralWavelength(int order);
    double getAlpha(int order);

    BiFunction<Double, Double, Double> getValueFunction(Blaze blaze);

    String label();
}
