package cz.cuni.mff.respefo.format.origin;

import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.util.UtilityClass;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class OriginManager extends UtilityClass {
    private static final String PACKAGE_TO_SCAN = "cz.cuni.mff.respefo.format.formats";

    private static Map<String, Class<?>> origins;

    public static void scan() {
        Reflections reflections = new Reflections(PACKAGE_TO_SCAN);
        Set<Class<?>> originClasses = reflections.getTypesAnnotatedWith(Origin.class);

        origins = new HashMap<>();

        for (Class<?> originClass : originClasses) {
            Origin annotation = originClass.getAnnotation(Origin.class);

            origins.put(annotation.key(), originClass);
        }

        if (origins.isEmpty()) {
            Log.warning("No origins were loaded.");
        }
    }

    public static Class<?> getOriginClass(String key) {
        if (origins.containsKey(key)) {
            return origins.get(key);
        }

        throw new IllegalArgumentException("There is no origin with the key [" + key + "].");
    }

    protected OriginManager() throws IllegalAccessException {
        super();
    }
}
