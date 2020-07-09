package cz.cuni.mff.respefo.util.builders;

import cz.cuni.mff.respefo.format.Data;
import cz.cuni.mff.respefo.resources.ColorManager;
import cz.cuni.mff.respefo.resources.ColorResource;
import cz.cuni.mff.respefo.util.utils.ChartUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.swtchart.*;
import org.swtchart.ILineSeries.PlotSymbolType;

import java.util.function.Function;

// TODO: Maybe create a Theme class?
public class ChartBuilder extends ControlBuilder<Chart, ChartBuilder> {
    //TODO: Maybe change this to ColorResource instead
    private static Color primaryColor = ColorManager.getColor(ColorResource.YELLOW);
    private static Color secondaryColor = ColorManager.getColor(ColorResource.BLACK);

    private boolean adjustRange = true;

    private ChartBuilder(Chart chart) {
        control = chart;
    }

    public static Color getPrimaryColor() {
        return primaryColor;
    }

    public static void setPrimaryColor(Color primaryColor) {
        ChartBuilder.primaryColor = primaryColor;
    }

    public static Color getSecondaryColor() {
        return secondaryColor;
    }

    public static void setSecondaryColor(Color secondaryColor) {
        ChartBuilder.secondaryColor = secondaryColor;
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

    public ChartBuilder yAxisLabel(String label) {
        control.getAxisSet().getYAxis(0).getTitle().setText(label);
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
    public Chart build() {
        setTheme();
        setLayoutData(new GridData(GridData.FILL_BOTH)); // TODO: maybe make this optional?

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
            yAxis.getGrid().setForeground(secondaryColor);

            lineSeries.setYAxisId(yAxisId);

            int xAxisId = control.getAxisSet().createXAxis();
            IAxis xAxis = control.getAxisSet().getXAxis(xAxisId);

            xAxis.getTick().setVisible(false);
            xAxis.getTitle().setVisible(false);
            xAxis.getGrid().setForeground(secondaryColor);

            lineSeries.setXAxisId(xAxisId);
        }
    }

    private void setTheme() {
        control.getTitle().setForeground(primaryColor);

        control.setBackground(secondaryColor);
        control.setBackgroundInPlotArea(secondaryColor);

        IAxisSet axisset = control.getAxisSet();

        axisset.getXAxis(0).getGrid().setForeground(secondaryColor);
        axisset.getYAxis(0).getGrid().setForeground(secondaryColor);

        axisset.getXAxis(0).getTick().setForeground(primaryColor);
        axisset.getYAxis(0).getTick().setForeground(primaryColor);
        axisset.getXAxis(0).getTitle().setForeground(primaryColor);
        axisset.getYAxis(0).getTitle().setForeground(primaryColor);

        control.getLegend().setVisible(false);
    }

    private void setLayoutData(Object layoutData) {
        control.setLayoutData(layoutData);
        control.getParent().layout();
    }

    @SuppressWarnings("unchecked")
    public abstract static class SeriesBuilder<B extends SeriesBuilder<B>> {
        final ISeries.SeriesType seriesType;
        LineStyle lineStyle = LineStyle.NONE;
        PlotSymbolType symbolType = PlotSymbolType.NONE;
        int symbolSize = 1;
        int lineWidth = 1;
        String name = "series";
        Color color = ColorManager.getColor(ColorResource.GREEN);
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
            this.color = ColorManager.getColor(colorResource);

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

        public B data(Data data) {
            this.xSeries = data.getX();
            this.ySeries = data.getY();

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
}