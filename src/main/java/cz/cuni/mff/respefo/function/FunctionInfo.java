package cz.cuni.mff.respefo.function;

import java.io.FileFilter;
import java.util.Optional;

public class FunctionInfo<T> {
    private final T instance;
    private final String name;
    private final FileFilter fileFilter;
    private final String group;

    public FunctionInfo(T instance, String name, FileFilter fileFilter, String group) {
        this.instance = instance;
        this.name = name;
        this.fileFilter = fileFilter;
        this.group = group;
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

    public Optional<String> getGroup() {
        return Optional.ofNullable(group);
    }
}
