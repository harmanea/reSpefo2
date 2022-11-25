package cz.cuni.mff.respefo.function.debug;

import cz.cuni.mff.respefo.component.FileExplorer;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.SpectrumFunction;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.spectrum.format.EchelleSpectrum;
import cz.cuni.mff.respefo.util.FileDialogs;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.collections.XYSeries;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static cz.cuni.mff.respefo.util.FileType.ASCII_FILES;
import static cz.cuni.mff.respefo.util.utils.FileUtils.stripFileExtension;

@Fun(name = "Print Echelle Orders", fileFilter = SpefoFormatFileFilter.class, group = "Debug")
public class PrintEchelleOrdersFunction extends SpectrumFunction {

    private static final DecimalFormat FORMAT = new DecimalFormat("0.0000", new DecimalFormatSymbols(Locale.US));

    @Override
    protected void execute(Spectrum spectrum) {
        try {
            EchelleSpectrum echelleSpectrum = (EchelleSpectrum) spectrum;
            printOrders(echelleSpectrum);
        } catch (ClassCastException exception) {
            Message.error("Not an echelle spectrum", exception);
        }
    }

    private void printOrders(EchelleSpectrum spectrum) {
        String fileName = FileDialogs.saveFileDialog(ASCII_FILES, stripFileExtension(spectrum.getFile().getPath()));
        if (fileName != null) {
            try (PrintWriter writer = new PrintWriter(fileName)) {
                for (XYSeries series : spectrum.getOriginalSeries()) {
                    for (int i = 0; i < series.getLength(); i++) {
                        writer.print(FORMAT.format(series.getX(i)));
                        writer.print("  ");
                        writer.println(FORMAT.format(series.getY(i)));
                    }

                    writer.println();
                }

                if (writer.checkError()) {
                    Message.warning("Some errors might have occurred while printing echelle orders");
                } else {
                    Message.info("File exported successfully.");
                    FileExplorer.getDefault().refresh();
                }

            } catch (IOException exception) {
                Message.error("Couldn't print echelle orders", exception);
            }
        }
    }
}
