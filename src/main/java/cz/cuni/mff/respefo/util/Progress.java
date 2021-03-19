package cz.cuni.mff.respefo.util;

import cz.cuni.mff.respefo.dialog.SpefoDialog;
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

    public static void init(Display display) {
        Progress.display = display;
    }

    public static void setControls(ProgressBar progressBar, Label progressLabel) {
        Progress.progressBar = progressBar;
        Progress.progressLabel = progressLabel;
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

    public void syncExec(Runnable runnable) {
        display.syncExec(runnable);
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

    @SuppressWarnings("unchecked")
    public <D extends SpefoDialog> DialogAndReturnCode<D> syncOpenDialog(Supplier<D> dialogSupplier) {
        SpefoDialog[] dialogs = new SpefoDialog[1];
        int[] returnValues = new int[1];

        display.syncExec(() -> {
            dialogs[0] = dialogSupplier.get();
            returnValues[0] = dialogs[0].open();
        });

        return new DialogAndReturnCode<>((D) dialogs[0], returnValues[0]);
    }

    public static class DialogAndReturnCode<D extends SpefoDialog> {
        private final D dialog;
        private final int returnValue;

        public DialogAndReturnCode(D dialog, int returnValue) {
            this.dialog = dialog;
            this.returnValue = returnValue;
        }

        public D getDialog() {
            return dialog;
        }

        public int getReturnValue() {
            return returnValue;
        }
    }
}
