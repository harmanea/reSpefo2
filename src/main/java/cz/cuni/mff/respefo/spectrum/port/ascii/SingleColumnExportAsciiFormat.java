package cz.cuni.mff.respefo.spectrum.port.ascii;

import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;

import java.io.PrintWriter;

public class SingleColumnExportAsciiFormat extends ExportAsciiFormat {
    @Override
    protected void printData(Spectrum spectrum, String fileName, double[] x, double[] y, PrintWriter writer) {
        if (!ArrayUtils.valuesHaveSameDifference(x)) {
            Message.warning("Not all x values have the same step.\nThis might lead to unexpected results.");
        }

        double xDiff = x[1] - x[0];
        writer.println(String.format("%01.04f  %01.04f", x[0], xDiff));

        for (int i = 0; i < x.length; i++) {
            writer.println(String.format("%01.04f", y[i]));
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
