package cz.cuni.mff.respefo.util.utils;

import cz.cuni.mff.respefo.util.UtilityClass;

import java.util.*;

public class CollectionUtils extends UtilityClass {
    @SafeVarargs
    public static <T> Set<T> setOf(T... values) {
        Set<T> set = new HashSet<>(Arrays.asList(values));
        return Collections.unmodifiableSet(set);
    }

    @SafeVarargs
    public static <T> List<T> listOf(T... values) {
        List<T> list = Arrays.asList(values);
        return Collections.unmodifiableList(list);
    }

    protected CollectionUtils() throws IllegalAccessException {
        super();
    }
}
