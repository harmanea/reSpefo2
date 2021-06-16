package cz.cuni.mff.respefo.format.origin;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class OriginSerializer extends JsonSerializer<Object> {
    @Override
    public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        jgen.writeStringField("key", value.getClass().getAnnotation(Origin.class).key());
        jgen.writeObjectField("data", value);
        jgen.writeEndObject();
    }
}
