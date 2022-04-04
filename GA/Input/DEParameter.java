package GA.Input;

import java.io.Serializable;

public class DEParameter implements Serializable {

    public  static final int    DEFAULT_N           = 20    ;
    public  static final double DEFAULT_F           = 0.5d  ;
    public  static final double DEFAULT_CR          = 0.7d  ;
    public  static final double DEFAULT_THR         = 0.9d  ;
    public  static final int    DEFAULT_BINS        = 2     ;
    public  static final int    DEFAULT_START_CH    = 100   ;
    public  static final int    DEFAULT_END_CH      = 1000  ;
    public  static final double DEFAULT_END_TIME    = 0.0d  ;
    public  static final double DEFAULT_END_FITNESS = 0.0d  ;
    public  static final double DEFAULT_END_GEN     = 0.0d  ;
    public  static final double DEFAULT_ISO_TIME    = 0.0d  ;

    public int populationSize;
    public double F, CR, THR;

    public int numBins;
    public int startCH, endCH;

    public double endTime, endFitness, endGeneration, isotopeTime;


    public DEParameter(){

        populationSize = DEFAULT_N           ;
        F              = DEFAULT_F           ;
        CR             = DEFAULT_CR          ;
        THR            = DEFAULT_THR         ;
        numBins        = DEFAULT_BINS        ;
        startCH        = DEFAULT_START_CH    ;
        endCH          = DEFAULT_END_CH      ;
        endTime        = DEFAULT_END_TIME    ;
        endFitness     = DEFAULT_END_FITNESS ;
        endGeneration  = DEFAULT_END_GEN     ;
        isotopeTime    = DEFAULT_ISO_TIME    ;
    }
}

