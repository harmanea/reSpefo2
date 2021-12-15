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

// TODO: find a different name then record
public class LstFile implements Iterable<LstFile.Record> {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy MM dd HH mm ss");
    public static final String TABLE_HEADER =
            "==============================================================================\n" +
            "   N.  Date & UT start       exp[s]      Filename       J.D.hel.  RVcorr\n" +
            "==============================================================================\n";

    private final String header;

    private final Map<Integer, Record> recordsByIndex;
    private final Map<String, Record> recordsByFileName;

    private File file;

    public LstFile(String header) {
        recordsByIndex = new LinkedHashMap<>();
        recordsByFileName = new LinkedHashMap<>();

        this.header = header;  // TODO: ensure the header has 4 lines
    }

    public LstFile(File file) throws SpefoException {  // TODO: move this to a static LstFile.open() method
        this.file = file;

        recordsByIndex = new LinkedHashMap<>();
        recordsByFileName = new LinkedHashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuilder headerBuilder = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                headerBuilder.append(readNonNullLine(br));
                if (i < 3) {
                    headerBuilder.append("\n");
                }
            }
            header = headerBuilder.toString();

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

                Record record = parseLine(line);

                recordsByIndex.put(record.getIndex(), record);
                if (record.getFileName() != null) {
                    recordsByFileName.put(record.getFileName(), record);
                }
            }

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
            writer.print(header);
            writer.print(TABLE_HEADER);

            for (Record record : recordsByIndex.values()) {
                writer.println(String.join(" ",
                        formatInteger(record.index, 5),
                        record.dateTimeStart.format(DATE_TIME_FORMATTER),
                        formatDouble(record.expTime, 5, 3, false),
                        record.fileName != null ? record.fileName : "",
                        formatDouble(record.hjd.getRJD(), 5, 4),
                        formatDouble(record.rvCorr, 3, 2)));
            }
        } catch (IOException exception) {
            throw new SpefoException("An error occurred while writing to file", exception);
        }
    }

    public void addRecord(Record record) {
        recordsByIndex.put(record.getIndex(), record);
        if (record.getFileName() != null) {
            recordsByFileName.put(record.getFileName(), record);
        }
    }

    public String getHeader() {
        return header;
    }

    public Optional<Record> getRecordByIndex(int index) {
        return Optional.ofNullable(recordsByIndex.get(index));
    }

    public Optional<Record> getRecordByFileName(String fileName) {
        return Optional.ofNullable(recordsByFileName.get(fileName));
    }

    private String readNonNullLine(BufferedReader br) throws IOException, InvalidFileFormatException {
        String line = br.readLine();
        if (line == null) {
            throw new InvalidFileFormatException("Unexpected end of file reached");
        }
        return line;
    }

    private Record parseLine(String line) {
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

        return new Record(index, dateTimeStart, expTime, fileName, hjd, rvCorr);
    }

    @Override
    public Iterator<Record> iterator() {
        return recordsByIndex.values().iterator();
    }

    public static class Record {
        private final int index;
        private final LocalDateTime dateTimeStart;
        private final double expTime;
        private final String fileName;
        private final JulianDate hjd;
        private final double rvCorr;

        public Record(int index, LocalDateTime dateTimeStart, double expTime, String fileName, JulianDate hjd, double rvCorr) {
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
