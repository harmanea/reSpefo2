package cz.cuni.mff.respefo.function.common;

import cz.cuni.mff.respefo.logging.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

public class Measurements implements Iterable<Measurement> {
    private final List<Measurement> elements;

    public Measurements() {
        elements = new ArrayList<>();
    }

    public void loadMeasurements(String fileName, boolean isCorrection) {
        try {
            Files.readAllLines(Paths.get(fileName))
                    .forEach(line -> parseLine(line, isCorrection));

        } catch (IOException exception) {
            Log.error("Couldn't load .stl file", exception);
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

    public void removeDuplicateNames() {
        Set<String> duplicateNames = elements.stream()
                .collect(groupingBy(Measurement::getName))
                .entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .collect(toSet());

        for (Measurement measurement : elements) {
            if (duplicateNames.contains(measurement.getName())) {
                measurement.setName(measurement.getName() + " " + measurement.getL0());
            }
        }
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
