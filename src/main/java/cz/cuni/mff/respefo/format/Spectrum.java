package cz.cuni.mff.respefo.format;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.format.asset.FunctionAsset;
import cz.cuni.mff.respefo.format.asset.FunctionAssetsDeserializer;
import cz.cuni.mff.respefo.format.origin.OriginDeserializer;
import cz.cuni.mff.respefo.format.origin.OriginSerializer;
import cz.cuni.mff.respefo.util.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class Spectrum {
    private static final int CURRENT_FORMAT = 1;
    private static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        MAPPER.disable(MapperFeature.AUTO_DETECT_CREATORS,
                MapperFeature.AUTO_DETECT_GETTERS,
                MapperFeature.AUTO_DETECT_IS_GETTERS,
                MapperFeature.AUTO_DETECT_SETTERS);
        MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        MAPPER.registerModule(new JSR310Module()); // LocalDateTime support

        SimpleModule doubleArrayListModule = new SimpleModule();
        doubleArrayListModule.addSerializer(DoubleArrayList.class, new DoubleArrayListSerializer());
        doubleArrayListModule.addDeserializer(DoubleArrayList.class, new DoubleArrayListDeserializer());
        MAPPER.registerModule(doubleArrayListModule);
    }

    private transient File file;

    private int format;
    private String version;

    @JsonDeserialize(using = OriginDeserializer.class)
    @JsonSerialize(using = OriginSerializer.class)
    private Object origin;

    private JulianDate hjd;
    private LocalDateTime dateOfObservation;
    private double rvCorrection; // Maybe add support for different types in the future

    @JsonDeserialize(using = FunctionAssetsDeserializer.class)
    private LinkedHashMap<String, FunctionAsset> functionAssets;

    private XYSeries series;

    public static Spectrum open(File file) throws SpefoException {
        Spectrum spectrum = readFile(file);
        spectrum.setFile(file);
        return spectrum;
    }

    private static Spectrum readFile(File file) throws SpefoException {
        try {
            return MAPPER.readValue(file, Spectrum.class);

        } catch (JsonParseException | JsonMappingException exception) {
            throw new SpefoException("An error occurred while processing JSON", exception);
        } catch (IOException exception) {
            throw new SpefoException("An error occurred while reading file", exception);
        }
    }

    public void save() throws SpefoException {
        saveAs(file);
    }

    public void saveAs(File file) throws SpefoException {
        saveToFile(file);
    }

    private void saveToFile(File destinationFile) throws SpefoException {
        try {
            ObjectWriter writer = MAPPER.writer();
            writer.writeValue(destinationFile, this);

        } catch (JsonGenerationException | JsonMappingException exception) {
            throw new SpefoException("An error occurred while processing JSON", exception);
        } catch (IOException exception) {
            throw new SpefoException("An error occurred while writing to file", exception);
        }
    }

    private Spectrum() {
        // default empty constructor
    }

    public Spectrum(XYSeries series) {
        format = CURRENT_FORMAT;
        version = VersionInfo.getVersion();
        functionAssets = new LinkedHashMap<>();

        hjd = new JulianDate();
        dateOfObservation = LocalDateTime.MIN;
        rvCorrection = Double.NaN;

        this.series = series;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Object getOrigin() {
        return origin;
    }

    public void setOrigin(Object origin) {
        this.origin = origin;
    }

    public JulianDate getHjd() {
        return hjd;
    }

    public void setHjd(JulianDate hjd) {
        this.hjd = hjd;
    }

    public LocalDateTime getDateOfObservation() {
        return dateOfObservation;
    }

    public void setDateOfObservation(LocalDateTime dateOfObservation) {
        this.dateOfObservation = dateOfObservation;
    }

    public double getRvCorrection() {
        return rvCorrection;
    }

    public void setRvCorrection(double rvCorrection) {
        this.rvCorrection = rvCorrection;
    }

    public XYSeries getSeries() {
        return series;
    }

    public void setSeries(XYSeries series) {
        this.series = series;
    }

    public XYSeries getProcessedSeries() {
        XYSeries processedSeries = series;

        for (FunctionAsset asset : functionAssets.values()) {
            processedSeries = asset.process(processedSeries);
        }

        return processedSeries;
    }

    public XYSeries getProcessedSeriesWithout(FunctionAsset omittedAsset) {
        XYSeries processedSeries = this.series;

        for (FunctionAsset asset : functionAssets.values()) {
            if (asset != omittedAsset) {
                processedSeries = asset.process(processedSeries);
            }
        }

        return processedSeries;
    }

    // TODO: make map functions available directly
    public Map<String, FunctionAsset> getFunctionAssets() {
        return functionAssets;
    }
}
