package cz.cuni.mff.respefo.function.scan;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.format.Spectrum;
import cz.cuni.mff.respefo.format.XYSeries;
import cz.cuni.mff.respefo.format.formats.fits.ImportFitsFormat;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.asset.dispersion.DispersionController;
import cz.cuni.mff.respefo.function.asset.dispersion.DispersionDialog;
import cz.cuni.mff.respefo.function.filter.FitsFileFilter;
import cz.cuni.mff.respefo.util.DoubleArrayList;
import cz.cuni.mff.respefo.util.Message;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@Fun(name = "Derive dispersion", fileFilter = FitsFileFilter.class, group = "FITS")
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

            new DispersionController(cmpValues, seriesA, seriesB, file).start();

        } catch (SpefoException e) {
            Message.error("An error occurred while reading files", e);
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

    private XYSeries readFitsFile(String fileName) throws SpefoException { // TODO: do this properly
        Spectrum spectrum = new ImportFitsFormat().importFrom(fileName);
        return spectrum.getSeries();
    }
}
