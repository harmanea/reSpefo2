package cz.cuni.mff.respefo.function.filter;

import cz.cuni.mff.respefo.util.utils.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

public class SpefoAndLstFileFilter implements FileFilter {

    public static final List<String> FILE_EXTENSIONS = unmodifiableList(asList("spf", "lst"));

    @Override
    public boolean accept(File pathname) {
        if (pathname.isDirectory()) {
            return false;
        }

        return FILE_EXTENSIONS.contains(FileUtils.getFileExtension(pathname));
    }
}
