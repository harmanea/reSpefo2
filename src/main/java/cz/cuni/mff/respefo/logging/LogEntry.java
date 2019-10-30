package cz.cuni.mff.respefo.logging;

import java.time.LocalDateTime;

public class LogEntry {
    private LocalDateTime dateTime;
    private LogLevel level;
    private String message;
    private Throwable cause;

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

    @Override
    public String toString() {
        return "LogEntry{" +
                "dateTime=" + dateTime +
                ", level=" + level +
                ", message='" + message + '\'' +
                ", cause=" + cause +
                '}';
    }
}
