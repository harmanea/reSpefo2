package cz.cuni.mff.respefo.logging;

import java.time.LocalDateTime;

public class LogEntry {
    private final LocalDateTime dateTime;
    private final LogLevel level;
    private final String message;
    private final Throwable cause;

    public LogEntry(LocalDateTime dateTime, LogLevel level, String message, Throwable cause) {
        this.dateTime = dateTime;
        this.level = level;
        this.message = message;
        this.cause = cause;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public LogLevel getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getCause() {
        return cause;
    }
}
