package cz.cuni.mff.respefo.util.collections;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class DoubleArrayListSerializer extends StdSerializer<DoubleArrayList> {

    public DoubleArrayListSerializer() {
        this(null);
    }

    public DoubleArrayListSerializer(Class<DoubleArrayList> t) {
        super(t);
    }

    @Override
    public void serialize(DoubleArrayList doubles, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartArray();
        for (double element : doubles) {
            jsonGenerator.writeNumber(element);
        }
        jsonGenerator.writeEndArray();
    }
}
