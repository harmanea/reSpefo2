package cz.cuni.mff.respefo.function.chiron;

import cz.cuni.mff.respefo.component.Project;
import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.filter.FitsFileFilter;
import cz.cuni.mff.respefo.function.open.OpenFunction;
import cz.cuni.mff.respefo.function.rectify.RectifyAsset;
import cz.cuni.mff.respefo.function.rectify.RectifyFunction;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.spectrum.port.fits.ChironFitsFormat;
import cz.cuni.mff.respefo.util.FileDialogs;
import cz.cuni.mff.respefo.util.FileType;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.collections.FitsFile;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.utils.FileUtils;
import cz.cuni.mff.respefo.util.utils.StringUtils;
import nom.tam.fits.FitsFactory;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

        List<Integer> selectedIndices = dialog.getSelectedIndices();

        new InteractiveChironController(data, selectedIndices)
                .rectify((series, asset) -> saveSpectrum(series, asset, file, selectedIndices));
    }

    private void saveSpectrum(XYSeries series, Optional<RectifyAsset> asset, File originalFile, List<Integer> selectedIndices) {
        try {
            Spectrum spectrum = new InteractiveChironFitsImportFormat(series).importFrom(originalFile.getPath());
            checkForNaNs(spectrum);
            checkForAttributesInLstFile(spectrum, originalFile);
            checkRVCorrection(spectrum);

            if (asset.isPresent()) {
                spectrum.putFunctionAsset(RectifyFunction.SERIALIZE_KEY, asset.get());
            }

            String suggestedFileName = FileUtils.stripFileExtension(originalFile.getPath()) + "-"
                    + StringUtils.combineIndices(selectedIndices.stream().map(i -> i + 1).collect(Collectors.toList())) + ".spf";
            String newFileName = FileDialogs.saveFileDialog(FileType.SPECTRUM, suggestedFileName);
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

    private static class InteractiveChironFitsImportFormat extends ChironFitsFormat {

        private final XYSeries series;

        public InteractiveChironFitsImportFormat(XYSeries series) {
            this.series = series;
        }

//        public XYSeries parseData(FitsFile fits) {
//            return series;
//        }
    }
}
