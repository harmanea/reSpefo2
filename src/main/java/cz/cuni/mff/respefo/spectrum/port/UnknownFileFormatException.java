package cz.cuni.mff.respefo.spectrum.port;

import cz.cuni.mff.respefo.exception.SpefoException;

public class UnknownFileFormatException extends SpefoException {
    public UnknownFileFormatException(String message) {
        super(message);
    }
}
