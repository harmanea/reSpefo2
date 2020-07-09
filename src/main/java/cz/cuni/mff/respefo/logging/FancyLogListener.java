package cz.cuni.mff.respefo.logging;

import cz.cuni.mff.respefo.resources.ColorManager;
import cz.cuni.mff.respefo.util.utils.ExceptionUtils;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static cz.cuni.mff.respefo.logging.LogLevel.WARNING;
import static cz.cuni.mff.respefo.resources.ColorResource.*;
import static org.eclipse.swt.SWT.*;

public class FancyLogListener implements LogListener {

    private final StyledText textField;
    private final NavigableMap<Integer, ActionRange> actionsMap;

    public FancyLogListener(Composite parent) {
        textField = new StyledText(parent, BORDER | READ_ONLY | H_SCROLL | V_SCROLL);
        actionsMap = new TreeMap<>();

        textField.setLeftMargin(5);

        // Handle clicks on actions
        textField.addListener(MouseDown, this::handleMouseDownEvent);
    }

    public void setLayoutData(Object layoutData) {
        textField.setLayoutData(layoutData);
    }

    private void handleMouseDownEvent(Event event) {
        int offset = textField.getOffsetAtPoint(new Point(event.x, event.y));
        if (offset != -1) {
            Map.Entry<Integer, ActionRange> entry = actionsMap.floorEntry(offset);
            if (entry != null && offset <= entry.getValue().upper) {
                executeAction(entry);
            }
        }
    }

    private void executeAction(Map.Entry<Integer, ActionRange> entry) {
        if (entry.getValue().oneShot) {
            StyleRange styleRange = new StyleRange(entry.getKey(), entry.getValue().upper - entry.getKey(), ColorManager.getColor(GRAY), null);
            styleRange.underline = true;
            styleRange.underlineStyle = UNDERLINE_SINGLE;
            textField.setStyleRange(styleRange);

            actionsMap.remove(entry.getKey());
        }

        entry.getValue().execute();
    }

    @Override
    public void notify(LogEntry entry) {
        String logString = String.format("%tT %s %s%n", entry.getDateTime(), entry.getLevel(), entry.getMessage());
        textField.append(logString);

        if (entry.getLevel().isMoreImportantOrEqualTo(WARNING)) {
            setStyleForImportantLogs(entry, logString);
        }

        if (entry.getCause() != null) {
            addStackTraceAction(entry.getCause());
        }

        textField.append("\n");
    }

    private void setStyleForImportantLogs(LogEntry entry, String logString) {
        StyleRange styleRange = new StyleRange(textField.getCharCount() - logString.length(),
                logString.length(),
                entry.getLevel().equals(WARNING) ? ColorManager.getColor(ORANGE) : ColorManager.getColor(RED),
                null);

        textField.setStyleRange(styleRange);
    }

    private void addStackTraceAction(Throwable cause) {
        final int index = textField.getCharCount();
        String actionText = "Show stacktrace";
        addAction(actionText, () -> {
            String stackTrace = ExceptionUtils.getStackTrace(cause).trim();
            textField.replaceTextRange(index, actionText.length(), stackTrace);
            textField.setStyleRange(new StyleRange(index, stackTrace.length(), null, null));
        }, true);
    }

    @SuppressWarnings("unused")
    private void addAction(String text, Runnable action, boolean oneShot) {
        int lower = textField.getCharCount();
        int upper = lower + text.length();
        actionsMap.put(lower, new ActionRange(upper, action, oneShot));

        StyleRange styleRange = new StyleRange(lower, text.length(), null, null);
        styleRange.underline = true;
        styleRange.underlineStyle = UNDERLINE_LINK;

        textField.append(text + "\n");
        textField.setStyleRange(styleRange);
    }

    private static class ActionRange {
        int upper;
        Runnable action;
        boolean oneShot;

        ActionRange(int upper, Runnable action, boolean oneShot) {
            this.upper = upper;
            this.action = action;
            this.oneShot = oneShot;
        }

        void execute() {
            action.run();
        }
    }
}
