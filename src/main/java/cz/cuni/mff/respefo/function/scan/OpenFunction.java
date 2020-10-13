package cz.cuni.mff.respefo.function.scan;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.format.Spectrum;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.asset.common.ChartKeyListener;
import cz.cuni.mff.respefo.function.asset.common.DragMouseListener;
import cz.cuni.mff.respefo.function.asset.common.ZoomMouseWheelListener;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.util.Message;

import java.io.File;

import static cz.cuni.mff.respefo.util.builders.ChartBuilder.LineSeriesBuilder.lineSeries;
import static cz.cuni.mff.respefo.util.builders.ChartBuilder.chart;

@Fun(name = "Open", fileFilter = SpefoFormatFileFilter.class)
public class OpenFunction implements SingleFileFunction {
    @Override
    public void execute(File file) {
        try {
            Spectrum spectrum = Spectrum.open(file);
            displaySpectrum(spectrum);

        } catch (SpefoException ex) {
            Message.error("Couldn't open file", ex);
        }
    }

    public static void displaySpectrum(Spectrum spectrum) {
        chart(ComponentManager.clearAndGetScene())
                .title(spectrum.getFile().getName())
                .xAxisLabel("X axis")
                .yAxisLabel("Y axis")
                .series(lineSeries()
                        .name("series")
                        .series(spectrum.getProcessedSeries()))
                .keyListener(ChartKeyListener::defaultBehaviour)
                .mouseAndMouseMoveListener(DragMouseListener::new)
                .mouseWheelListener(ZoomMouseWheelListener::new)
                .build();
    }
}
