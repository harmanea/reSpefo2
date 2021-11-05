package cz.cuni.mff.respefo.format.formats.ascii;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.format.InvalidFileFormatException;
import cz.cuni.mff.respefo.format.SimpleSpectrum;
import cz.cuni.mff.respefo.format.Spectrum;
import cz.cuni.mff.respefo.format.formats.ImportFileFormat;
import cz.cuni.mff.respefo.util.collections.DoubleArrayList;
import cz.cuni.mff.respefo.util.collections.XYSeries;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static java.lang.Double.parseDouble;

public class ImportAsciiFormat extends AsciiFormat implements ImportFileFormat {

    @Override
    public Spectrum importFrom(String fileName) throws SpefoException {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            DoubleArrayList xList = new DoubleArrayList(1000);
            DoubleArrayList yList = new DoubleArrayList(1000);

            String line = br.readLine();
            if (line == null) {
                throw new InvalidFileFormatException("The ASCII file is empty");
            }
            String firstLine = tryFirstLine(xList, yList, line);

            while ((line = br.readLine()) != null) {
                parseLine(xList, yList, line);
            }

            XYSeries series = new XYSeries(xList.toArray(), yList.toArray());

            Spectrum spectrum = new SimpleSpectrum(series);
            spectrum.setOrigin(new AsciiOrigin(fileName, firstLine));
            return spectrum;

        } catch (IOException exception) {
            throw new SpefoException("Error while importing file [" + fileName + "]", exception);
        }
    }

    private String tryFirstLine(DoubleArrayList xList, DoubleArrayList yList, String line) {
        try {
            parseLine(xList, yList, line);
            return null;

        } catch (InvalidFileFormatException exception) {
            return line;
        }
    }

    private void parseLine(DoubleArrayList xList, DoubleArrayList yList, String line) throws InvalidFileFormatException {
        String[] tokens = line.trim().split("\\s+", 2);

        try {
            xList.add(parseDouble(tokens[0]));
            yList.add(parseDouble(tokens[1]));

        } catch (NumberFormatException | IndexOutOfBoundsException exception) {
            throw new InvalidFileFormatException("The ASCII file is invalid. Caused by line [" + line + "].");
        }
    }
}
