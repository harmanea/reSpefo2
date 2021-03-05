package cz.cuni.mff.respefo.function.scan;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.format.Spectrum;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.MultiFileFunction;
import cz.cuni.mff.respefo.function.ProjectFunction;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.asset.rv.MeasureRVResult;
import cz.cuni.mff.respefo.function.asset.rv.MeasureRVResults;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.util.DoubleArrayList;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.Progress;
import cz.cuni.mff.respefo.util.builders.widgets.ButtonBuilder;
import cz.cuni.mff.respefo.util.builders.widgets.LabelBuilder;
import cz.cuni.mff.respefo.util.utils.FileUtils;
import cz.cuni.mff.respefo.util.utils.MathUtils;
import cz.cuni.mff.respefo.util.utils.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.builders.widgets.ButtonBuilder.newButton;
import static cz.cuni.mff.respefo.util.builders.widgets.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.builders.widgets.LabelBuilder.newLabel;
import static cz.cuni.mff.respefo.util.builders.widgets.TableBuilder.newTable;
import static cz.cuni.mff.respefo.util.builders.widgets.TextBuilder.newText;
import static cz.cuni.mff.respefo.util.utils.FormattingUtils.formatDouble;
import static cz.cuni.mff.respefo.util.utils.MathUtils.isNotNaN;
import static java.lang.Double.isNaN;
import static org.eclipse.swt.SWT.COLOR_WIDGET_BACKGROUND;

@Fun(name = "RV Results", fileFilter = SpefoFormatFileFilter.class, group = "Results")
public class RVResultsFunction implements SingleFileFunction, MultiFileFunction, ProjectFunction {

    @Override
    public void execute(File file) {
        Spectrum spectrum;
        try {
            spectrum = Spectrum.open(file);
        } catch (SpefoException exception) {
            Message.error("Couldn't open file", exception);
            return;
        }

        if (!spectrum.containsFunctionAsset(MeasureRVFunction.SERIALIZE_KEY)) {
            Message.warning("There are no RV measurements in this file.");
            return;
        }

        displayResults(spectrum);
    }

    private static void displayResults(Spectrum spectrum) {
        MeasureRVResults results = spectrum.getFunctionAsset(MeasureRVFunction.SERIALIZE_KEY, MeasureRVResults.class).get();

        final ScrolledComposite scrolledComposite = new ScrolledComposite(ComponentManager.clearAndGetScene(), SWT.V_SCROLL);
        scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        scrolledComposite.setLayout(new GridLayout());

        final Composite composite = newComposite()
                .gridLayoutData(GridData.FILL_BOTH)
                .layout(gridLayout().margins(10).spacings(10))
                .background(ComponentManager.getDisplay().getSystemColor(COLOR_WIDGET_BACKGROUND))
                .build(scrolledComposite);

        LabelBuilder labelBuilder = newLabel(SWT.LEFT)
                .gridLayoutData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);

        labelBuilder.text("Summary of radial velocities measured on " + FileUtils.stripFileExtension(spectrum.getFile().getName()))
                .build(composite);

        labelBuilder.text("RV correction: " + spectrum.getRvCorrection()).build(composite);

