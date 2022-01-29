package cz.cuni.mff.respefo.function.rectify;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.component.SpectrumExplorer;
import cz.cuni.mff.respefo.component.ToolBar;
import cz.cuni.mff.respefo.component.VerticalToggle;
import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.Serialize;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.clean.CleanAsset;
import cz.cuni.mff.respefo.function.clean.CleanFunction;
import cz.cuni.mff.respefo.function.common.ChartKeyListener;
import cz.cuni.mff.respefo.function.common.DragMouseListener;
import cz.cuni.mff.respefo.function.common.ZoomMouseWheelListener;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.function.open.OpenFunction;
import cz.cuni.mff.respefo.resources.ColorResource;
import cz.cuni.mff.respefo.resources.ImageResource;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.spectrum.format.EchelleSpectrum;
import cz.cuni.mff.respefo.spectrum.format.SimpleSpectrum;
import cz.cuni.mff.respefo.util.Async;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.collections.DoubleArrayList;
import cz.cuni.mff.respefo.util.collections.Point;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;
import cz.cuni.mff.respefo.util.utils.ChartUtils;
import cz.cuni.mff.respefo.util.utils.MathUtils;
import cz.cuni.mff.respefo.util.widget.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries;

import java.io.File;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

import static cz.cuni.mff.respefo.function.rectify.Blaze.K;
import static cz.cuni.mff.respefo.resources.ColorManager.getColor;
import static cz.cuni.mff.respefo.resources.ColorResource.*;
import static cz.cuni.mff.respefo.util.utils.CollectionUtils.listOf;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.AxisLabel.RELATIVE_FLUX;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.AxisLabel.WAVELENGTH;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.LineSeriesBuilder.lineSeries;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.ScatterSeriesBuilder.scatterSeries;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.newChart;
import static cz.cuni.mff.respefo.util.widget.TableBuilder.newTable;
import static java.lang.Math.*;
import static java.util.function.UnaryOperator.identity;
import static org.eclipse.swt.SWT.POP_UP;
import static org.eclipse.swt.SWT.PUSH;

@Fun(name = "Rectify", fileFilter = SpefoFormatFileFilter.class, group = "Preprocessing")
@Serialize(key = RectifyFunction.RECTIFY_SERIALIZE_KEY, assetClass = RectifyAsset.class)
@Serialize(key = RectifyFunction.BLAZE_SERIALIZE_KEY, assetClass = BlazeAsset.class)
public class RectifyFunction implements SingleFileFunction {

    public static final String RECTIFY_SERIALIZE_KEY = "rectify";
    public static final String BLAZE_SERIALIZE_KEY = "blaze";

    public static final String POINTS_SERIES_NAME = "points";
    public static final String SELECTED_SERIES_NAME = "selected";
    public static final String CONTINUUM_SERIES_NAME = "continuum";

    private static final List<Integer> DEFAULT_EXCLUDED_ORDERS = listOf(5, 10, 29, 35, 39, 43, 47, 51, 58, 61);
    private static final int DEFAULT_POLY_DEGREE = 7;

    private static RectifyAsset previousAsset;

    @Override
    public void execute(File file) {
        Spectrum spectrum;
        try {
            spectrum = Spectrum.open(file);
        } catch (SpefoException e) {
            Message.error("Couldn't open file", e);
            return;
        }

        switch (spectrum.getFormat()) {
            case SimpleSpectrum.FORMAT:
                rectifySimpleSpectrum((SimpleSpectrum) spectrum);
                break;
            case EchelleSpectrum.FORMAT:
                rectifyEchelleSpectrum((EchelleSpectrum) spectrum);
                break;
            default:
                Message.warning("Spectrum has an unexpected file format");
        }
    }

    private static void rectifySimpleSpectrum(SimpleSpectrum spectrum) {
        RectifyAsset asset = spectrum.getFunctionAsset(RECTIFY_SERIALIZE_KEY, RectifyAsset.class)
                .orElse(previousAsset != null
                        ? previousAsset.adjustToNewData(spectrum.getProcessedSeries())
                        : RectifyAsset.withDefaultPoints(spectrum.getProcessedSeries()));

        XYSeries series = spectrum.getProcessedSeriesWithout(asset);

        rectify(spectrum.getFile().getName(), series, asset, identity(), a -> finishSimpleSpectrum(spectrum, a));
    }

