package cz.cuni.mff.respefo.spectrum.origin;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class OriginDeserializer extends JsonDeserializer<Object> {
    @Override
    public Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);

        String originKey = node.get("key").asText();

        return jp.getCodec().treeToValue(node.get("data"), OriginManager.getOriginClass(originKey));
    }
}
