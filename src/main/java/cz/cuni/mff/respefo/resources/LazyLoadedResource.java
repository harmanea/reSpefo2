package cz.cuni.mff.respefo.resources;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Resource;

public abstract class LazyLoadedResource<T extends Resource, E extends Enum<E>> {
    private T resource;

    private Device device;
    private E key;

    public LazyLoadedResource(Device device, E key) {
        this.device = device;
        this.key = key;
    }

    public T getResource() {
        if (resource == null) {
            resource = loadResource(device, key);
        }

        return resource;
    }

    protected abstract T loadResource(Device device, E key);
}
