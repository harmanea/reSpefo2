package cz.cuni.mff.respefo.function.ew;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.component.FileExplorer;
import cz.cuni.mff.respefo.component.Project;
import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.MultiFileFunction;
import cz.cuni.mff.respefo.function.ProjectFunction;
import cz.cuni.mff.respefo.function.SpectrumFunction;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.Progress;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.utils.FileUtils;
import cz.cuni.mff.respefo.util.widget.LabelBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static cz.cuni.mff.respefo.util.layout.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.utils.FormattingUtils.formatDouble;
import static cz.cuni.mff.respefo.util.utils.MathUtils.isNotNaN;
import static cz.cuni.mff.respefo.util.widget.ButtonBuilder.newPushButton;
import static cz.cuni.mff.respefo.util.widget.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.widget.LabelBuilder.newLabel;
import static cz.cuni.mff.respefo.util.widget.TableBuilder.newTable;
import static java.lang.Double.isNaN;
import static org.eclipse.swt.SWT.COLOR_WIDGET_BACKGROUND;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@Fun(name = "EW Results", fileFilter = SpefoFormatFileFilter.class, group = "Results")
public class EWResultsFunction extends SpectrumFunction implements MultiFileFunction, ProjectFunction {

    @Override
    public void execute(Spectrum spectrum) {
        if (!spectrum.containsFunctionAsset(MeasureEWFunction.SERIALIZE_KEY)) {
            Message.warning("There are no EW measurements in this file.");
            return;
        }

        displayResults(spectrum);
    }

    public static void displayResults(Spectrum spectrum) {
        MeasureEWResults results = spectrum.getFunctionAsset(MeasureEWFunction.SERIALIZE_KEY, MeasureEWResults.class).get();
        XYSeries series = spectrum.getProcessedSeries();

        final ScrolledComposite scrolledComposite = new ScrolledComposite(ComponentManager.clearAndGetScene(), SWT.V_SCROLL | SWT.H_SCROLL);
        scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        scrolledComposite.setLayout(new GridLayout());

        final Composite composite = newComposite()
                .gridLayoutData(GridData.FILL_BOTH)
                .layout(gridLayout().margins(10).spacings(10))
                .background(ComponentManager.getDisplay().getSystemColor(COLOR_WIDGET_BACKGROUND))
                .build(scrolledComposite);

        LabelBuilder labelBuilder = newLabel(SWT.LEFT)
                .gridLayoutData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);

        labelBuilder
                .text("Summary of equivalent widths etc. measured on " + FileUtils.stripFileExtension(spectrum.getFile().getName()))
                .build(composite);