    private static void rectifyEchelleSpectrum(EchelleSpectrum spectrum) {
        EchelleRectificationContext context = new EchelleRectificationContext(spectrum);

        Async.sequence(context,
                RectifyFunction::fitScalePoly,
                RectifyFunction::selectOrders,
                RectifyFunction::rectifyAllEchelleOrders,
                RectifyFunction::finishEchelleSpectrum);
    }

    // TODO: Simplify / modularize this
    private static void fitScalePoly(EchelleRectificationContext context, Runnable callback) {
        Set<Integer> excludedOrders = new HashSet<>(DEFAULT_EXCLUDED_ORDERS);
        XYSeries mergedSeries = XYSeries.merge(context.series);

        final DoubleArrayList pointXCoordinates = new DoubleArrayList(context.series.length);
        final DoubleArrayList pointYCoordinates = new DoubleArrayList(context.series.length);
        for (int index = 0; index < 62; index++) {
            if (excludedOrders.contains(index)) {
                continue;
            }

            XYSeries currentSeries = context.series[index];
            double centralWavelength = K / (125 - index);
            double scale = MathUtils.intep(currentSeries.getXSeries(), currentSeries.getYSeries(), centralWavelength);
            pointXCoordinates.add(centralWavelength);
            pointYCoordinates.add(scale);
        }
        double[] xSeries = pointXCoordinates.toArray();
        double[] ySeries = pointYCoordinates.toArray();

        context.coeffs = MathUtils.fitPolynomial(xSeries, ySeries, DEFAULT_POLY_DEGREE);

        double[] fitXSeries = ArrayUtils.linspace(mergedSeries.getX(0), mergedSeries.getLastX(), 100);
        double[] fitYSeries = Arrays.stream(fitXSeries).map(x -> MathUtils.polynomial(x, context.coeffs)).toArray();

        final Chart chart = newChart()
                .title(context.spectrum.getFile().getName())
                .xAxisLabel(WAVELENGTH)
                .yAxisLabel(RELATIVE_FLUX)
                .series(lineSeries()
                        .name("series")
                        .series(mergedSeries))
                .series(scatterSeries()
                        .name("points")
                        .color(ColorResource.ORANGE)
                        .plotSymbolType(ILineSeries.PlotSymbolType.CROSS)
                        .symbolSize(5)
                        .xSeries(xSeries)
                        .ySeries(ySeries))
                .series(lineSeries()
                        .name("fit")
                        .color(ColorResource.YELLOW)
                        .xSeries(fitXSeries)
                        .ySeries(fitYSeries))
                .keyListener(ChartKeyListener::makeAllSeriesEqualRange)
                .mouseAndMouseMoveListener(DragMouseListener::new)
                .mouseWheelListener(ZoomMouseWheelListener::new)
                .makeAllSeriesEqualRange()
                .data("poly", DEFAULT_POLY_DEGREE)
                .forceFocus()
                .build(ComponentManager.clearAndGetScene());

        Consumer<double[]> updateCoeffs = newCoeffs -> {
            context.coeffs = newCoeffs;
            chart.getSeriesSet().getSeries("fit")
                    .setYSeries(
                            Arrays.stream(fitXSeries)
                                    .map(x -> MathUtils.polynomial(x, newCoeffs))
                                    .toArray()
                    );

            chart.redraw();
        };

        chart.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == SWT.CR || e.keyCode == SWT.END) {
                    callback.run();
                }
            }
        });

        final ToolBar.Tab tab = ComponentManager.getRightToolBar().addTab(parent -> new VerticalToggle(parent, SWT.DOWN),
                "Orders", "Echelle Orders", ImageResource.RULER_LARGE);

        tab.addTopBarButton("Confirm", ImageResource.CHECK, callback);

        final Menu menu = new Menu(ComponentManager.getShell(), POP_UP);
        for (int order = 3; order < 14; order++) {
            final int polyDegree = order;
            final MenuItem item = new MenuItem(menu, PUSH);
            item.setText(String.valueOf(order));
            item.addSelectionListener(new DefaultSelectionListener(event -> {
                chart.setData("poly", polyDegree);
                double[] newCoeffs = MathUtils.fitPolynomial(xSeries, ySeries, polyDegree);
                updateCoeffs.accept(newCoeffs);
            }));
        }

        tab.addTopBarMenuButton("Poly Degree", ImageResource.POLY, menu);

        final Table table = newTable(SWT.CHECK | SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL)
                .gridLayoutData(GridData.FILL_BOTH)
                .headerVisible(true)
                .linesVisible(true)
                .columns("No.", "From", "To")
                .fixedAspectColumns(1, 2, 2)
                .items(Arrays.asList(context.columnNames))
                .onSelection(event -> {
                    if (event.detail == SWT.CHECK) {
                        TableItem tableItem = (TableItem) event.item;
                        int index = ((Table) event.widget).indexOf(tableItem);
                        double centralWavelength = K / (125 - index);

                        if (tableItem.getChecked()) {
                            excludedOrders.add(index);

                            int insertionIndex = IntStream.range(0, pointXCoordinates.size())
                                    .filter(i -> pointXCoordinates.get(i) > centralWavelength)
                                    .findFirst()
                                    .orElse(pointXCoordinates.size());

                            double scale = MathUtils.intep(context.series[index].getXSeries(), context.series[index].getYSeries(), centralWavelength);
                            pointXCoordinates.add(centralWavelength, insertionIndex);
                            pointYCoordinates.add(scale, insertionIndex);

                        } else {
                            excludedOrders.remove(index);

                            int removalIndex = pointXCoordinates.indexOf(centralWavelength);
                            pointXCoordinates.remove(removalIndex);
                            pointYCoordinates.remove(removalIndex);
                        }
                        double[] newXSeries = pointXCoordinates.toArray();
                        double[] newYSeries = pointYCoordinates.toArray();

                        ISeries pointSeries = chart.getSeriesSet().getSeries("points");
                        pointSeries.setXSeries(newXSeries);
                        pointSeries.setYSeries(newYSeries);

                        double[] newCoeffs = MathUtils.fitPolynomial(newXSeries, newYSeries, (int) chart.getData("poly"));
                        updateCoeffs.accept(newCoeffs);
                    }
                })
                .build(tab.getWindow());

        for (int i = 0; i < 62; i++) {
            if (!excludedOrders.contains(i)) {
                table.getItem(i).setChecked(true);
            }
        }

        tab.show();
    }

    private static void selectOrders(EchelleRectificationContext context, Runnable callback) {
        ComponentManager.clearScene(true);
        EchelleSelectionDialog dialog = new EchelleSelectionDialog(context.columnNames);
        if (dialog.openIsOk()) {
            context.selectedIndices = dialog.getSelectedIndices();
            callback.run();
        }
    }

    private static void rectifyAllEchelleOrders(EchelleRectificationContext context, Runnable callback) {
        Async.loop(context.series.length, context,
                RectifyFunction::rectifySingleEchelleOrder,
                c -> callback.run());
    }

    private static void rectifySingleEchelleOrder(int index, EchelleRectificationContext context, Runnable callback) {
        if (context.selectedIndices.contains(index)) {
            // Interactive
            fineTuneBlazeParameters(index, context, callback);
        } else {
            // Automatic
            Blaze blaze = new Blaze(index, context.coeffs);
            context.rectifyAssets[index] = blaze.toRectifyAsset(context.series[index]);
            context.blazeAsset.removeIfPresent(blaze.getOrder());
            callback.run();
        }
    }

    private static void fineTuneBlazeParameters(int index, EchelleRectificationContext context, Runnable callback) {
        XYSeries currentSeries = context.series[index];
        Blaze blaze = new Blaze(index, context.coeffs);

        double[] xSeries = currentSeries.getXSeries();
        double[] ySeries = blaze.ySeries(xSeries);

        XYSeries blazeSeries = new XYSeries(xSeries, ySeries);
        XYSeries residuals = new XYSeries(currentSeries.getXSeries(),
                ArrayUtils.createArray(currentSeries.getLength(), i -> abs(currentSeries.getY(i) - ySeries[i])));

        Text[] texts = new Text[2];  // TODO: redesign this
        Runnable updateTexts = () -> {
            texts[0].setText(String.valueOf(blaze.getCentralWavelength()));
            texts[1].setText(String.valueOf(blaze.getScale()));
        };

        final Chart chart = newChart()
                .title("#" + (index + 1))
                .xAxisLabel("X axis")
                .yAxisLabel("Y axis")
                .series(lineSeries()
                        .name("series")
                        .series(currentSeries))
                .series(lineSeries()
                        .name("blaze")
                        .color(GRAY)
                        .series(blazeSeries))
                .series(lineSeries()
                        .name("residuals")
                        .color(ORANGE)
                        .series(residuals))
                .keyListener(ch -> new BlazeKeyListener(ch, blaze,
                        () ->  {
                            updateTexts.run();
                            updateChart(ch, blaze);
                        },
                        () -> {
                            context.blazeAsset.setParameters(blaze.getOrder(), blaze.getCentralWavelength(), blaze.getScale());
                            Display.getCurrent().asyncExec(() -> fineTuneRectificationPoints(index, context, callback, blaze));
                        }))
                .mouseAndMouseMoveListener(ch -> new BlazeMouseListener(ch, () ->  {
                    updateTexts.run();
                    updateChart(ch, blaze);
                }, blaze))
                .mouseWheelListener(ZoomMouseWheelListener::new)
                .plotAreaPaintListener(ch -> event -> {
                    boolean horizontal = (boolean) ch.getData("horizontal");

                    Point coordinates = ChartUtils.getCoordinatesFromRealValues(ch, blaze.getCentralWavelength(), blaze.getScale());

                    event.gc.setForeground(getColor(horizontal ? BLUE : CYAN));
                    event.gc.drawLine(0, (int) coordinates.y, event.width, (int) coordinates.y);

                    event.gc.setForeground(getColor(horizontal ? CYAN : BLUE));
                    event.gc.drawLine((int) coordinates.x, 0, (int) coordinates.x, event.height);
                })
                .makeAllSeriesEqualRange()
                .data("horizontal", false)
                .forceFocus()
                .build(ComponentManager.clearAndGetScene());

        final ToolBar.Tab tab = ComponentManager.getRightToolBar().addTab(parent -> new VerticalToggle(parent, SWT.DOWN),
                "Parameters", "Parameters", ImageResource.RULER_LARGE);

        tab.addTopBarButton("Confirm", ImageResource.CHECK, () -> {
            context.blazeAsset.setParameters(blaze.getOrder(), blaze.getCentralWavelength(), blaze.getScale());
            Display.getCurrent().asyncExec(() -> fineTuneRectificationPoints(index, context, callback, blaze));
        });

        CompositeBuilder compositeBuilder = CompositeBuilder.newComposite()
                .gridLayoutData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING)
                .layout(new GridLayout());
        LabelBuilder labelBuilder = LabelBuilder.newLabel(SWT.LEFT).bold();
        TextBuilder textBuilder = TextBuilder.newText(SWT.SINGLE).gridLayoutData(GridData.FILL_HORIZONTAL);
        ButtonBuilder buttonBuilder = ButtonBuilder.newButton(SWT.PUSH).text("Confirm");
        LabelBuilder separatorBuilder = LabelBuilder.newLabel(SWT.SEPARATOR | SWT.HORIZONTAL).gridLayoutData(GridData.FILL_HORIZONTAL);

        // central wavelength
        Composite composite = compositeBuilder.build(tab.getWindow());
        labelBuilder.text("Central wavelength").build(composite);
        texts[0] = textBuilder.text(String.valueOf(blaze.getCentralWavelength())).build(composite);
        buildButton(buttonBuilder, composite, texts[0], chart, blaze, blaze::setCentralWavelength);

        separatorBuilder.build(tab.getWindow());

        // scale
        composite = compositeBuilder.build(tab.getWindow());
        labelBuilder.text("Scale").build(composite);
        texts[1] = textBuilder.text(String.valueOf(blaze.getScale())).build(composite);
        buildButton(buttonBuilder, composite, texts[1], chart, blaze, blaze::setScale);

        tab.show();
    }

    private static void buildButton(ButtonBuilder buttonBuilder, Composite composite, Text text, Chart chart, Blaze blaze, DoubleConsumer consumer) {
        buttonBuilder
                .onSelection(e -> {
                    try {
                        double value = Double.parseDouble(text.getText());
                        consumer.accept(value);
                        updateChart(chart, blaze);
                        chart.forceFocus();

                    } catch (NumberFormatException exception) {
                        // ignore
                    }
                })
                .build(composite);
    }

    private static void fineTuneRectificationPoints(int index, EchelleRectificationContext context, Runnable callback, Blaze blaze) {
        XYSeries currentSeries = context.series[index];
        rectify("#" + (index + 1),
                currentSeries,
                blaze.toRectifyAsset(currentSeries),
                builder -> {
                    for (int i = max(index - 2, 0); i <= min(index + 2, context.rectifyAssets.length - 1); i++) {
                        if (i == index) {
                            continue;
                        }

                        builder.series(lineSeries()
                                .series(context.series[i])
                                .color(GRAY));
                    }
                    return builder;
                },
                asset -> {
                    context.rectifyAssets[index] = asset;
                    callback.run();
                });
    }

    private static void updateChart(Chart chart, Blaze blaze) {
        ISeries iSeries = chart.getSeriesSet().getSeries("series");
        XYSeries series = new XYSeries(iSeries.getXSeries(), iSeries.getYSeries());

        double[] blazeYSeries = blaze.ySeries(iSeries.getXSeries());
        double[] residuals = ArrayUtils.createArray(series.getLength(), i -> abs(series.getY(i) - blazeYSeries[i]));

        chart.getSeriesSet().getSeries("blaze").setYSeries(blazeYSeries);
        chart.getSeriesSet().getSeries("residuals").setYSeries(residuals);
        chart.redraw();
    }

    private static void rectify(String title, XYSeries series, RectifyAsset asset,
                                UnaryOperator<ChartBuilder> operator, Consumer<RectifyAsset> finish) {
        newChart()
                .title(title)
                .xAxisLabel(WAVELENGTH)
                .yAxisLabel(RELATIVE_FLUX)
                .series(lineSeries()
                        .name("original")
                        .color(ColorResource.GREEN)
                        .series(series))
                .series(lineSeries()
                        .name(CONTINUUM_SERIES_NAME)
                        .color(ColorResource.YELLOW)
                        .xSeries(series.getXSeries())
                        .ySeries(asset.getIntepData(series.getXSeries())))
                .series(scatterSeries()
                        .name(POINTS_SERIES_NAME)
                        .color(ColorResource.WHITE)
                        .symbolSize(3)
                        .xSeries(asset.getXCoordinatesArray())
                        .ySeries(asset.getYCoordinatesArray()))
                .series(scatterSeries()
                        .name(SELECTED_SERIES_NAME)
                        .color(ColorResource.RED)
                        .symbolSize(3)
                        .series(asset.getActivePoint()))
                .apply(operator)
                .keyListener(ChartKeyListener::makeAllSeriesEqualRange)
                .keyListener(ch -> new RectifyKeyListener(ch, asset,
                        () -> updateAllSeries(ch, asset, series),
                        newIndex -> updateActivePoint(ch, asset, newIndex),
                        () -> finish.accept(asset)))
                .mouseAndMouseMoveListener(ch -> new RectifyMouseListener(ch,
                        POINTS_SERIES_NAME,
                        index -> {
                            if (asset.getActiveIndex() != index) {
                                updateActivePoint(ch, asset, index);
                            }
                        },
                        point -> {
                            asset.moveActivePoint(point.x, point.y);
                            updateAllSeries(ch, asset, series);
                        },
                        point -> {
                            asset.addPoint(point);
                            updateAllSeries(ch, asset, series);
                        },
                        () -> {
                            asset.deleteActivePoint();
                            updateAllSeries(ch, asset, series);
                        }))
                .mouseWheelListener(ZoomMouseWheelListener::new)
                .makeAllSeriesEqualRange()
                .forceFocus()
                .build(ComponentManager.clearAndGetScene());
    }

    private static void updateActivePoint(Chart chart, RectifyAsset asset, int newIndex) {
        asset.setActiveIndex(newIndex);

        ILineSeries series = (ILineSeries) chart.getSeriesSet().getSeries(SELECTED_SERIES_NAME);

        series.setXSeries(asset.getActivePoint().getXSeries());
        series.setYSeries(asset.getActivePoint().getYSeries());

        chart.redraw();
    }

    private static void updateAllSeries(Chart chart, RectifyAsset asset, XYSeries xySeries) {
        ILineSeries lineSeries = (ILineSeries) chart.getSeriesSet().getSeries(POINTS_SERIES_NAME);
        lineSeries.setXSeries(asset.getXCoordinatesArray());
        lineSeries.setYSeries(asset.getYCoordinatesArray());

        lineSeries = (ILineSeries) chart.getSeriesSet().getSeries(SELECTED_SERIES_NAME);
        lineSeries.setXSeries(asset.getActivePoint().getXSeries());
        lineSeries.setYSeries(asset.getActivePoint().getYSeries());

        lineSeries = (ILineSeries) chart.getSeriesSet().getSeries(CONTINUUM_SERIES_NAME);
        lineSeries.setYSeries(asset.getIntepData(xySeries.getXSeries()));

        chart.redraw();
    }

    private static void finishSimpleSpectrum(SimpleSpectrum spectrum, RectifyAsset asset) {
        if (asset.isEmpty()) { // This can never occur
            spectrum.removeFunctionAsset(RECTIFY_SERIALIZE_KEY);
        } else {
            spectrum.putFunctionAsset(RECTIFY_SERIALIZE_KEY, asset);
            previousAsset = asset;
        }

        try {
            spectrum.save();
            SpectrumExplorer.getDefault().refresh();
            OpenFunction.displaySpectrum(spectrum);
            Message.info("Rectified spectrum saved successfully.");

        } catch (SpefoException exception) {
            Message.error("Spectrum file couldn't be saved.", exception);
        }
    }

    @SuppressWarnings("unused")  // Ignore unused callback
    private static void finishEchelleSpectrum(EchelleRectificationContext context, Runnable callback) {
        EchelleSpectrum spectrum = context.spectrum;
        spectrum.setRectifyAssets(context.rectifyAssets);
        if (context.blazeAsset.isEmpty()) {
            spectrum.removeFunctionAsset(BLAZE_SERIALIZE_KEY);
        } else {
            spectrum.putFunctionAsset(BLAZE_SERIALIZE_KEY, context.blazeAsset);
        }

        try {
            spectrum.save();
            SpectrumExplorer.getDefault().refresh();
            OpenFunction.displaySpectrum(spectrum);
            Message.info("Rectified spectrum saved successfully.");

        } catch (SpefoException exception) {
            Message.error("Spectrum file couldn't be saved.", exception);
        }
    }

    static class EchelleRectificationContext {
        final EchelleSpectrum spectrum;
        final XYSeries[] series;
        final String[][] columnNames;
        final BlazeAsset blazeAsset;
        final RectifyAsset[] rectifyAssets;

        double[] coeffs;
        Set<Integer> selectedIndices;

        EchelleRectificationContext(EchelleSpectrum spectrum) {
            this.spectrum = spectrum;

            series = spectrum.getOriginalSeries();

            columnNames = columnNames(series);

            spectrum.getFunctionAsset(CleanFunction.SERIALIZE_KEY, CleanAsset.class)
                    .ifPresent(asset -> cleanSeries(series, asset));

            blazeAsset = spectrum.getFunctionAsset(BLAZE_SERIALIZE_KEY, BlazeAsset.class)
                    .orElseGet(BlazeAsset::new);

            rectifyAssets = new RectifyAsset[series.length];
        }

        private static void cleanSeries(XYSeries[] series, CleanAsset asset) {
            Map<Integer, List<Integer>> deletedIndices = new HashMap<>();
            int n = series[0].getLength();
            for (int index : asset) {
                int order = Math.floorDiv(index, n);
                int indexInOrder = index % order;
                deletedIndices.putIfAbsent(order, new ArrayList<>());
                deletedIndices.get(order).add(indexInOrder);
            }

            for (Map.Entry<Integer, List<Integer>> entries : deletedIndices.entrySet()) {
                int order = entries.getKey();
                Set<Integer> indices = new HashSet<>(entries.getValue());
                XYSeries currentSeries = series[order];

                double[] remainingXSeries = IntStream.range(0, currentSeries.getLength())
                        .filter(index -> !indices.contains(index))
                        .mapToDouble(currentSeries::getX)
                        .toArray();

                double[] remainingYSeries = IntStream.range(0, currentSeries.getLength())
                        .filter(index -> !indices.contains(index))
                        .mapToDouble(currentSeries::getY)
                        .toArray();

                double[] newYSeries = MathUtils.intep(remainingXSeries, remainingYSeries, currentSeries.getXSeries());

                series[order] = new XYSeries(currentSeries.getXSeries(), newYSeries);
            }
        }

        private static String[][] columnNames(XYSeries[] series) {
            String[][] names = new String[series.length][3];
            for (int i = 0; i <= series.length - 1; i++) {
                XYSeries currentSeries = series[i];
                names[i] = new String[]{
                        Integer.toString(i + 1),
                        Double.toString(currentSeries.getX(0)),
                        Double.toString(currentSeries.getLastX())
                };
            }
            return names;
        }
    }
}
