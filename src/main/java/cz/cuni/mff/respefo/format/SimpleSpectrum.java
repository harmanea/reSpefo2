package cz.cuni.mff.respefo.format;

import cz.cuni.mff.respefo.util.collections.XYSeries;

import java.util.Arrays;

import static cz.cuni.mff.respefo.util.Constants.SPEED_OF_LIGHT;
import static java.lang.Double.isNaN;

public class SimpleSpectrum extends Spectrum {
    public static final int FORMAT = 1;

    private XYSeries series;

    private SimpleSpectrum() {
        super();  // default empty constructor
    }

    public SimpleSpectrum(XYSeries series) {
        super(FORMAT);

        this.series = series;
    }

    @Override
    public void updateRvCorrection(double newRvCorrection)  {
        double diff = newRvCorrection - (isNaN(rvCorrection) ? 0 : rvCorrection);
        double[] updatedXSeries = Arrays.stream(series.getXSeries())
                .map(value -> value + diff * (value / SPEED_OF_LIGHT))
                .toArray();
        series.updateXSeries(updatedXSeries);
        setRvCorrection(newRvCorrection);
    }

    @Override
    public XYSeries getSeries() {
        return series;
    }
}
