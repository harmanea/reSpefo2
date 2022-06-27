package cz.cuni.mff.respefo.spectrum.port.ascii;

import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.spectrum.port.ExportFileFormat;
import cz.cuni.mff.respefo.util.collections.XYSeries;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static cz.cuni.mff.respefo.util.utils.FileUtils.stripFileExtension;
import static cz.cuni.mff.respefo.util.utils.FileUtils.stripParent;
import static cz.cuni.mff.respefo.util.utils.FormattingUtils.formatDouble;

public class ExportAsciiFormat extends AsciiFormat implements ExportFileFormat {

    protected static final DecimalFormat FORMAT = new DecimalFormat("0.0000", new DecimalFormatSymbols(Locale.US));

    @Override
    public void exportTo(Spectrum spectrum, String fileName) throws SpefoException {
        XYSeries series = spectrum.getProcessedSeries();
        double[] x = series.getXSeries();
        double[] y = series.getYSeries();

        try (PrintWriter writer = new PrintWriter(fileName)) {
            writer.print("# ");
            if (spectrum.getOrigin() instanceof AsciiOrigin) {
                String originalFirstLine = ((AsciiOrigin) spectrum.getOrigin()).getFirstLine();
                if (originalFirstLine != null) {
                    writer.print(originalFirstLine);
                }

            } else {
                String strippedName = stripFileExtension(stripParent(fileName));
                writer.print(strippedName);
            }
            writer.println(" | RV correction: " + formatDouble(spectrum.getRvCorrection(), 2, 2, true));

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
