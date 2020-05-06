package cz.cuni.mff.respefo.util;

import cz.cuni.mff.respefo.component.ComponentManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

import java.util.function.Consumer;
import java.util.function.Function;

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
            T response = backgroundProcess.apply(new Progress());
            decreaseCounter();
            display.asyncExec(() -> callback.accept(response));
        };
        new Thread(runnable).start();
    }

    private static void increaseCounter() {
        synchronized (Progress.class) {
            inProgress++;

            if (inProgress > 1) {
                display.asyncExec(() -> {
                    progressLabel.setText("(" + inProgress + ") processes are running");
                    progressLabel.requestLayout();
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
                display.asyncExec(() -> {
                    progressLabel.setText("(1) process is running");
                    progressLabel.requestLayout();
                });

            } else if (inProgress > 1) {
                display.asyncExec(() -> {
                    progressLabel.setText("(" + inProgress + ") processes are running");
                    progressLabel.requestLayout();
                });
            }
        }
    }

    public void asyncExec(Runnable runnable) {
        display.asyncExec(runnable);
    }

    public void refresh(String label, int range) {
        selection = 0;
        this.range = range;
        this.label = label;

        synchronized (Progress.class) {
            if (inProgress == 1) {
                asyncExec(() -> {
                    progressLabel.setText(label);
                    progressLabel.setVisible(true);
                    progressLabel.requestLayout();

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

                        progressLabel.setText(label);
                        progressLabel.requestLayout();
                    }
                });
            }
        }
    }
}
