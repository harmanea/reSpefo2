package cz.cuni.mff.respefo.function.dir;

import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.ProjectFunction;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.spectrum.asset.AppendableFunctionAsset;
import cz.cuni.mff.respefo.spectrum.asset.FunctionAsset;
import cz.cuni.mff.respefo.util.FileDialogs;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.Progress;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Fun(name = "Import Measurements", fileFilter = SpefoFormatFileFilter.class)
public class ImportMeasurementsFunction implements ProjectFunction {

    @Override
    public void execute(List<File> files) {
        Optional<String> importFromDirectory = FileDialogs.directoryDialog(false);
        if (!importFromDirectory.isPresent()) {
            return;
        }

        List<File> importFiles = listSpectrumFiles(importFromDirectory.get());
        if (importFiles == null) {
            Message.warning("Couldn't read import files");
            return;
        }

        Progress.withProgressTracking(p -> {
            p.refresh("Importing measurements", files.size());

            int[] counter = new int[]{0, 0};
            for (File toFile : files) {
                importFiles.stream()
                        .filter(fromFile -> toFile.getName().equals(fromFile.getName()))
                        .findFirst()
                        .ifPresent(fromFile -> importMeasurements(fromFile, toFile, counter));
                p.step();
            }

            return counter;
        }, counter -> {
            String message = String.format("Measurements imported:\n\nUpdated: %d\nFailed: %d\nUnchanged: %d",
                    counter[0], counter[1], files.size() - counter[0] - counter[1]);

            Message.info(message);
        });
    }

    private List<File> listSpectrumFiles(String directory) {
        File[] files = new File(directory).listFiles();
        if (files == null) {
            return null;

        } else {
            return Stream.of(files)
                    .filter(file -> !file.isDirectory())
                    .filter(file -> file.getName().endsWith(".spf"))
                    .collect(Collectors.toList());
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void importMeasurements(File from, File to, int[] counter) {
        try {
            Spectrum fromSpectrum = Spectrum.open(from);
            Spectrum toSpectrum = Spectrum.open(to);

            for (Map.Entry<String, FunctionAsset> entry : fromSpectrum.getFunctionAssets()) {
                if (entry.getValue() instanceof AppendableFunctionAsset) {
                    append(toSpectrum, (AppendableFunctionAsset) entry.getValue(), entry.getKey());
                }
            }

            toSpectrum.save();
            counter[0] += 1;

        } catch (SpefoException exception) {
            Log.error("Couldn't process spectrum", exception);
            counter[1] += 1;
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends AppendableFunctionAsset<T>> void append(Spectrum toSpectrum, T fromAsset, String key) {
        Optional<? extends AppendableFunctionAsset<T>> optionalTo = (Optional<? extends AppendableFunctionAsset<T>>) toSpectrum.getFunctionAsset(key, fromAsset.getClass());
        if (optionalTo.isPresent()) {
            optionalTo.get().append(fromAsset);
        } else {
            toSpectrum.putFunctionAsset(key, fromAsset);
        }
    }
}
