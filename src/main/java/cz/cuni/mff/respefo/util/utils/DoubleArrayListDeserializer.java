package cz.cuni.mff.respefo.util.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import cz.cuni.mff.respefo.util.DoubleArrayList;

import java.io.IOException;

public class DoubleArrayListDeserializer extends StdDeserializer<DoubleArrayList> {

    public DoubleArrayListDeserializer() {
        this(null);
    }

    public DoubleArrayListDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public DoubleArrayList deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        double[] elements = jsonParser.readValueAs(double[].class);

        return new DoubleArrayList(elements);
    }
}
