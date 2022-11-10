package cz.cuni.mff.respefo.logging;

import cz.cuni.mff.respefo.resources.ColorManager;
import cz.cuni.mff.respefo.util.Async;
import cz.cuni.mff.respefo.util.utils.ExceptionUtils;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static cz.cuni.mff.respefo.logging.LogLevel.WARNING;
import static cz.cuni.mff.respefo.resources.ColorResource.*;
import static org.eclipse.swt.SWT.*;

public class FancyLogListener implements LogListener, LogActionListener {

    private final StyledText textField;
    private final NavigableMap<Integer, ActionRange> actionsMap;

    private boolean scrollToEnd = false;

    private final Clipboard clipboard;

    public FancyLogListener(Composite parent) {
        textField = new StyledText(parent, READ_ONLY | H_SCROLL | V_SCROLL);
        actionsMap = new TreeMap<>();

        textField.setLeftMargin(5);

        // Handle clicks on actions
        textField.addListener(MouseDown, this::handleMouseDownEvent);

        // Scroll to end
        textField.addModifyListener(event -> { if (scrollToEnd) scrollToEnd(); });

        clipboard = new Clipboard(parent.getDisplay());
    }

    public void copy() {
        if (textField.getText().length() > 0) {
            clipboard.setContents(new Object[]{textField.getText()}, new Transfer[]{TextTransfer.getInstance()});
        }
    }

    public void setLayoutData(Object layoutData) {
        textField.setLayoutData(layoutData);
    }

    public void setScrollToEnd(boolean scrollToEnd) {
        this.scrollToEnd = scrollToEnd;
        if (scrollToEnd) {
            scrollToEnd();
        }
    }

    public void setMinimumLevel(LogLevel minimumLevel) {
        Log.registerListener(this, minimumLevel);
    }

    private void handleMouseDownEvent(Event event) {
        int offset = textField.getOffsetAtPoint(new Point(event.x, event.y));
        if (offset != -1) {
            Map.Entry<Integer, ActionRange> entry = actionsMap.floorEntry(offset);
            if (entry != null && offset <= entry.getValue().upper) {
                executeAction(entry.getKey(), entry.getValue().upper, entry.getValue().action, entry.getValue().oneShot);
            }
        }
    }

    private void executeAction(int lower, int upper, Runnable action, boolean oneShot) {
        if (oneShot) {
            StyleRange styleRange = new StyleRange(lower, upper - lower, ColorManager.getColor(GRAY), null);
            styleRange.underline = true;
            styleRange.underlineStyle = UNDERLINE_SINGLE;
            textField.setStyleRange(styleRange);

            actionsMap.remove(lower);
        }

        action.run();
    }

    private void scrollToEnd() {
        textField.setTopIndex(textField.getLineCount() - 1);
    }

    @Override
    public void notify(LogEntry entry) {
        Async.exec(() -> addLogEntry(entry));
    }

    private void addLogEntry(LogEntry entry) {
        String logString = String.format("%tT %s %s%n", entry.getDateTime(), entry.getLevel(), entry.getMessage());
        textField.append(logString);

        if (entry.getLevel().isMoreImportantOrEqualTo(WARNING)) {
            setStyleForImportantLogs(entry, logString);
        }

        if (entry.getCause() != null) {
            textField.append(ExceptionUtils.getStackTrace(entry.getCause()));
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

    @Override
    public void notify(LogAction action) {
        Async.exec(() -> addAction(action.getText(), action.getLabel(), action.getAction(), action.isOneShot()));
    }

    private void addAction(String text, String label, Runnable action, boolean oneShot) {
        textField.append(text);

        int lower = textField.getCharCount();
        int upper = lower + label.length();
        actionsMap.put(lower, new ActionRange(upper, action, oneShot));

        StyleRange styleRange = new StyleRange(lower, label.length(), null, null);
        styleRange.underline = true;
        styleRange.underlineStyle = UNDERLINE_LINK;

        textField.append(label + "\n");
        textField.setStyleRange(styleRange);
    }

    private static class ActionRange {
        final int upper;
        final Runnable action;
        final boolean oneShot;

        ActionRange(int upper, Runnable action, boolean oneShot) {
            this.upper = upper;
            this.action = action;
            this.oneShot = oneShot;
        }
    }
}
