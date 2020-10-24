package cz.cuni.mff.respefo.function.scan;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.component.OverwriteDialog;
import cz.cuni.mff.respefo.format.FormatManager;
import cz.cuni.mff.respefo.format.Spectrum;
import cz.cuni.mff.respefo.format.UnknownFileFormatException;
import cz.cuni.mff.respefo.format.formats.ImportFileFormat;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.Serialize;
import cz.cuni.mff.respefo.function.SingleOrMultiFileFunction;
import cz.cuni.mff.respefo.function.asset.port.FileFormatSelectionDialog;
import cz.cuni.mff.respefo.function.asset.port.PostImportAsset;
import cz.cuni.mff.respefo.function.asset.port.RVCorrectionDialog;
import cz.cuni.mff.respefo.function.filter.CompatibleFormatFileFilter;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.util.FileDialogs;
import cz.cuni.mff.respefo.util.FileType;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.Progress;
import cz.cuni.mff.respefo.util.utils.FileUtils;
import org.eclipse.swt.SWT;

import java.io.File;
import java.util.*;

import static cz.cuni.mff.respefo.component.OverwriteDialog.*;
import static cz.cuni.mff.respefo.util.utils.FileUtils.filesListToString;
import static org.eclipse.swt.SWT.CANCEL;

@Fun(name = "Import", fileFilter = CompatibleFormatFileFilter.class)
@Serialize(key = ImportFunction.SERIALIZE_KEY, assetClass = PostImportAsset.class)
public class ImportFunction implements SingleOrMultiFileFunction {

    public static final String SERIALIZE_KEY = "import";

    @Override
    public void execute(File file) {
        Spectrum spectrum;
        try {
            List<ImportFileFormat> fileFormats = FormatManager.getImportFileFormats(file.getPath());

            FileFormatSelectionDialog<ImportFileFormat> dialog = new FileFormatSelectionDialog<>(fileFormats, "Import");
            if (dialog.openIsOk()) {
                spectrum = dialog.getFileFormat().importFrom(file.getPath());
            } else {
                return;
            }

        } catch (SpefoException exception) {
            Message.error("An error occurred while importing file.", exception);
            return;
        }

        // Post-import check, this is a temporary solution
        checkForNaNs(spectrum);
        checkRVCorrection(spectrum);

        String fileName = FileDialogs.saveFileDialog(FileType.SPECTRUM, FileUtils.replaceFileExtension(file.getPath(), "spf"));
        if (fileName != null) {
            try {
                if (saveAs(spectrum, new File(fileName))) {
                    ComponentManager.getFileExplorer().refresh();
                }
            } catch (SpefoException exception) {
                Message.error("An error occurred while saving file.", exception);
            }
        }
    }

    private boolean saveAs(Spectrum spectrum, File file) throws SpefoException {
        if (file.exists()) {
            OverwriteDialog dialog = new OverwriteDialog(file);
            int response = dialog.open();
            if (response == RENAME) {
                return saveAs(spectrum, file.toPath().resolveSibling(dialog.getNewName()).toFile());
            } else if (response == CANCEL || response == SKIP) {
                return false;
            } /* else response == REPLACE */
        }

        spectrum.saveAs(file);
        return true;
    }

