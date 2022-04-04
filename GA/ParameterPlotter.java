package GA;

import GA.Input.FitParameterSet;
import Helper.Plot.PlotSeries;
import Target.*;
import java.awt.*;
import java.util.LinkedList;

public class ParameterPlotter {

    private static final int MAX_SIZE   = 10000 ;
    private static final int NUM_COLORS =   20  ;

    private LinkedList<FitParameterSet> fitParameterSets;

    public ParameterPlotter(){

        fitParameterSets = new LinkedList<>();
    }

    public void clear(){
        fitParameterSets = new LinkedList<>();
    }

    public void add(FitParameterSet fitParameterSet){

        fitParameterSets.add(fitParameterSet);
        if (fitParameterSets.size() > MAX_SIZE) fitParameterSets.removeFirst();
    }

    public LinkedList<PlotSeries> makePlots() {

        int colorIndex = 0;

        int numPara = 6; // gen,fit,q,res,a,b

        for (Layer layer : fitParameterSets.getFirst().target.getLayerList()){

            numPara++;
            numPara += layer.getElementList().size();
        }

        double[][] data = new double[numPara][fitParameterSets.size()];

        LinkedList<PlotSeries> plotSeries = new LinkedList<>();

        int columnCounter;
        int index = 0;

        for (FitParameterSet fitParameterSet : fitParameterSets){

            data[0][index] = fitParameterSet.generation;
            data[1][index] = fitParameterSet.fitness;
            data[2][index] = fitParameterSet.charge;
            data[3][index] = fitParameterSet.resolution;
            data[4][index] = fitParameterSet.a;
            data[5][index] = fitParameterSet.b;

            columnCounter = 5;

            for (Layer layer : fitParameterSets.get(index).target.getLayerList()){

                columnCounter++;
                data[columnCounter][index] = layer.getArealDensity();

                for (Element element : layer.getElementList()){
                    columnCounter++;
                    data[columnCounter][index] = element.getRatio();
                }
            }

            index++;
        }

        final float off = 0.33f;

        PlotSeries fitness = new PlotSeries("fitness", data[0], data[1]);
        fitness.setStroke(4);
        float h = (float) colorIndex++ / (float) NUM_COLORS + off;
        fitness.setColor(new Color(Color.HSBtoRGB(h, 1, 1)));
        plotSeries.add(fitness);

        PlotSeries charge = new PlotSeries("charge", data[0], data[2]);
        charge.setStroke(4);
        h = (float) colorIndex++ / (float) NUM_COLORS + off;
        charge.setColor(new Color(Color.HSBtoRGB(h, 1, 1)));
        plotSeries.add(charge);

        PlotSeries detRes = new PlotSeries("det-res", data[0], data[3]);
        detRes.setStroke(4);
        h = (float) colorIndex++ / (float) NUM_COLORS + off;
        detRes.setColor(new Color(Color.HSBtoRGB(h, 1, 1)));
        plotSeries.add(detRes);

        PlotSeries factor = new PlotSeries("cal-factor", data[0], data[4]);
        factor.setStroke(4);
        h = (float) colorIndex++ / (float) NUM_COLORS + off;
        factor.setColor(new Color(Color.HSBtoRGB(h, 1, 1)));
        plotSeries.add(factor);

        PlotSeries offset = new PlotSeries("cal-offset", data[0], data[5]);
        offset.setStroke(4);
        h = (float) colorIndex++ / (float) NUM_COLORS + off;
        offset.setColor(new Color(Color.HSBtoRGB(h, 1, 1)));
        plotSeries.add(offset);

        columnCounter = 5;

        int layerCounter = 0;

        for (Layer layer : fitParameterSets.getFirst().target.getLayerList()){

            columnCounter++;
            layerCounter++;
            String seriesName = "layer_" + layerCounter + " (AD)";

            PlotSeries temp = new PlotSeries(seriesName, data[0], data[columnCounter]);
            temp.setStroke(4);
            h = (float) colorIndex++ / (float) NUM_COLORS + off;
            temp.setColor(new Color(Color.HSBtoRGB(h, 1, 1)));
            temp.setDashed(true);
            plotSeries.add(temp);

            for (Element element : layer.getElementList()){

                columnCounter++;
                seriesName = "layer_" + layerCounter + " (" + element.getName() + ")";

                temp = new PlotSeries(seriesName, data[0], data[columnCounter]);
                temp.setStroke(4);
                h = (float) colorIndex++ / (float) NUM_COLORS + off;
                temp.setColor(new Color(Color.HSBtoRGB(h, 1, 1)));
                temp.setShowSysmbol(true);
                plotSeries.add(temp);
            }
        }

        return plotSeries;
    }

}
