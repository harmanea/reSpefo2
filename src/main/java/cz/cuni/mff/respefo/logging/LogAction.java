package cz.cuni.mff.respefo.logging;

public class LogAction {
    private final String text;
    private final String label;
    private final Runnable action;
    private final boolean oneShot;

    public LogAction(String text, String label, Runnable action, boolean oneShot) {
        this.text = text;
        this.label = label;
        this.action = action;
        this.oneShot = oneShot;
    }

    public String getText() {
        return text;
    }

    public String getLabel() {
        return label;
    }

    public Runnable getAction() {
        return action;
    }

    public boolean isOneShot() {
        return oneShot;
    }
}
