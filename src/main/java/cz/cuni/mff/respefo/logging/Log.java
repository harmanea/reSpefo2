package cz.cuni.mff.respefo.logging;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Log {
    private static List<LogListener> listeners = new ArrayList<>();

    public static void registerListener(LogListener listener) {
        listeners.add(listener);
    }

    public static void error(String message) {
        notifyListeners(LogLevel.ERROR, message, null);
    }

    public static void error(String message, Throwable cause) {
        notifyListeners(LogLevel.ERROR, message, cause);
    }

    public static void error(String message, Throwable cause, Object... params) {
        notifyListeners(LogLevel.ERROR, String.format(message, params), cause);
    }

    public static void warning(String message) {
        notifyListeners(LogLevel.WARNING, message, null);
    }

    public static void warning(String message, Object... params) {
        notifyListeners(LogLevel.WARNING, String.format(message, params), null);
    }

    public static void info(String message) {
        notifyListeners(LogLevel.INFO, message, null);
    }

    public static void info(String message, Object... params) {
        notifyListeners(LogLevel.INFO, String.format(message, params), null);
    }

    public static void debug(String message) {
        notifyListeners(LogLevel.DEBUG, message, null);
    }

    public static void debug(String message, Object... params) {
        notifyListeners(LogLevel.DEBUG, String.format(message, params), null);
    }

    public static void trace(String message) {
        notifyListeners(LogLevel.TRACE, message, null);
    }

    public static void trace(String message, Object... params) {
        notifyListeners(LogLevel.TRACE, String.format(message, params), null);
    }

    private static void notifyListeners(LogLevel level, String message, Throwable cause) {
        notifyListeners(level, message, cause, null);
    }

    private static void notifyListeners(LogLevel level, String message, Throwable cause, Runnable action) {
        LogEntry logEntry = new LogEntry(LocalDateTime.now(), level, message, cause, action);

        for (LogListener listener : listeners) {
            listener.notify(logEntry);
        }
    }

    private Log() {}
}
