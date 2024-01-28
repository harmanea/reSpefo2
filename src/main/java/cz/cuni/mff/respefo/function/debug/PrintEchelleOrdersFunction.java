package cz.cuni.mff.respefo.function.debug;

import cz.cuni.mff.respefo.component.FileExplorer;
import cz.cuni.mff.respefo.dialog.OverwriteDialog;
import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.MultiFileFunction;
import cz.cuni.mff.respefo.function.SpectrumFunction;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.spectrum.format.EchelleSpectrum;
import cz.cuni.mff.respefo.util.FileDialogs;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.Progress;
import cz.cuni.mff.respefo.util.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static cz.cuni.mff.respefo.dialog.OverwriteDialog.*;
import static cz.cuni.mff.respefo.util.FileType.ASCII_FILES;
import static cz.cuni.mff.respefo.util.utils.FileUtils.filesListToString;
import static cz.cuni.mff.respefo.util.utils.FileUtils.stripFileExtension;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.range;
import static org.eclipse.swt.SWT.CANCEL;

@Fun(name = "Print Echelle Orders", fileFilter = SpefoFormatFileFilter.class, group = "Debug")
public class PrintEchelleOrdersFunction extends SpectrumFunction implements MultiFileFunction {

    @Override
    protected void execute(Spectrum spectrum) {
        try {
            EchelleSpectrum echelleSpectrum = (EchelleSpectrum) spectrum;
            execute(echelleSpectrum);
        } catch (ClassCastException exception) {
            Message.error("Not an echelle spectrum", exception);
        }
    }

    private void execute(EchelleSpectrum spectrum) {
        FileDialogs.saveFileDialog(ASCII_FILES, stripFileExtension(spectrum.getFile().getPath()))
                .ifPresent(fileName -> {
                    try {
                        if (printTo(spectrum, fileName)) {
                            Message.info("Echelle orders printed successfully.");
                            FileExplorer.getDefault().refresh();
                        }

                    } catch (SpefoException exception) {
                        Message.error("An error occurred while printing echelle orders.", exception);
                    }
                });
    }

    private static boolean printTo(EchelleSpectrum spectrum, String fileName) throws SpefoException {
        Path path = Paths.get(fileName);
        if (Files.exists(path)) {
            OverwriteDialog dialog = new OverwriteDialog(path);
            int response = dialog.open();
            if (response == RENAME) {
                return printTo(spectrum, path.resolveSibling(dialog.getNewName()).toString());
            } else if (response == CANCEL || response == SKIP) {
                return false;
            } /* else response == REPLACE */
        }

        printOrders(spectrum, fileName);
        return true;
    }

    @Override
    public void execute(List<File> spectrumFiles) {
        Progress.withProgressTracking(p -> {
            p.refresh("Printing echelle orders", spectrumFiles.size() * 2);
            List<File> failedFiles = new ArrayList<>();

            List<EchelleSpectrum> spectra = new ArrayList<>();
            for (File spectrumFile : spectrumFiles) {
                try {
                    EchelleSpectrum spectrum = (EchelleSpectrum) Spectrum.open(spectrumFile);
                    spectra.add(spectrum);
                } catch (SpefoException | ClassCastException exception) {
                    Log.error("An error occurred while opening echelle spectrum file " + spectrumFile.toPath(), exception);
                    failedFiles.add(spectrumFile);
                } finally {
                    p.step();
                }
            }

            int applyToAllAction = 0;
            for (EchelleSpectrum spectrum : spectra) {
                try {
                    String fileName = FileUtils.replaceFileExtension(spectrum.getFile().getPath(), ".txt");

                    int response = printTo(p, spectrum, fileName, applyToAllAction);
                    if (response == CANCEL) {
                        break;
                    } else if (response < 0) {
                        applyToAllAction = response;
                    }

                } catch (SpefoException exception) {
                    Log.error("An error occurred while printing echelle orders of file " + spectrum.getFile().toPath(), exception);
                    failedFiles.add(spectrum.getFile());
                } finally {
                    p.step();
                }
            }

            return failedFiles;
        }, failedFiles -> {
            FileExplorer.getDefault().refresh();
            if (!failedFiles.isEmpty()) {
                Message.warning("Some files failed to export:\n\n" + filesListToString(failedFiles));
            } else {
                Message.info("All files exported successfully.");
            }
        });
    }

    private static int printTo(Progress p, EchelleSpectrum spectrum, String fileName, int applyToAllAction) throws SpefoException {
        Path path = Paths.get(fileName);
        if (Files.exists(path)) {
            if (applyToAllAction < 0) {
                if (applyToAllAction == REPLACE) {
                    printOrders(spectrum, fileName);
                } /* else applyToAllAction == SKIP */
                return applyToAllAction;
            }

            Progress.DialogAndReturnCode<OverwriteDialog> overwriteDialogAndReturnCode = p.syncOpenDialog(() -> new OverwriteDialog(path));
            int response = overwriteDialogAndReturnCode.getReturnValue();
            OverwriteDialog dialog = overwriteDialogAndReturnCode.getDialog();

            if (response == REPLACE) {
                printOrders(spectrum, fileName);
                if (dialog.applyToAll()) {
                    return REPLACE;
                }
            } else if (response == RENAME) {
                return printTo(p, spectrum, path.resolveSibling(dialog.getNewName()).toString(), applyToAllAction);

            } else if (response == SKIP) {
                if (overwriteDialogAndReturnCode.getDialog().applyToAll()) {
                    return SKIP;
                }
            }  else /* response == CANCEL */ {
                return CANCEL;
            }
        } else {
            printOrders(spectrum, fileName);
        }

        return applyToAllAction;
    }

    private static void printOrders(EchelleSpectrum spectrum, String fileName) throws SpefoException {
        try {
            String text = stream(spectrum.getOriginalSeries())
                    .parallel()
                    .map(series -> range(0, series.getLength())
                            .mapToObj(i -> format("%.04f  %.04f", series.getX(i), series.getY(i)))
                            .collect(joining("\n")))
                    .collect(joining("\n\n"));

            Files.write(Paths.get(fileName), text.getBytes());

        } catch (IOException exception) {
            throw new SpefoException("Error while writing to file", exception);
        }
    }
}
