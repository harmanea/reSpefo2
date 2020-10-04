package cz.cuni.mff.respefo.function.filter;

import cz.cuni.mff.respefo.format.formats.fits.FitsFormat;
import cz.cuni.mff.respefo.util.utils.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

public class FitsFileFilter implements FileFilter {
    @Override
    public boolean accept(File pathname) {
        if (pathname.isDirectory()) {
            return false;
        }

        return Arrays.asList(FitsFormat.FILE_EXTENSIONS).contains(FileUtils.getFileExtension(pathname));
    }
}
