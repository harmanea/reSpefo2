package cz.cuni.mff.respefo.exception;

public class InvalidFileFormatException extends SpefoException {
    public InvalidFileFormatException(String message) {
        super(message);
    }

    public InvalidFileFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
