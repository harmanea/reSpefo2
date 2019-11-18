package cz.cuni.mff.respefo.resources;

class NoSuchResourceException extends RuntimeException {
    NoSuchResourceException(String message) {
        super(message);
    }

    NoSuchResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
