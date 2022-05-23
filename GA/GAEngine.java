package GA;

import Helper.Helper;
import Helper.Plot.PlotWindow;
import IBA.CalculationSetup.CalculationSetup;
import IBA.Simulator.SimulationData;
import IBA.Simulator.SimulationResultPlotter;
import IBA.Simulator.SpectrumSimulator;
import Target.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

import GA.Input.*;

public class GAEngine {

    private Population population;
    private final DEParameter deParameter;
    private final SpectrumSimulator spectrumSimulator;
    private final SimulationResultPlotter simulationResultPlotter;
    private final FitnessPlotter fitnessPlotter;
    private final ParameterPlotter parameterPlotter;
    private double bestFitness, averageFitness, averageTime;
    private long lastMillis, totalTime;
    private int generationCounter, fittestIndex, processors;
    private double[] originalSpectrum;
    private boolean stop;

    public GAEngine(SpectrumSimulator spectrumSimulator, DEParameter deParameter, CalculationSetup calculationSetup){

        this.spectrumSimulator  = spectrumSimulator                                                       ;
        this.deParameter        = deParameter                                                             ;
        fitnessPlotter          = new FitnessPlotter()                                                    ;
        parameterPlotter        = new ParameterPlotter()                                                  ;
        simulationResultPlotter = new SimulationResultPlotter(calculationSetup)                           ;
        stop                    = false                                                                   ;
    }

    public void initialize(){

        processors = Runtime.getRuntime().availableProcessors();
        System.out.println("Available processors: " + processors);

        int length = spectrumSimulator.experimentalSpectrum.length;
        originalSpectrum = new double[length];
        System.arraycopy(spectrumSimulator.experimentalSpectrum,0, originalSpectrum,0,length);

        if (deParameter.numBins > 1) reBin(deParameter.numBins);

        population = new Population(spectrumSimulator, deParameter.populationSize);

        fitnessPlotter.clear();
        parameterPlotter.clear();
        generationCounter = 0;
        totalTime = 0;
        lastMillis = System.currentTimeMillis();

        stop = false;
    }

    public void reset(){

        int numBins = deParameter.numBins;
        double[] experimentalSpectrum;
        int length = originalSpectrum.length;
        experimentalSpectrum = new double[length];
        System.arraycopy(originalSpectrum,0, experimentalSpectrum,0,length);

        spectrumSimulator.getDetectorCalibration().scaleFactorDown(numBins);

        deParameter.startCH *= numBins;
        deParameter.endCH   *= numBins;

        spectrumSimulator.setStartChannel(deParameter.startCH);
        spectrumSimulator.setStopChannel(deParameter.endCH);
        spectrumSimulator.setExperimentalSpectrum(experimentalSpectrum);

        stop = false;
    }

