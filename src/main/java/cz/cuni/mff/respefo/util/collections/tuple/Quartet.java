package cz.cuni.mff.respefo.util.collections.tuple;

import java.util.stream.Stream;

public class Quartet<A, B, C, D> extends Tuple {
    public final A a;
    public final B b;
    public final C c;
    public final D d;

    public Quartet(A a, B b, C c, D d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public <E> Quintet<A, B, C, D, E> append(E e) {
        return new Quintet<>(a, b, c, d, e);
    }

    @Override
    protected Stream<Object> values() {
        return Stream.of(a, b, c, d);
    }
}
