package cz.cuni.mff.respefo.format;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import cz.cuni.mff.respefo.function.FunctionManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FunctionAssetsDeserializer extends JsonDeserializer<Map<String, FunctionAsset>> {
    @Override
    public Map<String, FunctionAsset> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);

        Map<String, FunctionAsset> assets = new HashMap<>();
        for (Iterator<Map.Entry<String, JsonNode>> it = node.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> field = it.next();

            String key = field.getKey();
            FunctionAssetDeserializer deserializer = FunctionManager.getDeserializer(key);
            assets.put(key, deserializer.deserialize(field.getValue()));
        }

        return assets;
    }
}
