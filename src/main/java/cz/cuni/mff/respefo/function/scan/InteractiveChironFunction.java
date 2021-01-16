package cz.cuni.mff.respefo.function.scan;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.format.Spectrum;
import cz.cuni.mff.respefo.format.XYSeries;
import cz.cuni.mff.respefo.format.formats.fits.ChironFitsFormat;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.asset.port.InteractiveChironController;
import cz.cuni.mff.respefo.function.asset.port.InteractiveChironSelectionDialog;
import cz.cuni.mff.respefo.function.filter.FitsFileFilter;
import cz.cuni.mff.respefo.util.FileDialogs;
import cz.cuni.mff.respefo.util.FileType;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.utils.FileUtils;
import cz.cuni.mff.respefo.util.utils.FitsUtils;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.ImageHDU;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static cz.cuni.mff.respefo.function.scan.ImportFunction.checkForNaNs;
import static cz.cuni.mff.respefo.function.scan.ImportFunction.checkRVCorrection;

@Fun(name = "Interactive Chiron Rectification", fileFilter = FitsFileFilter.class, group = "FITS")
public class InteractiveChironFunction implements SingleFileFunction {

    @Override
    public void execute(File file) {
        float[][][] data;
        try {
            FitsFactory.setAllowHeaderRepairs(true);
            data = (float[][][]) FitsUtils.extractData(file.getPath());

        } catch (ClassCastException classCastException) {
            Message.error("The HDU kernel is not a 3-D array of type float", classCastException);
            return;

        } catch (SpefoException exception) {
            Message.error("Couldn't import spectrum", exception);
            return;

        } finally {
            FitsFactory.setAllowHeaderRepairs(false);
        }

        String[][] names = new String[data.length][3];
        for (int i = 0; i <= data.length - 1; i++) {
            float[][] matrix = data[i];
            names[i][0] = Integer.toString(i + 1);
            names[i][1] = Float.toString(matrix[0][0]);
            names[i][2] = Float.toString(matrix[matrix.length - 1][0]);
        }

        InteractiveChironSelectionDialog dialog = new InteractiveChironSelectionDialog(names);
        if (dialog.openIsNotOk()) {
            return;
        }

        InteractiveChironController controller = new InteractiveChironController(data, dialog.getSelected());
        controller.rectify(series -> saveSpectrum(series, file.getPath(), dialog.getSelected()));
    }

    private void saveSpectrum(XYSeries series, String fileName, boolean[] selected) {
        try {
            Spectrum spectrum = new InteractiveChironFitsImportFormat(series).importFrom(fileName);
            checkForNaNs(spectrum);
            checkRVCorrection(spectrum);

            String newFileName = FileDialogs.saveFileDialog(FileType.SPECTRUM, getSuggestedFileName(fileName, selected));
            if (newFileName == null) {
                return;
            }

            spectrum.saveAs(new File(newFileName));
            ComponentManager.getFileExplorer().refresh();

            OpenFunction.displaySpectrum(spectrum);

        } catch (SpefoException exception) {
            Message.error("An error occurred while saving file", exception);
        }
    }

    private String getSuggestedFileName(String fileName, boolean[] selected) {
        String suggestedFileName = FileUtils.stripFileExtension(fileName);

        List<Integer> selectedIndices = IntStream.rangeClosed(1, selected.length).filter(i -> selected[i-1]).boxed().collect(Collectors.toList());
        if (selectedIndices.size() <= 5) {
            suggestedFileName += "-" + selectedIndices.stream().map(i -> Integer.toString(i)).collect(Collectors.joining(","));
        }

        return suggestedFileName + ".spf";
    }

    private static class InteractiveChironFitsImportFormat extends ChironFitsFormat {

        private final XYSeries series;

        public InteractiveChironFitsImportFormat(XYSeries series) {
            this.series = series;
        }

        @Override
        public XYSeries parseData(ImageHDU imageHDU) {
            return series;
        }
    }
}
