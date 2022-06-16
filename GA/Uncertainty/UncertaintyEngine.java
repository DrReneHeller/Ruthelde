package GA.Uncertainty;

import GA.Individual;
import IBA.Simulator.SpectrumSimulator;
import Target.Target;
import java.util.LinkedList;

public class UncertaintyEngine {

    public boolean running;
    public UncertaintyInput input;
    private SpectrumSimulator ss;
    public LinkedList<UncertaintyDataEntry> data;
    private final UncertaintyInputWindow inputWindow;
    private final UncertaintyOutputWindow outputWindow;
    private int fitCounter, spectrumCounter, parameterCounter;
    private Target target;

    public UncertaintyEngine() {

        input        = new UncertaintyInput()            ;
        inputWindow  = new UncertaintyInputWindow(input) ;
        outputWindow = new UncertaintyOutputWindow()     ;
        running      = false                             ;
    }

    public void initialize(SpectrumSimulator ss){

        this.ss          = ss.getDeepCopy();
        this.target      = ss.getTarget().getDeepCopy();
        data             = new LinkedList<>();
        outputWindow.setData(data);
        fitCounter       = 0;
        spectrumCounter  = 0;
        parameterCounter = 0;
        shuffleSetup();
        calcArtificialSpectrum();
        outputWindow.clear();
    }

    public void prepareNextSimulation(int numBins){

        fitCounter++;

        if (fitCounter == input.numberOfFits) {

            fitCounter = 0;
            spectrumCounter++;

            if (spectrumCounter == input.numberOfSpectra) {

                spectrumCounter = 0;
                shuffleSetup();
                parameterCounter++;
            }

            calcArtificialSpectrum();
        }

        //addDataEntry();
        ss.getTarget().randomize(1.0d);

        double min, max, val;

        min = ss.getExperimentalSetup().getMinCharge();
        max = ss.getExperimentalSetup().getMaxCharge();
        val = min + Math.random() * (max - min);
        ss.getExperimentalSetup().setCharge(val);

        min = ss.getExperimentalSetup().getMinCharge();
        max = ss.getExperimentalSetup().getMaxCharge();
        val = min + Math.random() * (max - min);
        ss.setCharge(val);

        min = ss.getDetectorCalibration().getFactorMin();
        max = ss.getDetectorCalibration().getFactorMax();
        val = min + Math.random() * (max - min);
        ss.getDetectorSetup().setCalibrationFactor(val);

        min = ss.getDetectorCalibration().getOffsetMin();
        max = ss.getDetectorCalibration().getOffsetMax();
        val = min + Math.random() * (max - min);
        ss.getDetectorSetup().setCalibrationOffset(val);
    }

    private void shuffleSetup(){

        double charge = input.q_min + Math.random() * (input.q_max - input.q_min);
        ss.getExperimentalSetup().setMinCharge((1.0f - input.q_var/100.f)*charge);
        ss.getExperimentalSetup().setMaxCharge((1.0f + input.q_var/100.f)*charge);
        ss.getExperimentalSetup().setCharge(charge);

        double res = input.dE_min + Math.random() * (input.dE_max - input.dE_min);
        ss.getDetectorSetup().setMinRes((1.0f - input.dE_var /100.f)*res);
        ss.getDetectorSetup().setMaxRes((1.0f + input.dE_var /100.f)*res);
        ss.getDetectorSetup().setResolution(res);

        double E0 = input.E0_min + Math.random() * (input.E0_max - input.E0_min);
        ss.getExperimentalSetup().setE0(E0);

        double alpha = input.alpha_min + Math.random() * (input.alpha_max - input.alpha_min);
        ss.getExperimentalSetup().setAlpha(alpha);

        double theta = input.theta_min + Math.random() * (input.theta_max - input.theta_min);
        ss.getExperimentalSetup().setTheta(theta);
    }

    public SpectrumSimulator getSpectrumSimulator(){
        return ss;
    }

    public void updateOutput(Individual individual, int numBins){

        UncertaintyDataEntry dataEntry = new UncertaintyDataEntry(parameterCounter, spectrumCounter, fitCounter);

        double q_set   = ss.getCharge()                        ;
        double res_set = ss.getDetectorSetup().getResolution() ;
        double alpha   = ss.getExperimentalSetup().getAlpha()  ;
        double theta   = ss.getExperimentalSetup().getTheta()  ;
        double E0      = ss.getExperimentalSetup().getE0()     ;

        dataEntry.setInputParameter(q_set, res_set, alpha, theta, E0);

        dataEntry.target    = individual.getTarget().getDeepCopy()      ;
        dataEntry.calFactor = individual.getCalibrationFactor()/numBins ;
        dataEntry.calOffset = individual.getCalibrationOffset()         ;
        dataEntry.res_fit   = individual.getResolution()                ;
        dataEntry.q_fit     = individual.getCharge()                    ;

        data.add(dataEntry);
        outputWindow.update(input);
    }

    public void showInputWindow(){
        inputWindow.setVisible(true);
    }

    public void showPlotWindow(){
        outputWindow.setVisible(true);
    }

    private void calcArtificialSpectrum(){

        ss.setTarget(target.getDeepCopy());

        double[] simulatedSpectrum = ss.simulate().getSimulatedSpectrum();

        int length = simulatedSpectrum.length;
        double integral = 0.0f;

        //integrate spectrum
        for (int i=0; i<length; i++){  integral += simulatedSpectrum[i]; }

        //normalize spectrum integral to 1
        for (int i=0; i<length; i++){  simulatedSpectrum[i] /= integral; }

        //generate width-array
        double[] w = new double[length];
        double sum = 0.0f;

        for (int i=0; i<length; i++){

            sum += simulatedSpectrum[i];
            w[i] = sum;
        }

        //Fill artificial spectrum with events
        double[] artificialSpectrum = new double[length];

        for (long i=0; i<integral; i++){

            double r = Math.random();
            int index = 0;
            while (r > w[index]) index++;
            artificialSpectrum[index]++;
        }

        Target newTarget = ss.getTarget();
        newTarget.randomize(1.0d);
        ss.setTarget(newTarget);
        ss.setExperimentalSpectrum(artificialSpectrum);
    }
}
