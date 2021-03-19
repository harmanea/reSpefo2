package cz.cuni.mff.respefo.format.asset;

import cz.cuni.mff.respefo.util.collections.XYSeries;

public interface FunctionAsset {
    default XYSeries process(XYSeries series) {
        return series;
    }
}
