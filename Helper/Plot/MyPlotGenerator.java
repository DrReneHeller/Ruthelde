package Helper.Plot;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

public class MyPlotGenerator {

    public LinkedList<PlotSeries> plotSeries;
    public PlotProperties plotProperties;
    private boolean blockRefresh = false;

    public MyPlotGenerator(){

        plotSeries = new LinkedList<>();
        plotProperties = new PlotProperties();
    }

    public void render(JPanel plotPanel){

        if(!blockRefresh) {

            final ChartPanel cp = getChartPanel();

            plotPanel.setLayout(new java.awt.BorderLayout());
            plotPanel.removeAll();
            plotPanel.add(cp, BorderLayout.CENTER);
            plotPanel.validate();

            cp.addMouseListener(new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent e) {
                    blockRefresh = true;
                }

                @Override
                public void mouseReleased(MouseEvent e) {

                    double XMin = cp.getChart().getXYPlot().getDomainAxis().getLowerBound();
                    double XMax = cp.getChart().getXYPlot().getDomainAxis().getUpperBound();
                    double YMin = cp.getChart().getXYPlot().getRangeAxis().getLowerBound();
                    double YMax = cp.getChart().getXYPlot().getRangeAxis().getUpperBound();

                    plotProperties.xMin = XMin;
                    plotProperties.xMax = XMax;
                    plotProperties.yMin = YMin;
                    plotProperties.yMax = YMax;

                    blockRefresh = false;
                }
            });
        }

    }

    private ChartPanel getChartPanel() {

        XYSeriesCollection dataCollection = new XYSeriesCollection();

        for (PlotSeries ps: plotSeries) {

            XYSeries xys = new XYSeries(ps.seriesProperties.name);

            for (DataPoint dp: ps.data) {
                double x = dp.x;
                double y = dp.y;

                if (plotProperties.logX && x<=0) x = 1E-6;
                if (plotProperties.logY && y<=0) y = 1E-6;

                xys.add(x,y);
            }
            dataCollection.addSeries(xys);
        }

        JFreeChart chart = ChartFactory.createXYLineChart(null, plotProperties.xAxisName, plotProperties.yAxisName, dataCollection,
                PlotOrientation.VERTICAL, true, true, true);

        chart.setBackgroundPaint(new Color(80, 80, 80));
        chart.getLegend().setBackgroundPaint(new Color(80,80,80));
        chart.getLegend().setFrame(new BlockBorder(new Color(200,200,200)));
        chart.getLegend().setItemPaint(new Color(200,200,200));

        chart.setAntiAlias(true);

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(new Color(50,50,50));
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        int i=0;

        for (PlotSeries ps: plotSeries) {

            SeriesProperties seriesProperties = ps.seriesProperties;

            renderer.setSeriesPaint(i, seriesProperties.color);
            renderer.setSeriesLinesVisible(i, seriesProperties.showLine);
            renderer.setSeriesShapesVisible(i, seriesProperties.showSymbols);

            if (seriesProperties.showSymbols) {
                renderer.setSeriesShape(i, seriesProperties.symbol.getShape());
                renderer.setSeriesShapesFilled(i, seriesProperties.symbol.filled);
            }

            if (seriesProperties.dashed) {
                renderer.setSeriesStroke(i, new BasicStroke(seriesProperties.stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,1.0f, new float[] {1.0f, 10.0f}, 0.0f));
            } else {
                renderer.setSeriesStroke(i, new BasicStroke(seriesProperties.stroke));
            }

            plot.setRenderer(i, renderer);

            i++;
        }

        NumberAxis domainAxis, rangeAxis;

        if (plotProperties.logX) {
            domainAxis = new LogarithmicAxis(plotProperties.xAxisName);
        } else {
            domainAxis = new NumberAxis(plotProperties.xAxisName);
        }

        if (plotProperties.logY) {
            rangeAxis = new LogarithmicAxis(plotProperties.yAxisName);
        } else {
            rangeAxis = new NumberAxis(plotProperties.yAxisName);
        }

        if (!plotProperties.autoScaleX) domainAxis.setRange(plotProperties.xMin, plotProperties.xMax);
        if (!plotProperties.autoScaleY) rangeAxis.setRange(plotProperties.yMin, plotProperties.yMax);

        domainAxis.setTickLabelPaint(new Color(200,200,200));
        rangeAxis.setTickLabelPaint(new Color(200,200,200));
        domainAxis.setLabelPaint(new Color(200,200,200));
        rangeAxis.setLabelPaint(new Color(200,200,200));

        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);

        return new ChartPanel(chart);
    }
}
