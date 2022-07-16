package cz.cuni.mff.respefo.function.rectify;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.component.SpectrumExplorer;
import cz.cuni.mff.respefo.component.ToolBar;
import cz.cuni.mff.respefo.component.VerticalToggle;
import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.Serialize;
import cz.cuni.mff.respefo.function.SpectrumFunction;
import cz.cuni.mff.respefo.function.clean.CleanAsset;
import cz.cuni.mff.respefo.function.clean.CleanFunction;
import cz.cuni.mff.respefo.function.common.ChartKeyListener;
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
import cz.cuni.mff.respefo.util.utils.FileUtils;
import cz.cuni.mff.respefo.util.utils.MathUtils;
import cz.cuni.mff.respefo.util.widget.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries;
import org.swtchart.Range;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.DoubleUnaryOperator;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

import static cz.cuni.mff.respefo.resources.ColorManager.getColor;
import static cz.cuni.mff.respefo.resources.ColorResource.*;
import static cz.cuni.mff.respefo.util.utils.CollectionUtils.setOf;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.AxisLabel.FLUX;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.AxisLabel.WAVELENGTH;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.LineSeriesBuilder.lineSeries;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.ScatterSeriesBuilder.scatterSeries;
import static cz.cuni.mff.respefo.util.widget.ChartBuilder.newChart;
import static cz.cuni.mff.respefo.util.widget.TableBuilder.newTable;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Arrays.copyOfRange;
import static java.util.function.UnaryOperator.identity;
import static org.eclipse.swt.SWT.*;

@Fun(name = "Rectify", fileFilter = SpefoFormatFileFilter.class, group = "Preprocessing")
@Serialize(key = RectifyFunction.RECTIFY_SERIALIZE_KEY, assetClass = RectifyAsset.class)
@Serialize(key = RectifyFunction.BLAZE_SERIALIZE_KEY, assetClass = BlazeAsset.class)
public class RectifyFunction extends SpectrumFunction {

    public static final String RECTIFY_SERIALIZE_KEY = "rectify";
    public static final String BLAZE_SERIALIZE_KEY = "blaze";

    public static final String POINTS_SERIES_NAME = "points";
    public static final String SELECTED_SERIES_NAME = "selected";
    public static final String CONTINUUM_SERIES_NAME = "continuum";

    private static RectifyAsset previousAsset;

    private static Set<Integer> previousExcludedOrders = setOf(5, 10, 29, 35, 39, 43, 47, 51, 58);  // TODO: Make this automatic?
    private static int previousPolyDegree = 9;

