package cz.cuni.mff.respefo.util;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.component.SpefoDialog;
import javafx.util.Pair;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Progress {
    private static ProgressBar progressBar;
    private static Label progressLabel;
    private static Display display;

    private static int inProgress = 0;

    private int selection;
    private int range;
    private String label;

    private Progress() {}

    public static void init(ProgressBar progressBar, Label progressLabel) {
        Progress.progressBar = progressBar;
        Progress.progressLabel = progressLabel;
        display = ComponentManager.getDisplay();
    }

    public static <T> void withProgressTracking(Function<Progress, T> backgroundProcess, Consumer<T> callback) {
        Runnable runnable = () -> {
            increaseCounter();
            try {
                T response = backgroundProcess.apply(new Progress());
                display.asyncExec(() -> callback.accept(response));
            } catch(Exception exception) {
                display.asyncExec(() -> { throw exception; });
            } finally {
                decreaseCounter();
            }
        };
        new Thread(runnable).start();
    }

    private static void increaseCounter() {
        synchronized (Progress.class) {
            inProgress++;

            if (inProgress > 1) {
                display.asyncExec(() -> {
                    updateLabel("(" + inProgress + ") processes are running");
                    progressBar.setSelection(progressBar.getMaximum());
                });
            }
        }
    }

    private static void decreaseCounter() {
        synchronized (Progress.class) {
            inProgress--;

            if (inProgress == 0) {
                display.asyncExec(() -> {
                    progressLabel.setVisible(false);
                    progressBar.setVisible(false);
                });

            } else if (inProgress == 1) {
                display.asyncExec(() -> updateLabel("(1) process is running"));

            } else if (inProgress > 1) {
                display.asyncExec(() -> updateLabel("(" + inProgress + ") processes are running"));
            }
        }
    }

    public void asyncExec(Runnable runnable) {
        display.asyncExec(runnable);
    }

    @SuppressWarnings("unchecked")
    public <T extends SpefoDialog> Pair<Integer, T> syncOpenDialog(Supplier<T> dialogSupplier) {
        SpefoDialog[] dialogs = new SpefoDialog[1];
        int[] returnValues = new int[1];

        display.syncExec(() -> {
            dialogs[0] = dialogSupplier.get();
            returnValues[0] = dialogs[0].open();
        });

        return new Pair<>(returnValues[0], (T) dialogs[0]);
    }

    public void refresh(String label, int range) {
        selection = 0;
        this.range = range;
        this.label = label;

        synchronized (Progress.class) {
            if (inProgress == 1) {
                asyncExec(() -> {
                    progressLabel.setVisible(true);
                    updateLabel(label);

                    progressBar.setMaximum(range);
                    progressBar.setSelection(0);
                    progressBar.setVisible(true);
                });
            }
        }
    }

    public void step() {
        step(1);
    }

    public void step(int value) {
        selection += value;

        synchronized (Progress.class) {
            if (inProgress == 1) {
                asyncExec(() -> {
                    progressBar.setSelection(selection);

                    if (!progressLabel.getText().equals(label)) {
                        progressBar.setMaximum(range);

                        updateLabel(label);
                    }
                });
            }
        }
    }

    private static void updateLabel(String text) {
        progressLabel.setText(text);
        progressLabel.requestLayout();
    }
}
