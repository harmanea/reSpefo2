package cz.cuni.mff.respefo.format;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class DataSerializer extends JsonSerializer<Data> {
    @Override
    public void serialize(Data value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {

    }
}
