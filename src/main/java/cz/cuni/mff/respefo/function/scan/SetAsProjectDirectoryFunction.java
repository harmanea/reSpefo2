package cz.cuni.mff.respefo.function.scan;

import cz.cuni.mff.respefo.component.Project;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.filter.DirectoryFileFilter;

import java.io.File;

@Fun(name = "Set as Project Directory", fileFilter = DirectoryFileFilter.class)
public class SetAsProjectDirectoryFunction implements SingleFileFunction {
    @Override
    public void execute(File file) {
        Project.setRootDirectory(file);
    }
}
