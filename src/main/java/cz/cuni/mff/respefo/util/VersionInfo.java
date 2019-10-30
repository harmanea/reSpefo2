package cz.cuni.mff.respefo.util;

import cz.cuni.mff.respefo.ReSpefo;

public class VersionInfo extends UtilityClass {

    private static final String VERSION;
    static {
        String implementationVersion = ReSpefo.class.getPackage().getImplementationVersion();
        if (implementationVersion == null) {
            VERSION = "local";
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
