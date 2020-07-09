package cz.cuni.mff.respefo.util.utils;

import cz.cuni.mff.respefo.util.UtilityClass;

public class StringUtils extends UtilityClass {

    public static String substringBefore(String str, int ch) {
        int index = str.indexOf(ch);
        if (index == -1) {
            return str;
        } else {
            return str.substring(0, index);
        }
    }

    protected StringUtils() throws IllegalAccessException {
        super();
    }
}
