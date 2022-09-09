package com.ruthelde.Target;

import com.ruthelde.Stopping.DataTable;

import java.io.Serializable;
import java.util.LinkedList;

public class Element implements Serializable {

    public  static final int    MAX_ATOMIC_NUMBER          = 92  ;
    private static final int    DEFAULT_ATOMIC_NUMBER      = 14  ;
    private static final double DEFAULT_AREAL_DENSITY      = 100 ;
    private static final double DEFAULT_RATIO              = 1   ;
    private static final double DEFAULT_MIN_RATIO          = 0   ;
    private static final double DEFAULT_MAX_RATIO          = 1   ;

    private static final String[] ELEMENT_NAMES = {

        "H","He","Li","Be","B","C","N","O","F","Ne","Na","Mg","Al","Si","P",
        "S","Cl","Ar","K","Ca","Sc","Ti","V","Cr","Mn","Fe","Co","Ni","Cu",
        "Zn","Ga","Ge","As","Se","Br","Kr","Rb","Sr","Y","Zr","Nb","Mo","Tc",
        "Ru","Rh","Pd","Ag","Cd","In","Sn","Sb","Te","I","Xe","Cs","Ba","La",
        "Ce","Pr","Nd","Pm","Sm","Eu","Gd","Tb","Dy","Ho","Er","Tm","Yb","Lu",
        "Hf","Ta","W","Re","Os","Ir","Pt","Au","Hg","Tl","Pb","Bi","Po","At",
        "Rn","Fr","Ra","Ac","Th","Pa","U"
    };

    private LinkedList<Isotope> isotopeList;
    private int atomicNumber;
    private double arealDensity;
    private double ratio;
    private double min_ratio, max_ratio;

    public Element() {
        this.isotopeList = new LinkedList<Isotope>();
        this.setAtomicNumber(        DEFAULT_ATOMIC_NUMBER  );
        this.setRatio(               DEFAULT_RATIO          );
        this.setConstrains(          DEFAULT_MIN_RATIO, DEFAULT_MAX_RATIO );
        this.setArealDensity(        DEFAULT_AREAL_DENSITY  );
    }


    public Element(double ratio, double arealDensity) {
        this.isotopeList = new LinkedList<Isotope>();
        this.setAtomicNumber(        DEFAULT_ATOMIC_NUMBER                );
        this.setRatio(               ratio                                );
        this.setConstrains(          DEFAULT_MIN_RATIO, DEFAULT_MAX_RATIO );
        this.setArealDensity(        arealDensity                         );
    }

    public LinkedList<Isotope> getIsotopeList() {
        return this.isotopeList;
    }


    public void     setArealDensity(double arealDensity) {
        if (arealDensity >= 0) {
            this.arealDensity = arealDensity;
        }
    }

    public double   getArealDensity() {
        return arealDensity;
    }


    public void     setRatio(double ratio) {
        if (ratio >=0) {
            this.ratio = ratio;
        }
    }

    public double   getRatio() {
        return ratio;
    }

    public void setConstrains(double min_ratio, double max_ratio) {

        if (min_ratio >= 0.0d && min_ratio <= max_ratio) {
            this.min_ratio = min_ratio;
            this.max_ratio = max_ratio;
        }

    }

    public double getMin_ratio() {
        return min_ratio;
    }

    public double getMax_ratio() {
        return max_ratio;
    }

    public void     setAtomicNumber(int atomicNumber) {
        if (atomicNumber > 0 && atomicNumber <= MAX_ATOMIC_NUMBER) {
            this.atomicNumber = atomicNumber;
            this.setAsNaturalElement();
        }
    }

    public boolean  setAtomicNumberByName(String elementName) {
        boolean result = false;
        int atomicNumber = symbolToAtomicNumber(elementName);
        if (atomicNumber != -1)
        {
            this.atomicNumber = atomicNumber;
            this.setAsNaturalElement();
            result = true;
        }
        return result;
    }

    public int      getAtomicNumber() {
        return atomicNumber;
    }

    public String   getName() {
        return ELEMENT_NAMES[atomicNumber-1];
    }

    public double getAverageMass() {
        double result = 0.0;
        double sumRatios = 0.0;
        for (Isotope isotope: isotopeList) {
            result    += isotope.getAbundance() * isotope.getMass();
            sumRatios += isotope.getAbundance();
        }

        result /= sumRatios;
        return result;
    }


    public void     setIsotopeMass(int isotopeIndex, double isotopeMass) {
        if (checkIsotopeIndex(isotopeIndex)) {
            Isotope isotope = isotopeList.get(isotopeIndex);
            isotope.setMass(isotopeMass);
        }
    }

    public void     setIsotopeRatio(int isotopeIndex, double isotopeRatio) {
        if (checkIsotopeIndex(isotopeIndex)) {
            Isotope isotope = isotopeList.get(isotopeIndex);
            isotope.setAbundance(isotopeRatio);
        }
    }

    public void     addIsotope() {
        this.isotopeList.add(new Isotope(0.0d,0.0d));
    }

    public void     removeIsotope(int isotopeIndex) {
        if (checkIsotopeIndex(isotopeIndex) && this.isotopeList.size() > 1) {
            this.isotopeList.remove(isotopeIndex);
        }
    }

    public void     normalizeIsotopes() {
        double ratioSum = 0.0d;

        for (Isotope isotope : isotopeList) {
            ratioSum += isotope.getAbundance();
        }
        for (Isotope isotope : isotopeList) {
            isotope.setAbundance(isotope.getAbundance()/ratioSum);
        }
    }

    public void     naturalizeIsotopes() {
        setAsNaturalElement();
    }


    public double   getMassDensity()
    {
        return DataTable.element_data[(this.atomicNumber-1)*24+1];
    }

    public Element getDeepCopy(){

        Element result = new Element();

        result.setAtomicNumber(atomicNumber);
        result.setRatio(ratio);
        result.setConstrains(min_ratio, max_ratio);
        result.setArealDensity(arealDensity);

        result.getIsotopeList().clear();
        result.addIsotope();

        for (int i=0; i<isotopeList.size()-1; i++){
            result.setIsotope(i, isotopeList.get(i).getDeepCopy());
            result.addIsotope();
        }

        result.setIsotope(isotopeList.size()-1, isotopeList.getLast().getDeepCopy());

        return result;
    }

    public void     setIsotope(int index, Isotope isotope){
        isotopeList.set(index, isotope);
    }


    private void    setAsNaturalElement() {
        int numberOfIsotopes = (int) DataTable.element_data[(this.atomicNumber-1)*24+3];
        Isotope isotope;
        isotopeList.clear();

        for (int i=0; i<numberOfIsotopes; i++) {
            isotope = new Isotope();
            isotope.setMass(     DataTable.element_data[(this.atomicNumber-1)*24+4+2*i          ]);
            isotope.setAbundance(DataTable.element_data[(this.atomicNumber - 1) * 24 + 5 + 2 * i]);
            isotopeList.add(isotope);
        }
    }

    private int     symbolToAtomicNumber(String elementName) {
        int result = -1;

        for (int i=0; i<MAX_ATOMIC_NUMBER; i++) {
            if(ELEMENT_NAMES[i].equals(elementName)) {
                result = i+1;
                break;
            }
        }

        return result;
    }

    private boolean checkIsotopeIndex(int isotopeIndex) {
        return (isotopeIndex >= 0 && isotopeIndex < isotopeList.size());
    }
}
