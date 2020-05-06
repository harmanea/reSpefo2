package cz.cuni.mff.respefo.util;

public class NotYetImplementedException extends RuntimeException {
    /**
     * @deprecated to highlight it in the IDE
     */
    @Deprecated
    public NotYetImplementedException() {
        super("This function is not yet implemented!");
    }
}
