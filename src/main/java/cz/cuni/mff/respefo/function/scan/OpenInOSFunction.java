package cz.cuni.mff.respefo.function.scan;

import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.util.Message;

import java.awt.*;
import java.io.File;

@Fun(name = "__ Open in File Manager __")
public class OpenInOSFunction implements SingleFileFunction {
    @Override
    public void execute(File file) {
        if (Desktop.isDesktopSupported()) {
            try {
                if (!file.isDirectory()) {
                    file = file.getParentFile();
                }

                Desktop.getDesktop().open(file);

            } catch (Exception exception) {
                Message.error("Couldn't open directory.", exception);
            }
        } else {
            Message.warning("Opening files using the associated application is not available on this OS.");
        }
    }
}
