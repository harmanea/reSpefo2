package cz.cuni.mff.respefo.util.collections;

import java.util.Objects;

/**
 * A simple immutable tuple with two named fields, x and y.
 * It's instances are first compared using the x coordinate and then using the y coordinate.
 */
public class Point implements Comparable<Point> {
    private final double x;
    private final double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public int compareTo(Point o) {
        int xComparison = Double.compare(x, o.x);
        if (xComparison == 0) {
            return Double.compare(y, o.y);
        } else {
            return xComparison;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return Double.compare(point.x, x) == 0 && Double.compare(point.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
