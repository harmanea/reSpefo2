package cz.cuni.mff.respefo.function.scan;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.format.Spectrum;
import cz.cuni.mff.respefo.format.XYSeries;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.SingleOrMultiFileFunction;
import cz.cuni.mff.respefo.function.asset.ew.MeasureEWResult;
import cz.cuni.mff.respefo.function.asset.ew.MeasureEWResultPointCategory;
import cz.cuni.mff.respefo.function.asset.ew.MeasureEWResults;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.utils.FileUtils;
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
import java.util.Locale;
import java.util.stream.Stream;

import static cz.cuni.mff.respefo.util.builders.ButtonBuilder.pushButton;
import static cz.cuni.mff.respefo.util.builders.CompositeBuilder.composite;
import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.builders.LabelBuilder.label;
import static cz.cuni.mff.respefo.util.utils.FormattingUtils.formatDouble;
import static cz.cuni.mff.respefo.util.utils.MathUtils.isNotNaN;
import static java.lang.Double.isNaN;

@Fun(name = "EW Results", fileFilter = SpefoFormatFileFilter.class, group = "Results")
public class EWResultsFunction implements SingleOrMultiFileFunction {
    @Override
    public void execute(File file) {
        Spectrum spectrum;
        try {
            spectrum = Spectrum.open(file);
        } catch (SpefoException exception) {
            Message.error("Couldn't open file", exception);
            return;
        }

        if (!spectrum.getFunctionAssets().containsKey(MeasureEWFunction.SERIALIZE_KEY)) {
            Message.warning("There are no EW measurements in this file.");
            return;
        }

        displayResults(spectrum);
    }

