package cz.cuni.mff.respefo.logging;

public enum LogLevel {
    ERROR,
    WARNING,
    INFO,
    DEBUG,
    TRACE;

    public boolean isMoreImportantOrEqualTo(LogLevel other) {
        return this.compareTo(other) <= 0;
    }

    public boolean isLessImportantThan(LogLevel other) {
        return this.compareTo(other) > 0;
    }
}
