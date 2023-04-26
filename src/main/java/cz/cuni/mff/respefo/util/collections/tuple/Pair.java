package cz.cuni.mff.respefo.util.collections.tuple;

import java.util.stream.Stream;

public class Pair<A, B> extends Tuple {
    public final A a;
    public final B b;

    Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public <C> Triplet<A, B, C> append(C c) {
        return new Triplet<>(a, b, c);
    }

    @Override
    protected Stream<Object> values() {
        return Stream.of(a, b);
    }
}
