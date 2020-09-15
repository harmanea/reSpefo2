package cz.cuni.mff.respefo.format.formats;

public interface FileFormat {
    String[] fileExtensions();
    String name();
    String description();
    boolean isDefault();
}
