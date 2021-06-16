package cz.cuni.mff.respefo.function.filter;

import cz.cuni.mff.respefo.util.utils.FileUtils;

import java.io.File;
import java.io.FileFilter;

public class SpefoFormatFileFilter implements FileFilter {
    @Override
    public boolean accept(File pathname) {
        if (pathname.isDirectory()) {
            return false;
        }

        return FileUtils.getFileExtension(pathname).equals("spf");
    }
}