    @Override
    public void execute(Spectrum spectrum) {
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
        for (int i = 0; i < context.series.length; i++) {
            int order = Blaze.indexToOrder(i);
            if (context.blazeAsset.hasParameters(order)) {
                context.xCoordinates[i] = context.blazeAsset.getCentralWavelength(order);
                context.yCoordinates[i] = context.blazeAsset.getScale(order);

            } else {
                double centralWavelength = Blaze.orderToCentralWavelength(order);
                XYSeries series = context.series[i];
                int index = ArrayUtils.indexOfFirstGreaterThan(series.getXSeries(), centralWavelength);
                double scale = MathUtils.robustMean(copyOfRange(series.getYSeries(), max(0, index - 5), min(series.getLength(), index + 5)));

                context.xCoordinates[i] = centralWavelength;
                context.yCoordinates[i] = scale;
            }
        }

        XYSeries mergedSeries = XYSeries.merge(context.series);
        double[] fitXSeries = ArrayUtils.linspace(mergedSeries.getX(0), mergedSeries.getLastX(), 5_000);
        double[] fitYSeries;
        if (context.polyDegree > 0) {
            context.recalculatePolyCoeffs();
            fitYSeries = Arrays.stream(fitXSeries).map(x -> MathUtils.polynomial(x, context.polyCoeffs)).toArray();
        } else {
            fitYSeries = MathUtils.intep(context.pointXSeries(), context.pointYSeries(), fitXSeries);
        }

        Consumer<Chart> updateCoeffsAndFit = ch -> {
            ISeries fitSeries = ch.getSeriesSet().getSeries("fit");
            if (context.polyDegree > 0) {
                context.recalculatePolyCoeffs();
                fitSeries.setYSeries(
                        Arrays.stream(fitXSeries)
                                .map(x -> MathUtils.polynomial(x, context.polyCoeffs))
                                .toArray()
                );
            } else {
                fitSeries.setYSeries(
                        MathUtils.intep(context.pointXSeries(), context.pointYSeries(), fitXSeries)
                );
            }
            ch.redraw();
            ch.forceFocus();
        };

        final Chart chart = newChart()
                .title(context.spectrum.getFile().getName())
                .xAxisLabel(WAVELENGTH)
                .yAxisLabel(FLUX)
                .series(lineSeries()
                        .name("series")
                        .series(mergedSeries))
                .series(scatterSeries()
                        .name(POINTS_SERIES_NAME)
                        .color(WHITE)
                        .plotSymbolType(ILineSeries.PlotSymbolType.CIRCLE)
                        .symbolSize(3)
                        .xSeries(context.pointXSeries())
                        .ySeries(context.pointYSeries()))
                .series(scatterSeries()
                        .name(SELECTED_SERIES_NAME)
                        .color(RED)
                        .plotSymbolType(ILineSeries.PlotSymbolType.CIRCLE)
                        .symbolSize(3)
                        .xSeries(new double[] {context.pointXSeries()[0]})
                        .ySeries(new double[] {context.pointYSeries()[0]}))
                .series(lineSeries()
                        .name("fit")
                        .color(YELLOW)
                        .xSeries(fitXSeries)
                        .ySeries(fitYSeries))
                .keyListener(ChartKeyListener::new)
                .keyListener(KeyListener.keyPressedAdapter(e -> {
                    if (e.keyCode == SWT.CR || e.keyCode == SWT.END) {
                        callback.run();
                    }
                }))
                .mouseAndMouseMoveListener(ch -> new RectifyMouseListener(ch,
                        POINTS_SERIES_NAME,
                        index -> {
                            ILineSeries selected = (ILineSeries) ch.getSeriesSet().getSeries(SELECTED_SERIES_NAME);
                            ILineSeries points = (ILineSeries) ch.getSeriesSet().getSeries(POINTS_SERIES_NAME);

                            selected.setXSeries(new double[] {points.getXSeries()[index]});
                            selected.setYSeries(new double[] {points.getYSeries()[index]});

                            ch.redraw();
                        },
                        point -> {
                            ISeries selectedSeries = ch.getSeriesSet().getSeries(SELECTED_SERIES_NAME);
                            ISeries pointSeries = ch.getSeriesSet().getSeries(POINTS_SERIES_NAME);
                            int index = Arrays.binarySearch(context.xCoordinates, selectedSeries.getXSeries()[0]);  // TODO: Cache this
                            if (index >= 0) {
                                double clampMin = max(context.series[index].getX(0),
                                        index > 0
                                                ? context.xCoordinates[index - 1] + MathUtils.DOUBLE_PRECISION
                                                : Double.NEGATIVE_INFINITY);
                                double clampMax = min(context.series[index].getLastX(),
                                        index + 1 < context.series.length
                                                ? context.xCoordinates[index + 1] - MathUtils.DOUBLE_PRECISION
                                                : Double.POSITIVE_INFINITY);

                                context.xCoordinates[index] = MathUtils.clamp(context.xCoordinates[index] + point.x, clampMin, clampMax);
                                context.yCoordinates[index] += point.y;

                                selectedSeries.setXSeries(new double[] {context.xCoordinates[index]});
                                selectedSeries.setYSeries(new double[] {context.yCoordinates[index]});

                                pointSeries.setXSeries(context.pointXSeries());
                                pointSeries.setYSeries(context.pointYSeries());

                                updateCoeffsAndFit.accept(ch);
                            }
                        },
                        point -> {},
                        () -> {}))
                .mouseWheelListener(ZoomMouseWheelListener::new)
                .plotAreaPaintListener(ch -> event -> {
                    ILineSeries selected = (ILineSeries) ch.getSeriesSet().getSeries(SELECTED_SERIES_NAME);
                    double x = selected.getXSeries()[0];
                    double y = selected.getYSeries()[0];

                    int index = Arrays.binarySearch(context.xCoordinates, x);  // TODO: Cache this
                    if (index >= 0) {
                        Point position = ChartUtils.getCoordinatesFromRealValues(ch, x, y);

                        event.gc.setForeground(getColor(RED));
                        event.gc.drawText(Integer.toString(index + 1),
                                (int) position.x - (index > 8 ? 8 : 4),
                                (int) position.y - 20, true);
                    }
                })
                .adjustRange()
                .forceFocus()
                .build(ComponentManager.clearAndGetScene());

        final ToolBar.Tab tab = ComponentManager.getRightToolBar().addTab(parent -> new VerticalToggle(parent, SWT.DOWN),
                "Orders", "Echelle Orders", ImageResource.NUMBERED_LIST_LARGE);

        tab.addTopBarButton("Confirm", ImageResource.CHECK, callback);

        final Menu menu = new Menu(ComponentManager.getShell(), POP_UP);
        for (int order = 5; order < 16; order++) {
            final int polyDegree = order;
            final MenuItem item = new MenuItem(menu, RADIO);
            item.setText(String.valueOf(order));
            if (order == context.polyDegree) {
                item.setSelection(true);
            }
            item.addSelectionListener(new DefaultSelectionListener(event -> {
                context.polyDegree = polyDegree;
                updateCoeffsAndFit.accept(chart);
            }));
        }
        final MenuItem menuItem = new MenuItem(menu, RADIO);
        menuItem.setText("Spline");
        if (context.polyDegree <= 0) {
            menuItem.setSelection(true);
        }
        menuItem.addSelectionListener(new DefaultSelectionListener(event -> {
            context.polyDegree = -1;
            updateCoeffsAndFit.accept(chart);
        }));

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

                        updateCoeffsAndFit.accept(chart);
                    }
                })
                .build(tab.getWindow());

        tab.show();
    }

    private static void selectOrdersLoop(EchelleRectificationContext context, Runnable callback) {
        Async.whileLoop(context, RectifyFunction::selectOrdersAndInteractivelyRectifyThem, c -> callback.run());
    }

    private static void selectOrdersAndInteractivelyRectifyThem(EchelleRectificationContext context, Consumer<Boolean> callback) {
        EchelleSelectionDialog dialog = new EchelleSelectionDialog(context.columnNames, context.rectifiedIndices);
        if (dialog.openIsOk()) {
            ComponentManager.clearScene(true);
            Iterator<Integer> selectedIndices = dialog.getSelectedIndices();
            if (selectedIndices.hasNext()) {
                Async.whileLoop(context,
                        (ctx, call) -> rectifySingleEchelleOrder(selectedIndices, ctx, call),
                        ctx -> {
                            if (ctx.polyDegree > 0) {
                                ctx.recalculatePolyCoeffs();
                            }
                            callback.accept(true);
                        });
            } else {
                callback.accept(false);
            }
        }
    }

    private static void rectifySingleEchelleOrder(Iterator<Integer> selectedIndices, EchelleRectificationContext context, Consumer<Boolean> callback) {
        if (selectedIndices.hasNext()) {
            int index = selectedIndices.next();
            context.rectifiedIndices.add(index);
            fineTuneBlazeParameters(index, context, () -> callback.accept(true));
        } else {
            callback.accept(false);
        }
    }

    private static void rectifyRemainingEchelleOrders(EchelleRectificationContext context, Runnable callback) {
        for (int index = 0; index < context.series.length; index++) {
            if (!context.rectifiedIndices.contains(index)) {
                Blaze blaze = new Blaze(index, context.scaleFunction());
                if (context.blazeAsset.hasParameters(blaze.getOrder())) {
                    context.rectifyAssets[index] = context.spectrum.getRectifyAssets()[index];
                } else {
                    context.rectifyAssets[index] = blaze.toRectifyAsset(context.series[index]);
                }
            }
        }

        callback.run();
    }

    private static void fineTuneBlazeParameters(int index, EchelleRectificationContext context, Runnable callback) {
        XYSeries currentSeries = context.series[index];
        Blaze blaze = new Blaze(index, context.scaleFunction());

        final ToolBar.Tab tab = ComponentManager.getRightToolBar().addTab(parent -> new VerticalToggle(parent, SWT.DOWN),
                "Parameters", "Parameter Calibration", ImageResource.RULER_LARGE);

        CompositeBuilder compositeBuilder = CompositeBuilder.newComposite()
                .gridLayoutData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING)
                .layout(new GridLayout());
        LabelBuilder labelBuilder = LabelBuilder.newLabel(SWT.LEFT).bold();
        TextBuilder textBuilder = TextBuilder.newText(SWT.SINGLE)
                .gridLayoutData(GridData.FILL_HORIZONTAL)
                .editable(false);

        Composite composite = compositeBuilder.build(tab.getWindow());
        labelBuilder.text("K:").build(composite);
        final Text kText = textBuilder.text(String.valueOf(blaze.getK())).build(composite);
        labelBuilder.text("Central wavelength:").build(composite);
        final Text wavelengthText = textBuilder.text(String.valueOf(blaze.getCentralWavelength())).build(composite);

        LabelBuilder.newLabel(SWT.SEPARATOR | SWT.HORIZONTAL)
                .gridLayoutData(GridData.FILL_HORIZONTAL)
                .build(tab.getWindow());

        final int digits = 3;
        composite = compositeBuilder.build(tab.getWindow());
        labelBuilder.text("Alpha:").build(composite);
        final Spinner alphaSpinner = SpinnerBuilder.newSpinner(NONE)
                .gridLayoutData(GridData.FILL_HORIZONTAL)
                .digits(digits)
                .bounds(1, 2000)
                .increment(1, 100)
                .selection(blaze.getSpinnerAlpha(digits))
                .build(composite);

        Consumer<Chart> update = ch -> {
            kText.setText(String.valueOf(blaze.getK()));
            wavelengthText.setText(String.valueOf(blaze.getCentralWavelength()));
            alphaSpinner.setSelection(blaze.getSpinnerAlpha(digits));
            updateChart(ch, blaze);
        };

        final double originalScale = blaze.getScale();
        final double originalCentralWavelength = blaze.getCentralWavelength();

        if (context.blazeAsset.hasParameters(blaze.getOrder())) {
            blaze.updateFromAsset(context.blazeAsset);
        }

        double[] blazeXSeries = currentSeries.getXSeries();
        double[] blazeYSeries = blaze.ySeries(blazeXSeries);

        XYSeries rectified = new XYSeries(currentSeries.getXSeries(),
                ArrayUtils.divideArrayValues(currentSeries.getYSeries(), blazeYSeries));

        final Chart chart = newChart()
                .title("#" + (index + 1))
                .xAxisLabel(WAVELENGTH)
                .yAxisLabel(FLUX)
                .series(lineSeries()
                        .name("series")
                        .series(currentSeries))
                .series(lineSeries()
                        .name("blaze")
                        .color(SILVER)
                        .xSeries(blazeXSeries)
                        .ySeries(blazeYSeries))
                .newYAxis()
                .series(lineSeries()
                        .name("rectified")
                        .color(ORANGE)
                        .series(rectified)
                        .yAxis(2))
                .apply(builder -> {
                    for (int i = max(index - 1, 0); i <= min(index + 1, context.rectifyAssets.length - 1); i++) {
                        if (i != index) {
                            XYSeries series = context.series[i];

                            RectifyAsset asset;
                            if (context.rectifyAssets[i] != null) {
                                asset = context.rectifyAssets[i];
                            } else {
                                asset = new Blaze(i, context.scaleFunction()).toRectifyAsset(series);
                            }

                            builder.series(lineSeries()
                                            .color(YELLOW)
                                            .xSeries(series.getXSeries())
                                            .ySeries(ArrayUtils.divideArrayValues(series.getYSeries(),
                                                                                  asset.getIntepData(series.getXSeries())))
                                            .yAxis(2));
                        }
                    }
                    return builder;
                })
                .keyListener(ch -> new BlazeKeyListener(ch, blaze,
                        () -> update.accept(ch),
                        () -> {
                            printParametersToFile(context.spectrum, blaze);
                            if (blaze.isUnchanged(context.blazeAsset)) {
                                Async.exec(() -> fineTuneRectificationPoints(index, context, callback, context.spectrum.getRectifyAssets()[index]));
                            } else {
                                context.xCoordinates[index] = blaze.getCentralWavelength();
                                context.yCoordinates[index] = blaze.getScale();
                                blaze.saveToAsset(context.blazeAsset);
                                Async.exec(() -> fineTuneRectificationPoints(index, context, callback, blaze.toRectifyAsset(currentSeries)));
                            }
                        }))
                .mouseAndMouseMoveListener(ch -> new BlazeMouseListener(ch, () -> update.accept(ch), blaze))
                .mouseWheelListener(ZoomMouseWheelListener::new)
                .verticalLine(originalCentralWavelength, GRAY, LINE_DOT)
                .horizontalLine(originalScale, GRAY, LINE_DOT)
                .plotAreaPaintListener(ch -> event -> {
                    boolean horizontal = (boolean) ch.getData("horizontal");

                    Point coordinates = ChartUtils.getCoordinatesFromRealValues(ch, blaze.getCentralWavelength(), blaze.getScale());

                    event.gc.setLineStyle(LINE_SOLID);

                    event.gc.setForeground(getColor(horizontal ? BLUE : CYAN));
                    event.gc.drawLine(0, (int) coordinates.y, event.width, (int) coordinates.y);

                    event.gc.setForeground(getColor(horizontal ? CYAN : BLUE));
                    event.gc.drawLine((int) coordinates.x, 0, (int) coordinates.x, event.height);

                    int coordinate = ch.getAxisSet().getYAxis(ch.getSeriesSet().getSeries("rectified").getYAxisId()).getPixelCoordinate(1);
                    event.gc.setForeground(getColor(GOLD));
                    event.gc.setLineStyle(LINE_DOT);
                    event.gc.drawLine(0, coordinate, event.width, coordinate);
                })
                .data("horizontal", false)
                .adjustRange()
                .accept(RectifyFunction::adjustRectifiedSeriesAxisRange)
                .accept(ch -> ch.getAxisSet().getXAxis(0).zoomIn())
                .forceFocus()
                .build(ComponentManager.clearAndGetScene(false));

        alphaSpinner.addModifyListener(event -> {
            blaze.setAlpha(alphaSpinner.getSelection() / Math.pow(10, digits));
            updateChart(chart, blaze);
        });
    }

    private static void printParametersToFile(Spectrum spectrum, Blaze blaze) {
        Path path = spectrum.getFile().toPath().resolveSibling(FileUtils.stripFileExtension(spectrum.getFile().getName()) + ".par");
        String params = blaze.getOrder() + " " + blaze.getK() + " " + blaze.getAlpha();
        try {
            Files.write(path, params.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (Exception exception) {
            Message.error("Couldn't save parameters to a file", exception);
        }
    }

    private static void updateChart(Chart chart, Blaze blaze) {
        ISeries iSeries = chart.getSeriesSet().getSeries("series");

        double[] blazeYSeries = blaze.ySeries(iSeries.getXSeries());
        double[] rectifiedYSeries = ArrayUtils.divideArrayValues(iSeries.getYSeries(), blazeYSeries);

        chart.getSeriesSet().getSeries("blaze").setYSeries(blazeYSeries);
        chart.getSeriesSet().getSeries("rectified").setYSeries(rectifiedYSeries);
        adjustRectifiedSeriesAxisRange(chart);
        chart.redraw();
    }

    private static void adjustRectifiedSeriesAxisRange(Chart chart) {
        ISeries series = chart.getSeriesSet().getSeries("rectified");
        double maxAbsValue = Arrays.stream(series.getYSeries())
                .map(y -> Math.abs(y - 1))
                .max().getAsDouble();
        Range range = ChartUtils.rangeWithMargin(1 - maxAbsValue, 1 + 4 * maxAbsValue);
        chart.getAxisSet().getYAxis(series.getYAxisId()).setRange(range);
    }

    private static void fineTuneRectificationPoints(int index, EchelleRectificationContext context, Runnable callback, RectifyAsset asset) {
        XYSeries currentSeries = context.series[index];
        rectify("#" + (index + 1),
                currentSeries,
                asset,
                builder -> {
                    for (int i = max(index - 2, 0); i <= min(index + 2, context.rectifyAssets.length - 1); i++) {
                        if (i != index) {
                            builder.series(lineSeries().series(context.series[i]).color(GRAY));
                        }
                    }

//                    builder.newYAxis()
//                            .series(lineSeries()
//                                    .color(ORANGE)
//                                    .xSeries(currentSeries.getXSeries())
//                                    .ySeries(ArrayUtils.divideArrayValues(currentSeries.getYSeries(),
//                                                                          asset.getIntepData(currentSeries.getXSeries())))
//                                    .yAxis(2));
//
//                    for (int i = max(index - 1, 0); i <= min(index + 1, context.rectifyAssets.length - 1); i++) {
//                        if (i != index) {
//                            XYSeries series = context.series[i];
//
//                            RectifyAsset rectifyAsset;
//                            if (context.rectifyAssets[i] != null) {
//                                rectifyAsset = context.rectifyAssets[i];
//                            } else {
//                                rectifyAsset = new Blaze(i, context.scaleFunction()).toRectifyAsset(context.series[i]);
//                            }
//
//                            builder.series(lineSeries()
//                                    .color(YELLOW)
//                                    .xSeries(series.getXSeries())
//                                    .ySeries(ArrayUtils.divideArrayValues(series.getYSeries(),
//                                                                          rectifyAsset.getIntepData(series.getXSeries())))
//                                    .yAxis(2));
//                        }
//                    }

                    return builder;
                },
                newAsset -> {
                    context.rectifyAssets[index] = newAsset;
                    callback.run();
                });
    }

    private static void rectify(String title, XYSeries series, RectifyAsset asset,
                                UnaryOperator<ChartBuilder> operator, Consumer<RectifyAsset> finish) {
        newChart()
                .title(title)
                .xAxisLabel(WAVELENGTH)
                .yAxisLabel(FLUX)
                .series(lineSeries()
                        .name("original")
                        .color(ColorResource.GREEN)
                        .series(series))
                .series(lineSeries()
                        .name(CONTINUUM_SERIES_NAME)
                        .color(YELLOW)
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
                .keyListener(ChartKeyListener::new)
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
                .adjustRange()
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
        if (context.blazeAsset.isEmpty()) { // This can never occur
            spectrum.removeFunctionAsset(BLAZE_SERIALIZE_KEY);
        } else {
            spectrum.putFunctionAsset(BLAZE_SERIALIZE_KEY, context.blazeAsset);
        }
        previousPolyDegree = context.polyDegree;
        previousExcludedOrders = context.excludedOrders;

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

        final double[] xCoordinates;
        final double[] yCoordinates;

        final Set<Integer> excludedOrders = new HashSet<>(previousExcludedOrders);

        int polyDegree = previousPolyDegree;  // a value <= 0 means use intep
        double[] polyCoeffs;

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

            xCoordinates = new double[series.length];
            yCoordinates = new double[series.length];
        }

        public void recalculatePolyCoeffs() {
            double[] xSeries = pointXSeries();
            double[] ySeries = pointYSeries();

            polyCoeffs = MathUtils.fitPolynomial(xSeries, ySeries, polyDegree);
        }

        private static void cleanSeries(XYSeries[] series, CleanAsset asset) {
            Map<Integer, List<Integer>> deletedIndices = new HashMap<>();
            int n = series[0].getLength();
            for (int index : asset) {
                int order = Math.floorDiv(index, n);
                int indexInOrder = index % n;
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

                currentSeries.updateYSeries(newYSeries);
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

        private DoubleUnaryOperator scaleFunction() {
            if (polyDegree > 0) {
                return x -> MathUtils.polynomial(x, polyCoeffs);
            } else {
                return x -> MathUtils.intep(pointXSeries(), pointYSeries(), x);
            }
        }
    }
}
