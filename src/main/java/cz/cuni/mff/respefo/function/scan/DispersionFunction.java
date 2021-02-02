package cz.cuni.mff.respefo.function.scan;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.format.Spectrum;
import cz.cuni.mff.respefo.format.XYSeries;
import cz.cuni.mff.respefo.format.formats.fits.ImportFitsFormat;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.asset.dispersion.ComparisonLineResults;
import cz.cuni.mff.respefo.function.asset.dispersion.DispersionController;
import cz.cuni.mff.respefo.function.asset.dispersion.DispersionDialog;
import cz.cuni.mff.respefo.function.filter.FitsFileFilter;
import cz.cuni.mff.respefo.util.DoubleArrayList;
import cz.cuni.mff.respefo.util.FileDialogs;
import cz.cuni.mff.respefo.util.FileType;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.utils.FileUtils;
import cz.cuni.mff.respefo.util.utils.MathUtils;

import java.io.*;

import static cz.cuni.mff.respefo.function.scan.ImportFunction.checkForNaNs;
import static cz.cuni.mff.respefo.function.scan.ImportFunction.checkRVCorrection;
import static cz.cuni.mff.respefo.util.Constants.SPEED_OF_LIGHT;
import static cz.cuni.mff.respefo.util.utils.FormattingUtils.formatDouble;
import static cz.cuni.mff.respefo.util.utils.FormattingUtils.formatInteger;
import static cz.cuni.mff.respefo.util.utils.MathUtils.isNotNaN;

@Fun(name = "Derive Dispersion", fileFilter = FitsFileFilter.class, group = "FITS")
public class DispersionFunction implements SingleFileFunction {

    @Override
    public void execute(File file) {
        DispersionDialog dialog = new DispersionDialog();
        if (dialog.openIsNotOk()) {
            return;
        }

        try {
            double[] cmpValues = readCmpData(dialog.getCmpFileName());
            XYSeries seriesA = readFitsFile(dialog.getLabFileNameA());
            XYSeries seriesB = readFitsFile(dialog.getLabFileNameB());

            new DispersionController(cmpValues, seriesA, seriesB)
                    .start(results -> printToCmfFile(dialog.getCmpFileName(), dialog.getLabFileNameA(), dialog.getLabFileNameB(), results),
                            coeffs -> saveSpectrum(file.getPath(), coeffs));

        } catch (SpefoException e) {
            Message.error("An error occurred while reading files", e);
        }
    }

    private void printToCmfFile(String cmpFileName, String labFileNameA, String labFileNameB, ComparisonLineResults results) {
        File cmfFile = new File(FileUtils.replaceFileExtension(cmpFileName, "cmf"));
        try (PrintWriter writer = new PrintWriter(cmfFile)) {
            writer.println("Comparison lines measured from files " + labFileNameA + " & " + labFileNameB);
            writer.println("  ----------------------------------------------------------------\n");
            writer.println("   N.     x up    x down    x mean     rms        lab.       comp.    c.-l.\n");

            for (ComparisonLineResults.ComparisonLineResult result : results) {
                writer.print(formatInteger(result.getIndex() + 1, 5));
                writer.print(formatDouble(result.getXUp(), 5, 3));
                writer.print(formatDouble(result.getXDown(), 5, 3));
                writer.print(formatDouble(result.getX(), 5, 3));
                writer.print("   1.000");
                writer.print(formatDouble(result.getLaboratoryValue(), 7, 3));
                writer.print(formatDouble(result.getActualY(), 7, 3));
                writer.print(formatDouble(result.getResidual(), 3, 3));

                if (!result.isUsed()) {
                    writer.print("  not used");
                }

                writer.println();
            }

            writer.print("\n                                                              rms =");
            writer.println(formatDouble(results.meanRms(), 4, 3));
            writer.println("\n\n  Coefficients of dispersion polynomial:\n");

            double[] coeffs = results.getCoeffs();
            for (int i = 0; i < coeffs.length; i++) {
                writer.println("   order  " + i + "    " + String.format("%1.8e", coeffs[i]));
            }

            if (writer.checkError()) {
                throw new SpefoException("The print stream has encountered an error");
            }

            ComponentManager.getFileExplorer().refresh();
        } catch (Exception exception) {
            Message.error("An exception occurred while printing to file.", exception);
        }
    }

    private void saveSpectrum(String fileName, double[] coeffs) {
        try {
            Spectrum spectrum = new ImportFitsFormat().importFrom(fileName);

            double[] xSeries = spectrum.getSeries().getXSeries();
            for (int i = 0; i < xSeries.length; i++) {
                xSeries[i] = MathUtils.polynomial(i, coeffs);

                if (isNotNaN(spectrum.getRvCorrection())) {
                    xSeries[i] += spectrum.getRvCorrection() * (xSeries[i] / SPEED_OF_LIGHT);
                }
            }
            spectrum.getSeries().updateXSeries(xSeries);

            checkForNaNs(spectrum);
            checkRVCorrection(spectrum);

            String newFileName = FileDialogs.saveFileDialog(FileType.SPECTRUM, FileUtils.replaceFileExtension(fileName, "spf"));
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

    private double[] readCmpData(String cmpFileName) throws SpefoException {
        try (BufferedReader br = new BufferedReader(new FileReader(cmpFileName))) {
            DoubleArrayList values = new DoubleArrayList();

            String line;
            while ((line = br.readLine()) != null) {
                double value = Double.parseDouble(line);
                values.add(value);
            }

            return values.toArray();

        } catch (IOException exception) {
            throw new SpefoException("Couldn't read .cmp file", exception);
        } catch (NumberFormatException exception) {
            throw new SpefoException("Cmp file is invalid", exception);
        }
    }

    private XYSeries readFitsFile(String fileName) throws SpefoException {
        Spectrum spectrum = new ImportFitsFormat().importFrom(fileName); // Is there a more elegant way to do this?
        return spectrum.getSeries();
    }
}
