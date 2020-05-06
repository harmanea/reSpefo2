package cz.cuni.mff.respefo.format;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.cuni.mff.respefo.format.origin.OriginDeserializer;
import cz.cuni.mff.respefo.format.origin.OriginSerializer;
import cz.cuni.mff.respefo.util.VersionInfo;

import java.util.LinkedHashMap;
import java.util.Map;

public class SpectrumFile {
    private static final int CURRENT_FORMAT = 1;

    private int format;
    private String version;

    @JsonDeserialize(using = OriginDeserializer.class)
    @JsonSerialize(using = OriginSerializer.class)
    private Object origin;

    private Data data;

    @JsonDeserialize(using = FunctionAssetsDeserializer.class)
    private LinkedHashMap<String, FunctionAsset> functionAssets;

    // TODO: add history ?

    public SpectrumFile() {
        // default empty constructor
    }

    public SpectrumFile(Data data) {
        format = CURRENT_FORMAT;
        version = VersionInfo.getVersion();
        functionAssets = new LinkedHashMap<>();

        this.data = data;
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

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Map<String, FunctionAsset> getFunctionAssets() {
        return functionAssets;
    }

    public void setFunctionAssets(LinkedHashMap<String, FunctionAsset> functionAssets) {
        this.functionAssets = functionAssets;
    }
}
