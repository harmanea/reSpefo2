package cz.cuni.mff.respefo;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.format.FormatManager;
import cz.cuni.mff.respefo.format.origin.OriginManager;
import cz.cuni.mff.respefo.function.FunctionManager;
import cz.cuni.mff.respefo.resources.ColorManager;
import cz.cuni.mff.respefo.resources.ImageManager;
import cz.cuni.mff.respefo.util.Progress;

public class ReSpefo {

    public static void main(String[] args) throws Exception {

        // Scan for annotations -> fill function managers
        FunctionManager.scan();
        FormatManager.scan();
        OriginManager.scan();

        // Create Display & Shell -> init resource managers
        ComponentManager.init();

        ImageManager.init(ComponentManager.getDisplay());
        ColorManager.init(ComponentManager.getDisplay());
        Progress.init(ComponentManager.getDisplay());

        ComponentManager.build();

        // Main loop
        ComponentManager.open();

    }
}
