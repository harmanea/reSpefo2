package cz.cuni.mff.respefo.util.utils;

import cz.cuni.mff.respefo.util.UtilityClass;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;

public class ExceptionUtils extends UtilityClass {

    /**
     * Returns the exception stacktrace as a string.
     * @param throwable whose stacktrace to extract
     * @return string representation of the stacktrace
     * @see Exception#printStackTrace()
     */
    public static String getStackTrace(Throwable throwable) {
        Objects.requireNonNull(throwable);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        throwable.printStackTrace(printWriter);

        return stringWriter.toString();
    }

    protected ExceptionUtils() throws IllegalAccessException {
        super();
    }
}
