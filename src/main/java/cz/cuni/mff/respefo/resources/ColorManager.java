package cz.cuni.mff.respefo.resources;

import cz.cuni.mff.respefo.util.UtilityClass;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;

import java.util.EnumMap;

public class ColorManager extends UtilityClass {
    private static EnumMap<ColorResource, Color> colorMap;

    public static void init(Device device) {
        colorMap = new EnumMap<>(ColorResource.class);

        for (ColorResource c : ColorResource.values()) {
            addColor(device, c);
        }
    }

    private static void addColor(Device device, ColorResource resource) {
        try {
            colorMap.put(resource, resource.toColor(device));
        } catch (Exception exception) {
            throw new NoSuchResourceException("Resource [" + resource + "] could not be loaded.", exception);
        }
    }

    public static Color getColor(ColorResource resource) {
        if (colorMap.containsKey(resource)) {
            return colorMap.get(resource);
        } else {
            throw new NoSuchResourceException("Resource [" + resource + "] could not be found.");
        }
    }

    protected ColorManager() throws IllegalAccessException {
        super();
    }
}
