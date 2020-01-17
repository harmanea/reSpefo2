package cz.cuni.mff.respefo.format.scan;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.format.Data;
import cz.cuni.mff.respefo.format.FileFormat;
import cz.cuni.mff.respefo.format.SpectrumFile;
import cz.cuni.mff.respefo.logging.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static cz.cuni.mff.respefo.util.utils.FileUtils.stripFileExtension;
import static cz.cuni.mff.respefo.util.utils.FileUtils.stripParent;
import static java.lang.Double.parseDouble;

public class AsciiFormat implements FileFormat {
    private static final String[] FILE_EXTENSIONS = { "", "txt", "asc", "ascii" };

    @Override
    public String[] fileExtensions() {
        return FILE_EXTENSIONS;
    }

    @Override
    public SpectrumFile importFrom(String fileName) throws SpefoException {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            ArrayList<Double> xList = new ArrayList<>();
            ArrayList<Double> yList = new ArrayList<>();

            String line;
            while ((line = br.readLine()) != null) {
                processLine(xList, yList, line);
            }

            double[] xSeries = xList.stream().mapToDouble(Double::doubleValue).toArray();
            double[] ySeries = yList.stream().mapToDouble(Double::doubleValue).toArray();

            Data data = new Data(xSeries, ySeries);

            return new SpectrumFile(data);

        } catch (IOException exception) {
            throw new SpefoException("Error while importing file [" + fileName + "].", exception);
        }
    }

    private void processLine(List<Double> xList, List<Double> yList, String line) {
        String[] tokens = line.trim().split("\\s+", 2);

        try {
            xList.add(parseDouble(tokens[0]));
            yList.add(parseDouble(tokens[1]));

        } catch (NumberFormatException | IndexOutOfBoundsException exception) {
            Log.trace("Skipped line [%s].", line);
        }
    }

    @Override
    public void exportTo(SpectrumFile spectrumFile, String fileName) throws SpefoException {
        Data data = spectrumFile.getData();
        double[] x = data.getX();
        double[] y = data.getY();

        try (PrintWriter writer = new PrintWriter(fileName)) {
            String strippedName = stripFileExtension(stripParent(fileName));
            writer.println(strippedName);

            for (int i = 0; i < x.length; i++) {
                writer.print(x[i]);
                writer.print("  ");
                writer.println(y[i]);
            }

            if (writer.checkError()) {
                throw new SpefoException("Error while writing to file");
            }

        } catch (FileNotFoundException exception) {
            throw new SpefoException("Cannot find file [" + fileName + "]", exception);
        }
    }
}
