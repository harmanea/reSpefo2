package cz.cuni.mff.respefo.function.disp;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.component.Project;
import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.filter.FitsFileFilter;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.spectrum.port.fits.ImportFitsFormat;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.collections.FitsFile;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.utils.FileUtils;
import nom.tam.fits.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import static cz.cuni.mff.respefo.util.utils.FormattingUtils.formatDouble;
import static cz.cuni.mff.respefo.util.utils.FormattingUtils.formatInteger;
import static java.lang.String.format;

@Fun(name = "Derive Dispersion", fileFilter = FitsFileFilter.class, group = "FITS")
public class DispersionFunction implements SingleFileFunction {

    private static final String[] INVALID_KEYS = new String[] {"UT", "INSTRUME"};

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

            // TODO: Use the new Async methods instead
            new DispersionController(cmpValues, seriesA, seriesB)
                    .start(results -> printToCmfFile(dialog.getCmpFileName(), dialog.getLabFileNameA(), dialog.getLabFileNameB(), results),
                            coefficients -> saveToFITSHeader(file, coefficients));

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

            double[] coefficients = results.getCoefficients();
            for (int i = 0; i < coefficients.length; i++) {
                writer.println("   order  " + i + "    " + format("%1.8e", coefficients[i]));
            }

            if (writer.checkError()) {
                throw new SpefoException("The print stream has encountered an error");
            }

            Project.refresh();
        } catch (Exception exception) {
            Message.error("An exception occurred while printing to file.", exception);
        }
    }

    private void saveToFITSHeader(File originalFile, double[] coefficients) {
        try (Fits fits = new Fits()) {
            FitsFile fitsFile = new FitsFile(originalFile);
            Header oldHeader = fitsFile.getHeader();

            ImageHDU newHDU = ImageData.from(fitsFile.getData()).toHDU();

            Header newHeader = newHDU.getHeader();
            newHeader.updateLines(oldHeader);

            for (int i = 0; i < coefficients.length; i++) {
                newHeader.addValue(format("DCOEF%d", i + 1), coefficients[i], "Dispersion coefficient");
            }

            // Fix incorrect FITS header string entries in some files
            for (String key: INVALID_KEYS) {
                HeaderCard oldCard = oldHeader.getCard(key);
                HeaderCard newCard = newHeader.getCard(key);

                newCard.setValue(oldCard.getValue());
                newCard.setComment(oldCard.getComment());
            }

            fits.addHDU(newHDU);
            fits.write(originalFile);

            ComponentManager.clearScene(true);
            Message.info("Dispersion coefficients saved to FITS header successfully.");

        } catch (SpefoException | IOException exception) {
            Message.error("Dispersion coefficients couldn't be saved.", exception);
        }
    }

    private double[] readCmpData(String cmpFileName) throws SpefoException {
        try {
            return Files.readAllLines(Paths.get(cmpFileName))
                    .stream()
                    .mapToDouble(Double::parseDouble)
                    .toArray();

        } catch (IOException exception) {
            throw new SpefoException("Couldn't read .cmp file", exception);
        } catch (NumberFormatException exception) {
            throw new SpefoException("Cmp file is invalid", exception);
        }
    }

    private XYSeries readFitsFile(String fileName) throws SpefoException {
        Spectrum spectrum = new ImportFitsFormat().importFrom(fileName); // TODO: Is there a more elegant way to do this?
        return spectrum.getSeries();
    }
}
