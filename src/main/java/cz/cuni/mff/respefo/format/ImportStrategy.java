package cz.cuni.mff.respefo.format;

public interface ImportStrategy {
    String name();
    String description();
    boolean isDefault();
}
