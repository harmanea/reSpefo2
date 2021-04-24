package cz.cuni.mff.respefo.function.chiron;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.component.Project;
import cz.cuni.mff.respefo.format.Spectrum;
import cz.cuni.mff.respefo.format.formats.fits.ChironFitsFormat;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.filter.FitsFileFilter;
import cz.cuni.mff.respefo.function.open.OpenFunction;
import cz.cuni.mff.respefo.util.FileDialogs;
import cz.cuni.mff.respefo.util.FileType;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.collections.FitsFile;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.utils.FileUtils;
import nom.tam.fits.FitsFactory;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static cz.cuni.mff.respefo.function.port.ImportFunction.*;

@Fun(name = "Interactive Chiron Rectification", fileFilter = FitsFileFilter.class, group = "FITS")
public class InteractiveChironFunction implements SingleFileFunction {

    @Override
    public void execute(File file) {
        float[][][] data;
        boolean allowHeaderRepairs = FitsFactory.isAllowHeaderRepairs();
        try {
            FitsFactory.setAllowHeaderRepairs(true);
            FitsFile fits = new FitsFile(file, false, true);
            data = (float[][][]) fits.getData();

        } catch (ClassCastException classCastException) {
            Message.error("The HDU kernel is not a 3-D array of type float", classCastException);
            return;

        } catch (SpefoException exception) {
            Message.error("Couldn't import spectrum", exception);
            return;

        } finally {
            FitsFactory.setAllowHeaderRepairs(allowHeaderRepairs);
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

        new InteractiveChironController(data, dialog.getSelected())
                .rectify(series -> saveSpectrum(series, file, dialog.getSelected()));
    }

    private void saveSpectrum(XYSeries series, File originalFile, boolean[] selected) {
        try {
            Spectrum spectrum = new InteractiveChironFitsImportFormat(series).importFrom(originalFile.getPath());
            checkForNaNs(spectrum);
            checkForAttributesInLstFile(spectrum, originalFile);
            checkRVCorrection(spectrum);

            String newFileName = FileDialogs.saveFileDialog(FileType.SPECTRUM, getSuggestedFileName(originalFile.getPath(), selected));
            if (newFileName == null) {
                return;
            }

            spectrum.saveAs(new File(newFileName));
            Project.refresh();
            OpenFunction.displaySpectrum(spectrum);
            Message.info("File imported successfully.");

        } catch (SpefoException exception) {
            Message.error("Spectrum file couldn't be saved.", exception);
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
        public XYSeries parseData(FitsFile fits) {
            return series;
        }
    }
}
