package cz.cuni.mff.respefo.function.port;

import cz.cuni.mff.respefo.component.FileExplorer;
import cz.cuni.mff.respefo.dialog.OverwriteDialog;
import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.MultiFileFunction;
import cz.cuni.mff.respefo.function.SpectrumFunction;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.function.rv.MeasureRVFunction;
import cz.cuni.mff.respefo.function.rv.MeasureRVResults;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.spectrum.asset.FunctionAsset;
import cz.cuni.mff.respefo.spectrum.port.ExportFileFormat;
import cz.cuni.mff.respefo.spectrum.port.FormatManager;
import cz.cuni.mff.respefo.spectrum.port.UnknownFileFormatException;
import cz.cuni.mff.respefo.util.FileDialogs;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.Progress;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.utils.FileUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cz.cuni.mff.respefo.dialog.OverwriteDialog.*;
import static cz.cuni.mff.respefo.spectrum.port.fits.ExportFitsFormat.LOWER_PRECISION_ASSET_KEY;
import static cz.cuni.mff.respefo.util.Constants.SPEED_OF_LIGHT;
import static cz.cuni.mff.respefo.util.FileType.COMPATIBLE_SPECTRUM_FILES;
import static cz.cuni.mff.respefo.util.utils.FileUtils.filesListToString;
import static cz.cuni.mff.respefo.util.utils.FileUtils.stripFileExtension;
import static cz.cuni.mff.respefo.util.utils.MathUtils.isNotNaN;
import static org.eclipse.swt.SWT.CANCEL;

@Fun(name = "Export", fileFilter = SpefoFormatFileFilter.class)
public class ExportFunction extends SpectrumFunction implements MultiFileFunction {

    @Override
    public void execute(Spectrum spectrum) {
        FileDialogs.saveFileDialog(COMPATIBLE_SPECTRUM_FILES, stripFileExtension(spectrum.getFile().getPath()))
                .ifPresent(fileName -> {
                    try {
                        if (exportTo(spectrum, fileName)) {
                            Message.info("File exported successfully.");
                            FileExplorer.getDefault().refresh();
                        }
                    } catch (SpefoException exception) {
                        Message.error("An error occurred while exporting file.", exception);
                    }
                });
    }

    private static boolean exportTo(Spectrum spectrum, String fileName) throws SpefoException {
        Path path = Paths.get(fileName);
        if (Files.exists(path)) {
            OverwriteDialog dialog = new OverwriteDialog(path);
            int response = dialog.open();
            if (response == RENAME) {
                return exportTo(spectrum, path.resolveSibling(dialog.getNewName()).toString());
            } else if (response == CANCEL || response == SKIP) {
                return false;
            } /* else response == REPLACE */
        }

        List<ExportFileFormat> fileFormats = FormatManager.getExportFileFormats(fileName);
        ExportDialog exportDialog = new ExportDialog(fileFormats);
        if (exportDialog.openIsOk()) {
            ExportDialog.Options options = exportDialog.getOptions();
            if (options.applyZeroPointRvCorrection) {
                applyZeroPointRvCorrection(spectrum);
            }
            if (options.useLowerPrecision) {
                addFitsExportLowerPrecisionAsset(spectrum);
            }
            options.format.exportTo(spectrum, fileName);
            return true;
        }

        return false;
    }

