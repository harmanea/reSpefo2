package cz.cuni.mff.respefo.function.rv;

import cz.cuni.mff.respefo.spectrum.asset.AppendableFunctionAsset;
import cz.cuni.mff.respefo.util.utils.MathUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class MeasureRVResults implements AppendableFunctionAsset<MeasureRVResults> {
    private final List<MeasureRVResult> results;

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

    @Override
    public void append(MeasureRVResults other) {
        results.addAll(other.results);
    }

    public String[] getCategories() {
        return results.stream()
                .map(MeasureRVResult::getCategory)
                .distinct()
                .sorted()
                .toArray(String[]::new);
    }

    public Stream<MeasureRVResult> getResultsOfCategory(String category) {
        return results.stream()
                .filter(result -> result.category.equals(category));
    }

    public int getNumberOfResultsInCategory(String category) {
        return (int) getResultsOfCategory(category).count();
    }

    public boolean isRepeatedMeasurement(MeasureRVResult result) {
        return results.stream().anyMatch(result::isRepeated);
    }

    public double getRvOfCategory(String category) {
        if (getNumberOfResultsInCategory(category) < 5) {
            return getResultsOfCategory(category).mapToDouble(MeasureRVResult::getRv).average().orElse(Double.NaN);
        } else {
            return MathUtils.robustMean(getResultsOfCategory(category).mapToDouble(MeasureRVResult::getRv).sorted().toArray());
        }
    }

    public double getSemOfCategory(String category) {
        double rv = getRvOfCategory(category);
        double[] rvs = results.stream()
                .filter(result -> result.category.equals(category))
                .mapToDouble(MeasureRVResult::getRv)
                .toArray();

        return MathUtils.sem(rvs, rv);
    }
}
