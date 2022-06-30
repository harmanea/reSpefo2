package cz.cuni.mff.respefo.function.ew;

import cz.cuni.mff.respefo.component.SpectrumExplorer;
import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.Serialize;
import cz.cuni.mff.respefo.function.SpectrumFunction;
import cz.cuni.mff.respefo.function.common.Measurements;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.collections.XYSeries;

import java.util.Optional;

@Fun(name = "Measure EW", fileFilter = SpefoFormatFileFilter.class, group = "Measure")
@Serialize(key = MeasureEWFunction.SERIALIZE_KEY, assetClass = MeasureEWResults.class)
public class MeasureEWFunction extends SpectrumFunction {
    public static final String SERIALIZE_KEY = "ew";

    @Override
    public void execute(Spectrum spectrum) {
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

        measurements.removeDuplicateNames();

        // TODO: Use the new Async methods instead
        new MeasureEWController(data)
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
            EWResultsFunction.displayResults(spectrum);
            Message.info("Measurements saved successfully.");

        } catch (SpefoException exception) {
            Message.error("Spectrum file couldn't be saved.", exception);
        }
    }
}
