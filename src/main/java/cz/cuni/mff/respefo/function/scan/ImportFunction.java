package cz.cuni.mff.respefo.function.scan;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.format.FormatManager;
import cz.cuni.mff.respefo.format.Spectrum;
import cz.cuni.mff.respefo.format.formats.ImportFileFormat;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.Serialize;
import cz.cuni.mff.respefo.function.SingleOrMultiFileFunction;
import cz.cuni.mff.respefo.function.asset.port.FileFormatSelectionDialog;
import cz.cuni.mff.respefo.function.asset.port.PostImportAsset;
import cz.cuni.mff.respefo.function.filter.CompatibleFormatFileFilter;
import cz.cuni.mff.respefo.util.FileType;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.utils.FileDialogs;
import cz.cuni.mff.respefo.util.utils.FileUtils;
import org.eclipse.swt.SWT;

import java.io.File;
import java.util.List;

@Fun(name = "Import", fileFilter = CompatibleFormatFileFilter.class)
@Serialize(key = ImportFunction.SERIALIZE_KEY, assetClass = PostImportAsset.class)
public class ImportFunction implements SingleOrMultiFileFunction {

    public static final String SERIALIZE_KEY = "import";

    // TODO: handle overwriting
    // TODO: message on success
    @Override
    public void execute(File file) {
        Spectrum spectrum;
        try {
            List<ImportFileFormat> fileFormats = FormatManager.getImportFileFormats(file.getPath());

            FileFormatSelectionDialog<ImportFileFormat> dialog = new FileFormatSelectionDialog<>(fileFormats, "Import");
            if (dialog.open() == SWT.OK) {
                spectrum = dialog.getFileFormat().importFrom(file.getPath());
            } else {
                return;
            }

        } catch (SpefoException exception) {
            Message.error("An error occurred while importing file.", exception);
            return;
        }

        // TODO: handle post import problems like NaNs and missing RvCorrection
        spectrum.getFunctionAssets().put(SERIALIZE_KEY, new PostImportAsset(0));

        String fileName = FileDialogs.saveFileDialog(FileType.SPECTRUM, FileUtils.replaceFileExtension(file.getPath(), "spf"));

        if (fileName != null) {
            try {
                spectrum.saveAs(new File(fileName));
                ComponentManager.getFileExplorer().refresh();
            } catch (SpefoException exception) {
                Message.error("An error occurred while saving file.", exception);
            }
        }
    }

    @Override
    public void execute(List<File> files) {
        Message.warning("This function is not yet implemented.\nUse the single file version instead.");
    }
}
