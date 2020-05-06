package cz.cuni.mff.respefo.logging;

import java.time.LocalDateTime;

public class LogEntry {
    private LocalDateTime dateTime;
    private LogLevel level;
    private String message;
    private Throwable cause;
    private Runnable action;

    public LogEntry(LocalDateTime dateTime, LogLevel level, String message, Throwable cause, Runnable action) {
        this.dateTime = dateTime;
        this.level = level;
        this.message = message;
        this.cause = cause;
        this.action = action;
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

    public Runnable getAction() {
        return action;
    }

    @Override
    public String toString() {
        return "LogEntry{" +
                "dateTime=" + dateTime +
                ", level=" + level +
                ", message='" + message + '\'' +
                ", cause=" + cause +
                ", action=" + action +
                '}';
    }
}
