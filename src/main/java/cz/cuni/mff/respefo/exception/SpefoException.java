package cz.cuni.mff.respefo.exception;

import java.io.FileNotFoundException;

public class SpefoException extends Exception {
    public SpefoException() {
        super();
    }

    public SpefoException(String message) {
        super(message);
    }

    public SpefoException(Throwable cause) {
        super(cause);
    }

    public SpefoException(String message, Throwable cause) {
        super(message, cause);
    }

    public SpefoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public static SpefoException fromFileNotFoundException(FileNotFoundException exception, String fileName) {
        return new SpefoException("Cannot find file [" + fileName + "]", exception);
    }
}
