package IBA.Detector;

import java.io.Serializable;
import java.util.Random;

public class DetectorCalibration implements Serializable {

    private final static double DEFAULT_FACTOR             = 1.75d ;
    private final static double DEFAULT_FACTOR_MIN         = 1.70d ;
    private final static double DEFAULT_FACTOR_MAX         = 1.80d ;
    private final static double DEFAULT_OFFSET             = 25.0d ;
    private final static double DEFAULT_OFFSET_MIN         = 20.0d ;
    private final static double DEFAULT_OFFSET_MAX         = 30.0d ;

    private double factor                 ;
    private double factor_min, factor_max ;
    private double offset                 ;
    private double offset_min, offset_max ;

    public DetectorCalibration() {
        this.setFactor(DEFAULT_FACTOR);
        this.setFactorMin(DEFAULT_FACTOR_MIN);
        this.setFactorMax(DEFAULT_FACTOR_MAX);
        this.setOffset(DEFAULT_OFFSET);
        this.setOffsetMin(DEFAULT_OFFSET_MIN);
        this.setOffsetMax(DEFAULT_OFFSET_MAX);
    }

    public void setFactor(double factor) {
        if (factor >0) {

            this.factor = factor;
            if (factor < factor_min) factor_min = factor;
            if (factor > factor_max) factor_max = factor;
        }

    }

    public double getFactor() {
        return factor;
    }

    public void setFactorMin(double factor_min){

        if (factor_min > 0 && factor_min <= factor){ this.factor_min = factor_min; }
    }

    public double getFactorMin() {
        return factor_min;
    }

    public void setFactorMax(double factor_max){

        if (factor_max > 0 && factor_max >= factor){ this.factor_max = factor_max; }
    }

    public double getFactorMax() { return factor_max; }

    public void scaleFactorUp(int numBins){

        factor *= numBins;
        factor_min *= numBins;
        factor_max *= numBins;

        System.out.println("test");
    }

    public void scaleFactorDown(int numBins){

        factor /= numBins;
        factor_min /= numBins;
        factor_max /= numBins;
    }

    public void setOffset(double offset) {

        this.offset = offset;
        if (offset < offset_min) offset_min = offset;
        if (offset > offset_max) offset_max = offset;
    }

    public double getOffset() {
        return offset;
    }

    public void setOffsetMin(double offset_min){
        if (offset_min <= offset) this.offset_min = offset_min;
    }

    public double getOffsetMin() { return offset_min; }

    public void setOffsetMax(double offset_max){
        if (offset_max >= offset) this.offset_max = offset_max;
    }

    public double getOffsetMax() { return offset_max; }

    public void randomize(){

        Random rand = new Random();
        factor = factor_min + rand.nextDouble() * (factor_max - factor_min);
        offset = offset_min + rand.nextDouble() * (offset_max - offset_min);
    }

    public DetectorCalibration getDeepCopy(){

        DetectorCalibration result = new DetectorCalibration();

        result.setFactor(factor);
        result.setFactorMin(factor_min);
        result.setFactorMax(factor_max);

        result.setOffset(offset);
        result.setOffsetMin(offset_min);
        result.setOffsetMax(offset_max);

        return result;
    }
}
