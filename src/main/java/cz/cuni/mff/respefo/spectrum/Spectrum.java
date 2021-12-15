package cz.cuni.mff.respefo.spectrum;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.spectrum.asset.FunctionAsset;
import cz.cuni.mff.respefo.spectrum.asset.FunctionAssetsDeserializer;
import cz.cuni.mff.respefo.spectrum.format.EchelleSpectrum;
import cz.cuni.mff.respefo.spectrum.format.SimpleSpectrum;
import cz.cuni.mff.respefo.spectrum.origin.OriginDeserializer;
import cz.cuni.mff.respefo.spectrum.origin.OriginSerializer;
import cz.cuni.mff.respefo.util.collections.*;
import cz.cuni.mff.respefo.util.info.VersionInfo;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public abstract class Spectrum {
    public static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        MAPPER.disable(MapperFeature.AUTO_DETECT_CREATORS,
                MapperFeature.AUTO_DETECT_GETTERS,
                MapperFeature.AUTO_DETECT_IS_GETTERS,
                MapperFeature.AUTO_DETECT_SETTERS);
        MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        MAPPER.registerModule(new JSR310Module()); // LocalDateTime support

        SimpleModule doubleArrayListModule = new SimpleModule();
        doubleArrayListModule.addSerializer(DoubleArrayList.class, new DoubleArrayListSerializer());
        doubleArrayListModule.addDeserializer(DoubleArrayList.class, new DoubleArrayListDeserializer());
        MAPPER.registerModule(doubleArrayListModule);
    }

    private static final Map<Integer, Class<? extends Spectrum>> FORMATS = new HashMap<>();
    static {
        FORMATS.put(SimpleSpectrum.FORMAT, SimpleSpectrum.class);
        FORMATS.put(EchelleSpectrum.FORMAT, EchelleSpectrum.class);
    }

    private transient File file;

    private int format;
    private String version;

    @JsonDeserialize(using = OriginDeserializer.class)
    @JsonSerialize(using = OriginSerializer.class)
    protected Object origin;

    protected JulianDate hjd;
    protected LocalDateTime dateOfObservation;
    protected double rvCorrection; // Maybe add support for different types in the future
    protected double expTime;

    @JsonDeserialize(using = FunctionAssetsDeserializer.class)
    protected LinkedHashMap<String, FunctionAsset> functionAssets;

    public static Spectrum open(File file) throws SpefoException {
        Spectrum spectrum = readFile(file);
        spectrum.setFile(file);
        return spectrum;
    }

    private static Spectrum readFile(File file) throws SpefoException {
        try {
            JsonNode root = MAPPER.readTree(file);
            int format = root.get("format").asInt();
            return MAPPER.treeToValue(root, FORMATS.get(format));

        } catch (JsonParseException | JsonMappingException exception) {
            throw new SpefoException("An error occurred while processing JSON", exception);
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
        saveToFile(file);
        setFile(file);
    }

    private void saveToFile(File destinationFile) throws SpefoException {
        try {
            ObjectWriter writer = MAPPER.writer();
            writer.writeValue(destinationFile, this);

        } catch (JsonGenerationException | JsonMappingException exception) {
            throw new SpefoException("An error occurred while generating JSON", exception);
        } catch (IOException exception) {
            throw new SpefoException("An error occurred while writing to file", exception);
        }
    }

    protected Spectrum() {
        // default empty constructor
    }

    protected Spectrum(int format) {
        this.format = format;

        version = VersionInfo.getVersion();
        functionAssets = new LinkedHashMap<>();
        hjd = new JulianDate();
        dateOfObservation = LocalDateTime.MIN;
        rvCorrection = Double.NaN;
        expTime = Double.NaN;
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

    public String getVersion() {
        return version;
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

    public abstract void updateRvCorrection(double newRvCorrection);

    public double getExpTime() {
        return expTime;
    }

    public void setExpTime(double expTime) {
        this.expTime = expTime;
    }

    public abstract XYSeries getSeries();

    public XYSeries getProcessedSeries() {
        XYSeries processedSeries = getSeries();

        for (FunctionAsset asset : functionAssets.values()) {
            processedSeries = asset.process(processedSeries);
        }

        return processedSeries;
    }

    public XYSeries getProcessedSeriesWithout(FunctionAsset omittedAsset) {
        XYSeries processedSeries = getSeries();

        for (FunctionAsset asset : functionAssets.values()) {
            if (asset != omittedAsset) {
                processedSeries = asset.process(processedSeries);
            }
        }

        return processedSeries;
    }

    @SuppressWarnings({"unchecked", "unused"})
    public <T extends FunctionAsset> Optional<T> getFunctionAsset(String key, Class<T> cls) {
        return Optional.ofNullable((T) functionAssets.get(key));
    }

    public void putFunctionAsset(String key, FunctionAsset asset) {
        functionAssets.put(key, asset);
    }

    public void removeFunctionAsset(String key) {
        functionAssets.remove(key);
    }

    public boolean containsFunctionAsset(String key) {
        return functionAssets.containsKey(key);
    }

    public static Comparator<Spectrum> hjdComparator() {
        Comparator<Spectrum> comparator = Comparator.comparing(a -> a.hjd);
        return comparator.thenComparing(a -> a.file);
    }
}
