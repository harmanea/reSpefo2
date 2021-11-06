package cz.cuni.mff.respefo.function;

import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.spectrum.asset.FunctionAsset;
import cz.cuni.mff.respefo.util.UtilityClass;
import org.reflections.Reflections;

import java.io.FileFilter;
import java.util.*;

public class FunctionManager extends UtilityClass {
    private static final String PACKAGE_TO_SCAN = "cz.cuni.mff.respefo.function";

    private static List<FunctionInfo<SingleFileFunction>> singleFileFunctions;
    private static List<FunctionInfo<MultiFileFunction>> multiFileFunctions;
    private static List<FunctionInfo<ProjectFunction>> projectFunctions;

    private static Map<String, Class<? extends FunctionAsset>> assetClasses;

    public static void scan() {
        Reflections reflections = new Reflections(PACKAGE_TO_SCAN);
        Set<Class<?>> functionClasses = reflections.getTypesAnnotatedWith(Fun.class);

        singleFileFunctions = new ArrayList<>();
        multiFileFunctions = new ArrayList<>();
        projectFunctions = new ArrayList<>();
        assetClasses = new HashMap<>();

        for (Class<?> functionClass : functionClasses) {
            try {
                Fun funAnnotation = functionClass.getAnnotation(Fun.class);

                String name = funAnnotation.name();
                FileFilter fileFilter = funAnnotation.fileFilter().getDeclaredConstructor().newInstance();
                String group = funAnnotation.group().equals("") ? null : funAnnotation.group();

                Object instance = functionClass.getDeclaredConstructor().newInstance();

                if (instance instanceof SingleFileFunction) {
                    singleFileFunctions.add(new FunctionInfo<>((SingleFileFunction) instance, name, fileFilter, group));
                }
                if (instance instanceof MultiFileFunction) {
                    multiFileFunctions.add(new FunctionInfo<>((MultiFileFunction) instance, name, fileFilter, group));
                }
                if (instance instanceof ProjectFunction) {
                    projectFunctions.add(new FunctionInfo<>((ProjectFunction) instance, name, fileFilter, group));
                }

                Serialize serializeAnnotation = functionClass.getAnnotation(Serialize.class);
                if (serializeAnnotation != null) {
                    assetClasses.put(serializeAnnotation.key(), serializeAnnotation.assetClass());
                }

            } catch (Exception exception) {
                Log.error("Error while loading function class [%s].", exception, functionClass);
            }
        }

        if (singleFileFunctions.isEmpty() && multiFileFunctions.isEmpty() && projectFunctions.isEmpty()) {
            Log.warning("No functions were loaded.");
        } else {
            singleFileFunctions.sort(Comparator.comparing(FunctionInfo::getName));
            multiFileFunctions.sort(Comparator.comparing(FunctionInfo::getName));
            projectFunctions.sort(Comparator.comparing(FunctionInfo::getName));
        }
    }

    public static List<FunctionInfo<SingleFileFunction>> getSingleFileFunctions() {
        return singleFileFunctions;
    }

    public static List<FunctionInfo<MultiFileFunction>> getMultiFileFunctions() {
        return multiFileFunctions;
    }

    public static List<FunctionInfo<ProjectFunction>> getProjectFunctions() {
        return projectFunctions;
    }

    public static Class<? extends FunctionAsset> getAssetClass(String key) {
        if (assetClasses.containsKey(key)) {
            return assetClasses.get(key);
        }

        throw new IllegalArgumentException("There is no function asset class with the key [" + key + "].");
    }

    public static SingleFileFunction getSingleFileFunctionByName(String name) {
        for (FunctionInfo<SingleFileFunction> functionInfo : singleFileFunctions) {
            if (functionInfo.getName().equals(name)) {
                return functionInfo.getInstance();
            }
        }

        throw new IllegalArgumentException("There is no function named [" + name + "]");
    }

    public static MultiFileFunction getMultiFileFunctionByName(String name) {
        for (FunctionInfo<MultiFileFunction> functionInfo : multiFileFunctions) {
            if (functionInfo.getName().equals(name)) {
                return functionInfo.getInstance();
            }
        }

        throw new IllegalArgumentException("There is no function named [" + name + "]");
    }

    protected FunctionManager() throws IllegalAccessException {
        super();
    }
}
