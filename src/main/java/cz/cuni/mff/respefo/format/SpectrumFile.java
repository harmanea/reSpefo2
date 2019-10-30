package cz.cuni.mff.respefo.format;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Map;

public class SpectrumFile {
    private int format;
    private String version;
    // TODO: add origin information

    @JsonDeserialize(using = DataDeserializer.class)
    @JsonSerialize(using = DataSerializer.class)
    private Data data;

    @JsonDeserialize(using = FunctionAssetsDeserializer.class)
    @JsonSerialize(using = FunctionAssetsSerializer.class)
    private Map<String, FunctionAsset> functionAssets;

    // TODO: add history ?

    public SpectrumFile() {
        // default empty constructor
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

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Map<String, FunctionAsset> getFunctionAssets() {
        return functionAssets;
    }

    public void setFunctionAssets(Map<String, FunctionAsset> functionAssets) {
        this.functionAssets = functionAssets;
    }
}
