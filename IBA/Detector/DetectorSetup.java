package IBA.Detector;

import java.io.Serializable;

public class DetectorSetup implements Serializable {

    private final static double DEFAULT_RESOLUTION  =  16.0d ;
    private final static double DEFAULT_MIN_RES     =  15.0d ;
    private final static double DEFAULT_MAX_RES     =  17.0d ;
    private final static double DEFAULT_SOLID_ANGLE =   3.3d ;

    private DetectorCalibration   calibration      ;
    private double                resolution       ;
    private double                minRes           ;
    private double                maxRes           ;
    private double                solidAngle       ;

    public DetectorSetup() {

        this.setSolidAngle(DEFAULT_SOLID_ANGLE);
        this.setResolution(DEFAULT_RESOLUTION);
        this.setMinRes(DEFAULT_MIN_RES);
        this.setMaxRes(DEFAULT_MAX_RES);
        calibration = new DetectorCalibration();
    }

    public void setCalibrationOffset(double offset) {
        this.calibration.setOffset(offset);
    }

    public void setCalibrationFactor(double a) {
        this.calibration.setFactor(a);
    }

    public void setCalibration(DetectorCalibration detectorCalibration){
        this.calibration = detectorCalibration;
    }

    public DetectorCalibration getCalibration() {
        return this.calibration;
    }

    public void setResolution(double resolution) {
        if (resolution >= 0) {
            this.resolution = resolution;
            if (resolution < minRes) minRes = resolution;
            if (resolution > maxRes) maxRes = resolution;
        }
    }

    public double getResolution() {
        return resolution;
    }

    public void setMinRes(double minRes) {
        if (minRes > 0 && minRes <= resolution) {
            this.minRes = minRes;
        }
    }

    public double getMinRes() {return minRes;}

    public void setMaxRes(double maxRes) {

        if (maxRes >= resolution) { this.maxRes = maxRes; }
    }

    public double getMaxRes() {return maxRes;}

    public void setSolidAngle(double solidAngle) {
        if (solidAngle > 0) this.solidAngle = solidAngle;
    }

    public double getSolidAngle() {
        return solidAngle;
    }

    public DetectorSetup getDeepCopy(){

        DetectorSetup result = new DetectorSetup();

        result.setSolidAngle(this.solidAngle);
        result.setResolution(this.resolution);
        result.setMinRes(this.minRes);
        result.setMaxRes(this.maxRes);
        result.setCalibration(this.calibration.getDeepCopy());

        return result;

    }

}
