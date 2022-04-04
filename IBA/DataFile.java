package IBA;

import GA.Input.*;
import IBA.CalculationSetup.CalculationSetup;
import IBA.Detector.DetectorSetup;
import IBA.ExperimentalSetup.ExperimentalSetup;
import Main.WindowPositions;
import Target.Target;
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
