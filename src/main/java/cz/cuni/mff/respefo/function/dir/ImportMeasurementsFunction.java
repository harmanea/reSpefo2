package cz.cuni.mff.respefo.function.dir;

import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.filter.DirectoryFileFilter;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.spectrum.asset.AppendableFunctionAsset;
import cz.cuni.mff.respefo.spectrum.asset.FunctionAsset;
import cz.cuni.mff.respefo.util.FileDialogs;
import cz.cuni.mff.respefo.util.Message;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Fun(name = "Import Measurements", fileFilter = DirectoryFileFilter.class)
public class ImportMeasurementsFunction implements SingleFileFunction {

    @Override
    public void execute(File importToDirectory) {
        String importFromDirectory = FileDialogs.directoryDialog(false);
        if (importFromDirectory == null) {
            return;
        }

        File[] files = importToDirectory.listFiles(new SpefoFormatFileFilter());

        try (Stream<Path> stream = Files.list(Paths.get(importFromDirectory))) {
            stream.filter(path -> !Files.isDirectory(path))
                    .filter(path -> path.toString().endsWith(".spf"))
                    .map(this::openFile)
                    .filter(Objects::nonNull)
                    .forEach(spectrum -> append(spectrum, files));

            Message.info("Measurements imported successfully");
        } catch (IOException exception) {
            Message.error("Couldn't import measurements", exception);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void append(Spectrum fromSpectrum, File[] toFiles) {
        Optional<File> optionalFile = Arrays.stream(toFiles)
                .filter(file -> fromSpectrum.getFile().getName().equals(file.getName()))
                .findFirst();

        if (optionalFile.isPresent()) {
            try {
                Spectrum toSpectrum = Spectrum.open(optionalFile.get());

                for (Map.Entry<String, FunctionAsset> entry : fromSpectrum.getFunctionAssets()) {
                    if (entry.getValue() instanceof AppendableFunctionAsset) {
                        append(toSpectrum, (AppendableFunctionAsset) entry.getValue(), entry.getKey());
                    }
                }

                toSpectrum.save();

            } catch (SpefoException exception) {
                Log.error("Couldn't import measurements", exception);
            }
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

    private Spectrum openFile(Path path) {
        try {
            return Spectrum.open(path.toFile());

        } catch (SpefoException e) {
            return null;
        }
    }
}
