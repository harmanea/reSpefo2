package cz.cuni.mff.respefo.logging;

import cz.cuni.mff.respefo.util.utils.StringUtils;
import org.eclipse.swt.widgets.Label;

import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

public class LabelLogListener implements LogListener {

    private static final int DECAY_TIME_IN_MILLISECONDS = 60 * 1000;
    private static final String DATE_TIME_PATTERN = "HH:mm:ss";

    private final Label label;
    private final DateTimeFormatter formatter;
    private final Supplier<Boolean> shouldDisplay;

    public LabelLogListener(Label label, Supplier<Boolean> shouldDisplay) {
        this.label = label;
        this.formatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
        this.shouldDisplay = shouldDisplay;
    }

    @Override
    public void notify(LogEntry entry) {
        if (Boolean.TRUE.equals(shouldDisplay.get())) {
            String formattedDate = entry.getDateTime().format(formatter);
            label.getDisplay().asyncExec(() -> setTextAndRequestLayout(formattedDate + " " + StringUtils.substringBefore(entry.getMessage(), '\n')));

            label.getDisplay().timerExec(DECAY_TIME_IN_MILLISECONDS, () -> {
                if (!label.isDisposed() && label.getText().startsWith(formattedDate)) {
                    setTextAndRequestLayout("");
                }
            });
        }
    }

    private void setTextAndRequestLayout(String text) {
        label.setText(text);
        label.requestLayout();
    }
}
