package cz.cuni.mff.respefo.function.open;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.component.ToolBar;
import cz.cuni.mff.respefo.component.VerticalToggle;
import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.SpectrumFunction;
import cz.cuni.mff.respefo.function.common.ChartKeyListener;
import cz.cuni.mff.respefo.function.common.DragMouseListener;
import cz.cuni.mff.respefo.function.common.ZoomMouseWheelListener;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.resources.ImageResource;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.spectrum.origin.BaseOrigin;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.collections.JulianDate;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import java.time.LocalDateTime;
import java.util.Arrays;

import static cz.cuni.mff.respefo.util.widget.ChartBuilder.LineSeriesBuilder.lineSeries;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.newChart;
import static cz.cuni.mff.respefo.util.widget.TableBuilder.newTable;
import static java.lang.Double.isNaN;

@Fun(name = "Open", fileFilter = SpefoFormatFileFilter.class)
public class OpenFunction extends SpectrumFunction {
    @Override
    public void execute(Spectrum spectrum) {
        displaySpectrum(spectrum);
    }

    public static void displaySpectrum(Spectrum spectrum) {
        newChart()
                .title(spectrum.getFile().getName())
                .series(lineSeries()
                        .name("series")
                        .series(spectrum.getProcessedSeries()))
                .keyListener(ChartKeyListener::new)
                .mouseAndMouseMoveListener(DragMouseListener::new)
                .mouseWheelListener(ZoomMouseWheelListener::new)
                .adjustRange()
                .forceFocus()
                .build(ComponentManager.clearAndGetScene());

        final ToolBar.Tab infoTab = ComponentManager.getRightToolBar().addTab(parent -> new VerticalToggle(parent, SWT.DOWN),
                "Info", "Information", ImageResource.INFO_LARGE);

        final Table table = newTable(SWT.SINGLE)
                .gridLayoutData(GridData.FILL_BOTH)
                .headerVisible(false)
                .linesVisible(true)
                .columns(2)
                .items(Arrays.asList(
                        new String[] {"File", spectrum.getFile().getName()},
                        new String[] {"Format", Integer.toString(spectrum.getFormat())},
                        new String[] {"Version", spectrum.getVersion()},
                        new String[] {"Origin", ((BaseOrigin) spectrum.getOrigin()).getFileName()},
                        new String[] {"HJD", isNaN(spectrum.getHjd().getJD()) ? "N/A" : Double.toString(spectrum.getHjd().getJD())},
                        new String[] {"Date OBS", spectrum.getDateOfObservation().equals(LocalDateTime.MIN) ? "N/A" : spectrum.getDateOfObservation().toString()},
                        new String[] {"RV Corr", Double.toString(spectrum.getRvCorrection())},
                        new String[] {"Exp Time", isNaN(spectrum.getExpTime()) ? "N/A" : Double.toString(spectrum.getExpTime())}
                ))
                .packColumns()
                .listener(SWT.MouseDoubleClick, event -> {
                    TableItem item = ((Table) event.widget).getItem(new Point(event.x, event.y));
                    if (item != null) {
                        edit(spectrum, item);
                    }
                })
                .build(infoTab.getWindow());

        infoTab.addTopBarButton("Edit", ImageResource.EDIT, () -> {
            int index = table.getSelectionIndex();
            if (index >= 0) {
                edit(spectrum, table.getItem(index));
            }
        });
    }

    private static void trySave(Spectrum spectrum, TableItem item, String text) {
        try {
            spectrum.save();
            item.setText(1, text);
        } catch (SpefoException exception) {
            Message.error("Couldn't save spectrum", exception);
        }
    }

    private static void edit(Spectrum spectrum, TableItem item) {
        switch (item.getText(0)) {
            case "HJD": {
                double hjd = spectrum.getHjd().getJD();
                DoubleNumberDialog numberDialog = new DoubleNumberDialog("HJD", isNaN(hjd) ? 0 : hjd);
                if (numberDialog.openIsOk()) {
                    spectrum.setHjd(new JulianDate(numberDialog.getValue()));
                    trySave(spectrum, item, Double.toString(numberDialog.getValue()));
                }
                break;
            }

            case "Date OBS": {
                LocalDateTime dateTime = spectrum.getDateOfObservation();
                DateTimeDialog dateTimeDialog = new DateTimeDialog(dateTime.equals(LocalDateTime.MIN) ? LocalDateTime.now() : dateTime);
                if (dateTimeDialog.openIsOk()) {
                    spectrum.setDateOfObservation(dateTimeDialog.getDateTime());
                    trySave(spectrum, item, dateTimeDialog.getDateTime().toString());
                }
                break;
            }

            case "RV Corr": {
                double rvCorr = spectrum.getRvCorrection();
                DoubleNumberDialog numberDialog = new DoubleNumberDialog("RV Corr", isNaN(rvCorr) ? 0 : rvCorr);
                if (numberDialog.openIsOk()) {
                    double newRvCorrection = numberDialog.getValue();
                    if (Double.compare(rvCorr, newRvCorrection) != 0) {
                        if (Message.question("Do you want to adjust the spectrum values according to the difference?")) {
                            spectrum.updateRvCorrection(newRvCorrection);
                        } else {
                            spectrum.setRvCorrection(newRvCorrection);
                        }
                        trySave(spectrum, item, Double.toString(numberDialog.getValue()));
                    }
                }
                break;
            }

            case "Exp Time": {
                DoubleNumberDialog numberDialog = new DoubleNumberDialog("Exp Time", spectrum.getExpTime());
                if (numberDialog.openIsOk()) {
                    spectrum.setExpTime(numberDialog.getValue());
                    trySave(spectrum, item, Double.toString(numberDialog.getValue()));
                }
                break;
            }
        }
    }
}
