package com.ruthelde.GA.Uncertainty;

import com.ruthelde.GA.Individual;
import com.ruthelde.IBA.Simulator.SpectrumSimulator;
import com.ruthelde.Target.Target;
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
    private double charge, dE, E0, alpha, theta;
    private double or_charge, or_dE, or_E0, or_alpha, or_theta;

    public UncertaintyEngine() {

        input        = new UncertaintyInput()            ;
        inputWindow  = new UncertaintyInputWindow(input) ;
        outputWindow = new UncertaintyOutputWindow()     ;
        running      = false                             ;
    }

    public void initialize(SpectrumSimulator ss){

        or_charge = ss.getCharge()                        ;
        or_dE     = ss.getDetectorSetup().getResolution() ;
        or_E0     = ss.getExperimentalSetup().getE0()     ;
        or_alpha  = ss.getExperimentalSetup().getAlpha()  ;
        or_theta  = ss.getExperimentalSetup().getTheta()  ;

        this.ss          = ss.getDeepCopy();
        this.target      = ss.getTarget().getDeepCopy();
        data             = new LinkedList<>();
        outputWindow.setData(data);
        fitCounter       = 0;
        spectrumCounter  = 0;
        parameterCounter = 0;
        shuffleSetup();
        calcArtificialSpectrum();
        ss.getExperimentalSetup().setE0(or_E0);
        ss.getExperimentalSetup().setAlpha(or_alpha);
        ss.getExperimentalSetup().setTheta(or_theta);
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

        //Randomize target
        ss.getTarget().randomize(1.0d);


        //Randomize exp. parameter
        double min, max, val;

        min = ss.getExperimentalSetup().getMinCharge();
        max = ss.getExperimentalSetup().getMaxCharge();
        val = min + Math.random() * (max - min);
        ss.getExperimentalSetup().setCharge(val);

        min = ss.getDetectorSetup().getMinRes();
        max = ss.getDetectorSetup().getMaxRes();
        val = min + Math.random() * (max - min);
        ss.getDetectorSetup().setResolution(val);

        min = ss.getDetectorCalibration().getFactorMin();
        max = ss.getDetectorCalibration().getFactorMax();
        val = min + Math.random() * (max - min);
        ss.getDetectorSetup().setCalibrationFactor(val);

        min = ss.getDetectorCalibration().getOffsetMin();
        max = ss.getDetectorCalibration().getOffsetMax();
        val = min + Math.random() * (max - min);
        ss.getDetectorSetup().setCalibrationOffset(val);

        ss.getExperimentalSetup().setE0(or_E0);
        ss.getExperimentalSetup().setAlpha(or_alpha);
        ss.getExperimentalSetup().setTheta(or_theta);
    }

    private void shuffleSetup(){

        charge = input.q_min + Math.random() * (input.q_max - input.q_min);
        ss.getExperimentalSetup().setMinCharge((1.0f - input.q_var/100.f)*charge);
        ss.getExperimentalSetup().setMaxCharge((1.0f + input.q_var/100.f)*charge);
        ss.getExperimentalSetup().setCharge(charge);
        //TODO: FIXE_ME

        dE = input.dE_min + Math.random() * (input.dE_max - input.dE_min);
        ss.getDetectorSetup().setMinRes((1.0f - input.dE_var /100.f)*dE);
        ss.getDetectorSetup().setMaxRes((1.0f + input.dE_var /100.f)*dE);
        ss.getDetectorSetup().setResolution(dE);
        //TODO: FIXE_ME

        E0 = input.E0_min + Math.random() * (input.E0_max - input.E0_min);
        ss.getExperimentalSetup().setE0(E0);

        alpha = input.alpha_min + Math.random() * (input.alpha_max - input.alpha_min);
        ss.getExperimentalSetup().setAlpha(alpha);

        theta = input.theta_min + Math.random() * (input.theta_max - input.theta_min);
        ss.getExperimentalSetup().setTheta(theta);
    }

    public SpectrumSimulator getSpectrumSimulator(){
        return ss;
    }

    public void updateOutput(Individual individual, int numBins){

        UncertaintyDataEntry dataEntry = new UncertaintyDataEntry(parameterCounter, spectrumCounter, fitCounter);
        dataEntry.setInputParameter(charge, dE, alpha, theta, E0);

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

        ss.setCharge(charge);
        ss.getDetectorSetup().setResolution(dE);
        ss.getExperimentalSetup().setE0(E0);
        ss.getExperimentalSetup().setAlpha(alpha);
        ss.getExperimentalSetup().setTheta(theta);

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
