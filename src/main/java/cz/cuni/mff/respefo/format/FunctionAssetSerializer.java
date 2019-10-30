package cz.cuni.mff.respefo.format;

import com.fasterxml.jackson.core.JsonGenerator;

public interface FunctionAssetSerializer {
    void serialize(FunctionAsset asset, JsonGenerator jgen);
}
