package cz.cuni.mff.respefo.format.formats.ascii;

import cz.cuni.mff.respefo.format.formats.FileFormat;

public class AsciiFormat implements FileFormat {
    public static final String[] FILE_EXTENSIONS = { "", "txt", "asc", "ascii" };

    @Override
    public String[] fileExtensions() {
        return FILE_EXTENSIONS;
    }

    @Override
    public String name() {
        return "Default ASCII";
    }

    @Override
    public String description() {
        return "The default way of storing plain text files.\n\n" +
                "Consists of two columns of floating point numbers representing the x and y values of the data points. " +
                "The first line can optionally contain some extra information. ";
    }

    @Override
    public boolean isDefault() {
        return true;
    }
}
