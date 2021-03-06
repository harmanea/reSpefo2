package cz.cuni.mff.respefo.function.port;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.component.FileExplorer;
import cz.cuni.mff.respefo.dialog.OverwriteDialog;
import cz.cuni.mff.respefo.format.FormatManager;
import cz.cuni.mff.respefo.format.Spectrum;
import cz.cuni.mff.respefo.format.UnknownFileFormatException;
import cz.cuni.mff.respefo.format.formats.ExportFileFormat;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.MultiFileFunction;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.util.FileDialogs;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.Progress;
import cz.cuni.mff.respefo.util.utils.FileUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static cz.cuni.mff.respefo.dialog.OverwriteDialog.*;
import static cz.cuni.mff.respefo.util.FileType.COMPATIBLE_SPECTRUM_FILES;
import static cz.cuni.mff.respefo.util.utils.FileUtils.filesListToString;
import static cz.cuni.mff.respefo.util.utils.FileUtils.stripFileExtension;
import static org.eclipse.swt.SWT.CANCEL;

@Fun(name = "Export", fileFilter = SpefoFormatFileFilter.class)
public class ExportFunction implements SingleFileFunction, MultiFileFunction {

    @Override
    public void execute(File spectrumFile) {
        Spectrum spectrum;
        try {
            spectrum = Spectrum.open(spectrumFile);
        } catch (SpefoException exception) {
            Message.error("An error occurred while opening file.", exception);
            return;
        }

        String fileName = FileDialogs.saveFileDialog(COMPATIBLE_SPECTRUM_FILES, stripFileExtension(spectrumFile.getPath()));
        if (fileName != null) {
            try {
                if (exportTo(spectrum, fileName)) {
                    Message.info("File exported successfully.");
                    FileExplorer.getDefault().refresh();
                }
            } catch (SpefoException exception) {
                Message.error("An error occurred while exporting file.", exception);
            }
        }
    }

    private static boolean exportTo(Spectrum spectrum, String fileName) throws SpefoException {
        if (Files.exists(Paths.get(fileName))) {
            OverwriteDialog dialog = new OverwriteDialog(new File(fileName));
            int response = dialog.open();
            if (response == RENAME) {
                return exportTo(spectrum, Paths.get(fileName).resolveSibling(dialog.getNewName()).toString());
            } else if (response == CANCEL || response == SKIP) {
                return false;
            } /* else response == REPLACE */
        }

        List<ExportFileFormat> fileFormats = FormatManager.getExportFileFormats(fileName);

        FileFormatSelectionDialog<ExportFileFormat> dialog = new FileFormatSelectionDialog<>(fileFormats, "Export");
        if (dialog.openIsOk()) {
            dialog.getFileFormat().exportTo(spectrum, fileName);
        }
        return true;
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

        FileFormatSelectionDialog<ExportFileFormat> formatDialog = new FileFormatSelectionDialog<>(fileFormats, "Export");
        if (formatDialog.openIsNotOk()) {
            return;
        }
        ExportFileFormat exportFormat = formatDialog.getFileFormat();

        Progress.withProgressTracking(p -> {
            p.refresh("Exporting files", spectrumFiles.size() * 2);
            List<File> failedFiles = new ArrayList<>();

            List<Spectrum> spectra = new ArrayList<>();
            for (File spectrumFile : spectrumFiles) {
                try {
                    Spectrum spectrum = Spectrum.open(spectrumFile);
                    spectra.add(spectrum);
                } catch (SpefoException exception) {
                    Log.error("An error occured while opening file " + spectrumFile.toPath(), exception);
                    failedFiles.add(spectrumFile);
                } finally {
                    p.step();
                }
            }

            int applyToAllAction = 0;
            for (Spectrum spectrum : spectra) {
                try {
                    String fileName = FileUtils.replaceFileExtension(spectrum.getFile().getPath(), fileExtension);

                    int response = exportTo(p, spectrum, fileName, exportFormat, applyToAllAction);
                    if (response == CANCEL) {
                        break;
                    } else if (response < 0) {
                        applyToAllAction = response;
                    }

                } catch (SpefoException exception) {
                    Log.error("An error occured while exporting file " + spectrum.getFile().toPath(), exception);
                    failedFiles.add(spectrum.getFile());
                } finally {
                    p.step();
                }
            }

            return failedFiles;
        }, failedFiles -> {
            FileExplorer.getDefault().refresh();
            if (!failedFiles.isEmpty()) {
                Message.warning("Some files failed to import:\n\n" + filesListToString(failedFiles));
            } else {
                Message.info("All files exported successfully.");
            }
        });
    }

    private static int exportTo(Progress p, Spectrum spectrum, String fileName, ExportFileFormat exportFormat, int applyToAllAction) throws SpefoException {
        if (Files.exists(Paths.get(fileName))) {
            if (applyToAllAction < 0) {
                if (applyToAllAction == REPLACE) {
                    exportFormat.exportTo(spectrum, fileName);
                } /* else applyToAllAction == SKIP */
                return applyToAllAction;
            }

            Progress.DialogAndReturnCode<OverwriteDialog> overwriteDialogAndReturnCode = p.syncOpenDialog(() -> new OverwriteDialog(new File(fileName)));
            int response = overwriteDialogAndReturnCode.getReturnValue();
            OverwriteDialog dialog = overwriteDialogAndReturnCode.getDialog();

            if (response == REPLACE) {
                exportFormat.exportTo(spectrum, fileName);
                if (dialog.applyToAll()) {
                    return REPLACE;
                }
            } else if (response == RENAME) {
                return exportTo(p, spectrum, Paths.get(fileName).resolveSibling(dialog.getNewName()).toString(), exportFormat, applyToAllAction);

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
}
