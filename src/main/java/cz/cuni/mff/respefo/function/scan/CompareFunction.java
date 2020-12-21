package cz.cuni.mff.respefo.function.scan;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.format.Spectrum;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.MultiFileFunction;
import cz.cuni.mff.respefo.function.asset.common.ChartKeyListener;
import cz.cuni.mff.respefo.function.asset.common.DragMouseListener;
import cz.cuni.mff.respefo.function.asset.common.ZoomMouseWheelListener;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.resources.ColorResource;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.Progress;
import cz.cuni.mff.respefo.util.builders.widgets.ChartBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static cz.cuni.mff.respefo.resources.ColorResource.*;
import static cz.cuni.mff.respefo.util.builders.widgets.ChartBuilder.LineSeriesBuilder.lineSeries;

@Fun(name = "Compare", fileFilter = SpefoFormatFileFilter.class)
public class CompareFunction implements MultiFileFunction {
    private static final ColorResource[] COLORS = {GREEN, BLUE, ORANGE, CYAN, WHITE, PURPLE, PINK, GRAY};

    @Override
    public void execute(List<File> files) {
        if (files.size() > COLORS.length) {
            Message.warning("You can only compare up to " + COLORS.length + " spectra files.");
            return;
        }

        Progress.withProgressTracking(progress -> loadSpectra(progress, files), this::displaySpectra);
    }

    private List<Spectrum> loadSpectra(Progress progress, List<File> files) {
        progress.refresh("Loading files", files.size() - 1);

        List<Spectrum> spectra = new ArrayList<>();
        for (File file : files) {
            try {
                Spectrum spectrum = Spectrum.open(file);
                spectra.add(spectrum);
            } catch (SpefoException exception) {
                progress.asyncExec(() -> Message.error("Couldn't open file", exception));
            } finally {
                progress.step();
            }
        }
        return spectra;
    }

    private void displaySpectra(List<Spectrum> spectra) {
        ChartBuilder chartBuilder = ChartBuilder.newChart()
                .title("Compare")
                .xAxisLabel("X Axis")
                .yAxisLabel("Y Axis");

        for (int i = 0; i < spectra.size(); i++) {
            Spectrum spectrum = spectra.get(i);
            chartBuilder.series(
                    lineSeries()
                            .name(String.valueOf(i))
                            .color(COLORS[i])
                            .series(spectrum.getProcessedSeries())
            );
        }

        chartBuilder
                .keyListener(ChartKeyListener::makeAllSeriesEqualRange)
                .mouseAndMouseMoveListener(DragMouseListener::new)
                .mouseWheelListener(ZoomMouseWheelListener::new)
                .makeAllSeriesEqualRange()
                .build(ComponentManager.clearAndGetScene());
    }
}
