package cz.cuni.mff.respefo.util;

import org.eclipse.swt.widgets.Display;

import java.util.function.Consumer;

public class Async extends UtilityClass {
    public static <C> void loop(int count, C context, LoopAction<C> action, Consumer<C> finish) {
        loopInternal(0, count, context, action, finish);
    }

    private static <C> void loopInternal(int index, int count, C context, LoopAction<C> action, Consumer<C> finish) {
        if (index < count) {
            asyncExec(() -> action.accept(index, context, () -> loopInternal(index + 1, count, context, action, finish)));
        } else {
            asyncExec(() -> finish.accept(context));
        }
    }

    @FunctionalInterface
    public interface LoopAction<C> {
        void accept(int index, C context, Runnable callback);
    }

    @SafeVarargs
    public static <C> void sequence(C context, SequenceAction<C>... actions) {
        sequenceInternal(0, context, actions);
    }

    @SafeVarargs
    private static <C> void sequenceInternal(int index, C context, SequenceAction<C>... actions) {
        if (index < actions.length) {
            asyncExec(() -> actions[index].accept(context, () -> sequenceInternal(index + 1, context, actions)));
        }
    }

    @FunctionalInterface
    public interface SequenceAction<C> {
        void accept(C context, Runnable callback);
    }

    public static <C> void whileLoop(C context, WhileLoopAction<C> action, Consumer<C> finish) {
        whileLoopInternal(true, context, action, finish);
    }

    private static <C> void whileLoopInternal(boolean shouldContinue, C context, WhileLoopAction<C> action, Consumer<C> finish) {
        if (shouldContinue) {
            asyncExec(() -> action.accept(context, shouldStillContinue -> whileLoopInternal(shouldStillContinue, context, action, finish)));
        } else {
            asyncExec(() -> finish.accept(context));
        }
    }

    @FunctionalInterface
    public interface WhileLoopAction<C> {
        void accept(C context, Consumer<Boolean> callback);
    }

    private static void asyncExec(Runnable runnable) {
        Display.getCurrent().asyncExec(runnable);
    }

    protected Async() throws IllegalAccessException {
        super();
    }
}
