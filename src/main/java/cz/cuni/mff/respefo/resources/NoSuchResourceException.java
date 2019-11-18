package cz.cuni.mff.respefo.resources;

public class NoSuchResourceException extends RuntimeException {
    public NoSuchResourceException(String message) {
        super(message);
    }

    public NoSuchResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