    @Override
    public void execute(List<File> files) {
        Progress.withProgressTracking(p -> {
            p.refresh("Importing files", files.size() * 2);
            List<File> failedFiles = new ArrayList<>();

            Set<ImportFileFormat> applicableFormats = null;
            for (File file : files) {
                try {
                    List<ImportFileFormat> importFormats = FormatManager.getImportFileFormats(file.getPath());
                    if (applicableFormats == null) {
                        applicableFormats = new HashSet<>(importFormats);
                    } else {
                        applicableFormats.retainAll(importFormats);
                    }

                } catch (UnknownFileFormatException exception) {
                    throw new IllegalStateException("There is no import format applicable to the file " + file.toPath(), exception);
                } finally {
                    p.step();
                }
            }

            if (applicableFormats == null || applicableFormats.isEmpty()) {
                throw new IllegalStateException("There is no import format applicable to the whole selection.");
            }

            List<ImportFileFormat> applicableFormatsList = new ArrayList<>(applicableFormats);
            Progress.DialogAndReturnCode<FileFormatSelectionDialog<ImportFileFormat>> dialogAndReturnCode =
                    p.syncOpenDialog(() -> new FileFormatSelectionDialog<>(applicableFormatsList, "Import"));
            if (dialogAndReturnCode.getReturnValue() != SWT.OK) {
                return null;
            }

            int applyToAllAction = 0;
            for (File file : files) {
                try {
                    Spectrum spectrum = dialogAndReturnCode.getDialog().getFileFormat().importFrom(file.getPath());
                    // Post-import check, this is a temporary solution
                    checkForNaNs(spectrum);
                    checkRVCorrection(p, spectrum);

                    String fileName = FileUtils.replaceFileExtension(file.getPath(), "spf");
                    int response = saveAs(p, spectrum, new File(fileName), applyToAllAction);
                    if (response == CANCEL) {
                        break;
                    } else if (response < 0) {
                        applyToAllAction = response;
                    }
                } catch (SpefoException exception) {
                    Log.error("An error occured while importing file " + file.toPath());
                    failedFiles.add(file);
                } finally {
                    p.step();
                }
            }

            return failedFiles;
        }, failedFiles -> {
            if (failedFiles != null) {
                if (!failedFiles.isEmpty()) {
                    Message.warning("Some files failed to import:\n\n" + filesListToString(failedFiles));
                }
                ComponentManager.getFileExplorer().refresh();
            }
        });
    }

    private static int saveAs(Progress p, Spectrum spectrum, File file, int applyToAllAction) throws SpefoException {
        if (file.exists()) {
            if (applyToAllAction < 0) {
                if (applyToAllAction == REPLACE) {
                    spectrum.saveAs(file);
                } /* else applyToAllAction == SKIP */
                return applyToAllAction;
            }

            Progress.DialogAndReturnCode<OverwriteDialog> overwriteDialogAndReturnCode = p.syncOpenDialog(() -> new OverwriteDialog(file));
            int response = overwriteDialogAndReturnCode.getReturnValue();
            OverwriteDialog dialog = overwriteDialogAndReturnCode.getDialog();

            if (response == REPLACE) {
                spectrum.saveAs(file);
                if (dialog.applyToAll()) {
                    return REPLACE;
                }
            } else if (response == RENAME) {
                return saveAs(p, spectrum, file.toPath().resolveSibling(dialog.getNewName()).toFile(), applyToAllAction);

            } else if (response == SKIP) {
                if (overwriteDialogAndReturnCode.getDialog().applyToAll()) {
                    return SKIP;
                }
            }  else /* response == CANCEL */ {
                return CANCEL;
            }

        } else {
            spectrum.saveAs(file);
        }

        return applyToAllAction;
    }

    private static void checkForNaNs(Spectrum spectrum) {
        if (Arrays.stream(spectrum.getSeries().getYSeries()).anyMatch(Double::isNaN)) {
            spectrum.putFunctionAsset(SERIALIZE_KEY, new PostImportAsset(0));
        }
    }

    private static void checkRVCorrection(Spectrum spectrum) {
        if (Double.isNaN(spectrum.getRvCorrection())) {
            RVCorrectionDialog dialog = new RVCorrectionDialog();
            spectrum.setRvCorrection(dialog.openIsOk() ? dialog.getRvCorr() : 0);
        }
    }

    private static void checkRVCorrection(Progress p, Spectrum spectrum) {
        if (Double.isNaN(spectrum.getRvCorrection())) {
            Progress.DialogAndReturnCode<RVCorrectionDialog> overwriteDialogAndReturnCode = p.syncOpenDialog(RVCorrectionDialog::new);
            int response = overwriteDialogAndReturnCode.getReturnValue();
            RVCorrectionDialog dialog = overwriteDialogAndReturnCode.getDialog();

            spectrum.setRvCorrection(response == SWT.OK ? dialog.getRvCorr() : 0);
        }
    }
}
