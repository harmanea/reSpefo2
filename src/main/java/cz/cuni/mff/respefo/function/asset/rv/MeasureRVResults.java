package cz.cuni.mff.respefo.function.asset.rv;

import cz.cuni.mff.respefo.format.asset.FunctionAsset;
import cz.cuni.mff.respefo.util.utils.MathUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MeasureRVResults implements FunctionAsset {
    private final List<MeasureRVResult> results;
    private double rvCorrection; // TODO: do we need this here?

    public MeasureRVResults() {
        this.results = new ArrayList<>();
    }

    public boolean isEmpty() {
        return results.isEmpty();
    }

    public void add(MeasureRVResult result) {
        results.add(result);
    }

    public void remove(MeasureRVResult result) {
        results.remove(result);
    }

    public void append(MeasureRVResults other) {
        results.addAll(other.results);
    }

    public void setRvCorrection(double rvCorrection) {
        this.rvCorrection = rvCorrection;
    }

    public double getRvCorrection() {
        return rvCorrection;
    }

    public String[] getCategories() {
        return results.stream()
                .map(MeasureRVResult::getCategory)
                .distinct()
                .sorted()
                .toArray(String[]::new);
    }

    public MeasureRVResult[] getResultsOfCategory(String category) {
        return results.stream()
                .filter(result -> result.category.equals(category))
                .toArray(MeasureRVResult[]::new);
    }

    public double getRvOfCategory(String category) {
        MeasureRVResult[] resultsOfCategory = getResultsOfCategory(category);

        if (resultsOfCategory.length < 5) {
            return Arrays.stream(resultsOfCategory).mapToDouble(MeasureRVResult::getRv).average().orElse(Double.NaN);
        } else {
            return MathUtils.robustMean(Arrays.stream(resultsOfCategory).mapToDouble(MeasureRVResult::getRv).sorted().toArray());
        }
    }

    public double getRmseOfCategory(String category) {
        double rv = getRvOfCategory(category);
        double[] rvs = results.stream()
                .filter(result -> result.category.equals(category))
                .mapToDouble(MeasureRVResult::getRv)
                .toArray();

        return MathUtils.rmse(rvs, rv);
    }
}
