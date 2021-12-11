package cz.cuni.mff.respefo.util.widget;

import cz.cuni.mff.respefo.resources.ColorResource;
import cz.cuni.mff.respefo.util.collections.XYSeries;
import cz.cuni.mff.respefo.util.utils.ChartUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.swtchart.*;
import org.swtchart.ILineSeries.PlotSymbolType;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static cz.cuni.mff.respefo.resources.ColorManager.getColor;

public class ChartBuilder extends AbstractControlBuilder<ChartBuilder, Chart> {

    private static final ColorResource PRIMARY_COLOR = ColorResource.YELLOW;
    private static final ColorResource SECONDARY_COLOR = ColorResource.BLACK;

    private ChartBuilder(int style) {
        super((Composite parent) -> new Chart(parent, style));
        addProperty(this::setTheme);
        addProperty(ch -> ch.setLayoutData(new GridData(GridData.FILL_BOTH)));
        addProperty(ch -> ch.getParent().layout());
    }

    public static ChartBuilder newChart() {
        return new ChartBuilder(SWT.NONE);
    }

    public ChartBuilder title(String title) {
        addProperty(ch -> ch.getTitle().setText(title));
        return this;
    }

    public ChartBuilder xAxisLabel(String label) {
        addProperty(ch -> ch.getAxisSet().getXAxis(0).getTitle().setText(label));
        return this;
    }

    public ChartBuilder xAxisLabel(AxisLabel axisLabel) {
        addProperty(ch -> ch.getAxisSet().getXAxis(0).getTitle().setText(axisLabel.getLabel()));
        return this;
    }

    public ChartBuilder yAxisLabel(String label) {
        addProperty(ch -> ch.getAxisSet().getYAxis(0).getTitle().setText(label));
        return this;
    }

    public ChartBuilder yAxisLabel(AxisLabel axisLabel) {
        addProperty(ch -> ch.getAxisSet().getYAxis(0).getTitle().setText(axisLabel.getLabel()));
        return this;
    }

    public ChartBuilder hideYAxis() {
        addProperty(ch -> {
            IAxis axis = ch.getAxisSet().getYAxis(0);
            axis.getTick().setVisible(false);
            axis.getTitle().setVisible(false);
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

            if (ch.getSeriesSet().getSeries().length > 1) {
                adjustExtraSeries(ch, lineSeries);
            }
        });

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

    public ChartBuilder makeAllSeriesEqualRange() {
        addProperty(ChartUtils::makeAllSeriesEqualRange);
        return this;
    }

    public ChartBuilder centerAroundSeries(String name) {
        addProperty(ch -> ChartUtils.centerAroundSeries(ch, name));
        return this;
    }

    public ChartBuilder adjustRange() {
        addProperty(ch -> ch.getAxisSet().adjustRange());
        return this;
    }

    public ChartBuilder forceFocus() {
        addProperty(Control::forceFocus);
        return this;
    }

    public ChartBuilder apply(UnaryOperator<ChartBuilder> operator) {
        return operator.apply(this);
    }

    public ChartBuilder data(String key, Object value) {
        addProperty(ch -> ch.setData(key, value));
        return this;
    }

    private void adjustExtraSeries(Chart chart, ILineSeries lineSeries) {
        int yAxisId = chart.getAxisSet().createYAxis();
        IAxis yAxis = chart.getAxisSet().getYAxis(yAxisId);

        yAxis.getTick().setVisible(false);
        yAxis.getTitle().setVisible(false);
        yAxis.getGrid().setForeground(getColor(SECONDARY_COLOR));

        lineSeries.setYAxisId(yAxisId);

        int xAxisId = chart.getAxisSet().createXAxis();
        IAxis xAxis = chart.getAxisSet().getXAxis(xAxisId);

        xAxis.getTick().setVisible(false);
        xAxis.getTitle().setVisible(false);
        xAxis.getGrid().setForeground(getColor(SECONDARY_COLOR));

        lineSeries.setXAxisId(xAxisId);
    }

    private void setTheme(Chart chart) {
        chart.getTitle().setForeground(getColor(PRIMARY_COLOR));

        chart.setBackground(getColor(SECONDARY_COLOR));
        chart.setBackgroundInPlotArea(getColor(SECONDARY_COLOR));

        IAxisSet axisset = chart.getAxisSet();

        axisset.getXAxis(0).getGrid().setForeground(getColor(SECONDARY_COLOR));
        axisset.getYAxis(0).getGrid().setForeground(getColor(SECONDARY_COLOR));

        axisset.getXAxis(0).getTick().setForeground(getColor(PRIMARY_COLOR));
        axisset.getYAxis(0).getTick().setForeground(getColor(PRIMARY_COLOR));
        axisset.getXAxis(0).getTitle().setForeground(getColor(PRIMARY_COLOR));
        axisset.getYAxis(0).getTitle().setForeground(getColor(PRIMARY_COLOR));

        chart.getLegend().setVisible(false);
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
        WAVELENGTH("wavelength (Å)"),
        RELATIVE_FLUX("relative flux I(λ)"),
        PIXELS("pixels");

        private final String label;

        AxisLabel(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}
