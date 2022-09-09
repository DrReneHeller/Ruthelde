package com.ruthelde.IBA.Simulator;

import com.ruthelde.Helper.Plot.PlotSeries;
import com.ruthelde.IBA.CalculationSetup.CalculationSetup;
import com.ruthelde.Target.*;
import java.awt.*;
import java.util.LinkedList;

public class SimulationResultPlotter {

    private final CalculationSetup calculationSetup;

    public SimulationResultPlotter(CalculationSetup calculationSetup){
        this.calculationSetup = calculationSetup;
    }

    public LinkedList<PlotSeries> makePlots(SimulationData simulationData, Target target){

        LinkedList<IsotopeFitData> isotopeList = simulationData.getIsotopeList();
        double[] simulatedSpectrum = simulationData.getSimulatedSpectrum();
        double[] experimentalSpectrum = simulationData.getExperimentalSpectrum();
        double[] energy = simulationData.getEnergy();
        int numberOfChannels = simulationData.getNumberOfChannels();

        boolean showIsotopes = calculationSetup.isShowIsotopes() ;
        boolean showLayers   = calculationSetup.isShowLayers()   ;
        boolean showElements = calculationSetup.isShowElements() ;

        int numberOfLayers = target.getLayerList().size();
        int numberOfIsotopes = target.getNumberOfIsotopes();
        int numberOfElementsThroughAllLayers = target.getNumberOfElementsThroughAllLayers();
        final float colOffset = 0.33f;

        int numberOfElements = 0;
        int tempZ = 0;
        for (IsotopeFitData isotopeFitData : isotopeList) {
            if (isotopeFitData.Z != tempZ) {
                numberOfElements++;
                tempZ = isotopeFitData.Z;
            }
        }

        LinkedList<PlotSeries> plotSeries = new LinkedList<>();

        PlotSeries ps_sim = new PlotSeries("Simulated"   , energy, simulatedSpectrum   );
        ps_sim.seriesProperties.color = Color.BLUE;
        ps_sim.seriesProperties.dashed = true;
        ps_sim.seriesProperties.stroke = 4;
        plotSeries.add(ps_sim);

        PlotSeries ps_exp = new PlotSeries("Experimental", energy, experimentalSpectrum);
        ps_exp.seriesProperties.showLine = false;
        ps_exp.seriesProperties.showSymbols = true;
        ps_exp.seriesProperties.color = Color.RED;
        plotSeries.add(ps_exp);

        int plotIndex, isotopeMass;
        Element element;
        double[][] data;

        if (showElements) {
            if (showLayers && !showIsotopes)
            {
                plotIndex = 0;
                data = new double[numberOfElementsThroughAllLayers][numberOfChannels];
                int layerIndex = 0;
                for (Layer layer : target.getLayerList()) {
                    for (Element temp_element : layer.getElementList()) {

                        for (IsotopeFitData isotopeFitData : isotopeList) {
                            if (isotopeFitData.Z == temp_element.getAtomicNumber()) {
                                for (int i=0; i<numberOfChannels; i++)
                                {
                                    data[plotIndex][i] += isotopeFitData.spectra[layerIndex][i];
                                }
                            }
                        }

                        String seriesName = temp_element.getName() + "_Layer_" + (layerIndex+1);
                        PlotSeries ps = new PlotSeries(seriesName, energy, data[plotIndex]);
                        float h = (float) plotIndex / (float) numberOfLayers + colOffset;
                        ps.setColor(new Color(Color.HSBtoRGB(h, 1, 1)));
                        ps.setStroke(2);
                        plotSeries.add(ps);

                        plotIndex++;
                    }
                    layerIndex++;
                }
            } else {

                tempZ = isotopeList.get(0).Z;
                plotIndex = 0;
                data = new double[numberOfElements][numberOfChannels];
                element = new Element();

                for (IsotopeFitData isotopeFitData : isotopeList) {

                    if (isotopeFitData.Z != tempZ) {
                        element.setAtomicNumber(tempZ);

                        String seriesName = element.getName();
                        PlotSeries ps = new PlotSeries(seriesName, energy, data[plotIndex]);
                        float h = (float) plotIndex / (float) isotopeList.size() + colOffset;
                        ps.setColor(new Color(Color.HSBtoRGB(h, 1, 1)));
                        ps.setStroke(2);
                        plotSeries.add(ps);

                        plotIndex++;
                        tempZ = isotopeFitData.Z;
                    }

                    for (int k=0; k<numberOfLayers; k++) {
                        for(int j=0; j<numberOfChannels; j++) {
                            data[plotIndex][j] += isotopeFitData.spectra[k][j];
                        }
                    }

                }

                element.setAtomicNumber(tempZ);

                String seriesName = element.getName();
                PlotSeries ps = new PlotSeries(seriesName, energy, data[plotIndex]);
                float h = (float) plotIndex / (float) isotopeList.size() + colOffset;
                ps.setColor(new Color(Color.HSBtoRGB(h, 1, 1)));
                ps.setStroke(2);
                plotSeries.add(ps);
            }
        }

        if (showIsotopes) {
            if (showLayers) {
                plotIndex = 0;
                data = new double[numberOfIsotopes][numberOfChannels];
                int layerIndex = 0;
                for (Layer layer : target.getLayerList()) {
                    for (Element temp_element : layer.getElementList()) {
                        for (Isotope temp_isotope : temp_element.getIsotopeList()) {
                            for (IsotopeFitData isotopeFitData : isotopeList) {
                                if (temp_element.getAtomicNumber() == isotopeFitData.Z && temp_isotope.getMass() == isotopeFitData.M) {
                                    if (numberOfChannels >= 0)
                                        System.arraycopy(isotopeFitData.spectra[layerIndex], 0, data[plotIndex], 0, numberOfChannels);
                                }
                            }
                            isotopeMass = (int)Math.ceil(temp_isotope.getMass());

                            String seriesName = isotopeMass + temp_element.getName() + "_Layer_" + (layerIndex+1);
                            PlotSeries ps = new PlotSeries(seriesName, energy, data[plotIndex]);
                            float h = (float) plotIndex / (float) isotopeList.size() + colOffset;
                            ps.setColor(new Color(Color.HSBtoRGB(h, 1, 1)));
                            ps.setStroke(2);
                            plotSeries.add(ps);

                            plotIndex++;
                        }
                    }
                    layerIndex++;
                }
            } else {
                data = new double[isotopeList.size()][numberOfChannels];
                plotIndex = 0;
                element = new Element();
                for (IsotopeFitData isotopeFitData : isotopeList) {
                    for (int k=0; k<numberOfLayers; k++) {
                        for (int j=0; j<numberOfChannels; j++) {
                            data[plotIndex][j] += isotopeFitData.spectra[k][j];
                        }
                    }
                    element.setAtomicNumber(isotopeFitData.Z);
                    isotopeMass = (int)Math.ceil(isotopeFitData.M);

                    String seriesName = isotopeMass + element.getName();
                    PlotSeries ps = new PlotSeries(seriesName, energy, data[plotIndex]);
                    float h = (float) plotIndex / (float) isotopeList.size() + colOffset;
                    ps.setColor(new Color(Color.HSBtoRGB(h, 1, 1)));
                    ps.setStroke(2);
                    plotSeries.add(ps);

                    plotIndex++;
                }
            }
        }

        if (showLayers && !showElements && !showIsotopes) {
            data = new double[numberOfLayers][numberOfChannels];
            for (int i=0; i<numberOfLayers; i++) {
                for (IsotopeFitData isotopeFitData : isotopeList) {
                    for (int j=0; j<numberOfChannels; j++) {
                        data[i][j] += isotopeFitData.spectra[i][j];
                    }
                }

                String seriesName = "Layer " + (i+1);
                PlotSeries ps = new PlotSeries(seriesName, energy, data[i]);
                float h = (float) i / (float) numberOfLayers + colOffset;
                ps.setColor(new Color(Color.HSBtoRGB(h, 1, 1)));
                ps.setStroke(2);
                plotSeries.add(ps);
            }
        }

        return plotSeries;
    }

}
