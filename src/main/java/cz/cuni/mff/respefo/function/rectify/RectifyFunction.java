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
import cz.cuni.mff.respefo.util.collections.Point;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.utils.ArrayUtils;
import cz.cuni.mff.respefo.util.utils.ChartUtils;
import cz.cuni.mff.respefo.util.utils.MathUtils;
import cz.cuni.mff.respefo.util.widget.ChartBuilder;
import cz.cuni.mff.respefo.util.widget.DefaultSelectionListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries;

import java.io.File;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

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

    private static final List<Integer> DEFAULT_EXCLUDED_ORDERS = listOf(5, 10, 29, 35, 39, 43, 47, 51, 58, 61); // TODO: Does this make sense?
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
                RectifyFunction::selectOrdersLoop,
                RectifyFunction::rectifyRemainingEchelleOrders,
                RectifyFunction::finishEchelleSpectrum);
    }

    private static void fitScalePoly(EchelleRectificationContext context, Runnable callback) {
        context.xCoordinates = IntStream.range(0, context.series.length)
                .mapToDouble(index -> Blaze.getCentralWavelength(125 - index))
                .toArray();

        context.yCoordinates = IntStream.range(0, context.series.length)
                .mapToDouble(index -> getScale(context.series[index], context.xCoordinates[index]))
                .toArray();

        context.recalculatePolyCoeffs();

        XYSeries mergedSeries = XYSeries.merge(context.series);
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
                        .name(POINTS_SERIES_NAME)
                        .color(ColorResource.ORANGE)
                        .plotSymbolType(ILineSeries.PlotSymbolType.CROSS)
                        .symbolSize(5)
                        .xSeries(context.pointXSeries())
                        .ySeries(context.pointYSeries()))
                .series(lineSeries()
                        .name("fit")
                        .color(ColorResource.YELLOW)
                        .xSeries(fitXSeries)
                        .ySeries(fitYSeries))
                .keyListener(ChartKeyListener::makeAllSeriesEqualRange)
                .keyListener(KeyListener.keyPressedAdapter(e -> {
                    if (e.keyCode == SWT.CR || e.keyCode == SWT.END) {
                        callback.run();
                    }
                }))
                .mouseAndMouseMoveListener(DragMouseListener::new)
                .mouseWheelListener(ZoomMouseWheelListener::new)
                .makeAllSeriesEqualRange()
                .forceFocus()
                .build(ComponentManager.clearAndGetScene());

        Runnable updateFit = () -> {
            chart.getSeriesSet().getSeries("fit")
                    .setYSeries(
                            Arrays.stream(fitXSeries)
                                    .map(x -> MathUtils.polynomial(x, context.coeffs))
                                    .toArray()
                    );
            chart.redraw();
        };

        final ToolBar.Tab tab = ComponentManager.getRightToolBar().addTab(parent -> new VerticalToggle(parent, SWT.DOWN),
                "Orders", "Echelle Orders", ImageResource.RULER_LARGE);

        tab.addTopBarButton("Confirm", ImageResource.CHECK, callback);

        final Menu menu = new Menu(ComponentManager.getShell(), POP_UP);
        for (int order = 3; order < 14; order++) {
            final int polyDegree = order;
            final MenuItem item = new MenuItem(menu, PUSH);
            item.setText(String.valueOf(order));
            item.addSelectionListener(new DefaultSelectionListener(event -> {
                context.polyDegree = polyDegree;
                context.recalculatePolyCoeffs();
                updateFit.run();
            }));
        }

        tab.addTopBarMenuButton("Poly Degree", ImageResource.POLY, menu);

        newTable(SWT.CHECK | SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL)
                .gridLayoutData(GridData.FILL_BOTH)
                .headerVisible(true)
                .linesVisible(true)
                .columns("No.", "From", "To")
                .fixedAspectColumns(1, 2, 2)
                .items(Arrays.asList(context.columnNames))
                .decorate((i, item) -> item.setChecked(!context.excludedOrders.contains(i)))
                .onSelection(event -> {
                    if (event.detail == SWT.CHECK) {
                        TableItem tableItem = (TableItem) event.item;
                        int index = ((Table) event.widget).indexOf(tableItem);

                        if (tableItem.getChecked()) {
                            context.excludedOrders.remove(index);
                        } else {
                            context.excludedOrders.add(index);
                        }

                        ISeries pointSeries = chart.getSeriesSet().getSeries(POINTS_SERIES_NAME);
                        pointSeries.setXSeries(context.pointXSeries());
                        pointSeries.setYSeries(context.pointYSeries());

                        context.recalculatePolyCoeffs();
                        updateFit.run();
                    }
                })
                .build(tab.getWindow());

        tab.show();
    }

    private static double getScale(XYSeries series, double centralWavelength) {
        int index = ArrayUtils.indexOfFirstGreaterThan(series.getXSeries(), centralWavelength);
        return MathUtils.robustMean(Arrays.copyOfRange(series.getYSeries(), max(0, index - 5), min(series.getLength(), index + 5)));
    }

    private static void selectOrdersLoop(EchelleRectificationContext context, Runnable callback) {
        Async.whileLoop(context, RectifyFunction::selectOrdersAndInteractivelyRectifyThem, c -> callback.run());
    }

    private static void selectOrdersAndInteractivelyRectifyThem(EchelleRectificationContext context, Consumer<Boolean> callback) {
        ComponentManager.clearScene(true);
        EchelleSelectionDialog dialog = new EchelleSelectionDialog(context.columnNames, context.rectifiedIndices);
        if (dialog.openIsOk()) {
            context.selectedIndices = dialog.getSelectedIndices();
            if (context.selectedIndices.hasNext()) {
                Async.whileLoop(context, RectifyFunction::rectifySingleEchelleOrder,
                        c -> {
                            c.recalculatePolyCoeffs();
                            callback.accept(true);
                        });
            } else {
                callback.accept(false);
            }
        }
    }

    private static void rectifySingleEchelleOrder(EchelleRectificationContext context, Consumer<Boolean> callback) {
        if (context.selectedIndices.hasNext()) {
            int index = context.selectedIndices.next();
            context.rectifiedIndices.add(index);
            fineTuneBlazeParameters(index, context, () -> callback.accept(true));
        } else {
            callback.accept(false);
        }
    }

    private static void rectifyRemainingEchelleOrders(EchelleRectificationContext context, Runnable callback) {
        for (int index = 0; index < context.series.length; index++) {
            if (!context.rectifiedIndices.contains(index)) {
                Blaze blaze = new Blaze(index, context.coeffs);
                if (context.blazeAsset.hasParameters(blaze.getOrder())) {
                    double[] parameters = context.blazeAsset.getParameters(blaze.getOrder());
                    blaze.setCentralWavelength(parameters[0]);
                    blaze.setScale(parameters[1]);
                }
                context.rectifyAssets[index] = blaze.toRectifyAsset(context.series[index]);
            }
        }

        callback.run();
    }

    private static void fineTuneBlazeParameters(int index, EchelleRectificationContext context, Runnable callback) {
        XYSeries currentSeries = context.series[index];
        Blaze blaze = new Blaze(index, context.coeffs);

        final double originalScale = blaze.getScale();

        double[] blazeXSeries = currentSeries.getXSeries();
        double[] blazeYSeries = blaze.ySeries(blazeXSeries);

        XYSeries residuals = new XYSeries(currentSeries.getXSeries(),
                ArrayUtils.createArray(currentSeries.getLength(), i -> abs(currentSeries.getY(i) - blazeYSeries[i])));

        newChart()
                .title("#" + (index + 1))
                .xAxisLabel("X axis")
                .yAxisLabel("Y axis")
                .series(lineSeries()
                        .name("series")
                        .series(currentSeries))
                .series(lineSeries()
                        .name("blaze")
                        .color(GRAY)
                        .xSeries(blazeXSeries)
                        .ySeries(blazeYSeries))
                .series(lineSeries()
                        .name("residuals")
                        .color(ORANGE)
                        .series(residuals))
                .keyListener(ch -> new BlazeKeyListener(ch, blaze,
                        () ->  updateChart(ch, blaze),
                        () -> {
                            context.xCoordinates[index] = blaze.getCentralWavelength();
                            context.yCoordinates[index] = blaze.getScale();
                            context.blazeAsset.setParameters(blaze.getOrder(), blaze.getCentralWavelength(), blaze.getScale());
                            Display.getCurrent().asyncExec(() -> fineTuneRectificationPoints(index, context, callback, blaze));
                        }))
                .mouseAndMouseMoveListener(ch -> new BlazeMouseListener(ch, () -> updateChart(ch, blaze), blaze))
                .mouseWheelListener(ZoomMouseWheelListener::new)
                .plotAreaPaintListener(ch -> event -> {
                    int y = ch.getAxisSet().getYAxis(0).getPixelCoordinate(originalScale);
                    event.gc.setForeground(getColor(DARK_GRAY));
                    event.gc.setLineStyle(SWT.LINE_DOT);
                    event.gc.drawLine(0, y, event.width, y);
                    event.gc.setLineStyle(SWT.LINE_SOLID);

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
    }

    private static void fineTuneRectificationPoints(int index, EchelleRectificationContext context, Runnable callback, Blaze blaze) {
        XYSeries currentSeries = context.series[index];
        rectify("#" + (index + 1),
                currentSeries,
                blaze.toRectifyAsset(currentSeries),
                builder -> {
                    for (int i = max(index - 2, 0); i <= min(index + 2, context.rectifyAssets.length - 1); i++) {
                        if (i != index) {
                            builder.series(lineSeries().series(context.series[i]).color(GRAY));
                        }
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
        final Set<Integer> rectifiedIndices;

        double[] xCoordinates;
        double[] yCoordinates;
        final Set<Integer> excludedOrders = new HashSet<>(DEFAULT_EXCLUDED_ORDERS);
        int polyDegree = DEFAULT_POLY_DEGREE;
        double[] coeffs;

        Iterator<Integer> selectedIndices;

        EchelleRectificationContext(EchelleSpectrum spectrum) {
            this.spectrum = spectrum;

            series = spectrum.getOriginalSeries();

            columnNames = columnNames(series);

            spectrum.getFunctionAsset(CleanFunction.SERIALIZE_KEY, CleanAsset.class)
                    .ifPresent(asset -> cleanSeries(series, asset));

            blazeAsset = spectrum.getFunctionAsset(BLAZE_SERIALIZE_KEY, BlazeAsset.class)
                    .orElseGet(BlazeAsset::new);

            rectifyAssets = new RectifyAsset[series.length];

            rectifiedIndices = new HashSet<>(series.length);
        }

        public void recalculatePolyCoeffs() {
            double[] xSeries = pointXSeries();
            double[] ySeries = pointYSeries();

            coeffs = MathUtils.fitPolynomial(xSeries, ySeries, polyDegree);
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

        private double[] pointXSeries() {
            return IntStream.range(0, xCoordinates.length)
                    .filter(index -> !excludedOrders.contains(index))
                    .mapToDouble(index -> xCoordinates[index])
                    .toArray();
        }

        private double[] pointYSeries() {
            return IntStream.range(0, yCoordinates.length)
                    .filter(index -> !excludedOrders.contains(index))
                    .mapToDouble(index -> yCoordinates[index])
                    .toArray();
        }
    }
}
