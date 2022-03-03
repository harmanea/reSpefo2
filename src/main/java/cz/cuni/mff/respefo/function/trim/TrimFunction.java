package cz.cuni.mff.respefo.function.trim;

import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.Serialize;
import cz.cuni.mff.respefo.function.SpectrumFunction;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.function.open.OpenFunction;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.util.Message;

// TODO: Is this function even used?

@Fun(name = "Trim", fileFilter = SpefoFormatFileFilter.class, group = "Preprocessing")
@Serialize(key = TrimFunction.SERIALIZE_KEY, assetClass = TrimAsset.class)
public class TrimFunction extends SpectrumFunction {
    public static final String SERIALIZE_KEY = "trim";

    @Override
    public void execute(Spectrum spectrum) {
        TrimAsset asset = spectrum.getFunctionAsset (SERIALIZE_KEY, TrimAsset.class).orElse(new TrimAsset());

        TrimDialog dialog = new TrimDialog();
        dialog.setMin(asset.getMin());
        dialog.setMax(asset.getMax());
        if (dialog.openIsNotOk()) {
            return;
        }

        asset.setMin(dialog.getMin());
        asset.setMax(dialog.getMax());

        if (asset.getMin() == Double.NEGATIVE_INFINITY && asset.getMax() == Double.POSITIVE_INFINITY) {
            spectrum.removeFunctionAsset(SERIALIZE_KEY);
        } else {
            spectrum.putFunctionAsset(SERIALIZE_KEY, asset);
        }

        try {
            spectrum.save();
            OpenFunction.displaySpectrum(spectrum);
            Message.info("Trimmed spectrum saved successfully.");

        } catch (SpefoException e) {
            Message.error("Couldn't save file", e);
        }
    }
}
