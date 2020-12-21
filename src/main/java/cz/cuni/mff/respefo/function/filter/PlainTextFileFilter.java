package cz.cuni.mff.respefo.function.filter;

import cz.cuni.mff.respefo.format.formats.ascii.AsciiFormat;
import cz.cuni.mff.respefo.util.utils.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PlainTextFileFilter implements FileFilter {

    private static final Set<String> FILE_EXTENSIONS;
    static {
        FILE_EXTENSIONS = new HashSet<>();

        FILE_EXTENSIONS.addAll(AsciiFormat.FILE_EXTENSIONS);
        FILE_EXTENSIONS.addAll(Arrays.asList("stl", "lst", "rvr", "dat", "rv", "ac", "cor", "rvs", "res", "inp", "par", "eqw", "cmf", "cmp"));
    }

    @Override
    public boolean accept(File pathname) {
        if (pathname.isDirectory()) {
            return false;
        }

        return FILE_EXTENSIONS.contains(FileUtils.getFileExtension(pathname));
    }
}
