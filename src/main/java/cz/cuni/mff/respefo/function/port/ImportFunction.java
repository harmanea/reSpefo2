package cz.cuni.mff.respefo.function.port;

import cz.cuni.mff.respefo.component.Project;
import cz.cuni.mff.respefo.dialog.OverwriteDialog;
import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.MultiFileFunction;
import cz.cuni.mff.respefo.function.Serialize;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.filter.CompatibleFormatFileFilter;
import cz.cuni.mff.respefo.function.lst.LstFile;
import cz.cuni.mff.respefo.function.open.OpenFunction;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.spectrum.port.FormatManager;
import cz.cuni.mff.respefo.spectrum.port.ImportFileFormat;
import cz.cuni.mff.respefo.spectrum.port.UnknownFileFormatException;
import cz.cuni.mff.respefo.util.FileDialogs;
import cz.cuni.mff.respefo.util.FileType;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.Progress;
import cz.cuni.mff.respefo.util.collections.JulianDate;
import cz.cuni.mff.respefo.util.utils.FileUtils;
import org.eclipse.swt.SWT;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

import static cz.cuni.mff.respefo.dialog.OverwriteDialog.*;
import static cz.cuni.mff.respefo.util.utils.FileUtils.filesListToString;
import static cz.cuni.mff.respefo.util.utils.FileUtils.stripFileExtension;
import static cz.cuni.mff.respefo.util.utils.MathUtils.isNotNaN;
import static java.lang.Double.isNaN;
import static org.eclipse.swt.SWT.CANCEL;

@Fun(name = "Import", fileFilter = CompatibleFormatFileFilter.class)
@Serialize(key = ImportFunction.SERIALIZE_KEY, assetClass = PostImportAsset.class)
public class ImportFunction implements SingleFileFunction, MultiFileFunction {

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
        checkForAttributesInLstFile(spectrum, file);
        checkRVCorrection(spectrum);

        String fileName = FileDialogs.saveFileDialog(FileType.SPECTRUM, FileUtils.replaceFileExtension(file.getPath(), "spf"));
        if (fileName != null) {
            try {
                if (saveAs(spectrum, new File(fileName))) {
                    Project.refresh();
                    OpenFunction.displaySpectrum(spectrum);
                    Message.info("File imported successfully.");
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
                    checkForAttributesInLstFile(spectrum, file);
                    checkRVCorrection(p, spectrum);

                    String fileName = FileUtils.replaceFileExtension(file.getPath(), "spf");
                    int response = saveAs(p, spectrum, new File(fileName), applyToAllAction);
                    if (response == CANCEL) {
                        break;
                    } else if (response < 0) {
                        applyToAllAction = response;
                    }
                } catch (SpefoException exception) {
                    Log.error("An error occurred while importing file " + file.toPath(), exception);
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
                } else {
                    Message.info("All files imported successfully.");
                }
                Project.refresh();
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

    public static void checkForNaNs(Spectrum spectrum) {
        if (Arrays.stream(spectrum.getSeries().getYSeries()).anyMatch(Double::isNaN)) {
            spectrum.putFunctionAsset(SERIALIZE_KEY, new PostImportAsset(0));
        }
    }

    public static void checkRVCorrection(Spectrum spectrum) {
        if (isNaN(spectrum.getRvCorrection())) {
            RVCorrectionDialog dialog = new RVCorrectionDialog();
            spectrum.setRvCorrection(dialog.openIsOk() ? dialog.getRvCorr() : 0);
        }
    }

    private static void checkRVCorrection(Progress p, Spectrum spectrum) {
        if (isNaN(spectrum.getRvCorrection())) {
            Progress.DialogAndReturnCode<RVCorrectionDialog> overwriteDialogAndReturnCode = p.syncOpenDialog(RVCorrectionDialog::new);
            int response = overwriteDialogAndReturnCode.getReturnValue();
            RVCorrectionDialog dialog = overwriteDialogAndReturnCode.getDialog();

            spectrum.setRvCorrection(response == SWT.OK ? dialog.getRvCorr() : 0);
        }
    }

    public static void checkForAttributesInLstFile(Spectrum spectrum, File originalFile) {
        if (isNaN(spectrum.getHjd().getJD())
                || spectrum.getDateOfObservation().equals(LocalDateTime.MIN)
                || isNaN(spectrum.getRvCorrection())
                || isNaN(spectrum.getExpTime())) {

            File[] lstFiles = originalFile.getParentFile().listFiles((dir, name) -> name.endsWith(".lst"));
            if (lstFiles != null && lstFiles.length > 0) {
                File file;
                if (lstFiles.length == 1) {
                    file = lstFiles[0];

                } else {
                    // Try matching the lst filename
                    Optional<File> optionalFile = Arrays.stream(lstFiles)
                            .filter(f -> matchLstFileName(f, originalFile))
                            .findFirst();

                    file = optionalFile.orElse(lstFiles[0]);
                }

                try {
                    LstFile lstFile = LstFile.open(file);
                    updateSpectrumUsingLstFile(spectrum, lstFile, originalFile.getName());

                } catch (SpefoException exception) {
                    Log.error("Couldn't load .lst file", exception);
                }
            }
        }
    }

    private static boolean matchLstFileName(File file, File originalFile) {
        String strippedFileName = stripFileExtension(file.getName());

        // Match with original file parent directory name
        if (strippedFileName.equals(stripFileExtension(originalFile.getParentFile().getName()))) {
            return true;
        }

        // Match with original filename prefix
        String strippedOriginalFileName = stripFileExtension(originalFile.getName());
        if (strippedOriginalFileName.length() > 5) {
            String prefix = strippedOriginalFileName.substring(0, strippedOriginalFileName.length() - 5);
            return prefix.equals(strippedFileName);
        }

        return false;
    }

    public static void updateSpectrumUsingLstFile(Spectrum spectrum, LstFile lstFile, String originalFileName) {
        // Try matching filename in the lst file
        Optional<LstFile.Row> optionalRow = lstFile.getRowByFileName(originalFileName);

        if (optionalRow.isPresent()) {
            updateSpectrumUsingLstFileRow(spectrum, optionalRow.get());

        } else {
            // Try using a number in the filename
            String strippedFileName = stripFileExtension(originalFileName);
            try {
                int fileIndex = Integer.parseInt(strippedFileName.substring(strippedFileName.length() - 5));
                LstFile.Row row = lstFile.getRowByIndex(fileIndex).get();
                updateSpectrumUsingLstFileRow(spectrum, row);

            } catch (NumberFormatException | IndexOutOfBoundsException | NoSuchElementException e) {
                // Filename does not conform to the naming convention
            }
        }
    }

    private static void updateSpectrumUsingLstFileRow(Spectrum spectrum, LstFile.Row row) {
        JulianDate hjd = row.getHjd();
        if (isNaN(spectrum.getHjd().getJD()) && isNotNaN(hjd.getJD())) {
            if (hjd.getJD() < 100_000) {
                // Convert reduced julian date to full date
                hjd = JulianDate.fromRJD(hjd.getJD());
            }
            spectrum.setHjd(hjd);
        }

        if (spectrum.getDateOfObservation().equals(LocalDateTime.MIN) && row.getDateTimeStart().getYear() > 0) {
            spectrum.setDateOfObservation(row.getDateTimeStart());
        }

        double rvCorr = row.getRvCorr();
        if (isNotNaN(rvCorr) && rvCorr != 0) {
            spectrum.updateRvCorrection(rvCorr);
        }

        if (isNaN(spectrum.getExpTime()) && isNotNaN(row.getExpTime())) {
            spectrum.setExpTime(row.getExpTime());
        }
    }
}