    @Override
    public void execute(List<File> spectrumFiles) {
        FileExtensionDialog dialog = new FileExtensionDialog();
        if (dialog.openIsNotOk()) {
            return;
        }
        String fileExtension = dialog.getFileExtension();

        List<ExportFileFormat> fileFormats;
        try {
            fileFormats = FormatManager.getExportFileFormats("file." + fileExtension);
        } catch (UnknownFileFormatException exception) {
            Message.error("Unknown file extension", exception);
            return;
        }

        ExportDialog exportDialog = new ExportDialog(fileFormats);
        if (exportDialog.openIsNotOk()) {
            return;
        }
        ExportDialog.Options options = exportDialog.getOptions();

        Progress.withProgressTracking(p -> {
            p.refresh("Exporting files", spectrumFiles.size() * 2);
            List<File> failedFiles = new ArrayList<>();

            List<Spectrum> spectra = new ArrayList<>();
            for (File spectrumFile : spectrumFiles) {
                try {
                    Spectrum spectrum = Spectrum.open(spectrumFile);
                    if (options.applyZeroPointRvCorrection) {
                        applyZeroPointRvCorrection(spectrum);
                    }
                    if (options.useLowerPrecision) {
                        addFitsExportLowerPrecisionAsset(spectrum);
                    }
                    spectra.add(spectrum);
                } catch (SpefoException exception) {
                    Log.error("An error occurred while opening file " + spectrumFile.toPath(), exception);
                    failedFiles.add(spectrumFile);
                } finally {
                    p.step();
                }
            }

            int applyToAllAction = 0;
            for (Spectrum spectrum : spectra) {
                try {
                    String fileName = FileUtils.replaceFileExtension(spectrum.getFile().getPath(), fileExtension);

                    int response = exportTo(p, spectrum, fileName, options.format, applyToAllAction);
                    if (response == CANCEL) {
                        break;
                    } else if (response < 0) {
                        applyToAllAction = response;
                    }

                } catch (SpefoException exception) {
                    Log.error("An error occurred while exporting file " + spectrum.getFile().toPath(), exception);
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

    private static int exportTo(Progress p, Spectrum spectrum, String fileName, ExportFileFormat exportFormat, int applyToAllAction) throws SpefoException {
        Path path = Paths.get(fileName);
        if (Files.exists(path)) {
            if (applyToAllAction < 0) {
                if (applyToAllAction == REPLACE) {
                    exportFormat.exportTo(spectrum, fileName);
                } /* else applyToAllAction == SKIP */
                return applyToAllAction;
            }

            Progress.DialogAndReturnCode<OverwriteDialog> overwriteDialogAndReturnCode = p.syncOpenDialog(() -> new OverwriteDialog(path));
            int response = overwriteDialogAndReturnCode.getReturnValue();
            OverwriteDialog dialog = overwriteDialogAndReturnCode.getDialog();

            if (response == REPLACE) {
                exportFormat.exportTo(spectrum, fileName);
                if (dialog.applyToAll()) {
                    return REPLACE;
                }
            } else if (response == RENAME) {
                return exportTo(p, spectrum, path.resolveSibling(dialog.getNewName()).toString(), exportFormat, applyToAllAction);

            } else if (response == SKIP) {
                if (overwriteDialogAndReturnCode.getDialog().applyToAll()) {
                    return SKIP;
                }
            }  else /* response == CANCEL */ {
                return CANCEL;
            }
        } else {
            exportFormat.exportTo(spectrum, fileName);
        }

        return applyToAllAction;
    }

    private static void applyZeroPointRvCorrection(Spectrum spectrum) {
        spectrum.getFunctionAsset(MeasureRVFunction.SERIALIZE_KEY, MeasureRVResults.class)
                .ifPresent(results -> {
                    double measuredRvCorrection = results.getRvOfCategory("corr");
                    if (isNotNaN(measuredRvCorrection)) {
                        addZeroPointCorrectionAsset(spectrum, measuredRvCorrection);
                    }
                });
    }

    private static void addZeroPointCorrectionAsset(Spectrum spectrum, double measuredRvCorrection) {
        double headerRvCorrection = spectrum.getRvCorrection();
        double diff = headerRvCorrection - measuredRvCorrection;

        spectrum.setRvCorrection(headerRvCorrection + diff);

        spectrum.putFunctionAsset("zero point correction", new FunctionAsset() {  // Key is irrelevant as it never gets saved
            @Override
            public XYSeries process(XYSeries series) {
                double[] updatedXSeries = Arrays.stream(series.getXSeries())
                        .map(value -> value + diff * (value / SPEED_OF_LIGHT))
                        .toArray();
                series.updateXSeries(updatedXSeries);

                return series;
            }
        });
    }

    private static void addFitsExportLowerPrecisionAsset(Spectrum spectrum) {
        spectrum.putFunctionAsset(LOWER_PRECISION_ASSET_KEY, new FunctionAsset() {});  // This asset serves only as a marker
    }
}
