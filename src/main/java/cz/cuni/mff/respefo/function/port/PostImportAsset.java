package cz.cuni.mff.respefo.function.port;

import cz.cuni.mff.respefo.format.asset.FunctionAsset;
import cz.cuni.mff.respefo.util.collections.XYSeries;

import java.util.Arrays;

public class PostImportAsset implements FunctionAsset {

    private double replacementForNans;

    private PostImportAsset() {
        // default empty constructor
    }

    public PostImportAsset(double replacementForNans) {
        this.replacementForNans = replacementForNans;
    }

    @Override
    public XYSeries process(XYSeries series) {
        double[] newYSeries = Arrays.stream(series.getYSeries())
                                    .map(y -> Double.isNaN(y) ? replacementForNans : y)
                                    .toArray();

        return new XYSeries(series.getXSeries(), newYSeries);
    }
}
