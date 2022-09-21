package com.ruthelde.Target;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Random;

public class Layer implements Serializable {

    private static final double   DEFAULT_AREAL_DENSITY  = 1000.0d ;
    private static final double   DEFAULT_MIN_AD         =  800.0d ;
    private static final double   DEFAULT_MAX_AD         = 1200.0d ;

    private LinkedList<Element> elementList;
    private double    arealDensity;
    private double    massDensity;
    private double    thickness;
    private double    min_AD, max_AD;

    public Layer() {
        this.elementList = new LinkedList<Element>();
        this.setArealDensity(DEFAULT_AREAL_DENSITY);
        this.setConstrains(DEFAULT_MIN_AD, DEFAULT_MAX_AD);

        Element element = new Element();
        element.setAtomicNumberByName("Si");
        element.setRatio(1);
        this.elementList.add(element);

        this.calculateElementsArealDensity();
        this.calculateMassDensity();
    }

    public LinkedList<Element> getElementList()
    {
        return this.elementList;
    }


    public void setArealDensity(double arealDensity) {
        if (arealDensity > 0) {
            this.arealDensity = arealDensity;
            calculateElementsArealDensity();
            if (arealDensity < min_AD) min_AD = arealDensity;
            if (arealDensity > max_AD) max_AD = arealDensity;
        }
    }

    public void setConstrains(double minAD, double maxAD){
        if (minAD > 0.0d && minAD <= arealDensity && maxAD >= arealDensity){
            this.min_AD = minAD;
            this.max_AD = maxAD;
        }

    }

    public double getMinAD(){return min_AD;}

    public double getMaxAD(){return max_AD;}

    public double   getArealDensity() {
        return arealDensity;
    }

    public void     setMassDensity(double massDensity) {
        this.massDensity = massDensity;
    }

    public double   getMassDensity() {
        return massDensity;
    }

    public void     setThickness(double thickness) {
        if (thickness > 0) {
            this.thickness = thickness;
            calculateArealDensity();
            calculateElementsArealDensity();
        }
    }

    public double   getThickness() {
        calculateThickness();
        return thickness;
    }


    public void     addElement() {
        this.elementList.add(new Element(0.0d, 0.0d));
    }

    public void     removeElement(int elementIndex) {
        if (checkElementIndex(elementIndex) && this.elementList.size() > 1) {
            this.elementList.remove(elementIndex);
            calculateElementsArealDensity();
            calculateMassDensity();
        }
    }

    public void     normalizeElements() {
        calculateArealDensityAndElementRatios();
        calculateMassDensity();
    }

    public void     setElementRatio(int elementIndex, double ratio) {
        if (checkElementIndex(elementIndex)) {
            Element element = elementList.get(elementIndex);
            element.setRatio(ratio);
            calculateElementsArealDensity();
            calculateMassDensity();
        }
    }

    public void     setElementArealDensity(int elementIndex, double arealDensity) {
        if (checkElementIndex(elementIndex)) {
            Element element = elementList.get(elementIndex);
            element.setArealDensity(arealDensity);
            calculateArealDensityAndElementRatios();
            calculateMassDensity();
        }
    }

    public void     setElementAtomicNumber(int elementIndex, int atomicNumber) {
        if (checkElementIndex(elementIndex)) {
            Element element = elementList.get(elementIndex);
            element.setAtomicNumber(atomicNumber);
            calculateElementsArealDensity();
            calculateMassDensity();
        }
    }

    public boolean  setElementAtomicNumberByName(int elementIndex, String atomicNumber) {
        boolean result = false;
        if (checkElementIndex(elementIndex)) {
            Element element = elementList.get(elementIndex);
            if (element.setAtomicNumberByName(atomicNumber)) {
                calculateElementsArealDensity();
                calculateMassDensity();
                result = true;
            }
        }
        return result;
    }


    public void     addIsotope(int elementIndex) {
        if (checkElementIndex(elementIndex)) {
            Element element = elementList.get(elementIndex);
            element.addIsotope();
        }
    }

    public void     removeIsotope(int elementIndex, int isotopeIndex) {
        if (checkElementIndex(elementIndex)) {
            Element element = elementList.get(elementIndex);
            element.removeIsotope(isotopeIndex);
        }
    }

    public void     normalizeIsotopes(int elementIndex) {
        if (checkElementIndex(elementIndex)) {
            Element element = this.elementList.get(elementIndex);
            element.normalizeIsotopes();
        }
    }

