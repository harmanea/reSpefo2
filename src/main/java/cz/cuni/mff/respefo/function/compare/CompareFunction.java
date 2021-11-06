package cz.cuni.mff.respefo.function.compare;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.MultiFileFunction;
import cz.cuni.mff.respefo.function.common.ChartKeyListener;
import cz.cuni.mff.respefo.function.common.DragMouseListener;
import cz.cuni.mff.respefo.function.common.ZoomMouseWheelListener;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.resources.ColorResource;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.Progress;
import cz.cuni.mff.respefo.util.utils.FileUtils;
import cz.cuni.mff.respefo.util.widget.ChartBuilder;
import org.eclipse.swt.custom.StyleRange;
import org.swtchart.Chart;
import org.swtchart.ITitle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static cz.cuni.mff.respefo.resources.ColorManager.getColor;
import static cz.cuni.mff.respefo.resources.ColorResource.*;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.LineSeriesBuilder.lineSeries;

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

        Chart chart = chartBuilder
                .keyListener(ChartKeyListener::makeAllSeriesEqualRange)
                .mouseAndMouseMoveListener(DragMouseListener::new)
                .mouseWheelListener(ZoomMouseWheelListener::new)
                .makeAllSeriesEqualRange()
                .build(ComponentManager.clearAndGetScene());

        setTitle(chart, spectra);
    }

    private void setTitle(Chart chart, List<Spectrum> spectra) {
        ITitle title = chart.getTitle();

        StringBuilder stringBuilder = new StringBuilder();
        StyleRange[] styleRanges = new StyleRange[spectra.size()];

        int end = 0;
        for (int i = 0; i < spectra.size(); i++) {
            String name = FileUtils.stripFileExtension(spectra.get(i).getFile().getName());

            stringBuilder.append(name);
            styleRanges[i] = new StyleRange(end, name.length(), getColor(COLORS[i]), null);
            if (i + 1 < spectra.size()) {
                stringBuilder.append(" x ");
            }

            end += name.length() + 3;
        }

        title.setText(stringBuilder.toString());
        title.setStyleRanges(styleRanges);
    }
}
