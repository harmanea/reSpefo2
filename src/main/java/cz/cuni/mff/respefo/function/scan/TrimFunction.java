package cz.cuni.mff.respefo.function.scan;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.format.Spectrum;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.Serialize;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.asset.trim.TrimAsset;
import cz.cuni.mff.respefo.function.asset.trim.TrimDialog;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.util.Message;
import org.eclipse.jface.window.Window;

import java.io.File;

import static cz.cuni.mff.respefo.util.builders.ChartBuilder.LineSeriesBuilder.lineSeries;
import static cz.cuni.mff.respefo.util.builders.ChartBuilder.chart;

@Fun(name = "Trim", fileFilter = SpefoFormatFileFilter.class)
@Serialize(key = "trim", assetClass = TrimAsset.class)
public class TrimFunction implements SingleFileFunction {
    @Override
    public void execute(File file) {
        Spectrum spectrum;
        try {
            spectrum = Spectrum.open(file);
        } catch (SpefoException e) {
            Message.error("Couldn't open file", e);
            return;
        }
        TrimAsset asset = (TrimAsset) spectrum.getFunctionAssets().getOrDefault("trim", new TrimAsset());

        TrimDialog dialog = new TrimDialog();
        dialog.setMin(asset.getMin());
        dialog.setMax(asset.getMax());
        if (dialog.open() != Window.OK) {
            Log.debug("Trim dialog cancelled");
            return;
        }

        asset.setMin(dialog.getMin());
        asset.setMax(dialog.getMax());

        if (asset.getMin() == Double.NEGATIVE_INFINITY && asset.getMax() == Double.POSITIVE_INFINITY) {
            spectrum.getFunctionAssets().remove("trim");
        } else {
            spectrum.getFunctionAssets().put("trim", asset);
        }

        try {
            spectrum.save();

            chart(ComponentManager.clearAndGetScene())
                    .title(file.getName())
                    .xAxisLabel("x axis")
                    .yAxisLabel("y axis")
                    .series(lineSeries()
                            .name("series")
                            .series(spectrum.getProcessedSeries()))
                    .build();

            Message.info("File saved successfully");
        } catch (SpefoException e) {
            Message.error("Couldn't save file", e);
        }
    }
}
