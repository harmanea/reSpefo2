package cz.cuni.mff.respefo.function;

import cz.cuni.mff.respefo.format.FunctionAssetDeserializer;
import cz.cuni.mff.respefo.format.FunctionAssetSerializer;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.util.UtilityClass;
import org.reflections.Reflections;

import java.io.FileFilter;
import java.util.*;

public class FunctionManager extends UtilityClass {
    private static final String PACKAGE_TO_SCAN = "cz.cuni.mff.respefo.function.scan";

    private static List<FunctionInfo<SingleFileFunction>> singleFileFunctions;
    private static List<FunctionInfo<MultiFileFunction>> multiFileFunctions;

    private static Map<String, FunctionAssetSerializer> serializers;
    private static Map<String, FunctionAssetDeserializer> deserializers;

    public static void scan() {
        Reflections reflections = new Reflections(PACKAGE_TO_SCAN);
        Set<Class<?>> functionClasses = reflections.getTypesAnnotatedWith(Fun.class);

        singleFileFunctions = new ArrayList<>();
        multiFileFunctions = new ArrayList<>();
        serializers = new HashMap<>();
        deserializers = new HashMap<>();

        for (Class<?> functionClass : functionClasses) {
            try {
                Fun annotation = functionClass.getAnnotation(Fun.class);

                String name = annotation.name();
                FileFilter fileFilter = annotation.fileFilter().getDeclaredConstructor().newInstance();

                Object instance = functionClass.getDeclaredConstructor().newInstance();

                if (instance instanceof SingleFileFunction) {
                    singleFileFunctions.add(new FunctionInfo<>((SingleFileFunction) instance, name, fileFilter));
                }
                if (instance instanceof MultiFileFunction) {
                    multiFileFunctions.add(new FunctionInfo<>((MultiFileFunction) instance, name, fileFilter));
                }

                Serialize serializeInfo = functionClass.getAnnotation(Serialize.class);
                if (serializeInfo != null) {
                    String key = serializeInfo.key();
                    FunctionAssetSerializer serializer = serializeInfo.serializer().getDeclaredConstructor().newInstance();
                    FunctionAssetDeserializer deserializer = serializeInfo.deserializer().getDeclaredConstructor().newInstance();

                    serializers.put(key, serializer);
                    deserializers.put(key, deserializer);
                }

            } catch (Exception exception) {
                Log.error("Error while loading function class [%s].", exception, functionClass);
            }
        }

        if (serializers.isEmpty()) {
            Log.warning("No functions were loaded.");
        }
    }

    public static List<FunctionInfo<SingleFileFunction>> getSingleFileFunctions() {
        return singleFileFunctions;
    }

    public static List<FunctionInfo<MultiFileFunction>> getMultiFileFunctions() {
        return multiFileFunctions;
    }

    public static FunctionAssetSerializer getSerializer(String key) {
        if (serializers.containsKey(key)) {
            return serializers.get(key);
        }

        throw new IllegalArgumentException("There is no function with the key [" + key + "].");
    }

    public static FunctionAssetDeserializer getDeserializer(String key) {
        if (deserializers.containsKey(key)) {
            return deserializers.get(key);
        }

        throw new IllegalArgumentException("There is no function with the key [" + key + "].");
    }

    protected FunctionManager() throws IllegalAccessException {
        super();
    }
}
