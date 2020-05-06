package cz.cuni.mff.respefo.function.scan;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.format.FormatManager;
import cz.cuni.mff.respefo.format.Spectrum;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.SingleOrMultiFileFunction;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.utils.FileUtils;

import java.io.File;
import java.util.List;

import static cz.cuni.mff.respefo.util.FileType.COMPATIBLE_SPECTRUM_FILES;
import static cz.cuni.mff.respefo.util.utils.FileUtils.stripFileExtension;

@Fun(name = "Export", fileFilter = SpefoFormatFileFilter.class)
public class ExportFunction implements SingleOrMultiFileFunction {

    // TODO: handle overwriting
    // TODO: message on success
    @Override
    public void execute(File spectrumFile) {
        Spectrum spectrum;
        try {
            spectrum = new Spectrum(spectrumFile);
        } catch (SpefoException exception) {
            Message.error("An error occurred while opening file.", exception);
            return;
        }

        String fileName = FileUtils.saveFileDialog(COMPATIBLE_SPECTRUM_FILES, stripFileExtension(spectrumFile.getName()));

        if (fileName != null) {
            try {
                FormatManager.exportTo(spectrum.getSpectrumFile(), fileName);
                ComponentManager.getFileExplorer().refresh();
            } catch (SpefoException exception) {
                Message.error("An error occurred while exporting file.", exception);
            }
        }
    }

    @Override
    public void execute(List<File> spectrumFiles) {
        Message.warning("This function is not yet implemented.\nUse the single file version instead.");
    }
}
