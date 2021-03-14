package cz.cuni.mff.respefo.function.scan;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.component.SpectrumExplorer;
import cz.cuni.mff.respefo.format.Spectrum;
import cz.cuni.mff.respefo.format.XYSeries;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.Serialize;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.asset.common.Measurements;
import cz.cuni.mff.respefo.function.asset.ew.EWMeasurementController;
import cz.cuni.mff.respefo.function.asset.ew.MeasureEWDialog;
import cz.cuni.mff.respefo.function.asset.ew.MeasureEWResults;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.util.Message;

import java.io.File;
import java.util.Optional;

@Fun(name = "Measure EW", fileFilter = SpefoFormatFileFilter.class, group = "Measure")
@Serialize(key = MeasureEWFunction.SERIALIZE_KEY, assetClass = MeasureEWResults.class)
public class MeasureEWFunction implements SingleFileFunction {
    public static final String SERIALIZE_KEY = "ew";

    @Override
    public void execute(File file) {
        Spectrum spectrum;
        try {
            spectrum = Spectrum.open(file);
        } catch (SpefoException exception) {
            Message.error("Couldn't open file", exception);
            return;
        }

        MeasureEWDialog dialog = new MeasureEWDialog();
        if (dialog.openIsNotOk()) {
            return;
        }

        Measurements measurements = new Measurements();
        for (String lstFileName : dialog.getFileNames()) {
            measurements.loadMeasurements(lstFileName, false);
        }

        XYSeries data = spectrum.getProcessedSeries();
        measurements.removeInvalid(data.getXSeries());

        if (measurements.isEmpty()) {
            Message.warning("No valid measurements found in the selected .stl files.");
            return;
        }

        new EWMeasurementController(data)
                .measure(measurements, results -> saveResults(spectrum, results));
    }

    private static void saveResults(Spectrum spectrum, MeasureEWResults results) {
        Optional<MeasureEWResults> oldResults = spectrum.getFunctionAsset(MeasureEWFunction.SERIALIZE_KEY, MeasureEWResults.class);
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
