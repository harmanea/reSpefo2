package cz.cuni.mff.respefo.util.collections.tuple;

import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Tuple {
    public static <A, B> Pair<A, B> of(A a, B b) {
        return new Pair<>(a, b);
    }

    public static <A, B, C> Triplet<A, B, C> of(A a, B b, C c) {
        return new Triplet<>(a, b, c);
    }

    public static <A, B, C, D> Quartet<A, B, C, D> of(A a, B b, C c, D d) {
        return new Quartet<>(a, b, c, d);
    }

    public static <A, B, C, D, E> Quintet<A, B, C, D, E> of(A a, B b, C c, D d, E e) {
        return new Quintet<>(a, b, c, d, e);
    }

    protected abstract Stream<Object> values();

    @Override
    public String toString() {
        return values()
                .map(String::valueOf)
                .collect(Collectors.joining(", ", "(", ")"));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        Iterator<Object> iterator = values().iterator();
        Iterator<Object> otherIterator = ((Tuple) other).values().iterator();

        while (iterator.hasNext() && otherIterator.hasNext()) {
            if (!iterator.next().equals(otherIterator.next())) {
                return false;
            }
        }

        return !iterator.hasNext() && !otherIterator.hasNext();
    }

    @Override
    public int hashCode() {
        return values().collect(Collectors.toList()).hashCode();
    }
}
