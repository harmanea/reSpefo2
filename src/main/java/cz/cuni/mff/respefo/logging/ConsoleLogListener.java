package cz.cuni.mff.respefo.logging;

import static java.lang.System.out;

/**
 * More of a proof of concept rather than an actual useful implementation.
 *
 * Can be used for debugging.
 */
public class ConsoleLogListener implements LogListener {

    private static final String PATTERN = "[%s] %s - %s%n";

    @Override
    public void notify(LogEntry entry) {
        out.printf(PATTERN, entry.getDateTime(), entry.getLevel().toString(), entry.getMessage());

        if (entry.getCause() != null) {
            entry.getCause().printStackTrace(out);
        }
    }
}
