package cz.cuni.mff.respefo.spectrum.format;

import cz.cuni.mff.respefo.function.rectify.RectifyAsset;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.util.collections.XYSeries;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static cz.cuni.mff.respefo.util.Constants.SPEED_OF_LIGHT;
import static cz.cuni.mff.respefo.util.utils.ArrayUtils.divideArrayValues;
import static java.lang.Double.isNaN;

public class EchelleSpectrum extends Spectrum {
    public static final int FORMAT = 2;

    private XYSeries[] series;

    private Map<Integer, RectifyAsset> rectifyAssets;

    private EchelleSpectrum() {
        super();  // default empty constructor
    }

    public EchelleSpectrum(XYSeries[] series) {
        super(FORMAT);

        this.series = series;
        rectifyAssets = new LinkedHashMap<>();
    }

    public XYSeries[] getOriginalSeries() {
        return series;
    }

    public Map<Integer, RectifyAsset> getRectifyAssets() {
        return rectifyAssets;
    }

    public void setRectifyAssets(Map<Integer, RectifyAsset> rectifyAssets) {
        this.rectifyAssets = rectifyAssets;
    }

    @Override
    public void updateRvCorrection(double newRvCorrection) {
        double diff = newRvCorrection - (isNaN(rvCorrection) ? 0 : rvCorrection);

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

        if (rectifyAssets.isEmpty()) {
            return XYSeries.merge(series);

        } else {
            XYSeries[] rectifiedSeries = rectifyAssets.entrySet().stream()
                    .map(entry -> {
                        XYSeries xySeries = series[entry.getKey()];
                        RectifyAsset asset = entry.getValue();

                        return new XYSeries(
                                xySeries.getXSeries(),
                                divideArrayValues(xySeries.getYSeries(), asset.getIntepData(xySeries.getXSeries()))
                        );
                    })
                    .toArray(XYSeries[]::new);

            return XYSeries.merge(rectifiedSeries);
        }
    }
}
