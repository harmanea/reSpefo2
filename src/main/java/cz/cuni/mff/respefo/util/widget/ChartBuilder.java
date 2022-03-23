package cz.cuni.mff.respefo.util.widget;

import cz.cuni.mff.respefo.resources.ColorResource;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.utils.ChartUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.swtchart.*;
import org.swtchart.ILineSeries.PlotSymbolType;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static cz.cuni.mff.respefo.resources.ColorManager.getColor;

public class ChartBuilder extends AbstractControlBuilder<ChartBuilder, Chart> {

    private static final ColorResource PRIMARY_COLOR = ColorResource.YELLOW;
    private static final ColorResource SECONDARY_COLOR = ColorResource.BLACK;

    private boolean hideTitle = true;
    private boolean hideYAxisLabel = true;
    private boolean hideXAxisLabel = true;

    private ChartBuilder(int style) {
        super((Composite parent) -> new Chart(parent, style));
        addProperty(this::addSecondaryAxis);
        addProperty(this::setTheme);
        addProperty(this::layoutSettings);
    }

    public static ChartBuilder newChart() {
        return new ChartBuilder(SWT.NONE);
    }

    public ChartBuilder title(String title) {
        hideTitle = false;
        addProperty(ch -> ch.getTitle().setText(title));
        return this;
    }

    public ChartBuilder xAxisLabel(String label) {
        hideXAxisLabel = false;
        addProperty(ch -> ch.getAxisSet().getXAxis(0).getTitle().setText(label));
        return this;
    }

    public ChartBuilder xAxisLabel(AxisLabel axisLabel) {
        return xAxisLabel(axisLabel.getLabel());
    }

    public ChartBuilder yAxisLabel(String label) {
        hideYAxisLabel = false;
        addProperty(ch -> ch.getAxisSet().getYAxis(0).getTitle().setText(label));
        return this;
    }

    public ChartBuilder yAxisLabel(AxisLabel axisLabel) {
        return yAxisLabel(axisLabel.getLabel());
    }

    public ChartBuilder hideYAxes() {
        addProperty(ch -> {
            for (IAxis axis : ch.getAxisSet().getYAxes()) {
                axis.getTick().setVisible(false);
                axis.getTitle().setVisible(false);
            }
        });
        return this;
    }

    public ChartBuilder zoomOut() {
        addProperty(ch -> ch.getAxisSet().zoomOut());
        return this;
    }

    public ChartBuilder series(SeriesBuilder<?> seriesBuilder) {
        addProperty(ch -> {
            ILineSeries lineSeries = (ILineSeries) ch.getSeriesSet().createSeries(seriesBuilder.seriesType, seriesBuilder.name);
            lineSeries.setXSeries(seriesBuilder.xSeries);
            lineSeries.setYSeries(seriesBuilder.ySeries);

            lineSeries.setLineStyle(seriesBuilder.lineStyle);
            lineSeries.setSymbolType(seriesBuilder.symbolType);
            lineSeries.setLineColor(seriesBuilder.color);
            lineSeries.setLineWidth(seriesBuilder.lineWidth);
            lineSeries.setSymbolColor(seriesBuilder.color);
            lineSeries.setSymbolSize(seriesBuilder.symbolSize);

            if (seriesBuilder.newYAxis) {
                int yAxisId = ch.getAxisSet().createYAxis();
                IAxis yAxis = ch.getAxisSet().getYAxis(yAxisId);

                yAxis.getTick().setVisible(false);
                yAxis.getTitle().setVisible(false);
                yAxis.getGrid().setForeground(getColor(SECONDARY_COLOR));

                lineSeries.setYAxisId(yAxisId);
            }
        });

        return this;
    }

    public ChartBuilder keyListener(KeyListener keyListener) {
        addProperty(ch -> ch.addKeyListener(keyListener));
        return this;
    }

    public ChartBuilder keyListener(Function<Chart, KeyListener> keyListenerProvider) {
        addProperty(ch -> ch.addKeyListener(keyListenerProvider.apply(ch)));
        return this;
    }

    public ChartBuilder mouseListener(Function<Chart, MouseListener> mouseListenerProvider) {
        addProperty(ch -> ch.getPlotArea().addMouseListener(mouseListenerProvider.apply(ch)));
        return this;
    }

    public ChartBuilder mouseMoveListener(Function<Chart, MouseMoveListener> mouseMoveListenerProvider) {
        addProperty(ch -> ch.getPlotArea().addMouseMoveListener(mouseMoveListenerProvider.apply(ch)));
        return this;
    }

    public <T extends MouseListener & MouseMoveListener> ChartBuilder mouseAndMouseMoveListener(Function<Chart, T> listenerProvider) {
        addProperty(ch -> {
            T listener = listenerProvider.apply(ch);
            ch.getPlotArea().addMouseListener(listener);
            ch.getPlotArea().addMouseMoveListener(listener);
        });
        return this;
    }

    public ChartBuilder mouseWheelListener(Function<Chart, MouseWheelListener> mouseWheelListenerProvider) {
        addProperty(ch -> ch.getPlotArea().addMouseWheelListener(mouseWheelListenerProvider.apply(ch)));
        return this;
    }

    public ChartBuilder plotAreaPaintListener(Function<Chart, PaintListener> paintListenerProvider) {
        addProperty(ch -> ch.getPlotArea().addPaintListener(paintListenerProvider.apply(ch)));
        return this;
    }

    public ChartBuilder verticalLine(double x, ColorResource color, int lineStyle) {
        return plotAreaPaintListener(ch -> event -> {
            int coordinate = ch.getAxisSet().getXAxis(0).getPixelCoordinate(x);
            event.gc.setForeground(getColor(color));
            event.gc.setLineStyle(lineStyle);
            event.gc.drawLine(coordinate, 0, coordinate, event.height);
        });
    }

