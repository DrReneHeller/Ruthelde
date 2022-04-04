package GA;

import Helper.Plot.PlotSeries;
import java.awt.*;
import java.util.LinkedList;

public class FitnessPlotter {

    private final static int MAX_ENTRIES = 10000;

    private double fitnessValues[][];

    private int numberOfEntries;

    public FitnessPlotter(){

        fitnessValues = new double[3][MAX_ENTRIES];
        numberOfEntries = 0;
        makePlots();
    }

    public void addDataEntry(double bestFitness, double averageFitness){

        numberOfEntries++;

        if (numberOfEntries < MAX_ENTRIES){

            fitnessValues[0][numberOfEntries-1] = numberOfEntries;
            fitnessValues[1][numberOfEntries-1] = bestFitness;
            fitnessValues[2][numberOfEntries-1] = averageFitness;

        } else {

            for (int i = 0; i < MAX_ENTRIES - 1; i++) {
                fitnessValues[0][i] = fitnessValues[0][i + 1];
                fitnessValues[1][i] = fitnessValues[1][i + 1];
                fitnessValues[2][i] = fitnessValues[2][i + 1];
            }

            fitnessValues[0][MAX_ENTRIES - 1] = numberOfEntries;
            fitnessValues[1][MAX_ENTRIES - 1] = bestFitness;
            fitnessValues[2][MAX_ENTRIES - 1] = averageFitness;
        }
    }

    public void clear(){

        numberOfEntries = 0;

        for (int i=0; i<3; i++){
            for (int j=0; j<MAX_ENTRIES; j++){
                fitnessValues[i][j] = 0;
            }
        }

    }

    public LinkedList<PlotSeries> makePlots() {

        LinkedList<PlotSeries> plotSeries = new LinkedList<>();

        PlotSeries psBest = new PlotSeries("Best", fitnessValues[0], fitnessValues[1]);
        psBest.setStroke(4);
        psBest.setColor(Color.RED);
        plotSeries.add(psBest);

        PlotSeries psAvr = new PlotSeries("Average", fitnessValues[0], fitnessValues[2]);
        psAvr.setStroke(4);
        psAvr.setColor(Color.GREEN);
        plotSeries.add(psAvr);

        return plotSeries;
    }
}
