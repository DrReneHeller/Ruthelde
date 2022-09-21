package com.ruthelde.GA.Input;

import com.ruthelde.IBA.Detector.DetectorSetup;
import com.ruthelde.IBA.ExperimentalSetup.ExperimentalSetup;
import com.ruthelde.Target.*;

public class FitParameterSet {

    public int generation;
    public double fitness;
    public double charge, resolution, a , b;
    public Target target;

    public FitParameterSet(int generation, double fitness, double charge, double resolution, double a, double b, Target target) {

        this.generation = generation;
        this.fitness = fitness;
        this.charge = charge;
        this.resolution = resolution;
        this.a = a;
        this.b = b;
        this.target = target.getDeepCopy();
    }

    public void scale(ExperimentalSetup es, DetectorSetup ds){

        double min   = es.getMinCharge();
        double max   = es.getMaxCharge();
        charge = (charge - min) / (max - min);
        if (charge > 1.0d) charge = 1.0d;
        if (charge < 0.0d) charge = 0.0d;

        min   = ds.getMinRes();
        max   = ds.getMaxRes();
        resolution = (resolution - min) / (max - min);
        if (resolution > 1.0d) resolution = 1.0d;
        if (resolution < 0.0d) resolution = 0.0d;

        min   = ds.getCalibration().getFactorMin();
        max   = ds.getCalibration().getFactorMax();
        a = (a - min) / (max - min);
        if (a > 1.0d) a = 1.0d;
        if (a < 0.0d) a = 0.0d;

        min   = ds.getCalibration().getOffsetMin();
        max   = ds.getCalibration().getOffsetMax();
        b = (b - min) / (max - min);
        if (b > 1.0d) b = 1.0d;
        if (b < 0.0d) b = 0.0d;

        for (Layer layer : target.getLayerList()){

            min = layer.getMinAD();
            max = layer.getMaxAD();
            double value = layer.getArealDensity();
            if (value > max) value = max;
            if (value < min) value = min;

            double AD = (value - min) / (max - min);
            if (AD > 1.0d) AD = 1.0d;
            if (AD < 0.0d) AD = 0.0d;
            if (max == min){AD = 1.0d;}
            layer.setArealDensity(AD);
        }

        final double sf = 100.0d;
        fitness /= sf;
    }

    public String getClippingReport(ExperimentalSetup es, DetectorSetup ds, int numBins){

        final double thr = 0.1d; // 10% above *min* or 10% below *max* waring will appear

        StringBuilder sb = new StringBuilder();
        int clipCount = 0;

        sb.append("Clipping warning for parameters: \n\r");

        double min   = es.getMinCharge();
        double max   = es.getMaxCharge();
        double value  = charge;

        if (value >= (max - (thr*(max-min))) || value <= (min + (thr*(max-min)))) {
            sb.append("  Charge\n\r");
            clipCount++;
        }

        min   = ds.getMinRes();
        max   = ds.getMaxRes();
        value  = resolution;

        if (value >= (max - (thr*(max-min))) || value <= (min + (thr*(max-min)))) {
            sb.append("  Detector resolution\n\r");
            clipCount++;
        }

        min   = ds.getCalibration().getFactorMin();
        max   = ds.getCalibration().getFactorMax();
        value  = a * numBins;

        if (value >= (max - (thr*(max-min))) || value <= (min + (thr*(max-min)))) {
            sb.append("  Calibration factor\n\r");
            clipCount++;
        }

        min   = ds.getCalibration().getOffsetMin();
        max   = ds.getCalibration().getOffsetMax();
        value  = b;

        if (value >= (max - (thr*(max-min))) || value <= (min + (thr*(max-min)))) {
            sb.append("  Calibration offset\n\r");
            clipCount++;
        }

        int layerIndex = 1;

        for (Layer layer : target.getLayerList()){

            min = layer.getMinAD();
            max = layer.getMaxAD();
            value  = layer.getArealDensity();

            if (value >= (max - (thr*(max-min))) || value <= (min + (thr*(max-min)))) {
                sb.append("  Layer " + layerIndex + " areal density \n\r");
                clipCount++;
            }

            if (layer.getElementList().size()>1) {

                for (Element element : layer.getElementList()) {

                    min = element.getMin_ratio();
                    max = element.getMax_ratio();
                    value = element.getRatio();

                    if (value >= (max - (thr*(max-min))) || value <= (min + (thr*(max-min)))) {
                        sb.append("  Layer " + layerIndex + " " + element.getName() + " ratio \n\r");
                        clipCount++;
                    }
                }
            }

            layerIndex++;
        }

        String result = "No parameters clipping.";

        if (clipCount > 0) {
            result = sb.toString();
        }

        return result;
    }
}
