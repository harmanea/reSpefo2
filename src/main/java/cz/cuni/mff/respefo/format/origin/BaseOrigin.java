package cz.cuni.mff.respefo.format.origin;

import cz.cuni.mff.respefo.util.utils.FileUtils;

import java.nio.file.Paths;

public abstract class BaseOrigin {
    protected String fileName;

    public BaseOrigin() {
        // default empty constructor
    }

    public BaseOrigin(String fileName) {
        this.fileName = FileUtils.getRelativePath(Paths.get(fileName)).toString();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
