package cz.cuni.mff.respefo.function;

import cz.cuni.mff.respefo.format.FunctionAssetDeserializer;
import cz.cuni.mff.respefo.format.FunctionAssetSerializer;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.util.UtilityClass;
import org.reflections.Reflections;

import java.util.*;

public class FunctionManager extends UtilityClass {
    private static final String PACKAGE_TO_SCAN = "cz.cuni.mff.respefo.function";

    private static List<SingleFileFunction> singleFileFunctions;
    private static List<MultiFileFunction> multiFileFunctions;
    private static Map<String, FunctionInfo> functionInfos;

    public static void scan() {
        Reflections reflections = new Reflections(PACKAGE_TO_SCAN);

        Set<Class<?>> functions = reflections.getTypesAnnotatedWith(Fun.class);

        singleFileFunctions = new ArrayList<>();
        multiFileFunctions = new ArrayList<>();
        functionInfos = new HashMap<>();

        for (Class<?> cls : functions) {
            try {
                Fun annotation = cls.getAnnotation(Fun.class);
                String key = annotation.key();
                String name = annotation.name();
                FunctionAssetSerializer serializer = annotation.serializer().getDeclaredConstructor().newInstance();
                FunctionAssetDeserializer deserializer = annotation.deserializer().getDeclaredConstructor().newInstance();

                functionInfos.put(key, new FunctionInfo(name, serializer, deserializer));

                Object instance = cls.getDeclaredConstructor().newInstance();

                if (instance instanceof SingleFileFunction) {
                    singleFileFunctions.add((SingleFileFunction) instance);
                }
                if (instance instanceof MultiFileFunction) {
                    multiFileFunctions.add((MultiFileFunction) instance);
                }

            } catch (Exception exception) {
                Log.error("Error while loading function class [%s].", exception, cls);
            }
        }
    }

    public static List<SingleFileFunction> getSingleFileFunctions() {
        return singleFileFunctions;
    }

    public static List<MultiFileFunction> getMultiFileFunctions() {
        return multiFileFunctions;
    }

    public static FunctionAssetSerializer getSerializer(String key) {
        if (functionInfos.containsKey(key)) {
            return functionInfos.get(key).getSerializer();
        }

        throw new IllegalArgumentException("There is no function with the key [" + key + "].");
    }

    public static FunctionAssetDeserializer getDeserializer(String key) {
        if (functionInfos.containsKey(key)) {
            return functionInfos.get(key).getDeserializer();
        }

        throw new IllegalArgumentException("There is no function with the key [" + key + "].");
    }

    protected FunctionManager() throws IllegalAccessException {
        super();
    }
}

class FunctionInfo {
    private String name;
    private FunctionAssetSerializer serializer;
    private FunctionAssetDeserializer deserializer;

    FunctionInfo(String name, FunctionAssetSerializer serializer, FunctionAssetDeserializer deserializer) {
        this.name = name;
        this.serializer = serializer;
        this.deserializer = deserializer;
    }

    public String getName() {
        return name;
    }

    FunctionAssetSerializer getSerializer() {
        return serializer;
    }

    FunctionAssetDeserializer getDeserializer() {
        return deserializer;
    }
}
