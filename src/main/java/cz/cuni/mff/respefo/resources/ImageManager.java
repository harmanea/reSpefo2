package cz.cuni.mff.respefo.resources;

import cz.cuni.mff.respefo.spectrum.port.FormatManager;
import cz.cuni.mff.respefo.util.UtilityClass;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;

import java.io.File;
import java.util.EnumMap;

import static cz.cuni.mff.respefo.resources.ImageResource.*;
import static cz.cuni.mff.respefo.util.utils.FileUtils.getFileExtension;

public class ImageManager extends UtilityClass {
    private static final String BASE_FOLDER = "/image/";

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

    public static Image getIconForFile(File file) {
        if (file.isDirectory()) {
            return getImage(FOLDER);
        } else {
            String fileExtension = getFileExtension(file);

            if (fileExtension.equals("spf")) {
                return getImage(SPECTRUM_FILE);
            } else if (FormatManager.getImportableFileExtensions().contains(fileExtension)) {
                return getImage(IMPORTABLE_FILE);
            } else if (fileExtension.equals("stl") || fileExtension.equals("lst")) {
                return getImage(SUPPORT_FILE);
            } else {
                return getImage(FILE);
            }
        }
    }

    protected ImageManager() throws IllegalAccessException {
        super();
    }
}
