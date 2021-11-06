package cz.cuni.mff.respefo.function.chiron;

import cz.cuni.mff.respefo.component.SpectrumExplorer;
import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.filter.EchelleSpectrumFileFilter;
import cz.cuni.mff.respefo.function.open.OpenFunction;
import cz.cuni.mff.respefo.function.rectify.RectifyAsset;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.spectrum.format.EchelleSpectrum;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.collections.XYSeries;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Fun(name = "Rectify Echelles", fileFilter = EchelleSpectrumFileFilter.class, group = "Preprocessing")
public class ChironRectifyFunction implements SingleFileFunction {

    @Override
    public void execute(File file) {
        EchelleSpectrum spectrum;
        try {
            spectrum = (EchelleSpectrum) Spectrum.open(file);
        } catch (SpefoException e) {
            Message.error("Couldn't open file", e);
            return;
        }

        XYSeries[] series = spectrum.getOriginalSeries();
        Map<Integer, RectifyAsset> currentAssets = spectrum.getRectifyAssets();

        String[][] names = new String[series.length][3];
        for (int i = 0; i <= series.length - 1; i++) {
            XYSeries xySeries = series[i];
            names[i] = new String[]{
                    Integer.toString(i + 1),
                    Double.toString(xySeries.getX(0)),
                    Double.toString(xySeries.getLastX())
            };
        }

        boolean[] selected = new boolean[series.length];
        for (int i = 0; i < series.length; i++) {
            selected[i] = currentAssets.containsKey(i);
        }

        InteractiveChironSelectionDialog dialog = new InteractiveChironSelectionDialog(names, selected);
        if (dialog.openIsNotOk()) {
            return;
        }

        List<Integer> selectedIndices = dialog.getSelectedIndices();

        LinkedHashMap<Integer, RectifyAsset> filteredAssets = currentAssets.entrySet()
                .stream()
                .filter(entry -> selectedIndices.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, LinkedHashMap::new));

        new InteractiveChironController(series, filteredAssets, selectedIndices)
                .rectify(assets -> finish(spectrum, assets));
    }

    private void finish(EchelleSpectrum spectrum, Map<Integer, RectifyAsset> assets) {
        spectrum.setRectifyAssets(assets);

        try {
            spectrum.save();
            SpectrumExplorer.getDefault().refresh();
            OpenFunction.displaySpectrum(spectrum);
            Message.info("Rectified spectrum saved successfully.");

        } catch (SpefoException exception) {
            Message.error("Spectrum file couldn't be saved.", exception);
        }
    }
}
