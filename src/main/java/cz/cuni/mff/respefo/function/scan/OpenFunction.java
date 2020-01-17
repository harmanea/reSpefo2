package cz.cuni.mff.respefo.function.scan;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.format.Spectrum;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.util.Message;

import java.io.File;

import static cz.cuni.mff.respefo.util.builders.ChartBuilder.LineSeriesBuilder.lineSeries;
import static cz.cuni.mff.respefo.util.builders.ChartBuilder.chart;

@Fun(name = "Open", fileFilter = SpefoFormatFileFilter.class)
public class OpenFunction implements SingleFileFunction {
    @Override
    public void execute(File file) {
        try {
            Spectrum spectrum = new Spectrum(file);

            ComponentManager.clearScene();

            chart(ComponentManager.getScene())
                    .title(file.getName())
                    .xAxisLabel("x axis")
                    .yAxisLabel("y axis")
                    .series(lineSeries()
                            .name("series")
                            .data(spectrum.getProcessedData()))
                    .build();

        } catch (SpefoException ex) {
            Message.error("Couldn't open file", ex);
        }
    }
}
