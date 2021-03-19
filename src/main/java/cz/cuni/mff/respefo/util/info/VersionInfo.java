package cz.cuni.mff.respefo.util.info;

import cz.cuni.mff.respefo.ReSpefo;
import cz.cuni.mff.respefo.util.UtilityClass;

public class VersionInfo extends UtilityClass {

    private static final String VERSION;
    static {
        String implementationVersion = ReSpefo.class.getPackage().getImplementationVersion();
        if (implementationVersion == null) {
            VERSION = System.getProperty("respefo.version");
        } else {
            VERSION = implementationVersion;
        }
    }

    public static String getVersion() {
        return VERSION;
    }

    protected VersionInfo() throws IllegalAccessException {
        super();
    }
}
