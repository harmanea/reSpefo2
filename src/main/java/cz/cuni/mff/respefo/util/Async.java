package cz.cuni.mff.respefo.util;

import cz.cuni.mff.respefo.component.ComponentManager;

import java.util.List;
import java.util.function.Consumer;

public class Async extends UtilityClass {

    public static void exec(Runnable runnable) {
        ComponentManager.getDisplay().asyncExec(runnable);
    }

    public static <C> void loop(int count, C context, LoopAction<C> action, Consumer<C> finish) {
        loopInternal(0, count, context, action, finish);
    }

    private static <C> void loopInternal(int index, int count, C context, LoopAction<C> action, Consumer<C> finish) {
        if (index < count) {
            exec(() -> action.accept(index, context, () -> loopInternal(index + 1, count, context, action, finish)));
        } else {
            exec(() -> finish.accept(context));
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
            exec(() -> actions[index].accept(context, () -> sequenceInternal(index + 1, context, actions)));
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
            exec(() -> action.accept(context, shouldStillContinue -> whileLoopInternal(shouldStillContinue, context, action, finish)));
        } else {
            exec(() -> finish.accept(context));
        }
    }

    @FunctionalInterface
    public interface WhileLoopAction<C> {
        void accept(C context, Consumer<Boolean> callback);
    }

    public static <C, I> void listIteratorLoop(C context, List<I> items, IteratorLoopAction<C, I> action, Consumer<C> finish) {
        listIteratorLoopInternal(0, context, items, action, finish);
    }

    private static <C, I> void listIteratorLoopInternal(int index, C context, List<I> items, IteratorLoopAction<C, I> action, Consumer<C> finish) {
        if (index >= items.size()) {
            exec(() -> finish.accept(context));

        } else if (index >= 0) {
            exec(() -> action.accept(items.get(index), context,
                    () -> listIteratorLoopInternal(index + 1, context, items, action, finish),
                    () -> listIteratorLoopInternal(index - 1, context, items, action, finish)
            ));
        }
    }

    @FunctionalInterface
    public interface IteratorLoopAction<C, I> {
        void accept(I item, C context, Runnable nextCallback, Runnable previousCallback);
    }

    protected Async() throws IllegalAccessException {
        super();
    }
}
