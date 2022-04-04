package GA;

import Helper.Plot.PlotWindow;
import javax.swing.*;
import java.util.LinkedList;

public class GAEngineWorker extends SwingWorker<Void,Integer>{

    private PlotWindow spectraPlotWindow, fitnessPlotWindow, parameterPlotWindow;
    private JTextArea infoBox;
    private GAEngine gaEngine;
    public boolean running, finished;

    public GAEngineWorker(GAEngine gaEngine, PlotWindow spectraPlotWindow, PlotWindow fitnessPlotWindow, PlotWindow parameterPlotWindow, JTextArea infoBox){
        this.gaEngine = gaEngine;
        this.spectraPlotWindow = spectraPlotWindow;
        this.fitnessPlotWindow = fitnessPlotWindow;
        this.parameterPlotWindow = parameterPlotWindow;
        this.infoBox = infoBox;
        this.running = true;
        this.finished = false;
    }

    public GAEngine getGaEngine(){
        return gaEngine;
    }

    public void stop(){
        running = false;
    }

    public boolean isFinished(){
        return finished;
    }

    @Override
    protected Void doInBackground() throws Exception {

        setProgress(0);
        gaEngine.initialize();

        while(running){
            if (gaEngine.evolve(spectraPlotWindow, fitnessPlotWindow, parameterPlotWindow, infoBox)) setProgress(100);
        }

        finished = true;

        gaEngine.reset();
        running = true;
        while (running) {try {Thread.sleep(10);} catch (Exception e){}}

        return null;
    }

    protected void process(LinkedList<Integer> buffer) {}
}
