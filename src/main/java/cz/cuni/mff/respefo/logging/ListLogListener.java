package cz.cuni.mff.respefo.logging;

import cz.cuni.mff.respefo.util.utils.ExceptionUtils;
import org.eclipse.swt.widgets.List;

import java.time.format.DateTimeFormatter;

public class ListLogListener implements LogListener {
    private static final String DATE_TIME_PATTERN = "HH:mm:ss";

    private List list;
    private DateTimeFormatter formatter;

    public ListLogListener(List list) {
        this.list = list;
        formatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
    }

    @Override
    public void notify(LogEntry entry) {
        list.add(entry.getDateTime().format(formatter) + " > " + entry.getMessage());
        if (entry.getCause() != null) {
            String stackTrace = ExceptionUtils.getStackTrace(entry.getCause());
            list.add(stackTrace);
        }
    }
}
