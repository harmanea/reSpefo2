package cz.cuni.mff.respefo.util.collections.tuple;

import java.util.stream.Stream;

public class Triplet<A, B, C> extends Tuple {
    public final A a;
    public final B b;
    public final C c;

    Triplet(A a, B b, C c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public <D> Quartet<A, B, C, D> append(D d) {
        return new Quartet<>(a, b, c, d);
    }

    @Override
    protected Stream<Object> values() {
        return Stream.of(a, b, c);
    }
}
