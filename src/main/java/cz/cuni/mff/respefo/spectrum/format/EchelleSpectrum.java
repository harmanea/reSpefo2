package cz.cuni.mff.respefo.spectrum.format;

import cz.cuni.mff.respefo.function.rectify.RectifyAsset;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.util.collections.XYSeries;

import java.util.Arrays;

import static cz.cuni.mff.respefo.util.Constants.SPEED_OF_LIGHT;
import static cz.cuni.mff.respefo.util.utils.ArrayUtils.divideArrayValues;

public class EchelleSpectrum extends Spectrum {
    public static final int FORMAT = 2;

    private XYSeries[] series;
    private RectifyAsset[] rectifyAssets;

    // Provide defaults for backwards compatibility with older Chiron spectra
    private int a = 125;
    private int b = -1;

    private EchelleSpectrum() {
        super();  // default empty constructor
    }

    public EchelleSpectrum(XYSeries[] series) {
        super(FORMAT);
        this.series = series;
    }

    public XYSeries[] getOriginalSeries() {
        return series;
    }

    public boolean isRectified() {
        return rectifyAssets != null;
    }

    public RectifyAsset[] getRectifyAssets() {
        return rectifyAssets;
    }

    public void setRectifyAssets(RectifyAsset[] rectifyAssets) {
        this.rectifyAssets = rectifyAssets;
    }

    public void setAB(int a, int b) {
        this.a = a;
        this.b = b;
    }

    public int indexToOrder(int index) {
        return a + b * index;
    }

    @Override
    public void updateRvCorrection(double newRvCorrection) {
        double diff = newRvCorrection - rvCorrection;

        for (XYSeries xySeries : series) {
            double[] updatedXSeries = Arrays.stream(xySeries.getXSeries())
                    .map(value -> value + diff * (value / SPEED_OF_LIGHT))
                    .toArray();
            xySeries.updateXSeries(updatedXSeries);
        }

        setRvCorrection(newRvCorrection);
    }

    @Override
    public XYSeries getSeries() {
        // k-way mergesort, could be cached if necessary

        if (isRectified()) {
            XYSeries[] rectifiedSeries = new XYSeries[series.length];
            for (int i = 0; i < series.length; i++) {
                XYSeries xySeries = series[i];
                RectifyAsset asset = rectifyAssets[i];

                rectifiedSeries[i] = new XYSeries(
                        xySeries.getXSeries(),
                        divideArrayValues(xySeries.getYSeries(), asset.getIntepData(xySeries.getXSeries()))
                );
            }

            return XYSeries.merge(rectifiedSeries);

        } else {
            return XYSeries.merge(series);
        }
    }
}
