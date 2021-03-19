package cz.cuni.mff.respefo.function.common;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.logging.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class Measurements implements Iterable<Measurement> {
    private final List<Measurement> elements;

    public Measurements() {
        elements = new ArrayList<>();
    }

    public void loadMeasurements(String fileName, boolean isCorrection) {
        try {
            parseFile(fileName, isCorrection);
        } catch (SpefoException exception) {
            Log.error("Couldn't load .stl file", exception);
        }
    }

    private void parseFile(String fileName, boolean isCorrection) throws SpefoException {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                parseLine(line, isCorrection);
            }

        } catch (FileNotFoundException exception) {
            throw new SpefoException("Couldn't find file", exception);
        } catch (IOException exception) {
            throw new SpefoException("An error occurred while parsing file", exception);
        }
    }

    private void parseLine(String line, boolean isCorrection) {
        String[] tokens = line.trim().split(" +", 3);
        if (tokens.length == 3) {
            try {
                double l0 = Double.parseDouble(tokens[0]);
                double radius = Double.parseDouble(tokens[1]);
                String name = tokens[2];

                elements.add(new Measurement(l0, radius, name, isCorrection));

            } catch (NumberFormatException exception) {
                Log.trace("Skipped line while parsing a measurements file:\n" + line);
            }
        }
    }

    public void removeInvalid(double[] xSeries) {
        double min = xSeries[0];
        double max = xSeries[xSeries.length - 1];

        elements.removeIf(measurement -> min > measurement.getL0() || measurement.getL0() > max);
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public Measurement get(int index) {
        return elements.get(index);
    }

    public int size() {
        return elements.size();
    }

    @Override
    public Iterator<Measurement> iterator() {
        return new Iterator<Measurement>() {
            private int i = 0;

            @Override
            public Measurement next() {
                if (i >= elements.size()) {
                    throw new NoSuchElementException();
                }

                return elements.get(i++);
            }

            @Override
            public boolean hasNext() {
                return i < elements.size();
            }
        };
    }
}