        for (String category : results.getCategories()) {
            final Group group = new Group(composite, SWT.NONE);
            group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER));
            group.setLayout(gridLayout().margins(10).build());
            group.setText("Results for category " + category);

            final Table table = newTable(SWT.BORDER)
                    .gridLayoutData(GridData.FILL_BOTH)
                    .linesVisible(true)
                    .headerVisible(true)
                    .unselectable()
                    .columns("rv", "radius", "lambda", "name", "comment", "")
                    .build(group);

            final Text meanText = newText(SWT.MULTI | SWT.READ_ONLY)
                    .gridLayoutData(GridData.FILL_BOTH)
                    .visible(false)
                    .build(group);

            DoubleArrayList values = new DoubleArrayList();
            for (MeasureRVResult result : results.getResultsOfCategory(category)) {
                final TableItem tableItem = new TableItem(table, SWT.NONE);
                tableItem.setText(0, String.format(Locale.US, "%4.4f", result.getRv()));
                tableItem.setText(1, Double.toString(result.getRadius()));
                tableItem.setText(2, String.format(Locale.US, "%8.4f", result.getL0()));
                tableItem.setText(3, result.getName());
                tableItem.setText(4, result.getComment());

                final Button button = newButton(SWT.PUSH).text("Delete").build(table);
                button.pack();

                TableEditor editor = new TableEditor(table);
                editor.horizontalAlignment = SWT.RIGHT;
                editor.grabVertical = true;
                editor.minimumWidth = button.getSize().x;
                editor.setEditor(button, tableItem, 5);

                button.addListener(SWT.Selection, event -> {
                    results.remove(result);

                    table.remove(table.indexOf(tableItem));

                    editor.dispose();
                    button.dispose();
                    tableItem.dispose();

                    if (table.getItemCount() == 0) {
                        group.dispose();
                        composite.requestLayout();

                    } else if (table.getItemCount() == 1) {
                        meanText.dispose();
                        group.requestLayout();

                    } else {
                        double mean = results.getRvOfCategory(category);
                        double[] rvs = Arrays.stream(results.getResultsOfCategory(category)).mapToDouble(MeasureRVResult::getRv).toArray();

                        meanText.setText("mean RV: " + String.format(Locale.US, "%4.4f", mean)
                                + "\nstd. error: " + String.format(Locale.US, "%4.4f", MathUtils.sem(rvs, mean)));
                        group.requestLayout();
                    }

                    if (results.isEmpty()) {
                        spectrum.removeFunctionAsset(MeasureRVFunction.SERIALIZE_KEY);
                        scrolledComposite.dispose();
                    }

                    try {
                        spectrum.save();
                    } catch (SpefoException exception) {
                        Message.error("Couldn't save changes", exception);
                    }
                });

                values.add(result.getRv());
            }

            if (values.size() > 1) {
                double mean = results.getRvOfCategory(category);

                meanText.setVisible(true);
                meanText.setText("mean RV: " + String.format(Locale.US, "%4.4f", mean)
                        + "\nstd. error: " + String.format(Locale.US, "%4.4f", MathUtils.sem(values.toArray(), mean)));
            }

            for (TableColumn column : table.getColumns()) {
                column.pack();
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
        Progress.withProgressTracking(p -> {
            p.refresh("Processing files", files.size());

            List<Spectrum> spectra = new ArrayList<>();
            for (File file : files) {
                try {
                    Spectrum spectrum = Spectrum.open(file);

                    if (spectrum.containsFunctionAsset(MeasureRVFunction.SERIALIZE_KEY)) {
                        spectra.add(spectrum);
                    } else {
                        Log.warning("There are no RV measurements in the file [" + file.getPath() + "]");
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
                Message.warning("No valid spectrum with RV measurements was loaded");
            } else {
                displayResults(spectra);
            }
        });
    }

    private static void displayResults(List<Spectrum> spectra) {
        final ScrolledComposite scrolledComposite = new ScrolledComposite(ComponentManager.clearAndGetScene(), SWT.V_SCROLL);
        scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        scrolledComposite.setLayout(new GridLayout());

        final Composite composite = newComposite()
                .gridLayoutData(GridData.FILL_BOTH)
                .layout(gridLayout().margins(10).spacings(10))
                .background(ComponentManager.getDisplay().getSystemColor(COLOR_WIDGET_BACKGROUND))
                .build(scrolledComposite);

        LabelBuilder labelBuilder = newLabel(SWT.LEFT)
                .gridLayoutData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);

        labelBuilder.text("Summary of radial velocities").build(composite);

        Table table = newTable(SWT.BORDER)
                .gridLayoutData(GridData.FILL_BOTH)
                .linesVisible(true)
                .headerVisible(true)
                .unselectable()
                .columns("Julian date", "RV correction")
                .build(composite);

        String[] categories = spectra.stream()
                .map(spectrum -> spectrum.getFunctionAsset(MeasureRVFunction.SERIALIZE_KEY, MeasureRVResults.class).get())
                .map(MeasureRVResults::getCategories)
                .flatMap(Stream::of)
                .distinct()
                .sorted()
                .toArray(String[]::new);

        for (String title : categories) {
            final TableColumn tableColumn = new TableColumn(table, SWT.NONE);
            tableColumn.setText(title);
            new TableColumn(table, SWT.NONE); // for sem
        }

        for (Spectrum spectrum : spectra) {
            final TableItem tableItem = new TableItem(table, SWT.NONE);
            tableItem.setText(0, String.format(Locale.US, "%8.4f", spectrum.getHjd().getJD()));
            tableItem.setText(1, String.format(Locale.US, "%4.2f", spectrum.getRvCorrection()));

            MeasureRVResults results = spectrum.getFunctionAsset(MeasureRVFunction.SERIALIZE_KEY, MeasureRVResults.class).get();
            for (int i = 0; i < categories.length; i++) {
                double result = results.getRvOfCategory(categories[i]);
                if (isNotNaN(result)) {
                    tableItem.setText(2 * i + 2, String.format(Locale.US, "%4.2f", result));

                    if (results.getResultsOfCategory(categories[i]).length > 1) {
                        double sem = results.getSemOfCategory(categories[i]);
                        tableItem.setText(2 * i + 3, String.format(Locale.US, "%5.2f", sem));
                    }
                }
            }
        }

        for (TableColumn column : table.getColumns()) {
            column.pack();
        }

        Composite buttonsComposite = newComposite()
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_END)
                .layout(gridLayout(3, true).margins(10).spacings(10))
                .build(composite);

        ButtonBuilder buttonBuilder = ButtonBuilder.newButton(SWT.PUSH).gridLayoutData(GridData.FILL_BOTH);
        buttonBuilder.text("Print to .rvs file").onSelection(event -> printToRvsFile(spectra)).build(buttonsComposite);
        buttonBuilder.text("Print to .cor file").onSelection(event -> printToCorFile(spectra)).build(buttonsComposite);
        buttonBuilder.text("Print to .ac file").onSelection(event -> printToAcFile(spectra)).build(buttonsComposite);

        scrolledComposite.setContent(composite);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        ComponentManager.getScene().layout();
        scrolledComposite.redraw();
    }

    private static void printToRvsFile(List<Spectrum> spectra) {
        printToRvFile(".rvs", writer -> {
            writer.print("Jul. date  RVCorr");

            String[] categories = spectra.stream()
                    .map(spectrum -> spectrum.getFunctionAsset(MeasureRVFunction.SERIALIZE_KEY, MeasureRVResults.class).get())
                    .map(MeasureRVResults::getCategories)
                    .flatMap(Stream::of)
                    .distinct()
                    .sorted()
                    .toArray(String[]::new);
            for (String category : categories) {
                writer.print(" " + StringUtils.trimmedOrPaddedString(category, 8) + "         ");
            }
            writer.println();

            for (Spectrum spectrum : spectra) {
                writer.print(formatDouble(spectrum.getHjd().getRJD(), 5, 4, false) + " ");
                writer.print(formatDouble(spectrum.getRvCorrection(), 2, 2, true));

                MeasureRVResults results = spectrum.getFunctionAsset(MeasureRVFunction.SERIALIZE_KEY, MeasureRVResults.class).get();
                for (String category : categories) {
                    double result = results.getRvOfCategory(category);
                    if (isNotNaN(result)) {
                        writer.print(" " + formatDouble(result, 4, 2) + " ");

                        if (results.getResultsOfCategory(category).length > 1) {
                            double sem = results.getSemOfCategory(category);
                            writer.print(formatDouble(sem, 5, 2, false));
                        } else {
                            writer.print("    0.00");
                        }
                    } else {
                        writer.print("  9999.99     0.00");
                    }
                }
                writer.println();
            }
        });
    }

    private static void printToCorFile(List<Spectrum> spectra) {
        printToRvFile(".cor", writer -> {
            writer.print("Jul. date ");

            String[] categories = spectra.stream()
                    .map(spectrum -> spectrum.getFunctionAsset(MeasureRVFunction.SERIALIZE_KEY, MeasureRVResults.class).get())
                    .map(MeasureRVResults::getCategories)
                    .flatMap(Stream::of)
                    .distinct()
                    .filter(category -> !category.equals("corr"))
                    .sorted()
                    .toArray(String[]::new);
            for (String category : categories) {
                writer.print(" " + StringUtils.trimmedOrPaddedString(category, 8) + "         ");
            }
            writer.println();

            for (Spectrum spectrum : spectra) {
                MeasureRVResults results = spectrum.getFunctionAsset(MeasureRVFunction.SERIALIZE_KEY, MeasureRVResults.class).get();

                if (isNaN(results.getRvOfCategory("corr"))) {
                    Log.warning("Spectrum " + spectrum.getFile().getName() + " was skipped because it does not have meaured corrections.");
                    continue;
                }

                writer.print(formatDouble(spectrum.getHjd().getRJD(), 5, 4, false));

                double rvCorr = spectrum.getRvCorrection() - results.getRvOfCategory("corr");
                for (String category : categories) {
                    double result = results.getRvOfCategory(category);
                    if (isNotNaN(result)) {
                        writer.print(" " + formatDouble(result + rvCorr, 4, 2) + " ");

                        if (results.getResultsOfCategory(category).length > 1) {
                            double sem = results.getSemOfCategory(category);
                            writer.print(formatDouble(sem, 5, 2, false));
                        } else {
                            writer.print("    0.00");
                        }
                    } else {
                        writer.print("  9999.99     0.00");
                    }
                }
                writer.println();
            }
        });
    }

    private static void printToAcFile(List<Spectrum> spectra) {
        printToRvFile(".ac", writer -> {
            for (Spectrum spectrum : spectra) {
                MeasureRVResults results = spectrum.getFunctionAsset(MeasureRVFunction.SERIALIZE_KEY, MeasureRVResults.class).get();

                if (isNaN(results.getRvOfCategory("corr"))) {
                    Log.warning("Spectrum " + spectrum.getFile().getName() + " was skipped because it does not have meaured corrections.");
                    continue;
                }

                double rvCorr = spectrum.getRvCorrection() - results.getRvOfCategory("corr");
                writer.print(formatDouble(rvCorr, 2, 2, true));

                writer.print("  ");

                writer.println(formatDouble(spectrum.getHjd().getRJD(), 5, 4, false));
            }
        });
    }

    private static void printToRvFile(String suffix, Consumer<PrintWriter> printer) {
        File rootDirectory = ComponentManager.getFileExplorer().getRootDirectory();
        String fileName = rootDirectory.getPath() + File.separator + rootDirectory.getName() + suffix;

        try (PrintWriter writer = new PrintWriter(fileName)) {
            writer.println("Summary of radial velocities");

            printer.accept(writer);

            Message.info("File created successfully");
            ComponentManager.getFileExplorer().refresh();
        } catch (FileNotFoundException exception) {
            Message.error("Couldn't print to " + suffix + " file", exception);
        }
    }
}
