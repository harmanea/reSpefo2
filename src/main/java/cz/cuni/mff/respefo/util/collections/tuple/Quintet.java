package cz.cuni.mff.respefo.util.collections.tuple;

import java.util.stream.Stream;

public class Quintet<A, B, C, D, E> extends Tuple {
    public final A a;
    public final B b;
    public final C c;
    public final D d;
    public final E e;

    public Quintet(A a, B b, C c, D d, E e) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
    }

    @Override
    protected Stream<Object> values() {
        return Stream.of(a, b, c, d, e);
    }
}
