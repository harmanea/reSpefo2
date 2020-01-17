package cz.cuni.mff.respefo.util.utils;

import cz.cuni.mff.respefo.util.UtilityClass;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtils extends UtilityClass {
    public static String getStackTrace(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        throwable.printStackTrace(printWriter);

        return stringWriter.toString();
    }

    protected ExceptionUtils() throws IllegalAccessException {
        super();
    }
}
