package cz.cuni.mff.respefo.logging;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Log {
    private static List<LogListener> listeners = new ArrayList<>();

    public static void registerListener(LogListener listener) {
        listeners.add(listener);
    }

    public static void error(String message, Throwable cause) {
        notifyListeners(LogLevel.ERROR, message, cause);
    }

    public static void error(String message, Throwable cause, Object... params) {
        error(String.format(message, params), cause);
    }

    public static void warning(String message) {
        notifyListeners(LogLevel.WARNING, message, null);
    }

    public static void warning(String message, Object... params) {
        warning(String.format(message, params));
    }

    public static void info(String message) {
        notifyListeners(LogLevel.INFO, message, null);
    }

    public static void info(String message, Object... params) {
        info(String.format(message, params));
    }

    public static void debug(String message) {
        notifyListeners(LogLevel.DEBUG, message, null);
    }

    public static void debug(String message, Object... params) {
        debug(String.format(message, params));
    }

    public static void trace(String message) {
        notifyListeners(LogLevel.TRACE, message, null);
    }

    public static void trace(String message, Object... params) {
        trace(String.format(message, params));
    }

    private static void notifyListeners(LogLevel level, String message, Throwable cause) {
        LogEntry logEntry = new LogEntry(LocalDateTime.now(), level, message, cause);

        listeners.forEach(listener -> listener.notify(logEntry));
    }

    private Log() {}
}
