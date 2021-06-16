package cz.cuni.mff.respefo.format;

import cz.cuni.mff.respefo.SpefoException;

public class InvalidFileFormatException extends SpefoException {
    public InvalidFileFormatException(String message) {
        super(message);
    }

    public InvalidFileFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
