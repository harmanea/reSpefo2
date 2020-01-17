package cz.cuni.mff.respefo.format;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import cz.cuni.mff.respefo.function.FunctionManager;

import java.io.IOException;
import java.util.Map;

public class FunctionAssetsSerializer extends JsonSerializer<Map<String, FunctionAsset>> {
    @Override
    public void serialize(Map<String, FunctionAsset> value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        for (Map.Entry<String, FunctionAsset> entry : value.entrySet()){
            String key = entry.getKey();

            FunctionAssetSerializer serializer = FunctionManager.getSerializer(key);
            serializer.serialize(entry.getValue(), jgen);
        }
        jgen.writeEndObject();
    }
}
