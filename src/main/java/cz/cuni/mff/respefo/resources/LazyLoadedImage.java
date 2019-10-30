package cz.cuni.mff.respefo.resources;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;

public class LazyLoadedImage extends LazyLoadedResource<Image, ImageResource> {

    public LazyLoadedImage(Device device, ImageResource key) {
        super(device, key);
    }

    @Override
    protected Image loadResource(Device device, ImageResource key) {
        return new Image(device, LazyLoadedImage.class.getResourceAsStream(key.getFileName()));
    }
}
