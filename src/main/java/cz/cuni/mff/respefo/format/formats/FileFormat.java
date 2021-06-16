package cz.cuni.mff.respefo.format.formats;

import java.util.List;

public interface FileFormat {
    List<String> fileExtensions();
    String name();
    String description();
    boolean isDefault();
}
