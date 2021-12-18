package cz.cuni.mff.respefo.function.lst;

import cz.cuni.mff.respefo.exception.InvalidFileFormatException;
import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.util.collections.JulianDate;
import cz.cuni.mff.respefo.util.utils.StringUtils;

import java.io.*;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static cz.cuni.mff.respefo.util.utils.FormattingUtils.formatDouble;
import static cz.cuni.mff.respefo.util.utils.FormattingUtils.formatInteger;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.util.Collections.nCopies;

public class LstFile implements Iterable<LstFile.Row> {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy MM dd HH mm ss");
    public static final String TABLE_HEADER =
            "==============================================================================\n" +
            "   N.  Date & UT start       exp[s]      Filename       J.D.hel.  RVcorr\n" +
            "==============================================================================\n";

    private final String header;

    private final Map<Integer, Row> rowsByIndex;
    private final Map<String, Row> rowsByFileName;

    private File file;

    public LstFile(String header) {
        rowsByIndex = new LinkedHashMap<>();
        rowsByFileName = new LinkedHashMap<>();

        this.header = ensureProperHeaderLength(header);
    }

    // Make sure the header has exactly 4 lines => it has 3 LF
    private String ensureProperHeaderLength(String header) {
        int numberOfLineFeeds = (int) header.chars().filter(ch -> ch == '\n').count();

        if (numberOfLineFeeds < 3) {
            header = header + String.join("", nCopies(3 - numberOfLineFeeds, "\n"));

        } else {
            while (numberOfLineFeeds > 3) {
                int lastIndex = header.lastIndexOf('\n');
                header = header.substring(0, lastIndex)
                        + (lastIndex >= header.length() ? "" : header.substring(lastIndex + 1));
                numberOfLineFeeds--;
            }
        }

        return header;
    }

    public static LstFile open(File file) throws SpefoException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuilder headerBuilder = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                headerBuilder.append(readNonNullLine(br));
                if (i < 3) {
                    headerBuilder.append("\n");
                }
            }
            String header = headerBuilder.toString();

            LstFile lstFile = new LstFile(header);
            lstFile.file = file;

            // Skip table header
            for (int i = 0; i < 4; i++) {
                readNonNullLine(br);
            }

            String line;
            while ((line = br.readLine()) != null) {
                // Skip blank lines
                if (StringUtils.isBlank(line)) {
                    continue;
                }

                Row row = parseLine(line);
                lstFile.addRow(row);
            }

            return lstFile;

        } catch (IndexOutOfBoundsException | NumberFormatException | DateTimeException exception) {
            throw new InvalidFileFormatException("The .lst file is invalid", exception);
        } catch (IOException exception) {
            throw new SpefoException("An error occurred while reading file", exception);
        }
    }

    public void save() throws SpefoException {
        if (file == null) {
            throw new IllegalStateException("No file selected");
        }

        saveAs(file);
    }

    public void saveAs(File file) throws SpefoException {
        this.file = file;

        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println(header);
            writer.println(TABLE_HEADER);

            for (Row row : rowsByIndex.values()) {
                writer.println(String.join(" ",
                        formatInteger(row.index, 5),
                        row.dateTimeStart.format(DATE_TIME_FORMATTER),
                        formatDouble(row.expTime, 5, 3, false),
                        row.fileName != null ? row.fileName : "",
                        formatDouble(row.hjd.getRJD(), 5, 4),
                        formatDouble(row.rvCorr, 3, 2)));
            }
        } catch (IOException exception) {
            throw new SpefoException("An error occurred while writing to file", exception);
        }
    }

    public void addRow(Row row) {
        rowsByIndex.put(row.getIndex(), row);
        if (row.getFileName() != null) {
            rowsByFileName.put(row.getFileName(), row);
        }
    }

    public String getHeader() {
        return header;
    }

    public Optional<Row> getRowByIndex(int index) {
        return Optional.ofNullable(rowsByIndex.get(index));
    }

    public Optional<Row> getRowByFileName(String fileName) {
        return Optional.ofNullable(rowsByFileName.get(fileName));
    }

    private static String readNonNullLine(BufferedReader br) throws IOException, InvalidFileFormatException {
        String line = br.readLine();
        if (line == null) {
            throw new InvalidFileFormatException("Unexpected end of file reached");
        }
        return line;
    }

    private static Row parseLine(String line) {
        String[] tokens = line.trim().replaceAll(" +", " ").split("\\s+");

        int index = parseInt(tokens[0]);

        LocalDateTime dateTimeStart = LocalDateTime.of(parseInt(tokens[1]), parseInt(tokens[2]),
                parseInt(tokens[3]), parseInt(tokens[4]), parseInt(tokens[5]), parseInt(tokens[6]));

        double expTime = parseDouble(tokens[7]);

        int offset = 0;
        String fileName = null;
        if (tokens.length > 10) {
            offset = 1;
            fileName = tokens[8];
        }

        JulianDate hjd = new JulianDate(parseDouble(tokens[8 + offset]));

        double rvCorr = parseDouble(tokens[9 + offset]);

        return new Row(index, dateTimeStart, expTime, fileName, hjd, rvCorr);
    }

    @Override
    public Iterator<Row> iterator() {
        return rowsByIndex.values().iterator();
    }

    public static class Row {
        private final int index;
        private final LocalDateTime dateTimeStart;
        private final double expTime;
        private final String fileName;
        private final JulianDate hjd;
        private final double rvCorr;

        public Row(int index, LocalDateTime dateTimeStart, double expTime, String fileName, JulianDate hjd, double rvCorr) {
            this.index = index;
            this.dateTimeStart = dateTimeStart;
            this.expTime = expTime;
            this.fileName = fileName;
            this.hjd = hjd;
            this.rvCorr = rvCorr;
        }

        public int getIndex() {
            return index;
        }

        public LocalDateTime getDateTimeStart() {
            return dateTimeStart;
        }

        public double getExpTime() {
            return expTime;
        }

        public String getFileName() {
            return fileName;
        }

        public JulianDate getHjd() {
            return hjd;
        }

        public double getRvCorr() {
            return rvCorr;
        }
    }
}
