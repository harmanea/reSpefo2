package cz.cuni.mff.respefo.spectrum.port.ascii;

import cz.cuni.mff.respefo.spectrum.port.FileFormat;

import java.util.List;

import static cz.cuni.mff.respefo.util.utils.CollectionUtils.listOf;

public class AsciiFormat implements FileFormat {
    public static final List<String> FILE_EXTENSIONS = listOf("", "txt", "asc", "ascii");

    @Override
    public List<String> fileExtensions() {
        return FILE_EXTENSIONS;
    }

    @Override
    public String name() {
        return "Default ASCII";
    }

    @Override
    public String description() {
        return "The default way of storing plain text files.\n\n" +
                "Consists of two columns of floating point numbers representing the x and y values of the data points." +
                "The first line can optionally contain some extra information.";
    }

    @Override
    public boolean isDefault() {
        return true;
    }
}
