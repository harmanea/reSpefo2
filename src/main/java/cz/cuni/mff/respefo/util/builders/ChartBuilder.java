package cz.cuni.mff.respefo.util.builders;

import cz.cuni.mff.respefo.format.XYSeries;
import cz.cuni.mff.respefo.resources.ColorResource;
import cz.cuni.mff.respefo.util.utils.ChartUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.swtchart.*;
import org.swtchart.ILineSeries.PlotSymbolType;

import java.util.function.Function;

import static cz.cuni.mff.respefo.resources.ColorManager.getColor;

public class ChartBuilder extends ControlBuilder<Chart, ChartBuilder> {

    private static final ColorResource PRIMARY_COLOR = ColorResource.YELLOW;
    private static final ColorResource SECONDARY_COLOR = ColorResource.BLACK;

    private boolean adjustRange = true;
    private boolean layoutData = true;

    private ChartBuilder(Chart chart) {
        control = chart;
    }

    public static ChartBuilder chart(Composite parent) {
        return new ChartBuilder(new Chart(parent, SWT.NONE));
    }

    public ChartBuilder title(String title) {
        control.getTitle().setText(title);
        return this;
    }

    public ChartBuilder xAxisLabel(String label) {
        control.getAxisSet().getXAxis(0).getTitle().setText(label);
        return this;
    }

    public ChartBuilder xAxisLabel(AxisLabel axisLabel) {
        control.getAxisSet().getXAxis(0).getTitle().setText(axisLabel.getLabel());
        return this;
    }

    public ChartBuilder yAxisLabel(String label) {
        control.getAxisSet().getYAxis(0).getTitle().setText(label);
        return this;
    }

    public ChartBuilder yAxisLabel(AxisLabel axisLabel) {
        control.getAxisSet().getYAxis(0).getTitle().setText(axisLabel.getLabel());
        return this;
    }

    public ChartBuilder hideYAxis() {
        control.getAxisSet().getYAxis(0).getTick().setVisible(false);
        control.getAxisSet().getYAxis(0).getTitle().setVisible(false);
        return this;
    }

    public ChartBuilder series(SeriesBuilder<?> seriesBuilder) {
        ILineSeries lineSeries = (ILineSeries) control.getSeriesSet().createSeries(seriesBuilder.seriesType, seriesBuilder.name);
        lineSeries.setXSeries(seriesBuilder.xSeries);
        lineSeries.setYSeries(seriesBuilder.ySeries);

        lineSeries.setLineStyle(seriesBuilder.lineStyle);
        lineSeries.setSymbolType(seriesBuilder.symbolType);
        lineSeries.setLineColor(seriesBuilder.color);
        lineSeries.setLineWidth(seriesBuilder.lineWidth);
        lineSeries.setSymbolColor(seriesBuilder.color);
        lineSeries.setSymbolSize(seriesBuilder.symbolSize);

        adjustExtraSeries(lineSeries);

        return this;
    }

    public ChartBuilder keyListener(Function<Chart, KeyListener> keyListenerProvider) {
        control.addKeyListener(keyListenerProvider.apply(control));

        return this;
    }

    public ChartBuilder mouseListener(Function<Chart, MouseListener> mouseListenerProvider) {
        control.getPlotArea().addMouseListener(mouseListenerProvider.apply(control));

        return this;
    }

    public ChartBuilder mouseMoveListener(Function<Chart, MouseMoveListener> mouseMoveListenerProvider) {
        control.getPlotArea().addMouseMoveListener(mouseMoveListenerProvider.apply(control));

        return this;
    }

    public <T extends MouseListener & MouseMoveListener> ChartBuilder mouseAndMouseMoveListener(Function<Chart, T> listenerProvider) {
        T listener = listenerProvider.apply(control);

        control.getPlotArea().addMouseListener(listener);
        control.getPlotArea().addMouseMoveListener(listener);

        return this;
    }

    public ChartBuilder mouseWheelListener(Function<Chart, MouseWheelListener> mouseWheelListenerProvider) {
        control.getPlotArea().addMouseWheelListener(mouseWheelListenerProvider.apply(control));

        return this;
    }

    public ChartBuilder makeAllSeriesEqualRange() {
        ChartUtils.makeAllSeriesEqualRange(control);
        adjustRange = false;

        return this;
    }

    public ChartBuilder centerAroundSeries(String name) {
        ChartUtils.centerAroundSeries(control, name);
        adjustRange = false;

        return this;
    }

    public ChartBuilder forceFocus() {
        control.forceFocus();

        return this;
    }

    @Override
    public ChartBuilder layoutData(Object data) {
        layoutData = false;

        return super.layoutData(data);
    }

    @Override
    public Chart build() {
        setTheme();

        if (layoutData) {
            control.setLayoutData(new GridData(GridData.FILL_BOTH));
            control.getParent().layout();
        }

        if (adjustRange) {
            control.getAxisSet().adjustRange();
        }

        return super.build();
    }

    private void adjustExtraSeries(ILineSeries lineSeries) {
        if (control.getSeriesSet().getSeries().length > 1) {
            int yAxisId = control.getAxisSet().createYAxis();
            IAxis yAxis = control.getAxisSet().getYAxis(yAxisId);

            yAxis.getTick().setVisible(false);
            yAxis.getTitle().setVisible(false);
            yAxis.getGrid().setForeground(getColor(SECONDARY_COLOR));

            lineSeries.setYAxisId(yAxisId);

            int xAxisId = control.getAxisSet().createXAxis();
            IAxis xAxis = control.getAxisSet().getXAxis(xAxisId);

            xAxis.getTick().setVisible(false);
            xAxis.getTitle().setVisible(false);
            xAxis.getGrid().setForeground(getColor(SECONDARY_COLOR));

            lineSeries.setXAxisId(xAxisId);
        }
    }

    private void setTheme() {
        control.getTitle().setForeground(getColor(PRIMARY_COLOR));

        control.setBackground(getColor(SECONDARY_COLOR));
        control.setBackgroundInPlotArea(getColor(SECONDARY_COLOR));

        IAxisSet axisset = control.getAxisSet();

        axisset.getXAxis(0).getGrid().setForeground(getColor(SECONDARY_COLOR));
        axisset.getYAxis(0).getGrid().setForeground(getColor(SECONDARY_COLOR));

        axisset.getXAxis(0).getTick().setForeground(getColor(PRIMARY_COLOR));
        axisset.getYAxis(0).getTick().setForeground(getColor(PRIMARY_COLOR));
        axisset.getXAxis(0).getTitle().setForeground(getColor(PRIMARY_COLOR));
        axisset.getYAxis(0).getTitle().setForeground(getColor(PRIMARY_COLOR));

        control.getLegend().setVisible(false);
    }

    @SuppressWarnings("unchecked")
    public abstract static class SeriesBuilder<B extends SeriesBuilder<B>> {
        final ISeries.SeriesType seriesType;
        LineStyle lineStyle = LineStyle.NONE;
        PlotSymbolType symbolType = PlotSymbolType.NONE;
        int symbolSize = 1;
        int lineWidth = 1;
        String name = "series";
        Color color = getColor(ColorResource.GREEN);
        double[] xSeries = {};
        double[] ySeries = {};

        private SeriesBuilder(ISeries.SeriesType seriesType) {
            this.seriesType = seriesType;
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
