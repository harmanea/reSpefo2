package cz.cuni.mff.respefo.logging;

import org.eclipse.swt.widgets.Label;

import java.time.format.DateTimeFormatter;

public class LabelLogListener implements LogListener {

    private static final int DECAY_TIME_IN_MILLISECONDS = 60 * 1000;
    private static final String DATE_TIME_PATTERN = "HH:mm:ss";

    private Label label;
    private DateTimeFormatter formatter;
    private Runnable clearLabel = () -> label.setText("");

    public LabelLogListener(Label label) {
        this.label = label;
        this.formatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
    }

    @Override
    public void notify(LogEntry entry) {
        if (entry.getLevel().isMoreImportantOrEqualTo(LogLevel.INFO)) {
            label.setText(entry.getDateTime().format(formatter) + " " + entry.getMessage());

            label.getDisplay().timerExec(DECAY_TIME_IN_MILLISECONDS, clearLabel);
        }
    }
}
