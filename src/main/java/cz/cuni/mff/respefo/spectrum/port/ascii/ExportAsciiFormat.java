package cz.cuni.mff.respefo.spectrum.port.ascii;

import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.spectrum.port.ExportFileFormat;
import cz.cuni.mff.respefo.util.collections.XYSeries;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import static cz.cuni.mff.respefo.util.utils.FileUtils.stripFileExtension;
import static cz.cuni.mff.respefo.util.utils.FileUtils.stripParent;
import static cz.cuni.mff.respefo.util.utils.FormattingUtils.formatDouble;

public class ExportAsciiFormat extends AsciiFormat implements ExportFileFormat {
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

            String previousLine = null;
            for (int i = 0; i < x.length; i++) {
                String line = String.format("%01.04f  %01.04f", x[i], y[i]);

                if (!line.equals(previousLine)) {
                    writer.println(line);
                    previousLine = line;
                }
            }

            if (writer.checkError()) {
                throw new SpefoException("Error while writing to file");
            }

        } catch (FileNotFoundException exception) {
            throw new SpefoException("Cannot find file [" + fileName + "]", exception);
        }
    }
}
