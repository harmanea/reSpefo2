package cz.cuni.mff.respefo.function.filter;

import cz.cuni.mff.respefo.spectrum.port.ascii.AsciiFormat;
import cz.cuni.mff.respefo.util.utils.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PlainTextFileFilter implements FileFilter {

    public static final Set<String> FILE_EXTENSIONS;
    static {
        Set<String> set = new HashSet<>();

        set.addAll(AsciiFormat.FILE_EXTENSIONS);
        set.addAll(Arrays.asList("stl", "lst", "rvr", "dat", "rv", "ac", "cor", "rvs", "res", "inp", "par", "eqw", "cmf", "cmp"));

        FILE_EXTENSIONS = Collections.unmodifiableSet(set);
    }

    @Override
    public boolean accept(File pathname) {
        if (pathname.isDirectory()) {
            return false;
        }

        return FILE_EXTENSIONS.contains(FileUtils.getFileExtension(pathname));
    }
}
