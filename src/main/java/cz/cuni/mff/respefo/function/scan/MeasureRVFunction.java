package cz.cuni.mff.respefo.function.scan;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.component.SpectrumExplorer;
import cz.cuni.mff.respefo.format.Spectrum;
import cz.cuni.mff.respefo.format.XYSeries;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.Serialize;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.asset.common.Measurements;
import cz.cuni.mff.respefo.function.asset.rv.MeasureRVDialog;
import cz.cuni.mff.respefo.function.asset.rv.MeasureRVResults;
import cz.cuni.mff.respefo.function.asset.rv.RVMeasurementController;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.util.DoubleArrayList;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.utils.MathUtils;

import java.io.File;
import java.util.Optional;

import static cz.cuni.mff.respefo.util.Constants.SPEED_OF_LIGHT;

@Fun(name = "Measure RV", fileFilter = SpefoFormatFileFilter.class, group = "Measure")
@Serialize(key = MeasureRVFunction.SERIALIZE_KEY, assetClass = MeasureRVResults.class)
public class MeasureRVFunction implements SingleFileFunction {
    public static final String SERIALIZE_KEY = "rv";

    @Override
    public void execute(File file) {
        Spectrum spectrum;
        try {
            spectrum = Spectrum.open(file);
        } catch (SpefoException exception) {
            Message.error("Couldn't open file", exception);
            return;
        }

        MeasureRVDialog dialog = new MeasureRVDialog();
        if (dialog.openIsNotOk()) {
            return;
        }

        Measurements measurements = new Measurements();
        for (String lstFileName : dialog.getMeasurementFileNames()) {
            measurements.loadMeasurements(lstFileName, false);
        }
        for (String lstFileName : dialog.getCorrectionFileNames()) {
            measurements.loadMeasurements(lstFileName, true);
        }

        XYSeries series = spectrum.getProcessedSeries();
        measurements.removeInvalid(series.getXSeries());

        if (measurements.isEmpty()) {
            Message.warning("No valid measurements found in the selected .stl files.");
            return;
        }

        double deltaRV = ((series.getX(1) - series.getX(0)) * SPEED_OF_LIGHT) / (series.getX(0) * 3);
        series = transformToEquidistant(series, deltaRV);

        new RVMeasurementController(series, deltaRV)
                .measure(measurements, results -> saveResults(spectrum, results));
    }

    private static XYSeries transformToEquidistant(XYSeries series, double deltaRV) {
        DoubleArrayList xList = new DoubleArrayList();
        xList.add(series.getX(0));
        while (xList.get(xList.size() - 1) < series.getLastX()) {
            xList.add(xList.get(xList.size() - 1) * (1 + deltaRV / SPEED_OF_LIGHT));
        }

        double[] newXSeries = xList.toArray();
        double[] newYSeries = MathUtils.intep(series.getXSeries(), series.getYSeries(), newXSeries);

        return new XYSeries(newXSeries, newYSeries);
    }

    private static void saveResults(Spectrum spectrum, MeasureRVResults results) {
        if (results.isEmpty()) {
            return;
        }

        Optional<MeasureRVResults> oldResults = spectrum.getFunctionAsset(SERIALIZE_KEY, MeasureRVResults.class);
        if (oldResults.isPresent()
                && Message.question("Measurements of this type is already saved in this file.\n\nDo you want to append to it?")) {

            oldResults.get().append(results);

        } else {
            spectrum.putFunctionAsset(SERIALIZE_KEY, results);
        }

        try {
            spectrum.save();
            SpectrumExplorer.getDefault().refresh();
            Message.info("Measurements saved successfully.");

        } catch (SpefoException exception) {
            Message.error("Spectrum file couldn't be saved.", exception);
        }
    }
}
