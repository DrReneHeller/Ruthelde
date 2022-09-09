package com.ruthelde.Target;

import com.ruthelde.Helper.Helper;

import java.io.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

public class Target implements Serializable{

    private LinkedList<Layer> layerList;

    public Target() {
        layerList = new LinkedList<Layer>();
        layerList.add(new Layer());
    }

    public LinkedList<Layer> getLayerList() {
        return this.layerList;
    }

    public double  getTotalThickness() {
        double result = 0.0d;

        for (Layer layer : layerList) {
            result += layer.getThickness();
        }

        return result;
    }

    public void    addLayer() {
        this.layerList.add(new Layer());
    }

    public void    removeLayer(int layerIndex) {
        if (layerIndex >= 0 && layerIndex<layerList.size() && layerList.size() > 1) {
            this.layerList.remove(layerIndex);
        }
    }

    public void    swapLayers(int layerIndex, int newLayerIndex) {
        if (checkLayerIndex(layerIndex) && checkLayerIndex(newLayerIndex)) {
            Collections.swap(layerList, layerIndex, newLayerIndex);
        }
    }


    public void    setLayerArealDensity(int layerIndex, double layerArealDensity) {
        if (checkLayerIndex(layerIndex)) {
            Layer layer = layerList.get(layerIndex);
            layer.setArealDensity(layerArealDensity);
        }
    }

    public void    setLayerThickness(int layerIndex, double layerThickness) {
        if (checkLayerIndex(layerIndex)) {
            Layer layer = layerList.get(layerIndex);
            layer.setThickness(layerThickness);
        }
    }

    public void    setLayerMassDensity(int layerIndex, double massDensity) {
        if (checkLayerIndex(layerIndex)) {
            Layer layer = layerList.get(layerIndex);
            layer.setMassDensity(massDensity);
        }
    }


    public void    addElement(int layerIndex) {
        if (checkLayerIndex(layerIndex)) {
            Layer layer = layerList.get(layerIndex);
            layer.addElement();
        }
    }

    public void    removeElement(int layerIndex, int elementIndex) {
        if (checkLayerIndex(layerIndex)) {
            Layer layer = layerList.get(layerIndex);
            layer.removeElement(elementIndex);
        }
    }

    public void    normalizeElements(int layerIndex) {
        if (checkLayerIndex(layerIndex)) {
            Layer layer = layerList.get(layerIndex);
            layer.normalizeElements();
        }
    }

    public void    setElementRatio(int layerIndex, int elementIndex, double ratio) {
        if (checkLayerIndex(layerIndex)) {
            Layer layer = layerList.get(layerIndex);
            layer.setElementRatio(elementIndex, ratio);
        }
    }

    public void    setElementArealDensity(int layerIndex, int elementIndex, double arealDensity) {
        if (checkLayerIndex(layerIndex)) {
            Layer layer = layerList.get(layerIndex);
            layer.setElementArealDensity(elementIndex, arealDensity);
        }
    }

    public void    setElementAtomicNumber(int layerIndex, int elementIndex, int atomicNumber) {
        if (checkLayerIndex(layerIndex)) {
            Layer layer = layerList.get(layerIndex);
            layer.setElementAtomicNumber(elementIndex, atomicNumber);
        }
    }

    public boolean setElementAtomicNumberByName(int layerIndex, int elementIndex, String atomicNumber) {
        boolean result = false;
        if (checkLayerIndex(layerIndex)) {
            Layer layer = layerList.get(layerIndex);
            result = layer.setElementAtomicNumberByName(elementIndex, atomicNumber);
        }
        return result;
    }

    public void randomize(double strength){

        Random rand = new Random();

        for (Layer layer : layerList){

            double minAD = layer.getMinAD();
            double maxAD = layer.getMaxAD();
            //double AD = rand.nextDouble()*(maxAD - minAD) + minAD;
            double AD = layer.getArealDensity() * (1.0d - strength/2.0d + rand.nextDouble()*strength);
            if (AD > maxAD) AD = maxAD;
            if (AD < minAD) AD = minAD;
            layer.setArealDensity(AD);

            int elementIndex = 0;
            for (Element element : layer.getElementList()){

                double minRatio = element.getMin_ratio();
                double maxRatio = element.getMax_ratio();
                //double ratio = minRatio + rand.nextDouble()*(maxRatio-minRatio);
                double ratio = element.getRatio() * (1.0d - strength/2.0d + rand.nextDouble()*strength);
                if (ratio > maxRatio) ratio = maxRatio;
                if (ratio < minRatio) ratio = minRatio;
                layer.setElementRatio(elementIndex, ratio);
                elementIndex++;
            }

            layer.normalizeElements();
        }
    }


