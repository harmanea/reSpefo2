package cz.cuni.mff.respefo.resources;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;

import java.util.EnumMap;

import static cz.cuni.mff.respefo.resources.ImageResource.*;

public class ImageManager {
    private static EnumMap<ImageResource, LazyLoadedImage> imageMap;

    public static void init(Device device) {
        imageMap = new EnumMap(ImageResource.class);

        imageMap.put(BOOKMARK, new LazyLoadedImage(device, BOOKMARK));
        imageMap.put(FOLDER, new LazyLoadedImage(device, FOLDER));
        imageMap.put(OPENED_FOLDER, new LazyLoadedImage(device, OPENED_FOLDER));
        imageMap.put(FILE, new LazyLoadedImage(device, FILE));
    }

    public static Image getImage(ImageResource resource) {
        if (imageMap.containsKey(resource)) {
            return imageMap.get(resource).getResource();
        } else {
            throw new NoSuchResourceException("Resource [" + resource + "] could not be found.");
        }
    }
}
