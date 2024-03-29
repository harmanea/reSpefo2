package cz.cuni.mff.respefo.util.collections;

import java.util.Objects;

/**
 * A simple immutable tuple with two named fields, x and y.
 * It's instances are first compared using the x coordinate and then using the y coordinate.
 */
public class Point implements Comparable<Point> {
    public final double x;
    public final double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double distanceTo(Point o) {
        return Math.hypot(x - o.x, y - o.y);
    }

    public double squaredDistanceTo(Point o) {
        double dx = x - o.x;
        double dy = y - o.y;

        return dx * dx + dy * dy;
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
