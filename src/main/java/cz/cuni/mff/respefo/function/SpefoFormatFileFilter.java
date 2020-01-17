package cz.cuni.mff.respefo.function;

import cz.cuni.mff.respefo.util.utils.FileUtils;

import java.io.File;
import java.io.FileFilter;

public class SpefoFormatFileFilter implements FileFilter {
    @Override
    public boolean accept(File pathname) {
        return FileUtils
                .getFileExtension(pathname)
                .equals("spf");
    }
}
