package cz.cuni.mff.respefo.function.rv;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.component.Project;
import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.MultiFileFunction;
import cz.cuni.mff.respefo.function.ProjectFunction;
import cz.cuni.mff.respefo.function.SpectrumFunction;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.util.Async;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.NaturalOrderComparator;
import cz.cuni.mff.respefo.util.Progress;
import cz.cuni.mff.respefo.util.utils.FileUtils;
import cz.cuni.mff.respefo.util.utils.StringUtils;
import cz.cuni.mff.respefo.util.widget.ButtonBuilder;
import cz.cuni.mff.respefo.util.widget.DefaultSelectionListener;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static cz.cuni.mff.respefo.util.layout.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.utils.FormattingUtils.formatDouble;
import static cz.cuni.mff.respefo.util.utils.MathUtils.isNotNaN;
import static cz.cuni.mff.respefo.util.widget.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.widget.LabelBuilder.newLabel;
import static cz.cuni.mff.respefo.util.widget.TableBuilder.newTable;
import static cz.cuni.mff.respefo.util.widget.TextBuilder.newText;
import static java.lang.Double.isNaN;
import static org.eclipse.swt.SWT.COLOR_WIDGET_BACKGROUND;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@Fun(name = "RV Results", fileFilter = SpefoFormatFileFilter.class, group = "Results")
public class RVResultsFunction extends SpectrumFunction implements MultiFileFunction, ProjectFunction {

    @Override
    public void execute(Spectrum spectrum) {
        if (!spectrum.containsFunctionAsset(MeasureRVFunction.SERIALIZE_KEY)) {
            Message.warning("There are no RV measurements in this file.");
            return;
        }

        displayResults(spectrum);
    }

    public static void displayResults(Spectrum spectrum) {
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

        setUpGroups(spectrum, composite, results);

        scrolledComposite.setContent(composite);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        ComponentManager.getScene().layout();
        scrolledComposite.redraw();
    }

    private static void setUpGroups(Spectrum spectrum, Composite parent, MeasureRVResults results) {
        List<Table> tables = new LinkedList<>();
        for (String category : results.getCategories()) {
            final Group group = new Group(parent, SWT.NONE);
            group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER));
            group.setLayout(gridLayout().margins(10).build());
            group.setText("Results for category " + category);

            final Table table = newTable(SWT.BORDER | SWT.SINGLE | SWT.NO_SCROLL)
                    .gridLayoutData(GridData.FILL_BOTH)
                    .linesVisible(true)
                    .headerVisible(true)
                    .columns("rv", "radius", "lambda", "name", "comment", "")
                    .items(() -> results.getResultsOfCategory(category).iterator(),
                            result -> new String[]{
                                    format(result.getRv(), 4, 4),
                                    Double.toString(result.getRadius()),
                                    format(result.getL0(), 8, 4),
                                    result.getName(),
                                    result.getComment()
                            },
                            (result, item) -> {
                                item.setData(result);
                                if (results.isRepeatedMeasurement(result)) {
                                    item.setBackground(2, ComponentManager.getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
                                    item.setBackground(3, ComponentManager.getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
                                }
                            })
                    .onSelection(event -> {
                        Table thisTable = (Table) event.widget;
                        for (Table otherTable : tables) {
                            if (otherTable != thisTable) {
                                otherTable.deselectAll();
                            }
                        }
                    })
                    .packColumns()
                    .build(group);
            tables.add(table);

            if (table.getItemCount() > 1) {
                String meanText = "mean RV: " + format(results.getRvOfCategory(category), 4, 4)
                        + "\nstd. error: " + format(results.getSemOfCategory(category), 4, 4);
                newText(SWT.MULTI | SWT.READ_ONLY)
                        .gridLayoutData(GridData.FILL_BOTH)
                        .text(meanText)
                        .build(group);
            }

            final Menu menu = new Menu(ComponentManager.getShell(), SWT.POP_UP);
            table.setMenu(menu);
            table.addMenuDetectListener(event -> event.doit = table.getSelectionCount() > 0);

            final MenuItem editMenuItem = new MenuItem(menu, SWT.PUSH);
            editMenuItem.setText("Edit");
            editMenuItem.addSelectionListener(new DefaultSelectionListener(event -> {
                TableItem tableItem = table.getItem(table.getSelectionIndex());
                MeasureRVResult result = (MeasureRVResult) tableItem.getData();

                MeasurementInputDialog dialog = new MeasurementInputDialog(result.category, result.comment);
                if (dialog.openIsOk()) {
                    result.comment = dialog.getComment();
                    result.category = dialog.getCategory();

                    Async.exec(() -> trySaveAndRefresh(spectrum, parent, results, tables));
                }
            }));

            final MenuItem deleteMenuItem = new MenuItem(menu, SWT.PUSH);
            deleteMenuItem.setText("Delete");
            deleteMenuItem.addSelectionListener(new DefaultSelectionListener(event -> {
                TableItem tableItem = table.getItem(table.getSelectionIndex());
                MeasureRVResult result = (MeasureRVResult) tableItem.getData();

                results.remove(result);

                if (results.isEmpty()) {
                    spectrum.removeFunctionAsset(MeasureRVFunction.SERIALIZE_KEY);
                }

                Async.exec(() -> trySaveAndRefresh(spectrum, parent, results, tables));
            }));
        }
    }