    public ChartBuilder horizontalLine(double y, ColorResource color, int lineStyle) {
        return plotAreaPaintListener(ch -> event -> {
            int coordinate = ch.getAxisSet().getYAxis(0).getPixelCoordinate(y);
            event.gc.setForeground(getColor(color));
            event.gc.setLineStyle(lineStyle);
            event.gc.drawLine(0, coordinate, event.width, coordinate);
        });
    }

    public ChartBuilder centerAroundSeries(String name) {
        addProperty(ch -> ChartUtils.centerAroundSeries(ch, name));
        return this;
    }

    public ChartBuilder adjustRange() {
        addProperty(ChartUtils::adjustRange);
        return this;
    }

    public ChartBuilder forceFocus() {
        addProperty(Control::forceFocus);
        return this;
    }

    public ChartBuilder apply(UnaryOperator<ChartBuilder> operator) {
        return operator.apply(this);
    }

    public ChartBuilder accept(Consumer<Chart> consumer) {
        addProperty(consumer);
        return this;
    }

    public ChartBuilder data(String key, Object value) {
        addProperty(ch -> ch.setData(key, value));
        return this;
    }

    private void addSecondaryAxis(Chart chart) {
        int axisId = chart.getAxisSet().createYAxis();
        IAxis axis = chart.getAxisSet().getYAxis(axisId);
        axis.setPosition(IAxis.Position.Secondary);
        axis.getTitle().setVisible(false);
    }

    private void setTheme(Chart chart) {
        chart.getTitle().setForeground(getColor(PRIMARY_COLOR));

        chart.setBackground(getColor(SECONDARY_COLOR));
        chart.setBackgroundInPlotArea(getColor(SECONDARY_COLOR));

        chart.getLegend().setVisible(false);

        IAxisSet axisSet = chart.getAxisSet();
        Stream.of(axisSet.getXAxis(0), axisSet.getYAxis(0), axisSet.getYAxis(1))
                .forEach(axis -> {
                    axis.getGrid().setStyle(LineStyle.NONE);
                    axis.getTick().setForeground(getColor(PRIMARY_COLOR));
                    axis.getTitle().setForeground(getColor(PRIMARY_COLOR));
                });

        if (hideTitle) {
            chart.getTitle().setVisible(false);
        }
        if (hideXAxisLabel) {
            axisSet.getXAxis(0).getTitle().setVisible(false);
        }
        if (hideYAxisLabel) {
            axisSet.getYAxis(0).getTitle().setVisible(false);
        }
    }

    private void layoutSettings(Chart chart) {
        chart.setLayoutData(new GridData(GridData.FILL_BOTH));
        chart.getParent().layout();
    }

    @SuppressWarnings("unchecked")
    public abstract static class SeriesBuilder<B extends SeriesBuilder<B>> {
        static final AtomicInteger nameCounter = new AtomicInteger();

        final ISeries.SeriesType seriesType;
        LineStyle lineStyle = LineStyle.NONE;
        PlotSymbolType symbolType = PlotSymbolType.NONE;
        int symbolSize = 1;
        int lineWidth = 1;
        String name;
        Color color = getColor(ColorResource.GREEN);
        double[] xSeries = {};
        double[] ySeries = {};
        boolean newYAxis = false;

        private SeriesBuilder(ISeries.SeriesType seriesType) {
            this.seriesType = seriesType;
            this.name = Integer.toString(nameCounter.addAndGet(1));
        }

        public B name(String name) {
            this.name = name;

            return (B) this;
        }

        public B color(Color color) {
            this.color = color;

            return (B) this;
        }

        public B color(ColorResource colorResource) {
            this.color = getColor(colorResource);

            return (B) this;
        }

        public B xSeries(double[] xSeries) {
            this.xSeries = xSeries;

            return (B) this;
        }

        public B ySeries(double[] ySeries) {
            this.ySeries = ySeries;

            return (B) this;
        }

        public B series(XYSeries xySeries) {
            this.xSeries = xySeries.getXSeries();
            this.ySeries = xySeries.getYSeries();

            return (B) this;
        }

        public B newYAxis() {
            this.newYAxis = true;

            return (B) this;
        }
    }

    public static class LineSeriesBuilder extends SeriesBuilder<LineSeriesBuilder> {

        private LineSeriesBuilder() {
            super(ISeries.SeriesType.LINE);

            lineStyle = LineStyle.SOLID;
            symbolType = PlotSymbolType.NONE;
        }

        public static LineSeriesBuilder lineSeries() {
            return new LineSeriesBuilder();
        }

        public LineSeriesBuilder lineWidth(int lineWidth) {
            this.lineWidth = lineWidth;

            return this;
        }
    }

    public static class ScatterSeriesBuilder extends SeriesBuilder<ScatterSeriesBuilder> {
        private ScatterSeriesBuilder() {
            super(ISeries.SeriesType.LINE);

            lineStyle = LineStyle.NONE;
            symbolType = PlotSymbolType.CIRCLE;
        }

        public static ScatterSeriesBuilder scatterSeries() {
            return new ScatterSeriesBuilder();
        }

        public ScatterSeriesBuilder symbolSize(int symbolSize) {
            this.symbolSize = symbolSize;

            return this;
        }

        public ScatterSeriesBuilder plotSymbolType(PlotSymbolType symbolType) {
            this.symbolType = symbolType;

            return this;
        }
    }

    public enum AxisLabel {
        WAVELENGTH("Wavelength (Å)"),
        RELATIVE_FLUX("Relative flux I(λ)"),
        FLUX("Flux"),
        PIXELS("Pixels");

        private final String label;

        AxisLabel(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}
