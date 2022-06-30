package cz.cuni.mff.respefo.resources;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;

public enum ColorResource {
    BLACK(0, 0, 0),
    BLUE(0, 0, 255),
    CYAN(0, 255, 255),
    GREEN(0, 255, 0),
    GRAY(128, 128, 128),
    ORANGE(255, 128, 0),
    PINK(255, 0, 255),
    PURPLE(128, 0, 255),
    RED(255, 0, 0),
    YELLOW(255, 255, 0),
    WHITE(255, 255, 255),
    MAGENTA(255, 0, 255),

    GOLD(255, 192, 0),
    SILVER(192, 192, 192),
    BROWN(128, 64, 0),
    MAROON(128, 0, 0),
    OLIVE(128, 128, 0),
    TEAL(0, 128, 128),
    NAVY(0, 0, 128),
    SALMON(255, 128, 128),
    KHAKI(192, 192, 128),
    INDIGO(64, 0, 128),
    ROYAL(64, 128, 255);

    private final int red;
    private final int green;
    private final int blue;
    private final int alpha;

    ColorResource(int red, int green, int blue) {
        this(red, green, blue, 255);
    }

    ColorResource(int red, int green, int blue, int alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public Color toColor(Device device) {
        return new Color(device, red, green, blue, alpha);
    }
}
