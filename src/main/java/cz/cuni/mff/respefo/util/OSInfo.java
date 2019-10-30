package cz.cuni.mff.respefo.util;

import static cz.cuni.mff.respefo.util.OperatingSystem.*;

public class OSInfo extends UtilityClass {
    private static final OperatingSystem OS;
    private static final String BITNESS = System.getProperty("sun.arch.data.model");

    static {
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("win")) {
            OS = WINDOWS;
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            OS = UNIX;
        } else if (osName.contains("mac")) {
            OS = MAC;
        } else {
            OS = UNKNOWN;
        }
    }

    public static OperatingSystem getOperatingSystem() {
        return OS;
    }

    public static String getBitness() {
        return BITNESS;
    }

    protected OSInfo() throws IllegalAccessException {
        super();
    }
}
