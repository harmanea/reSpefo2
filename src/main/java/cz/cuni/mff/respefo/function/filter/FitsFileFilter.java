package cz.cuni.mff.respefo.function.filter;

import cz.cuni.mff.respefo.spectrum.port.fits.FitsFormat;
import cz.cuni.mff.respefo.util.utils.FileUtils;

import java.io.File;
import java.io.FileFilter;

public class FitsFileFilter implements FileFilter {
    @Override
    public boolean accept(File pathname) {
        if (pathname.isDirectory()) {
            return false;
        }

        return FitsFormat.FILE_EXTENSIONS.contains(FileUtils.getFileExtension(pathname));
    }
}
