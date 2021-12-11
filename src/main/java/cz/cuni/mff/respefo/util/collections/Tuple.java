package cz.cuni.mff.respefo.util.collections;

public class Tuple {
    private Tuple() {}

    public static <A, B> Two<A, B> of(A a, B b) {
        return new Two<>(a, b);
    }

    public static <A, B, C> Three<A, B, C> of(A a, B b, C c) {
        return new Three<>(a, b, c);
    }

    public static <A, B, C, D> Four<A, B, C, D> of(A a, B b, C c, D d) {
        return new Four<>(a, b, c, d);
    }

    public static <A, B, C, D, E> Five<A, B, C, D, E> of(A a, B b, C c, D d, E e) {
        return new Five<>(a, b, c, d, e);
    }

    public static class Two<A, B> {
        public final A a;
        public final B b;

        Two(A a, B b) {
            this.a = a;
            this.b = b;
        }
    }

    public static class Three<A, B, C> extends Two<A, B> {
        public final C c;

        Three(A a, B b, C c) {
            super(a, b);
            this.c = c;
        }
    }

    public static class Four<A, B, C, D> extends Three<A, B, C> {
        public final D d;

        Four(A a, B b, C c, D d) {
            super(a, b, c);
            this.d = d;
        }
    }

    public static class Five<A, B, C, D, E> extends Four<A, B, C, D> {
        public final E e;

        Five(A a, B b, C c, D d, E e) {
            super(a, b, c, d);
            this.e = e;
        }
    }
}
