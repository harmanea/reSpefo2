package cz.cuni.mff.respefo.function.asset.common;

import cz.cuni.mff.respefo.util.utils.ChartUtils;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxisSet;

import java.util.function.Consumer;
import java.util.function.Function;

import static org.eclipse.swt.SWT.*;

public abstract class ChartKeyListener extends KeyAdapter {

    protected final Chart chart;

    protected ChartKeyListener(Chart chart) {
        this.chart = chart;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.keyCode) {
            case 'w':
            case ARROW_UP:
                applyToAxes(IAxisSet::getYAxes, IAxis::scrollUp);
                break;
            case 'a':
            case ARROW_LEFT:
                applyToAxes(IAxisSet::getXAxes, IAxis::scrollDown);
                break;
            case 's':
            case ARROW_DOWN:
                applyToAxes(IAxisSet::getYAxes, IAxis::scrollDown);
                break;
            case 'd':
            case ARROW_RIGHT:
                applyToAxes(IAxisSet::getXAxes, IAxis::scrollUp);
                break;

            case '+':
            case 16777259:
            case 49:
                applyToAxisSet(IAxisSet::zoomIn);
                break;
            case '-':
            case 16777261:
            case 47:
                applyToAxisSet(IAxisSet::zoomOut);
                break;

            case '2':
            case KEYPAD_2:
                applyToAxes(IAxisSet::getYAxes, IAxis::zoomOut);
                break;
            case '4':
            case KEYPAD_4:
                applyToAxes(IAxisSet::getXAxes, IAxis::zoomOut);
                break;
            case '6':
            case KEYPAD_6:
                applyToAxes(IAxisSet::getXAxes, IAxis::zoomIn);
                break;
            case '8':
            case KEYPAD_8:
                applyToAxes(IAxisSet::getYAxes, IAxis::zoomIn);
                break;

            case SPACE:
                adjustRange();
                break;
        }
    }

    private void applyToAxes(Function<IAxisSet, IAxis[]> axes, Consumer<IAxis> action) {
        for (IAxis axis : axes.apply(chart.getAxisSet())) {
            action.accept(axis);
        }
        chart.redraw();
    }

    private void applyToAxisSet(Consumer<IAxisSet> action) {
        action.accept(chart.getAxisSet());
        chart.redraw();
    }

    protected abstract void adjustRange();

    public static ChartKeyListener defaultBehaviour(Chart chart) {
        return new ChartKeyListener(chart) {
            @Override
            protected void adjustRange() {
                chart.getAxisSet().adjustRange();
                chart.redraw();
            }
        };
    }

    public static ChartKeyListener makeAllSeriesEqualRange(Chart chart) {
        return new ChartKeyListener(chart) {
            @Override
            protected void adjustRange() {
                ChartUtils.makeAllSeriesEqualRange(chart);
                chart.redraw();
            }
        };
    }

    public static ChartKeyListener centerAroundSeries(Chart chart, String seriesName) {
        return new ChartKeyListener(chart) {
            @Override
            protected void adjustRange() {
                ChartUtils.centerAroundSeries(chart, seriesName);
                chart.redraw();
            }
        };
    }

    public static ChartKeyListener customAction(Chart chart, Consumer<Chart> adjustRangeAction) {
        return new ChartKeyListener(chart) {
            @Override
            protected void adjustRange() {
                adjustRangeAction.accept(chart);
                chart.redraw();
            }
        };
    }
}
