package cz.cuni.mff.respefo.format.formats.ascii;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.format.Spectrum;
import cz.cuni.mff.respefo.format.XYSeries;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class SingleColumnExportAsciiFormat extends ExportAsciiFormat {
    @Override
    public void exportTo(Spectrum spectrum, String fileName) throws SpefoException {
        XYSeries series = spectrum.getProcessedSeries();
        double[] x = series.getXSeries();
        double[] y = series.getYSeries();

        if (!ArrayUtils.valuesHaveSameDifference(x)) {
            Message.warning("Not all x values have the same step. This might lead to unexpected results.");
        }

        try (PrintWriter writer = new PrintWriter(fileName)) {
            double xDiff = x[1] - x[0];
            writer.println(x[0] + " " + xDiff);

            for (int i = 0; i < x.length; i++) {
                writer.println(y[i]);
            }

            if (writer.checkError()) {
                throw new SpefoException("Error while writing to file");
            }

        } catch (FileNotFoundException exception) {
            throw new SpefoException("Cannot find file [" + fileName + "]", exception);
        }
    }

    @Override
    public String name() {
        return "Single column";
    }

    @Override
    public String description() {
        return "Plain text format for saving data in a single column.\n\n" +
                "The first line contains two floating point numbers specifying the first x value and the increment used to generate the other x values. " +
                "The following lines contain a single floating point number for each y value.\n\n" +
                "Note: This method should only be used if the increment in x values is the same for all points.";
    }

    @Override
    public boolean isDefault() {
        return false;
    }
}
