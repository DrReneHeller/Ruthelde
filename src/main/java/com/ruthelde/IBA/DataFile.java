package com.ruthelde.IBA;

import com.ruthelde.GA.Input.*;
import com.ruthelde.IBA.CalculationSetup.CalculationSetup;
import com.ruthelde.IBA.Detector.DetectorSetup;
import com.ruthelde.IBA.ExperimentalSetup.ExperimentalSetup;
import com.ruthelde.Main.WindowPositions;
import com.ruthelde.Target.Target;
import java.io.Serializable;

public class DataFile implements Serializable {

    public Target target;
    public ExperimentalSetup experimentalSetup;
    public CalculationSetup calculationSetup;
    public DetectorSetup detectorSetup;
    public DEParameter deParameter;
    public double[] experimentalSpectrum;
    public String spectrumName;
    public WindowPositions windowPositions;

    public DataFile(Target target, ExperimentalSetup experimentalSetup, CalculationSetup calculationSetup,
                    DetectorSetup detectorSetup, DEParameter deParameter, double[] experimentalSpectrum, String spectrumName,
                    WindowPositions windowPositions){
        this.target = target;
        this.experimentalSetup = experimentalSetup;
        this.calculationSetup = calculationSetup;
        this.detectorSetup = detectorSetup;
        this.deParameter = deParameter;
        this.experimentalSpectrum = experimentalSpectrum;
        this.spectrumName = spectrumName;
        this.windowPositions = windowPositions;
    }
}
