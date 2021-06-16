package cz.cuni.mff.respefo.resources;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;

public enum ColorResource {
    BLACK(0, 0, 0),
    BLUE(0, 0, 255),
    CYAN(0, 255, 255),
    GREEN(0, 255, 0),
    LIGHT_GRAY(192, 192, 192),
    GRAY(128, 128, 128),
    ORANGE(255, 128, 0),
    PINK(255, 0, 255),
    PURPLE(128, 0, 255),
    RED(255, 0, 0),
    YELLOW(255, 255, 0),
    WHITE(255, 255, 255);

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