        for (MeasureEWResult result : results) {
            final Group group = new Group(composite, SWT.NONE);
            group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER));
            group.setLayout(gridLayout().margins(10).build());
            group.setText("Results for measurement " + result.getName());
            if (results.isRepeatedMeasurement(result)) {
                group.setForeground(ComponentManager.getDisplay().getSystemColor(SWT.COLOR_WIDGET_DISABLED_FOREGROUND));
            }

            labelBuilder.text("EW: " + formatDouble(result.getEW(series), 4, 4)).build(group);

            if (result.containsCategory(MeasureEWResultPointCategory.Ic)) {
                double fwhm = result.getFWHM(series);
                if (isNaN(fwhm)) {
                    Log.warning("Couldn't compute FWHM for measurement " + result.getName());
                } else {
                    labelBuilder.text("FWHM: " + formatDouble(fwhm, 4, 4)).build(group);
                }
            }

            for (int i = 0; i < result.pointsCount(); i++) {
                labelBuilder
                        .text(result.getCategory(i).name() + ": " + formatDouble(series.getY(result.getPoint(i)), 4, 4))
                        .build(group);
            }

            newPushButton()
                    .text("Delete")
                    .layoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_END))
                    .onSelection(event -> {
                        results.remove(result);

                        int i = 0;
                        for (MeasureEWResult otherResult : results) {
                            if (result.isRepeated(otherResult) && !results.isRepeatedMeasurement(otherResult)) {
                                composite.getChildren()[i + 1].setForeground(ComponentManager.getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
                                break;
                            }
                            i++;
                        }

                        group.dispose();
                        composite.requestLayout();

                        if (results.isEmpty()) {
                            spectrum.removeFunctionAsset(MeasureEWFunction.SERIALIZE_KEY);
                            scrolledComposite.dispose();
                        }

                        try {
                            spectrum.save();
                        } catch (SpefoException exception) {
                            Message.error("Couldn't save changes", exception);
                        }
                    })
                    .build(group);
        }

        scrolledComposite.setContent(composite);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        ComponentManager.getScene().layout();
        scrolledComposite.redraw();
    }

    @Override
    public void execute(List<File> files) {
        Progress.withProgressTracking(p -> {
            p.refresh("Processing files", files.size());

            List<Spectrum> spectra = new ArrayList<>();
            for (File file : files) {
                try {
                    Spectrum spectrum = Spectrum.open(file);

                    if (spectrum.containsFunctionAsset(MeasureEWFunction.SERIALIZE_KEY)) {
                        spectra.add(spectrum);
                    } else {
                        Log.warning("There are no EW measurements in the file [" + file.getPath() + "]");
                    }
                } catch (SpefoException exception) {
                    Log.error("Couldn't open file [" + file.getPath() + "]", exception);
                } finally {
                    p.step();
                }
            }

            return spectra;
        }, spectra -> {
            if (spectra.isEmpty()) {
                Message.warning("No valid spectrum with EW measurements was loaded");
            } else {
                displayResults(spectra);
            }
        });
    }

    private static void displayResults(List<Spectrum> spectra) {
        final ScrolledComposite scrolledComposite = new ScrolledComposite(ComponentManager.clearAndGetScene(), SWT.V_SCROLL | SWT.H_SCROLL);
        scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        scrolledComposite.setLayout(new GridLayout());

        final Composite composite = newComposite()
                .gridLayoutData(GridData.FILL_BOTH)
                .layout(gridLayout().margins(10).spacings(10))
                .background(ComponentManager.getDisplay().getSystemColor(COLOR_WIDGET_BACKGROUND))
                .build(scrolledComposite);

        LabelBuilder labelBuilder = newLabel(SWT.LEFT)
                .gridLayoutData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);

        labelBuilder.text("Summary of equivalent widths etc.").build(composite);

        String[] names = spectra.stream()
                .map(spectrum -> spectrum.getFunctionAsset(MeasureEWFunction.SERIALIZE_KEY, MeasureEWResults.class).get())
                .map(MeasureEWResults::getMeasurementNames)
                .flatMap(Stream::of)
                .distinct()
                .sorted()
                .toArray(String[]::new);

        for (String name : names) {
            final Group group = new Group(composite, SWT.NONE);
            group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER));
            group.setLayout(gridLayout().margins(10).build());
            group.setText("Results for measurement " + name);

            Table table = newTable(SWT.BORDER)
                    .gridLayoutData(GridData.FILL_BOTH)
                    .linesVisible(true)
                    .headerVisible(true)
                    .unselectable()
                    .columns("Julian date", "EW", "FWHM", "V", "R", "Ic", "V/R", "(V+R)/2")
                    .build(group);

            for (Spectrum spectrum : spectra) {
                MeasureEWResults results = spectrum.getFunctionAsset(MeasureEWFunction.SERIALIZE_KEY, MeasureEWResults.class).get();
                if (results.hasMeasurementWithName(name)) {
                    MeasureEWResult result = results.getResultForName(name);
                    XYSeries series = spectrum.getProcessedSeries();

                    final TableItem tableItem = new TableItem(table, SWT.NONE);
                    tableItem.setText(0, formatLong(spectrum.getHjd().getJD()));
                    tableItem.setText(1, format(result.getEW(series)));

                    if (result.containsCategory(MeasureEWResultPointCategory.Ic)) {
                        double fwhm = result.getFWHM(series);
                        if (isNotNaN(fwhm)) {
                            tableItem.setText(2, format(fwhm));
                        }
                    }

                    int i = 3;
                    MeasureEWResultPointCategory[] categories = {MeasureEWResultPointCategory.V, MeasureEWResultPointCategory.R, MeasureEWResultPointCategory.Ic};
                    for (MeasureEWResultPointCategory category : categories) {
                        if (result.containsCategory(category)) {
                            tableItem.setText(i, format(series.getY(result.getPointForCategory(category))));
                        }
                        i++;
                    }

                    if (result.containsCategory(MeasureEWResultPointCategory.V) && result.containsCategory(MeasureEWResultPointCategory.R)) {
                        tableItem.setText(6, format(result.getVToR(series)));
                        tableItem.setText(7, format(result.getVRAvg(series)));
                    }
                }
            }

            for (TableColumn column : table.getColumns()) {
                column.pack();
            }
        }

        Composite buttonComposite = newComposite()
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_END)
                .layout(gridLayout().margins(10).spacings(10))
                .build(composite);

        newPushButton()
                .gridLayoutData(GridData.FILL_BOTH)
                .text("Print to file")
                .onSelection(event -> printToFile(spectra))
                .build(buttonComposite);

        scrolledComposite.setContent(composite);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        ComponentManager.getScene().layout();
        scrolledComposite.redraw();
    }

    private static String format(double value) {
        return String.format("%4.4f", value);
    }

    private static String formatLong(double value) {
        return String.format("%8.4f", value);
    }

    private static void printToFile(List<Spectrum> spectra) {
        String fileName = Project.getRootFileName(".eqw");

        try (PrintWriter writer = new PrintWriter(fileName)) {
            writer.println("Summary of equivalent widths etc.\n");

            String[] names = spectra.stream()
                    .map(spectrum -> spectrum.getFunctionAsset(MeasureEWFunction.SERIALIZE_KEY, MeasureEWResults.class).get())
                    .map(MeasureEWResults::getMeasurementNames)
                    .flatMap(Stream::of)
                    .distinct()
                    .sorted()
                    .toArray(String[]::new);

            for (String name : names) {
                writer.println();
                writer.println(name);
                writer.println(" Jul. date " + " " + "    EW    " + " " + "   FWHM   " + " " + "     V    " + " " + "     R    " + " " + "    Ic    " + " " + "    V/R   " + " " + "  (V+R)/2 ");

                for (Spectrum spectrum : spectra) {
                    MeasureEWResults results = spectrum.getFunctionAsset(MeasureEWFunction.SERIALIZE_KEY, MeasureEWResults.class).get();
                    if (results.hasMeasurementWithName(name)) {
                        MeasureEWResult result = results.getResultForName(name);
                        XYSeries series = spectrum.getProcessedSeries();

                        writer.print(formatDouble(spectrum.getHjd().getRJD(), 5, 4, false) + " ");
                        writer.print(formatDouble(result.getEW(series), 4, 4) + " ");

                        if (result.containsCategory(MeasureEWResultPointCategory.Ic)) {
                            double fwhm = result.getFWHM(series);
                            writer.print(formatDouble(isNotNaN(fwhm) ? fwhm : 9999.9999, 4, 4) + " ");
                        } else {
                            writer.print(" 9999.9999 ");
                        }

                        MeasureEWResultPointCategory[] categories = {MeasureEWResultPointCategory.V, MeasureEWResultPointCategory.R, MeasureEWResultPointCategory.Ic};
                        for (MeasureEWResultPointCategory category : categories) {
                            if (result.containsCategory(category)) {
                                writer.print(formatDouble(series.getY(result.getPointForCategory(category)), 4, 4) + " ");
                            } else {
                                writer.print(" 9999.9999 ");
                            }
                        }

                        if (result.containsCategory(MeasureEWResultPointCategory.V) && result.containsCategory(MeasureEWResultPointCategory.R)) {
                            writer.print(formatDouble(result.getVToR(series), 4, 4) + " ");
                            writer.print(formatDouble(result.getVRAvg(series), 4, 4));
                        } else {
                            writer.print(" 9999.9999  9999.9999");
                        }

                        writer.println();
                    }
                }
            }

            if (writer.checkError()) {
                throw new SpefoException("The PrintWriter is in an error state");

            } else {
                Message.info("File created successfully");
                FileExplorer.getDefault().refresh();
            }
        } catch (FileNotFoundException | SpefoException exception) {
            Message.error("Couldn't print to .cor file", exception);
        }
    }

}
