package cz.cuni.mff.respefo.function.scan;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.format.FormatManager;
import cz.cuni.mff.respefo.format.Spectrum;
import cz.cuni.mff.respefo.format.SpectrumFile;
import cz.cuni.mff.respefo.function.CompatibleFormatFileFilter;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.SingleOrMultiFileFunction;
import cz.cuni.mff.respefo.util.FileType;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.utils.FileUtils;

import java.io.File;
import java.util.List;

@Fun(name = "Import", fileFilter = CompatibleFormatFileFilter.class)
public class ImportFunction implements SingleOrMultiFileFunction {

    // TODO: handle overwriting
    // TODO: message on success
    @Override
    public void execute(File file) {
        SpectrumFile spectrumFile;
        try {
            spectrumFile = FormatManager.importFrom(file.getPath());

        } catch (SpefoException exception) {
            Message.error("An error occurred while importing file.", exception);
            return;
        }

        String fileName = FileUtils.saveFileDialog(FileType.SPECTRUM, FileUtils.stripFileExtension(file.getName()) + ".spf");

        if (fileName != null) {
            try {
                new Spectrum(spectrumFile, new File(fileName)).save();
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
