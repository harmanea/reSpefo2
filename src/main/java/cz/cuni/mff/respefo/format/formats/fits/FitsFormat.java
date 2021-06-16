package cz.cuni.mff.respefo.format.formats.fits;

import cz.cuni.mff.respefo.format.formats.FileFormat;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

public class FitsFormat implements FileFormat {
    public static final List<String> FILE_EXTENSIONS = unmodifiableList(asList("fits", "fts", "fit"));

    @Override
    public List<String> fileExtensions() {
        return FILE_EXTENSIONS;
    }

    @Override
    public String name() {
        return "Default FITS";
    }

    @Override
    public String description() {
        return "The default way of storing spectrum FITS files.\n\n" +
                "FITS (Flexible Image Transport System) format is defined in 'Astronomy and Astrophysics', volume 376, page 359.";
    }

    @Override
    public boolean isDefault() {
        return true;
    }
}
