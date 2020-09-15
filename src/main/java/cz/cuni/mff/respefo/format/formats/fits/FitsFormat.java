package cz.cuni.mff.respefo.format.formats.fits;

import cz.cuni.mff.respefo.format.formats.FileFormat;

public class FitsFormat implements FileFormat {
    public static final String[] FILE_EXTENSIONS = { "fits", "fts", "fit"};

    @Override
    public String[] fileExtensions() {
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
