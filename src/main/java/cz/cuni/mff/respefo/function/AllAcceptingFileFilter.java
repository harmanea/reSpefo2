package cz.cuni.mff.respefo.function;

import java.io.File;
import java.io.FileFilter;

public class AllAcceptingFileFilter implements FileFilter {
    @Override
    public boolean accept(File file) {
        return true;
    }
}
