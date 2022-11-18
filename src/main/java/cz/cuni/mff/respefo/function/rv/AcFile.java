package cz.cuni.mff.respefo.function.rv;

import cz.cuni.mff.respefo.exception.InvalidFileFormatException;
import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.util.collections.JulianDate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class AcFile {
    private final String header;
    private final List<Row> rows;

    private AcFile(String header, List<Row> rows) {
        this.header = header;
        this.rows = rows;
    }

    public String getHeader() {
        return header;
    }

    public Optional<Row> getRowByIndex(int index) {
        return index < rows.size()
                ? Optional.of(rows.get(index))
                : Optional.empty();
    }

    public Optional<Row> getRowByDate(JulianDate date) {
        return rows.stream()
                .filter(row -> row.getJulianDate().equals(date))
                .findFirst();
    }

    public static AcFile open(File file) throws SpefoException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String header = br.readLine();
            if (header == null) {
                throw new InvalidFileFormatException("The .ac file is empty");
            }

            List<Row> rows = br.lines()
                    .map(AcFile::parseLine)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            return new AcFile(header, rows);

        } catch (IOException exception) {
            throw new SpefoException("An error occurred while reading file", exception);
        }
    }

    private static Row parseLine(String line) {
        String[] tokens = line.trim().split(" ");

        try {
            double rvCorrAdjustment = Double.parseDouble(tokens[tokens.length > 2 ? 1 : 0]);
            JulianDate julianDate = JulianDate.fromRJD(Double.parseDouble(tokens[tokens.length > 2 ? 2 : 1]));

            return new Row(rvCorrAdjustment, julianDate);

        } catch (NumberFormatException exception) {
            return null;
        }
    }

    public static class Row {
        private final double rvCorrAdjustment;
        private final JulianDate julianDate;

        public Row(double rvCorrAdjustment, JulianDate julianDate) {
            this.rvCorrAdjustment = rvCorrAdjustment;
            this.julianDate = julianDate;
        }

        public double getRvCorrAdjustment() {
            return rvCorrAdjustment;
        }

        public JulianDate getJulianDate() {
            return julianDate;
        }
    }
}
