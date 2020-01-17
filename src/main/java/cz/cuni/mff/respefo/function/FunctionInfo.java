package cz.cuni.mff.respefo.function;

import java.io.FileFilter;

public class FunctionInfo<T> {
    private T instance;
    private String name;
    private FileFilter fileFilter;

    public FunctionInfo(T instance, String name, FileFilter fileFilter) {
        this.instance = instance;
        this.name = name;
        this.fileFilter = fileFilter;
    }

    public T getInstance() {
        return instance;
    }

    public String getName() {
        return name;
    }

    public FileFilter getFileFilter() {
        return fileFilter;
    }
}
