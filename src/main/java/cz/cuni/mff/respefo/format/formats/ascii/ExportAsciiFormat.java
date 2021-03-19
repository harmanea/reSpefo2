package cz.cuni.mff.respefo.format.formats.ascii;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.format.Spectrum;
import cz.cuni.mff.respefo.format.formats.ExportFileFormat;
import cz.cuni.mff.respefo.util.collections.XYSeries;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static cz.cuni.mff.respefo.util.utils.FileUtils.stripFileExtension;
import static cz.cuni.mff.respefo.util.utils.FileUtils.stripParent;

public class ExportAsciiFormat extends AsciiFormat implements ExportFileFormat {

    protected static final DecimalFormat FORMAT = new DecimalFormat("0.0000", new DecimalFormatSymbols(Locale.US));

    @Override
    public void exportTo(Spectrum spectrum, String fileName) throws SpefoException {
        XYSeries series = spectrum.getProcessedSeries();
        double[] x = series.getXSeries();
        double[] y = series.getYSeries();

        try (PrintWriter writer = new PrintWriter(fileName)) {
            if (spectrum.getOrigin() instanceof AsciiOrigin) {
                String originalFirstLine = ((AsciiOrigin) spectrum.getOrigin()).getFirstLine();
                if (originalFirstLine != null) {
                    writer.println(originalFirstLine);
                }

            } else {
                String strippedName = stripFileExtension(stripParent(fileName));
                writer.println(strippedName);
            }

            for (int i = 0; i < x.length; i++) {
                writer.print(FORMAT.format(x[i]));
                writer.print("  ");
                writer.println(FORMAT.format(y[i]));
            }

            if (writer.checkError()) {
                throw new SpefoException("Error while writing to file");
            }

        } catch (FileNotFoundException exception) {
            throw new SpefoException("Cannot find file [" + fileName + "]", exception);
        }
    }
}
