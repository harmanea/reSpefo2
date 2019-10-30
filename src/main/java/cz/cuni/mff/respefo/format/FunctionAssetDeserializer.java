package cz.cuni.mff.respefo.format;

import com.fasterxml.jackson.databind.JsonNode;

public interface FunctionAssetDeserializer {
    FunctionAsset deserialize(JsonNode node);
}
