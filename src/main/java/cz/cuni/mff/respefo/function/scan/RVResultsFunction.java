package cz.cuni.mff.respefo.function.scan;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.format.Spectrum;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.MultiFileFunction;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.asset.rv.MeasureRVResult;
import cz.cuni.mff.respefo.function.asset.rv.MeasureRVResults;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.util.DoubleArrayList;
import cz.cuni.mff.respefo.util.Message;
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
import java.util.stream.Stream;

import static cz.cuni.mff.respefo.util.builders.ButtonBuilder.pushButton;
import static cz.cuni.mff.respefo.util.builders.CompositeBuilder.composite;
import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.builders.LabelBuilder.label;
import static cz.cuni.mff.respefo.util.utils.FormattingUtils.formatDouble;
import static cz.cuni.mff.respefo.util.utils.MathUtils.isNotNaN;
import static java.lang.Double.isNaN;

@Fun(name = "RV Results", fileFilter = SpefoFormatFileFilter.class, group = "Results")
public class RVResultsFunction implements SingleFileFunction, MultiFileFunction {

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

        ScrolledComposite scrolledComposite = new ScrolledComposite(ComponentManager.clearAndGetScene(), SWT.V_SCROLL);
        scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        scrolledComposite.setLayout(new GridLayout());

        final Composite composite = composite(scrolledComposite)
                .layoutData(new GridData(GridData.FILL_BOTH))
                .layout(gridLayout().margins(10).spacings(10))
                .build();

