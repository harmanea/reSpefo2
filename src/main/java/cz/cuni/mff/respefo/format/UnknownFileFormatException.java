package cz.cuni.mff.respefo.format;

import cz.cuni.mff.respefo.SpefoException;

public class UnknownFileFormatException extends SpefoException {
    public UnknownFileFormatException(String message) {
        super(message);
    }
}
