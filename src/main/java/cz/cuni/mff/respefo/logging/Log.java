package cz.cuni.mff.respefo.logging;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Log {
    private static final Map<LogListener, LogLevel> listeners = new HashMap<>();
    private static final List<LogActionListener> actionListeners = new ArrayList<>();

    public static void registerListener(LogListener listener, LogLevel minimumLevel) {
        listeners.put(listener, minimumLevel);
    }

    public static void registerActionListener(LogActionListener listener) {
        actionListeners.add(listener);
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

    public static void action(String text, String label, Runnable action, boolean oneShot) {
        LogAction logAction = new LogAction(text, label, action, oneShot);

        for (LogActionListener listener : actionListeners) {
            listener.notify(logAction);
        }
    }

    private static void notifyListeners(LogLevel level, String message, Throwable cause) {
        LogEntry logEntry = new LogEntry(LocalDateTime.now(), level, message, cause);

        for (Map.Entry<LogListener, LogLevel> listenerEntry : listeners.entrySet()) {
            if (level.isMoreImportantOrEqualTo(listenerEntry.getValue())) {
                listenerEntry.getKey().notify(logEntry);
            }
        }
    }

    private Log() {}
}
