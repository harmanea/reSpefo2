package cz.cuni.mff.respefo.util;

import cz.cuni.mff.respefo.format.Data;
import cz.cuni.mff.respefo.resources.ColorManager;
import cz.cuni.mff.respefo.resources.ColorResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.swtchart.*;
import org.swtchart.ILineSeries.PlotSymbolType;

// TODO: Adding series could be improved and simplified
// Maybe create a Theme class?
public class ChartBuilder extends ControlBuilder<Chart, ChartBuilder> {
    //TODO: Maybe change this to ColorResource instead
    private static Color primaryColor = ColorManager.getColor(ColorResource.YELLOW);
    private static Color secondaryColor = ColorManager.getColor(ColorResource.BLACK);

    private ChartBuilder(Chart chart) {
        chart.getTitle().setForeground(primaryColor);

        chart.setBackground(secondaryColor);
        chart.setBackgroundInPlotArea(secondaryColor);

        IAxisSet axisset = chart.getAxisSet();

        axisset.getXAxis(0).getGrid().setForeground(secondaryColor);
        axisset.getYAxis(0).getGrid().setForeground(secondaryColor);

        axisset.getXAxis(0).getTick().setForeground(primaryColor);
        axisset.getYAxis(0).getTick().setForeground(primaryColor);
        axisset.getXAxis(0).getTitle().setForeground(primaryColor);
        axisset.getYAxis(0).getTitle().setForeground(primaryColor);

        chart.getLegend().setVisible(false);

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

    public ChartBuilder series(SeriesBuilder seriesBuilder) {
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

    private void adjustExtraSeries(ILineSeries lineSeries) {
        if (control.getSeriesSet().getSeries().length > 0) {
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

    @Override
    public Chart build() {
        setTheme();

        return super.build();
    }

    @SuppressWarnings("unchecked")
    public abstract static class SeriesBuilder<B extends SeriesBuilder> {
        final ISeries.SeriesType seriesType;
        LineStyle lineStyle = LineStyle.NONE;
        PlotSymbolType symbolType = PlotSymbolType.NONE;
        int symbolSize = 1;
        int lineWidth = 1;
        String name = "series";
        Color color = primaryColor;
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
