package cz.cuni.mff.respefo.function.scan;

import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.filter.DirectoryFileFilter;
import cz.cuni.mff.respefo.util.Message;

import java.awt.*;
import java.io.File;

@Fun(name = "Open in File Manager", fileFilter = DirectoryFileFilter.class)
public class OpenInFileManagerFunction implements SingleFileFunction {
    @Override
    public void execute(File file) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(file);

            } catch (Exception exception) {
                Message.error("Couldn't open directory.", exception);
            }
        } else {
            Message.warning("Opening files using the associated application is not available on this OS.");
        }
    }
}
