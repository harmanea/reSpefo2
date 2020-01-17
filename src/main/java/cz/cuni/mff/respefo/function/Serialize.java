package cz.cuni.mff.respefo.function;

import cz.cuni.mff.respefo.format.FunctionAssetDeserializer;
import cz.cuni.mff.respefo.format.FunctionAssetSerializer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Serialize {
    String key();
    Class<? extends FunctionAssetSerializer> serializer();
    Class<? extends FunctionAssetDeserializer> deserializer();
}