    public void     naturalizeIsotopes(int elementIndex) {
        if (checkElementIndex(elementIndex)) {
            Element element = elementList.get(elementIndex);
            element.naturalizeIsotopes();
        }
    }

    public void     setIsotopeMass(int elementIndex, int isotopeIndex, double isotopeMass) {
        if (checkElementIndex(elementIndex)) {
            Element element = elementList.get(elementIndex);
            element.setIsotopeMass(isotopeIndex, isotopeMass);
        }
    }

    public void     setIsotopeRatio(int elementIndex, int isotopeIndex, double isotopeRatio) {
        if (checkElementIndex(elementIndex)) {
            Element element = elementList.get(elementIndex);
            element.setIsotopeRatio(isotopeIndex, isotopeRatio);
        }
    }

    public double   getThicknessConversionFactor() {
        double molecularWeight = calculateMolecularWeight();
        return (1.66d * molecularWeight) / (100.0d * this.massDensity);
    }

    public double   getElementContribution(int Z) {

        double  result;

        double  sumElemRatios = 0.0 ;
        double  elementRatio  = 0.0 ;

        for (Element element : elementList) {
            sumElemRatios += element.getRatio();
            if (element.getAtomicNumber() == Z) {
                elementRatio = element.getRatio();
            }
        }

        result = elementRatio / sumElemRatios;
        return result;
    }

    public double   getIsotopeContribution(int Z, double M) {

        double  result           = 0.0   ;
        boolean isotopeIsPresent = false ;
        double  sumElemRatios    = 0.0   ;
        double  sumIsotopeRatios = 0.0   ;
        double  elementRatio     = 0.0   ;
        double  isotopeRatio     = 0.0   ;

        for (Element element : elementList) {
            sumElemRatios += element.getRatio();
            if (element.getAtomicNumber() == Z) {
                elementRatio = element.getRatio();
                for (Isotope isotope: element.getIsotopeList()) {
                    sumIsotopeRatios += isotope.getAbundance();
                    if (isotope.getMass() == M) {
                        isotopeIsPresent = true;
                        isotopeRatio = isotope.getAbundance();
                    }
                }
            }
        }

        if (isotopeIsPresent) result = (isotopeRatio / sumIsotopeRatios) * (elementRatio / sumElemRatios);
        return result;
    }

    private void    calculateArealDensityAndElementRatios() {
        this.arealDensity = 0.0d;
        for (Element element : elementList) {
            this.arealDensity += element.getArealDensity();
        }
        for (Element element : elementList) {
            element.setRatio(element.getArealDensity() / this.arealDensity);
        }
    }

    private void    calculateElementsArealDensity() {
        double sumOfAllRatios = 0;

        for (Element element : this.elementList) {
            sumOfAllRatios += element.getRatio();
        }
        for (Element element: this.elementList) {
            element.setArealDensity(this.arealDensity * element.getRatio() / sumOfAllRatios);
        }
    }

    private void    calculateMassDensity() {
        double result = 0.0d;
        double elementRatio;
        double sumOfAllRatios = 0.0d;

        for (Element element : this.elementList) {
            elementRatio = element.getRatio();
            sumOfAllRatios += elementRatio;
            result += elementRatio * element.getMassDensity();
        }

        result /= sumOfAllRatios;
        this.massDensity = result;
    }

    private void    calculateThickness() {
        double molecularWeight = calculateMolecularWeight();
        this.thickness = (1.66d * this.arealDensity * molecularWeight) / (100.0d * this.massDensity);
    }

    private void    calculateArealDensity() {
        this.arealDensity = (100.0d * this.massDensity * this.thickness) / (1.66d * calculateMolecularWeight());
    }

    private double  calculateMolecularWeight() {

        double result = 0.0d;
        double elementRatio, isotopeRatio, isotopeMass, sumOfAllElementRatios, sumOfAllIsotopeRatios, elementWeight;
        sumOfAllElementRatios = 0.0d;

        for (Element element : this.elementList) {
            sumOfAllIsotopeRatios = 0.0d;
            elementWeight = 0.0d;
            for (Isotope isotope : element.getIsotopeList()) {
                isotopeRatio = isotope.getAbundance();
                isotopeMass = isotope.getMass();

                sumOfAllIsotopeRatios += isotopeRatio;
                elementWeight += isotopeRatio * isotopeMass;
            }

            elementWeight = elementWeight / sumOfAllIsotopeRatios;
            elementRatio = element.getRatio();
            sumOfAllElementRatios += elementRatio;
            result += elementWeight * elementRatio;
        }

        result = result / sumOfAllElementRatios;
        return result;
    }


