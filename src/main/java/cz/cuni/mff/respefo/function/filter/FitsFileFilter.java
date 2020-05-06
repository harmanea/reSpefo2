package cz.cuni.mff.respefo.function.filter;

import cz.cuni.mff.respefo.format.scan.FitsFormat;
import cz.cuni.mff.respefo.util.utils.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

public class FitsFileFilter implements FileFilter {
    @Override
    public boolean accept(File pathname) {
        return Arrays.asList(FitsFormat.FILE_EXTENSIONS).contains(FileUtils.getFileExtension(pathname));
    }
}