        final Label titleLabel = label(composite, SWT.LEFT)
                .layoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING))
                .text("Summary of radial velocities measured on " + FileUtils.stripFileExtension(spectrum.getFile().getName()))
                .build();

        label(composite, SWT.LEFT)
                .layoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING))
                .text("RV correction: " + spectrum.getRvCorrection());

        composite.setBackground(titleLabel.getBackground());

        for (String category : results.getCategories()) {
            Group group = new Group(composite, SWT.NONE);
            group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER));
            group.setLayout(gridLayout().margins(10).build());
            group.setText("Results for category " + category);

            Table table = new Table(group, SWT.BORDER);
            table.setLayoutData(new GridData(GridData.FILL_BOTH));
            table.setLinesVisible(true);
            table.setHeaderVisible(true);
            table.addListener(SWT.Selection, event -> table.deselectAll());

            final Text meanText = new Text(group, SWT.MULTI | SWT.READ_ONLY);
            meanText.setLayoutData(new GridData(GridData.FILL_BOTH));
            meanText.setVisible(false);

            String[] titles = {"rv", "radius", "lambda", "name", "comment", ""};
            for (String title : titles) {
                TableColumn tableColumn = new TableColumn(table, SWT.NONE);
                tableColumn.setText(title);
            }

            DoubleArrayList values = new DoubleArrayList();
            for (MeasureRVResult result : results.getResultsOfCategory(category)) {
                TableItem tableItem = new TableItem(table, SWT.NONE);
                tableItem.setText(0, String.format(Locale.US, "%4.4f", result.getRv()));
                tableItem.setText(1, Double.toString(result.getRadius()));
                tableItem.setText(2, String.format(Locale.US, "%8.4f", result.getL0()));
                tableItem.setText(3, result.getName());
                tableItem.setText(4, result.getComment());

                Button button = pushButton(table)
                        .text("Delete")
                        .build();
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
                                + "\nrmse: " + String.format(Locale.US, "%4.4f", MathUtils.rmse(rvs, mean)));
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
                        + "\nrmse: " + String.format(Locale.US, "%4.4f", MathUtils.rmse(values.toArray(), mean)));
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
            }
        }

        if (spectra.isEmpty()) {
            Message.warning("No valid spectrum with RV measurements was loaded");
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
                .text("Summary of radial velocities")
                .build();

        composite.setBackground(titleLabel.getBackground());

        Table table = new Table(composite, SWT.BORDER);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        table.addListener(SWT.Selection, event -> table.deselectAll());

        String[] categories = spectra.stream()
                .map(spectrum -> spectrum.getFunctionAsset(MeasureRVFunction.SERIALIZE_KEY, MeasureRVResults.class).get())
                .map(MeasureRVResults::getCategories)
                .flatMap(Stream::of)
                .distinct()
                .sorted()
                .toArray(String[]::new);

        String[] titles = {"Julian date", "RV correction"};
        for (String title : titles) {
            TableColumn tableColumn = new TableColumn(table, SWT.NONE);
            tableColumn.setText(title);
        }
        for (String title : categories) {
            TableColumn tableColumn = new TableColumn(table, SWT.NONE);
            tableColumn.setText(title);
            new TableColumn(table, SWT.NONE); // for rmse
        }

        for (Spectrum spectrum : spectra) {
            TableItem tableItem = new TableItem(table, SWT.NONE);
            tableItem.setText(0, String.format(Locale.US, "%8.4f", spectrum.getHjd().getJD()));
            tableItem.setText(1, String.format(Locale.US, "%4.2f", spectrum.getRvCorrection()));

            MeasureRVResults results = spectrum.getFunctionAsset(MeasureRVFunction.SERIALIZE_KEY, MeasureRVResults.class).get();
            for (int i = 0; i < categories.length; i++) {
                double result = results.getRvOfCategory(categories[i]);
                if (isNotNaN(result)) {
                    tableItem.setText(2 * i + 2, String.format(Locale.US, "%4.2f", result));

                    if (results.getResultsOfCategory(categories[i]).length > 1) {
                        double rmse = results.getRmseOfCategory(categories[i]);
                        tableItem.setText(2 * i + 3, String.format(Locale.US, "%5.2f", rmse));
                    }
                }
            }
        }

        for (TableColumn column : table.getColumns()) {
            column.pack();
        }

        Composite buttonsComposite = composite(composite)
                .layoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false))
                .layout(gridLayout(2, true).margins(10).spacings(10))
                .build();

        pushButton(buttonsComposite)
                .layoutData(new GridData(GridData.FILL_BOTH))
                .text("Print to .rvs file")
                .onSelection(event -> printToRvsFile(spectra));

        pushButton(buttonsComposite)
                .layoutData(new GridData(GridData.FILL_BOTH))
                .text("Print to .cor file")
                .onSelection(event -> printToCorFile(spectra));

        scrolledComposite.setContent(composite);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        ComponentManager.getScene().layout();
        scrolledComposite.redraw();
    }

    private static void printToRvsFile(List<Spectrum> spectra) {
        String fileName = ComponentManager.getFileExplorer().getRootDirectory().getPath() + File.separator + ComponentManager.getFileExplorer().getRootDirectory().getName() + ".rvs";

        try (PrintWriter writer = new PrintWriter(fileName)) {
            writer.println("Summary of radial velocities\n");

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
                            double rmse = results.getRmseOfCategory(category);
                            writer.print(formatDouble(rmse, 5, 2, false));
                        } else {
                            writer.print("    0.00");
                        }
                    } else {
                        writer.print("  9999.99     0.00");
                    }
                }
                writer.println();
            }

            Message.info("File created successfully");
            ComponentManager.getFileExplorer().refresh();
        } catch (FileNotFoundException exception) {
            Message.error("Couldn't print to .rvs file", exception);
        }
    }

    private static void printToCorFile(List<Spectrum> spectra) {
        String fileName = ComponentManager.getFileExplorer().getRootDirectory().getPath() + File.separator + ComponentManager.getFileExplorer().getRootDirectory().getName() + ".cor";

        try (PrintWriter writer = new PrintWriter(fileName)) {
            writer.println("Summary of radial velocities\n");

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
                            double rmse = results.getRmseOfCategory(category);
                            writer.print(formatDouble(rmse, 5, 2, false));
                        } else {
                            writer.print("    0.00");
                        }
                    } else {
                        writer.print("  9999.99     0.00");
                    }
                }
                writer.println();
            }

            Message.info("File created successfully");
            ComponentManager.getFileExplorer().refresh();
        } catch (FileNotFoundException exception) {
            Message.error("Couldn't print to .cor file", exception);
        }
    }
}