    private static void displayResults(Spectrum spectrum) {
        MeasureEWResults results = (MeasureEWResults) spectrum.getFunctionAssets().get(MeasureEWFunction.SERIALIZE_KEY);
        XYSeries series = spectrum.getProcessedSeries();

        ScrolledComposite scrolledComposite = new ScrolledComposite(ComponentManager.clearAndGetScene(), SWT.V_SCROLL);
        scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        scrolledComposite.setLayout(new GridLayout());

        final Composite composite = composite(scrolledComposite)
                .layoutData(new GridData(GridData.FILL_BOTH))
                .layout(gridLayout().margins(10).spacings(10))
                .build();

        final Label titleLabel = label(composite, SWT.LEFT)
                .layoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING))
                .text("Summary of equivalent widths etc. measured on " + FileUtils.stripFileExtension(spectrum.getFile().getName()))
                .build();

        composite.setBackground(titleLabel.getBackground());

        for (MeasureEWResults.MeasurementAndResult mar : results) {
            Group group = new Group(composite, SWT.NONE);
            group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER));
            group.setLayout(gridLayout().margins(10).build());
            group.setText("Results for measurement " + mar.getMeasurement().getName());

            label(group, SWT.LEFT)
                    .layoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING))
                    .text("EW: " + formatDouble(mar.getResult().getEW(series), 4, 4))
                    .build();

            if (mar.getResult().containsCategory(MeasureEWResultPointCategory.Ic)) {
                double fwhm = mar.getResult().getFWHM(series);
                if (isNaN(fwhm)) {
                    Log.warning("Couldn't compute FWHM for measurement " + mar.getMeasurement().getName());
                } else {
                    label(group, SWT.LEFT)
                            .layoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING))
                            .text("FWHM: " + formatDouble(fwhm, 4, 4))
                            .build();
                }
            }

            for (int i = 0; i < mar.getResult().pointsCount(); i++) {
                label(group, SWT.LEFT)
                        .layoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING))
                        .text(mar.getResult().getCategory(i).name() + ": " + formatDouble(series.getY(mar.getResult().getPoint(i)), 4, 4))
                        .build();
            }
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
        List<Spectrum> spectra = new ArrayList<>();
        for (File file : files) {
            try {
                Spectrum spectrum = Spectrum.open(file);

                if (spectrum.getFunctionAssets().containsKey(MeasureEWFunction.SERIALIZE_KEY)) {
                    spectra.add(spectrum);
                } else {
                    Log.warning("There are no EW measurements in the file [" + file.getPath() + "]");
                }
            } catch (SpefoException exception) {
                Log.error("Couldn't open file [" + file.getPath() + "]", exception);
            }
        }

        if (spectra.isEmpty()) {
            Message.warning("No valid spectrum with EW measurements was loaded");
            return;
        }

        displayResults(spectra);
    }

    private static void displayResults(List<Spectrum> spectra) {
        ScrolledComposite scrolledComposite = new ScrolledComposite(ComponentManager.clearAndGetScene(), SWT.V_SCROLL);
        scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        scrolledComposite.setLayout(new GridLayout());

        final Composite composite = composite(scrolledComposite)
                .layoutData(new GridData(GridData.FILL_BOTH))
                .layout(gridLayout().margins(10).spacings(10))
                .build();

        final Label titleLabel = label(composite, SWT.LEFT)
                .layoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING))
                .text("Summary of equivalent widths etc.")
                .build();

        composite.setBackground(titleLabel.getBackground());

        String[] names = spectra.stream()
                .map(spectrum -> (MeasureEWResults) spectrum.getFunctionAssets().get(MeasureEWFunction.SERIALIZE_KEY))
                .map(MeasureEWResults::getMeasurementNames)
                .flatMap(Stream::of)
                .distinct()
                .sorted()
                .toArray(String[]::new);

        for (String name : names) {
            Group group = new Group(composite, SWT.NONE);
            group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER));
            group.setLayout(gridLayout().margins(10).build());
            group.setText("Results for measurement " + name);

            Table table = new Table(group, SWT.BORDER);
            table.setLayoutData(new GridData(GridData.FILL_BOTH));
            table.setLinesVisible(true);
            table.setHeaderVisible(true);
            table.addListener(SWT.Selection, event -> table.deselectAll());

            String[] titles = {"Julian date", "EW", "FWHM", "V", "R", "Ic", "V/R", "(V+R)/2"};
            for (String title : titles) {
                TableColumn tableColumn = new TableColumn(table, SWT.NONE);
                tableColumn.setText(title);
            }

            for (Spectrum spectrum : spectra) {
                MeasureEWResults results = (MeasureEWResults) spectrum.getFunctionAssets().get(MeasureEWFunction.SERIALIZE_KEY);
                if (results.hasMeasurementWithName(name)) {
                    MeasureEWResult result = results.getResultForName(name);
                    XYSeries series = spectrum.getProcessedSeries();

                    TableItem tableItem = new TableItem(table, SWT.NONE);
                    tableItem.setText(0, String.format(Locale.US, "%8.4f", spectrum.getHjd().getJD()));
                    tableItem.setText(1, String.format(Locale.US, "%4.4f", result.getEW(series)));

                    if (result.containsCategory(MeasureEWResultPointCategory.Ic)) {
                        double fwhm = result.getFWHM(series);
                        if (isNotNaN(fwhm)) {
                            tableItem.setText(2, String.format(Locale.US, "%4.4f", fwhm));
                        }
                    }

                    int i = 3;
                    MeasureEWResultPointCategory[] categories = {MeasureEWResultPointCategory.V, MeasureEWResultPointCategory.R, MeasureEWResultPointCategory.Ic};
                    for (MeasureEWResultPointCategory category : categories) {
                        if (result.containsCategory(category)) {
                            tableItem.setText(i, String.format(Locale.US, "%4.4f", series.getY(result.getPointForCategory(category))));
                        }
                        i++;
                    }

                    if (result.containsCategory(MeasureEWResultPointCategory.V) && result.containsCategory(MeasureEWResultPointCategory.R)) {
                        tableItem.setText(6, String.format(Locale.US, "%4.4f", result.getVToR(series)));
                        tableItem.setText(7, String.format(Locale.US, "%4.4f", result.getVRAvg(series)));
                    }
                }
            }

            for (TableColumn column : table.getColumns()) {
                column.pack();
            }
        }

        Composite buttonComposite = composite(composite)
                .layoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false))
                .layout(gridLayout().margins(10).spacings(10))
                .build();

        pushButton(buttonComposite)
                .layoutData(new GridData(GridData.FILL_BOTH))
                .text("Print to file")
                .onSelection(event -> printToFile(spectra));

        scrolledComposite.setContent(composite);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        ComponentManager.getScene().layout();
        scrolledComposite.redraw();
    }

    private static void printToFile(List<Spectrum> spectra) {
        String fileName = ComponentManager.getFileExplorer().getRootDirectory().getPath() + File.separator + ComponentManager.getFileExplorer().getRootDirectory().getName() + ".eqw";

        try (PrintWriter writer = new PrintWriter(fileName)) {
            writer.println("Summary of equivalent widths etc.\n");

            String[] names = spectra.stream()
                    .map(spectrum -> (MeasureEWResults) spectrum.getFunctionAssets().get(MeasureEWFunction.SERIALIZE_KEY))
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
                    MeasureEWResults results = (MeasureEWResults) spectrum.getFunctionAssets().get(MeasureEWFunction.SERIALIZE_KEY);
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

            Message.info("File created successfully");
            ComponentManager.getFileExplorer().refresh();
        } catch (FileNotFoundException exception) {
            Message.error("Couldn't print to .cor file", exception);
        }
    }

}
