package cz.cuni.mff.respefo.function.debug;

import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.MultiFileFunction;
import cz.cuni.mff.respefo.function.ew.MeasureEWFunction;
import cz.cuni.mff.respefo.function.ew.MeasureEWResult;
import cz.cuni.mff.respefo.function.ew.MeasureEWResults;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.util.Message;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Fun(name = "Fix EW", fileFilter = SpefoFormatFileFilter.class, group = "Debug")
public class TempFunction implements MultiFileFunction {
    @Override
    public void execute(List<File> files) {
        List<Spectrum> spectra = files.stream().map(this::open).collect(Collectors.toList());

        for (Spectrum spectrum : spectra) {
            Optional<MeasureEWResults> optionalResults = spectrum
                    .getFunctionAsset(MeasureEWFunction.SERIALIZE_KEY, MeasureEWResults.class);

            if (optionalResults.isPresent()) {
                MeasureEWResults results = optionalResults.get();
                MeasureEWResult result = results.getResultForName("Si II 2");

                if (result != null) {
                    results.remove(result);
                    result.setName("Si II 1");
                    results.add(result);
                }

                try {
                    spectrum.save();
                } catch (SpefoException exception) {
                    Message.error("Fail", exception);
                }
            }
        }
    }

    private Spectrum open(File file) {
        try {
            return Spectrum.open(file);
        } catch (SpefoException exception) {
            return null;
        }
    }
}
