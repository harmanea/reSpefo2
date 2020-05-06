package cz.cuni.mff.respefo.function;

import cz.cuni.mff.respefo.format.FunctionAsset;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.util.UtilityClass;
import org.reflections.Reflections;

import java.io.FileFilter;
import java.util.*;

public class FunctionManager extends UtilityClass {
    private static final String PACKAGE_TO_SCAN = "cz.cuni.mff.respefo.function.scan";

    private static List<FunctionInfo<SingleFileFunction>> singleFileFunctions;
    private static List<FunctionInfo<MultiFileFunction>> multiFileFunctions;

    private static Map<String, Class<? extends FunctionAsset>> assetClasses;

    public static void scan() {
        Reflections reflections = new Reflections(PACKAGE_TO_SCAN);
        Set<Class<?>> functionClasses = reflections.getTypesAnnotatedWith(Fun.class);

        singleFileFunctions = new ArrayList<>();
        multiFileFunctions = new ArrayList<>();
        assetClasses = new HashMap<>();

        for (Class<?> functionClass : functionClasses) {
            try {
                Fun funAnnotation = functionClass.getAnnotation(Fun.class);

                String name = funAnnotation.name();
                FileFilter fileFilter = funAnnotation.fileFilter().getDeclaredConstructor().newInstance();

                Object instance = functionClass.getDeclaredConstructor().newInstance();

                if (instance instanceof SingleFileFunction) {
                    singleFileFunctions.add(new FunctionInfo<>((SingleFileFunction) instance, name, fileFilter));
                }
                if (instance instanceof MultiFileFunction) {
                    multiFileFunctions.add(new FunctionInfo<>((MultiFileFunction) instance, name, fileFilter));
                }

                Serialize serializeAnnotation = functionClass.getAnnotation(Serialize.class);
                if (serializeAnnotation != null) {
                    assetClasses.put(serializeAnnotation.key(), serializeAnnotation.assetClass());
                }

            } catch (Exception exception) {
                Log.error("Error while loading function class [%s].", exception, functionClass);
            }
        }

        if (singleFileFunctions.isEmpty() && multiFileFunctions.isEmpty()) {
            Log.warning("No functions were loaded.");
        }
    }

    public static List<FunctionInfo<SingleFileFunction>> getSingleFileFunctions() {
        return singleFileFunctions;
    }

    public static List<FunctionInfo<MultiFileFunction>> getMultiFileFunctions() {
        return multiFileFunctions;
    }

    public static Class<? extends FunctionAsset> getAssetClass(String key) {
        if (assetClasses.containsKey(key)) {
            return assetClasses.get(key);
        }

        throw new IllegalArgumentException("There is no function asset class with the key [" + key + "].");
    }

    protected FunctionManager() throws IllegalAccessException {
        super();
    }
}
