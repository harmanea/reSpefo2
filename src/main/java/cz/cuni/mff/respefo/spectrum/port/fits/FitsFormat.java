package cz.cuni.mff.respefo.spectrum.port.fits;

import cz.cuni.mff.respefo.spectrum.port.FileFormat;

import java.util.List;

import static cz.cuni.mff.respefo.util.utils.CollectionUtils.listOf;

public class FitsFormat implements FileFormat {
    public static final List<String> FILE_EXTENSIONS = listOf("fits", "fts", "fit");

    @Override
    public final List<String> fileExtensions() {
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
