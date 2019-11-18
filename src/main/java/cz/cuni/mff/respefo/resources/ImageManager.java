package cz.cuni.mff.respefo.resources;

import cz.cuni.mff.respefo.util.UtilityClass;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;

import java.util.EnumMap;

import static java.io.File.separator;

public class ImageManager extends UtilityClass {
    private static final String BASE_FOLDER = separator + "image" + separator;

    private static EnumMap<ImageResource, Image> imageMap;

    public static void init(Device device) {
        imageMap = new EnumMap<>(ImageResource.class);

        for (ImageResource i : ImageResource.values()) {
            addImage(device, i);
        }
    }

    private static void addImage(Device device, ImageResource resource) {
        try {
            String path = BASE_FOLDER + resource.getFileName();
            Image image = new Image(device, ImageManager.class.getResourceAsStream(path));
            imageMap.put(resource, image);

        } catch (Exception exception) {
            throw new NoSuchResourceException("Resource [" + resource + "] could not be loaded.", exception);
        }
    }

    public static Image getImage(ImageResource resource) {
        if (imageMap.containsKey(resource)) {
            return imageMap.get(resource);
        } else {
            throw new NoSuchResourceException("Resource [" + resource + "] could not be found.");
        }
    }

    protected ImageManager() throws IllegalAccessException {
        super();
    }
}