    private static void trySaveAndRefresh(Spectrum spectrum, Composite parent, MeasureRVResults results, List<Table> tables) {
        try {
            spectrum.save();
        } catch (SpefoException exception) {
            Message.error("Couldn't save changes", exception);
        }

        if (results.isEmpty()) {
            ComponentManager.clearScene(true);

        } else {
            for (Table table : tables) {
                table.getParent().dispose();
            }

            setUpGroups(spectrum, parent, results);

            parent.layout();
        }
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
                .sorted(NaturalOrderComparator.getInstance())
                .toArray(String[]::new);

        for (String title : categories) {
            final TableColumn tableColumn = new TableColumn(table, SWT.NONE);
            tableColumn.setText(title);
            new TableColumn(table, SWT.NONE); // for sem
        }

        for (Spectrum spectrum : spectra) {
            final TableItem tableItem = new TableItem(table, SWT.NONE);
            tableItem.setText(0, format(spectrum.getHjd().getJD(), 8, 4));
            tableItem.setText(1, format(spectrum.getRvCorrection(), 4, 2));

            MeasureRVResults results = spectrum.getFunctionAsset(MeasureRVFunction.SERIALIZE_KEY, MeasureRVResults.class).get();
            for (int i = 0; i < categories.length; i++) {
                double result = results.getRvOfCategory(categories[i]);
                if (isNotNaN(result)) {
                    tableItem.setText(2 * i + 2, format(result, 4, 2));

                    if (results.getNumberOfResultsInCategory(categories[i]) > 1) {
                        double sem = results.getSemOfCategory(categories[i]);
                        tableItem.setText(2 * i + 3, format(sem, 5, 2));
                    }
                }
            }
        }

        for (TableColumn column : table.getColumns()) {
            column.pack();
        }

        Composite buttonsComposite = newComposite()
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_END)
                .layout(gridLayout(4, true).margins(10).spacings(10))
                .build(composite);

        Button includeErrorsButton = ButtonBuilder.newCheckButton()
                .gridLayoutData(GridData.VERTICAL_ALIGN_CENTER | GridData.GRAB_VERTICAL | GridData.FILL_HORIZONTAL)
                .text("Include error columns")
                .selection(true)
                .build(buttonsComposite);

        ButtonBuilder buttonBuilder = ButtonBuilder.newPushButton().gridLayoutData(GridData.FILL_BOTH);
        buttonBuilder.text("Print to .rvs file").onSelection(event -> printToRvsFile(spectra, includeErrorsButton.getSelection())).build(buttonsComposite);
        buttonBuilder.text("Print to .cor file").onSelection(event -> printToCorFile(spectra, includeErrorsButton.getSelection())).build(buttonsComposite);
        buttonBuilder.text("Print to .ac file").onSelection(event -> printToAcFile(spectra)).build(buttonsComposite);

