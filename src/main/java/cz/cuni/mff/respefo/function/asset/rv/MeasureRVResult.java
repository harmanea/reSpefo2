package cz.cuni.mff.respefo.function.asset.rv;

public class MeasureRVResult {
    double rv;
    double shift;
    double radius;
    String category;
    double l0;
    String name;
    String comment;

    private MeasureRVResult() {
        // default empty constructor
    }

    public MeasureRVResult(double rv, double shift, double radius, String category, double l0, String name, String comment) {
        this.rv = rv;
        this.shift = shift;
        this.radius = radius;
        this.category = category;
        this.l0 = l0;
        this.name = name;
        this.comment = comment;
    }

    public double getRv() {
        return rv;
    }

    public double getShift() {
        return shift;
    }

    public double getRadius() {
        return radius;
    }

    public String getCategory() {
        return category;
    }

    public double getL0() {
        return l0;
    }

    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }
}