    public void    addIsotope(int layerIndex, int elementIndex) {
        if (checkLayerIndex(layerIndex)) {
            Layer layer = layerList.get(layerIndex);
            layer.addIsotope(elementIndex);
        }
    }

    public void    removeIsotope(int layerIndex, int elementIndex, int isotopeIndex) {
        if (checkLayerIndex(layerIndex)) {
            Layer layer = layerList.get(layerIndex);
            layer.removeIsotope(elementIndex, isotopeIndex);
        }
    }

    public void    normalizeIsotopes(int layerIndex, int elementIndex) {
        if (checkLayerIndex(layerIndex)) {
            Layer layer = layerList.get(layerIndex);
            layer.normalizeIsotopes(elementIndex);
        }
    }

    public void    naturalizeIsotopes(int layerIndex, int elementIndex) {
        if (checkLayerIndex(layerIndex)) {
            Layer layer = layerList.get(layerIndex);
            layer.naturalizeIsotopes(elementIndex);
        }
    }

    public void    setIsotopeMass(int layerIndex, int elementIndex, int isotopeIndex, double isotopeMass) {
        if (checkLayerIndex(layerIndex)) {
            Layer layer = layerList.get(layerIndex);
            layer.setIsotopeMass(elementIndex, isotopeIndex, isotopeMass);
        }
    }

    public void    setIsotopeRatio(int layerIndex, int elementIndex, int isotopeIndex, double isotopeRatio) {
        if (checkLayerIndex(layerIndex)) {
            Layer layer = layerList.get(layerIndex);
            layer.setIsotopeRatio(elementIndex, isotopeIndex, isotopeRatio);
        }
    }

    public int getNumberOfIsotopes() {
        int numberOfIsotopes = 0;
        for (Layer layer : layerList) {
            for (Element element : layer.getElementList()) {
                for (Isotope isotope : element.getIsotopeList()) {
                    numberOfIsotopes++;
                }
            }
        }
        return numberOfIsotopes;
    }

    public int getNumberOfElementsThroughAllLayers() {
        int numberOfElementsThroughAllLayers = 0;
        for (Layer layer : layerList) {
            for (Element element : layer.getElementList()) {
                numberOfElementsThroughAllLayers++;
            }
        }
        return numberOfElementsThroughAllLayers;
    }

    public Target getDeepCopy(){

        Target result = new Target();

        for (int i=0; i<layerList.size()-1; i++){
            result.setLayer(i,layerList.get(i).getDeepCopy());
            result.addLayer();
        }

        result.setLayer(layerList.size()-1, layerList.getLast().getDeepCopy());

        return result;
    }

    private void setLayer(int index, Layer layer){
        layerList.set(index, layer);
    }

    public void getInfo(StringBuilder sb){

        int layerIndex = 0;

        for (Layer layer : layerList) {
            sb.append("Layer " + (layerIndex+1) + ": ");
            sb.append("AD="  + Helper.dblToDecStr(layer.getArealDensity(),2) + "x10^15 at/cm2, ");
            sb.append("rho=" + Helper.dblToDecStr(layer.getMassDensity(),2) + "g/cm3, ");
            sb.append("d="   + Helper.dblToDecStr(layer.getThickness(),2) + "nm\n\r\n\r");

            layer.normalizeElements();

            for (Element element : layer.getElementList()) {
                sb.append("  ");
                sb.append(element.getName() + ", ");
                sb.append("Z=" + element.getAtomicNumber() + ", ");
                sb.append("r=" + Helper.dblToDecStr(element.getRatio(),4) + ", ");
                sb.append("AD=" + Helper.dblToDecStr(element.getArealDensity(),2) + "x10^15 at/cm2, ");
                sb.append("rho=" + Helper.dblToDecStr(element.getMassDensity(),2) + "g/cm3\n\r");

                //taOutput.append("\n\r");
                //for (Isotope isotope : element.getIsotopeList()) {
                //    taOutput.append("    m=" + Helper.dblToDecStr(isotope.getMass(),2));
                //    taOutput.append(", r=" + Helper.dblToDecStr(isotope.getAbundance(),2) + "\n\r");
                //}
                //taOutput.append("\n\r");
            }
            layerIndex++;
            sb.append("\n\r");
        }

    }

    private boolean checkLayerIndex(int layerIndex) {
        return (layerIndex >= 0 && layerIndex < this.layerList.size());
    }
}