        scrolledComposite.setContent(composite);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        ComponentManager.getScene().layout();
        scrolledComposite.redraw();
    }

    private static void printToRvsFile(List<Spectrum> spectra, boolean includeErrors) {
        printToRvFile(".rvs", writer -> {
            writer.print("Jul. date  RVCorr");

            String[] categories = spectra.stream()
                    .map(spectrum -> spectrum.getFunctionAsset(MeasureRVFunction.SERIALIZE_KEY, MeasureRVResults.class).get())
                    .map(MeasureRVResults::getCategories)
                    .flatMap(Stream::of)
                    .distinct()
                    .sorted(NaturalOrderComparator.getInstance())
                    .toArray(String[]::new);
            for (String category : categories) {
                writer.print(" " + StringUtils.trimmedOrPaddedString(category, 8));
                if (includeErrors) {
                    writer.print("         ");
                }
            }
            writer.println();

            for (Spectrum spectrum : spectra) {
                writer.print(formatDouble(spectrum.getHjd().getRJD(), 5, 4, false) + " ");
                writer.print(formatDouble(spectrum.getRvCorrection(), 2, 2, true));

                MeasureRVResults results = spectrum.getFunctionAsset(MeasureRVFunction.SERIALIZE_KEY, MeasureRVResults.class).get();
                for (String category : categories) {
                    double result = results.getRvOfCategory(category);
                    String text = isNotNaN(result) ? " " + formatDouble(result, 4, 2) : "  9999.99";
                    writer.print(text);

                    if (includeErrors) {
                        double sem = results.getNumberOfResultsInCategory(category) > 1 ? results.getSemOfCategory(category) : 0;
                        writer.print(" " + formatDouble(sem, 5, 2, false));
                    }
                }
                writer.println();
            }
        });
    }

    private static void printToCorFile(List<Spectrum> spectra, boolean includeErrors) {
        printToRvFile(".cor", writer -> {
            writer.print("Jul. date ");

            String[] categories = spectra.stream()
                    .map(spectrum -> spectrum.getFunctionAsset(MeasureRVFunction.SERIALIZE_KEY, MeasureRVResults.class).get())
                    .map(MeasureRVResults::getCategories)
                    .flatMap(Stream::of)
                    .distinct()
                    .filter(category -> !category.equals("corr"))
                    .sorted(NaturalOrderComparator.getInstance())
                    .toArray(String[]::new);
            for (String category : categories) {
                writer.print(" " + StringUtils.trimmedOrPaddedString(category, 8));
                if (includeErrors) {
                    writer.print("         ");
                }
            }
            writer.println();

            for (Spectrum spectrum : spectra) {
                MeasureRVResults results = spectrum.getFunctionAsset(MeasureRVFunction.SERIALIZE_KEY, MeasureRVResults.class).get();

                if (isNaN(results.getRvOfCategory("corr"))) {
                    Log.warning("Spectrum " + spectrum.getFile().getName() + " was skipped because it does not have measured corrections.");
                    continue;
                }

                writer.print(formatDouble(spectrum.getHjd().getRJD(), 5, 4, false));

                double rvCorr = spectrum.getRvCorrection() - results.getRvOfCategory("corr");
                for (String category : categories) {
                    double result = results.getRvOfCategory(category);
                    String text = isNotNaN(result) ? " " + formatDouble(result + rvCorr, 4, 2) : "  9999.99";
                    writer.print(text);

                    if (includeErrors) {
                        double sem = results.getNumberOfResultsInCategory(category) > 1 ? results.getSemOfCategory(category) : 0;
                        writer.print(" " + formatDouble(sem, 5, 2, false));
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
                    Log.warning("Spectrum " + spectrum.getFile().getName() + " was skipped because it does not have measured corrections.");
                    continue;
                }

                double rvCorrDiff = spectrum.getRvCorrection() - results.getRvOfCategory("corr");
                writer.print(formatDouble(rvCorrDiff, 2, 2, true));

                writer.print("  ");

                writer.println(formatDouble(spectrum.getHjd().getRJD(), 5, 4, false));
            }
        });
    }

    private static void printToRvFile(String suffix, Consumer<PrintWriter> printer) {
        String fileName = Project.getRootFileName(suffix);
        try (PrintWriter writer = new PrintWriter(fileName)) {
            writer.println("Summary of radial velocities");

            printer.accept(writer);

            Message.info("File created successfully");
            Project.refresh();
        } catch (FileNotFoundException exception) {
            Message.error("Couldn't print to " + suffix + " file", exception);
        }
    }

    private static String format(double value, int before, int after) {
        return String.format(Locale.US, String.format("%%%d.%df", before, after), value);
    }
}
