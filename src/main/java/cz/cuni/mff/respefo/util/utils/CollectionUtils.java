package cz.cuni.mff.respefo.util.utils;

import cz.cuni.mff.respefo.util.UtilityClass;
import cz.cuni.mff.respefo.util.collections.tuple.Pair;

import java.util.*;

public class CollectionUtils extends UtilityClass {
    /**
     * Creates a fixed-size unmodifiable set of the specified values.
     */
    @SafeVarargs
    public static <T> Set<T> setOf(T... values) {
        Set<T> set = new HashSet<>(Arrays.asList(values));
        return Collections.unmodifiableSet(set);
    }

    /**
     * Creates a fixed-size unmodifiable list of the specified values.
     */
    @SafeVarargs
    public static <T> List<T> listOf(T... values) {
        List<T> list = Arrays.asList(values);
        return Collections.unmodifiableList(list);
    }

    @SafeVarargs
    public static <K, V> Map<K, V> mapOf(Pair<K, V>... entries) {
        Map<K, V> map = new HashMap<>();
        for (Pair<K, V> entry : entries) {
            map.put(entry.a, entry.b);
        }
        return Collections.unmodifiableMap(map);
    }

    protected CollectionUtils() throws IllegalAccessException {
        super();
    }
}
