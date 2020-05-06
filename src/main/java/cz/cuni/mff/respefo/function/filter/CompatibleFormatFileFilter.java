package cz.cuni.mff.respefo.function.filter;

import cz.cuni.mff.respefo.format.FormatManager;
import cz.cuni.mff.respefo.util.utils.FileUtils;

import java.io.File;
import java.io.FileFilter;

public class CompatibleFormatFileFilter implements FileFilter {
    @Override
    public boolean accept(File pathname) {
        if (pathname.isDirectory()) {
            return false;
        }

        String fileExtension = FileUtils.getFileExtension(pathname.getPath());

        return FormatManager.getKnownFileExtensions().contains(fileExtension);
    }
}