    public boolean evolve(PlotWindow spectraPlotWindow, PlotWindow fitnessPlotWindow, PlotWindow parameterPlotWindow, JTextArea infoBox){

        boolean plotRefresh = false;

        System.out.println("Starting calculation of new generation [" + generationCounter + "]");

        long currentMillis = System.currentTimeMillis();
        totalTime += currentMillis - lastMillis;
        lastMillis = currentMillis;

        System.out.print("  Implementing transitions ... ");

        double simTime = 0.0f           ;
        int    index   = 0              ;
        double F       = deParameter.F  ;
        double CR      = deParameter.CR ;

        LinkedList<Individual> children = new LinkedList<>();

        for (Individual parent : population.getIndividualList()) {

            int size = population.getIndividualList().size();

            int r1 = index;
            while (r1 == index) r1 = (int) ((Math.random() * (double) size));
            int r2 = r1;
            while (r2 == index || r2 == r1) r2 = (int) ((Math.random() * (double) size));
            int r3 = r2;
            while (r3 == index || r3 == r2 || r3 == r1) r3 = (int) ((Math.random() * (double) size));

            LinkedList<Gene> genes_r1 = population.getIndividualList().get(r1).getGenes();
            LinkedList<Gene> genes_r2 = population.getIndividualList().get(r2).getGenes();
            LinkedList<Gene> genes_r3 = population.getIndividualList().get(r3).getGenes();

            LinkedList<Gene> parentGenes = parent.getGenes();
            LinkedList<Gene> childGenes = new LinkedList<>();

            int geneIndex = 0;

            for (Gene gene : parentGenes) {

                double gene_r1 = genes_r1.get(geneIndex).val;
                double gene_r2 = genes_r2.get(geneIndex).val;
                double gene_r3 = genes_r3.get(geneIndex).val;

                double mutVal = gene_r1 + F * (gene_r2 - gene_r3);

                if (mutVal > gene.max || mutVal < gene.min) mutVal = gene.min + Math.random() * (gene.max - gene.min);

                if (Math.random() < CR) {
                    childGenes.add(new Gene(gene.min, gene.max, mutVal));
                } else {
                    childGenes.add(new Gene(gene.min, gene.max, gene.val));
                }
                geneIndex++;
            }

            Individual child = parent.getDeepCopy();

            child.setGenes(childGenes);

            //Normalize child
            int layerIndex = 0;
            Random rand = new Random();
            for (Layer childLayer : child.getTarget().getLayerList()) {

                Layer parentLayer = parent.getTarget().getLayerList().get(layerIndex);
                int elementIndex = 0;

                int numElements = parentLayer.getElementList().size();

                double[] s = new double[numElements];
                double[] c = new double[numElements];

                //Fill ratios
                for (Element childElement : childLayer.getElementList()) {

                    Element parentElement = parentLayer.getElementList().get(elementIndex);
                    s[elementIndex] = parentElement.getRatio();
                    c[elementIndex] = childElement.getRatio();
                    elementIndex++;
                }

                //Shuffle order
                int[] order = new int[numElements - 1];

                for (int i = 0; i < numElements - 1; i++) {
                    order[i] = i;
                }

                for (int i = order.length - 1; i > 0; i--) {

                    int k = rand.nextInt(i + 1);
                    int a = order[k];
                    order[k] = order[i];
                    order[i] = a;
                }

                //Do normalization
                for (int i : order) {

                    double diff = c[i] - s[i];

                    while (Math.abs(diff) > 0) {

                        int j = i;

                        while (i == j) {
                            j = rand.nextInt(numElements);
                        }

                        if ((s[j] - diff) < 0) {
                            diff = -s[j];
                            s[j] = 0;
                            c[j] = 0;

                        } else {
                            s[j] = s[j] - diff;
                            diff = 0;
                            s[i] = c[i];
                        }
                    }
                }

                //Copy data to child
                elementIndex = 0;
                for (Element element : childLayer.getElementList()) {
                    element.setRatio(s[elementIndex]);
                    elementIndex++;
                }

                layerIndex++;
            }

            //Add child to list for later simulation
            children.add(child);

            index++;
        }

        // Do all simulation work
        ExecutorService es = Executors.newFixedThreadPool(processors);
        List<Callable<Object>> simList = new ArrayList<>();
        for (Individual child : children) { simList.add(Executors.callable(new SimulationTask(child))); }
        try { es.invokeAll(simList); } catch (InterruptedException e) { e.printStackTrace(); }

        //Replace parents if necessary
        index = 0;
        for (Individual child : children) {

            simTime += child.getSimulationData().getSimulationTime();
            double childFitness = child.getFitness();
            double parentFitness = population.getIndividualList().get(index).getFitness();

            if (childFitness >= parentFitness) {

                population.getIndividualList().get(index).replace(child);

                if (childFitness > bestFitness) {
                    bestFitness  = childFitness;
                    fittestIndex = index;
                    plotRefresh  = true;
                }
            }

            index++;
        }

        averageTime = simTime / population.getIndividualList().size();
        averageFitness = population.getAverageFitness();

        //Check that we still have enough diversity if not replace part of the population by random individuals
        if (averageFitness/bestFitness > deParameter.THR){

            final double replaceFraction = 0.05d;

            int numRep = (int)Math.ceil((Math.random() * replaceFraction * (population.getIndividualList().size())));
            for (int i=0; i< numRep; i++) {
                int ii = (int) (Math.random() * (population.getIndividualList().size()));
                if (ii != fittestIndex) {
                    population.getIndividualList().set(ii, new Individual(spectrumSimulator.getDeepCopy(), 1.0d));
                }
            }
        }

        System.out.println("Done.");


        System.out.print("  Updating outputs ... ");

        fitnessPlotter.addDataEntry(bestFitness, averageFitness);
        fitnessPlotWindow.setPlotSeries(fitnessPlotter.makePlots());
        fitnessPlotWindow.refresh();

        double bestCharge = population.getIndividualList().get(fittestIndex).getCharge();
        double bestRes    = population.getIndividualList().get(fittestIndex).getResolution();
        double bestA      = population.getIndividualList().get(fittestIndex).getCalibrationFactor();
        bestA /= deParameter.numBins;
        double bestB      = population.getIndividualList().get(fittestIndex).getCalibrationOffset();
        Target bestTarget = population.getIndividualList().get(fittestIndex).getTarget().getDeepCopy();

        for (Layer layer: bestTarget.getLayerList()) layer.normalizeElements();
        FitParameterSet fitParameterSet = new FitParameterSet(generationCounter, bestFitness, bestCharge, bestRes, bestA, bestB, bestTarget);
        String clippingReport = fitParameterSet.getClippingReport(spectrumSimulator.getExperimentalSetup(), spectrumSimulator.getDetectorSetup(), deParameter.numBins);

        parameterPlotter.add(fitParameterSet);
        if (generationCounter > 0) {
            parameterPlotWindow.setPlotSeries(parameterPlotter.makePlots());
            parameterPlotWindow.refresh();
        }

        if (plotRefresh) {

            SimulationData simData = population.getIndividualList().get(fittestIndex).getSimulationData();
            Target target = population.getIndividualList().get(fittestIndex).getTarget();
            spectraPlotWindow.setPlotSeries(simulationResultPlotter.makePlots(simData, target));
            spectraPlotWindow.refresh();
        }

        getInfo(infoBox, clippingReport);

        System.out.println("Done.");

        System.out.print("  Checking for stop condition(s) ... ");

        //check if isotope simulation needs to be turned on
        final float ms = 1000.0f;
        if (deParameter.isotopeTime   > 0 && totalTime  / ms > deParameter.isotopeTime) {
            spectrumSimulator.getCalculationSetup().setSimulateIsotopes(true);
        }

        //check if goal reached
        if (deParameter.endTime       > 0 && totalTime  / ms > deParameter.endTime        ) stop=true;
        if (deParameter.endFitness    > 0 && bestFitness          > deParameter.endFitness) stop=true;
        if (deParameter.endGeneration > 0 && generationCounter > deParameter.endGeneration) stop=true;

        System.out.println("Done.");

        generationCounter++;

        System.out.println("Calculation of generation done. \n");

        return stop;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public int getGenerationCount() {return generationCounter;}

    public DEParameter getDeParameter(){return deParameter;}

    private void getInfo(JTextArea taOutput, String clippingReport){

        StringBuilder sb = new StringBuilder();
        final float ms = 1000.0f;

        sb.append("Generation \t = ").append(generationCounter).append("\n\r");
        sb.append("Avr. sim. time \t = ").append(Helper.dblToDecStr(averageTime, 2)).append(" ms\n\r");
        sb.append("Total time \t = ").append(Helper.dblToDecStr(totalTime / ms, 2)).append(" s\n\r\n\r");

        sb.append("DE-Para \t = ");
        sb.append("[N=").append(population.getIndividualList().size()).append(", ");
        sb.append("F=").append(Helper.dblToDecStr(deParameter.F, 2)).append(", ");
        sb.append("CR=").append(Helper.dblToDecStr(deParameter.CR, 2)).append(", ");
        sb.append("THR=").append(Helper.dblToDecStr(deParameter.THR, 2)).append("]\n\r");

        sb.append("Best fitness \t = ").append(Helper.dblToDecStr(bestFitness, 2)).append(" (No. ");
        sb.append(fittestIndex).append(")\n\r");
        sb.append("Avr. fitness \t = ").append(Helper.dblToDecStr(averageFitness, 2)).append(" (");
        sb.append(Helper.dblToDecStr(averageFitness / bestFitness * 100.0d, 1)).append("%)").append("\n\r\n\r");

        population.getIndividualList().get(fittestIndex).getInfo(sb, deParameter.numBins);

        sb.append("\n\r");

        sb.append(clippingReport);

        taOutput.setText(sb.toString());
    }

    public Individual getBest(){

        fittestIndex   = population.getBestFitnessIndex();
        return  population.getIndividualList().get(fittestIndex).getDeepCopy();
    }

    private void reBin(int numBins){

        int length = spectrumSimulator.experimentalSpectrum.length;
        originalSpectrum = new double[length];
        System.arraycopy(spectrumSimulator.experimentalSpectrum,0, originalSpectrum,0,length);

        int newNumCh = length / numBins;

        double[] newSpectrum = new double[newNumCh];

        for (int i=0; i<newNumCh; i++){

            double value = 0.0f;
            for (int j=0; j<numBins; j++){ value += spectrumSimulator.experimentalSpectrum[numBins * i +j]; }
            newSpectrum[i] = value;
        }

        spectrumSimulator.getDetectorCalibration().scaleFactorUp(numBins);

        deParameter.startCH /= numBins;
        deParameter.endCH   /= numBins;

        spectrumSimulator.setStartChannel(deParameter.startCH);
        spectrumSimulator.setStopChannel(deParameter.endCH);

        spectrumSimulator.setExperimentalSpectrum(newSpectrum);
    }
}