    public void     saveToFile(Component component) {

        File file;
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showSaveDialog(component);

        if (returnVal == JFileChooser.APPROVE_OPTION) {

            file = fc.getSelectedFile();
            Properties target_properties = new Properties();
            String prop, val;

            prop = "Area_Density";
            val = "" + arealDensity;
            target_properties.setProperty(prop, val);

            prop = "Number_of_Elements";
            val = "" + elementList.size();
            target_properties.setProperty(prop, val);

            int j = 0;

            for (Element element : elementList) {
                prop = "Element(" + j + ")_Element";
                val = "" + element.getName();
                target_properties.setProperty(prop, val);

                prop = "Element(" + j + ")_Ratio";
                val = "" + element.getRatio();
                target_properties.setProperty(prop, val);

                int num_iso = element.getIsotopeList().size();
                prop = "Element(" + j + ")_NumIsotopes";
                val = "" + num_iso;
                target_properties.setProperty(prop, val);

                int k = 0;

                for (Isotope isotope : element.getIsotopeList()) {
                    prop = "Element(" + j + ")(" + k + ")_IsotopeMass";
                    val = "" + isotope.getMass();
                    target_properties.setProperty(prop, val);

                    prop = "Element(" + j + ")(" + k + ")_IsotopeRatio";
                    val = "" + isotope.getAbundance();
                    target_properties.setProperty(prop, val);

                    k++;
                }

                j++;
            }

            try {
                target_properties.store(new FileOutputStream(file), "IBA Layer File");
            }
            catch (Exception ex) {
                System.out.println(ex);
            }
        }

    }

    public void     loadFromFile(Component component) {

        File file;
        JFileChooser fc = new JFileChooser();

        int returnVal = fc.showOpenDialog(component);

        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            file = fc.getSelectedFile();
            Properties target_properties = new Properties();
            String prop, val;

            try
            {
                target_properties.load(new FileInputStream(file));


                prop = "Area_Density";
                val = target_properties.getProperty(prop);
                setArealDensity(Double.parseDouble(val));

                prop = "Number_of_Elements";
                val = target_properties.getProperty(prop);
                int num_el = Integer.parseInt(val);

                elementList.clear();

                for (int j=0; j<num_el; j++)
                {
                    addElement();

                    prop = "Element(" + j +")_Element";
                    val = target_properties.getProperty(prop);
                    setElementAtomicNumberByName(j, val);

                    prop = "Element(" + j +")_Ratio";
                    val = target_properties.getProperty(prop);
                    setElementRatio(j, Double.parseDouble(val));

                    prop = "Element(" + j +")_NumIsotopes";
                    val = target_properties.getProperty(prop);
                    int num_iso = Integer.parseInt(val);
                    getElementList().get(j).getIsotopeList().clear();

                    for (int k=0; k<num_iso; k++)
                    {
                        addIsotope(j);

                        prop = "Element(" + j + ")(" + k + ")_IsotopeMass";
                        val = target_properties.getProperty(prop);
                        setIsotopeMass(j, k, Double.parseDouble(val));

                        prop = "Element(" + j + ")(" + k + ")_IsotopeRatio";
                        val = target_properties.getProperty(prop);
                        setIsotopeRatio(j, k, Double.parseDouble(val));
                    }
                }

            }
            catch (Exception ex)
            {
                System.out.println(ex);
            }
        }
    }

    public Layer    getDeepCopy(){

        Layer result = new Layer();

        for (int i=0; i<elementList.size()-1; i++){
            result.setElement(i,elementList.get(i).getDeepCopy());
            result.addElement();
        }

        result.setElement(elementList.size()-1,elementList.getLast().getDeepCopy());

        result.setArealDensity(arealDensity);
        result.setConstrains(min_AD, max_AD);
        result.calculateMassDensity();

        return result;
    }


    private boolean checkElementIndex(int elementIndex) {
        return (elementIndex >= 0 && elementIndex < elementList.size());
    }

    private void setElement(int index, Element element){
        elementList.set(index, element);
    }
}
