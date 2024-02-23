package com.ruthelde.IBA.Simulator;

import com.ruthelde.IBA.CalculationSetup.*;
import com.ruthelde.IBA.Detector.*;
import com.ruthelde.IBA.ExperimentalSetup.ExperimentalSetup;
import com.ruthelde.IBA.Kinematics.KinematicsCalculator;
import com.ruthelde.Stopping.*;
import com.ruthelde.Target.*;
import mr.go.sgfilter.SGFilter;
import java.util.LinkedList;

public class SpectrumSimulator {

    private final static int DEFAULT_NUMBER_OF_CHANNELS = 1024;

    public  double[]            experimentalSpectrum ;
    public  double[]            smoothedSpectrum     ;
    private ExperimentalSetup   experimentalSetup    ;
    private DetectorSetup       detectorSetup        ;
    private Target              target               ;
    private Target              foil                 ;
    private CalculationSetup    calculationSetup     ;
    private int                 numberOfChannels     ;
    private StoppingCalculator  stoppingCalculator   ;
    private SimulationData      simulationData       ;
    private LinkedList<IsotopeFitData>  isotopeList  ;
    private double[] energy                          ;
    private double[] simulatedSpectrum               ;
    private int startChannel, stopChannel            ;
    private double   LFF                             ;

    private StoppingParaFile stoppingParaFile;

    double     S[][] ;
    double     EMax, EMin, dE;

    private final double E_cutoff  = 50.0 ; //eV   //TODO: Implement into calculationSetup
    private final int    NUM_STEPS = 50    ;       //TODO: Implement into calculationSetup

    //--------------------- Constructor ------------------------------------------------------------------------------//

    public SpectrumSimulator(ExperimentalSetup experimentalSetup, DetectorSetup detectorSetup, Target target,
                             Target foil, CalculationSetup calculationSetup, StoppingParaFile stoppingParaFile){

        numberOfChannels     = DEFAULT_NUMBER_OF_CHANNELS   ;
        experimentalSpectrum = new double[numberOfChannels] ;
        smoothedSpectrum     = new double[numberOfChannels] ;

        this.experimentalSetup  = experimentalSetup  ;
        this.detectorSetup      = detectorSetup      ;
        this.foil               = foil               ;
        this.calculationSetup   = calculationSetup   ;
        this.stoppingParaFile   = stoppingParaFile   ;

        stoppingCalculator = new StoppingCalculator(stoppingParaFile);

        simulationData     = new SimulationData()    ;

        simulationData.setNumberOfChannels(numberOfChannels);
        isotopeList = new LinkedList<>();

        setTarget(target);
        simulateSpectrum();
    }


    //--------------------- Getter / Setter --------------------------------------------------------------------------//

    public double getCharge(){
        return experimentalSetup.getCharge();
    }

    public void setCharge(double charge){
        experimentalSetup.setCharge(charge);
    }

    public void setTarget(Target target) {

        this.target = target;

        int numberOfLayers = target.getLayerList().size();

        S          = new double[numberOfLayers][NUM_STEPS+2] ;
        EMax       = experimentalSetup.getE0()               ;
        EMin       = E_cutoff                                ;
        dE         = (EMax - EMin) / NUM_STEPS               ;

        preCalcStoppingValues(experimentalSetup, S, EMin, dE);
    }

    public Target getTarget() {return target;}

    public void setFoil(Target foil) {
        this.foil = foil;
    }

    public void setCalculationSetup(CalculationSetup calculationSetup) {
        this.calculationSetup = calculationSetup;
    }

    public void setExperimentalSpectrum(double[] experimentalSpectrum) {

        if (experimentalSpectrum.length > 0) {
            this.experimentalSpectrum = experimentalSpectrum;
            this.numberOfChannels = experimentalSpectrum.length;
            simulationData.setNumberOfChannels(numberOfChannels);
            smoothedSpectrum = new double[experimentalSpectrum.length];
            LFF = calcLFF();
        }
    }

    public DetectorCalibration getDetectorCalibration(){
        return detectorSetup.getCalibration();
    }

    public void setDetectorCalibration(DetectorCalibration detectorCalibration){
        this.detectorSetup.setCalibrationFactor(detectorCalibration.getFactor());
        this.detectorSetup.setCalibrationOffset(detectorCalibration.getOffset());
    }

    public void setStartChannel(int startChannel) {
        this.startChannel = startChannel;
    }

    public void setStopChannel(int stopChannel) {
        this.stopChannel = stopChannel;
    }

    public DetectorSetup getDetectorSetup(){return detectorSetup;}

    public CalculationSetup getCalculationSetup() {return calculationSetup;}

    public ExperimentalSetup getExperimentalSetup() {return experimentalSetup;}

    //--------------------- Simulation -------------------------------------------------------------------------------//

    public SimulationData simulate(){

        simulateSpectrum();
        return simulationData;
    }

    public double[] getSimulatedSpectrum(){

        return simulationData.getSimulatedSpectrum();
    }

    private void simulateSpectrum() {

        long millis = System.currentTimeMillis();

        //Build a list of all spectra (isotopes) we have to simulate
        int numberOfLayers = target.getLayerList().size();

        if (calculationSetup.isSimulateIsotopes()) {
            isotopeList = generateIsotopeList(numberOfLayers);
        } else {
            isotopeList = generateSimplifiedIsotopeList(numberOfLayers);
        }

        //Simulate all spectra
        int tempZ = 0;
        for (IsotopeFitData isotopeFitData : isotopeList) {

            if (isotopeFitData.Z != tempZ) {tempZ = isotopeFitData.Z;}

            try {
                simulateIsotopeSpectrum(isotopeFitData, S, EMin, dE);
            } catch (Exception e){System.out.println("Simulation error: "); e.printStackTrace();}
        }

        //Generate sum spectrum (simulated spectrum)
        simulatedSpectrum = new double[numberOfChannels];

        for (IsotopeFitData isotopeFitData : isotopeList) {
            for (int j=0; j<numberOfLayers; j++) {
                for (int k=0; k<numberOfChannels; k++) {
                    simulatedSpectrum[k] += isotopeFitData.spectra[j][k];
                }
            }
        }

        millis = System.currentTimeMillis() - millis;

        //Scale experimental spectrum according to detector calibration
        energy = new double[numberOfChannels];
        DetectorCalibration detectorCalibration = detectorSetup.getCalibration();
        for (int j=0; j<numberOfChannels; j++) {
            energy[j] = detectorCalibration.getFactor()*j + detectorCalibration.getOffset();
        }

        simulationData.setEnergy(energy)                             ;
        simulationData.setIsotopeFitData(isotopeList)                ;
        simulationData.setSimulatedSpectrum(simulatedSpectrum)       ;
        simulationData.setExperimentalSpectrum(experimentalSpectrum) ;
        simulationData.setSimulationTime(millis)                     ;
        simulationData.setFitness(calFitness())                      ;
    }


    private void simulateIsotopeSpectrum(IsotopeFitData isotopeFitData, double S[][], double EMin, double dE) {

        int     layerIndex       = 0     ;
        boolean interfaceReached = false ;
        boolean stopSimulation   = false ;

        Layer layer;
        Projectile projectile;
        projectile = experimentalSetup.getProjectile();

        double E0    = experimentalSetup.getE0()     ;
        double theta = experimentalSetup.getTheta()  ;
        double alpha = experimentalSetup.getAlpha()  ;
        double beta  = experimentalSetup.getBeta()   ;
        double Q     = experimentalSetup.getCharge() ;

        double a     = detectorSetup.getCalibration().getFactor() ;
        double b     = detectorSetup.getCalibration().getOffset()           ;
        double omega = detectorSetup.getSolidAngle()                        ;

        int    Z2 = isotopeFitData.Z                          ;
        double M2 = isotopeFitData.M                          ;
        double c  = isotopeFitData.concentrations[layerIndex] ;

        double dx = 5.0; //Step width for ion penetration. If a layer is thinner it is recognized and handled separately.

        //Set initial parameter when the ion reaches the target's surface
        double E = E0;
        double str2_F = Math.pow(experimentalSetup.getDeltaE0() / 2.355, 2);

        //Calculate ion energy after scattering at the surface
        projectile.setE(E);
        double K = KinematicsCalculator.getBSKFactorA(projectile, M2, theta);
        double E_det = K * E;

        //Calculate energy and corresponding channel of the back scattered ion at the detector
        E_det          = E_det - calculateEnergyLossThroughFoil() ;
        double channel = Math.floor((E_det - b) / a)              ;
        double E_det_b = (a * channel + b)                        ;

        //Set start conditions for brick calculations
        layer                            = target.getLayerList().get(layerIndex)         ;
        double thicknessConversionFactor = layer.getThicknessConversionFactor() * 1000.0 ;
        double sumLayerThicknesses       = layer.getThickness()                          ;
        double S_brick_out               = 0.0                                           ;
        double depth                     = 0.0                                           ;
        double brickThicknesses[]        = new double[numberOfChannels]                  ;
        int    brickLayerIndexes[]       = new    int[numberOfChannels]                  ;
        int    brickIndex                = 0                                             ;
        double targetThickness           = target.getTotalThickness()                    ;

        //Stepwise construct bricks and their contribution to the spectrum
        while (E_det > E_cutoff && depth < targetThickness && !stopSimulation) {

            //Set start value for the brick
            double n = 0.0;
            double E_ion_front = E;

            //Calculate the brick's stopping cross section for incoming path
            projectile.setE(E_ion_front);
            double S_brick_in = calculateStopping(projectile, layer, layerIndex, EMin, dE, S);
            S_brick_in = S_brick_in / thicknessConversionFactor;

            double E_det_prev = E_det;

            //Find the brick's end
            while (E_det > E_det_b) {

                // Move one step (dx) deeper into the current brick
                n++;
                E = E_ion_front - n * S_brick_in * dx / Math.cos(Math.toRadians(alpha));
                projectile.setE(K*E);
                S_brick_out = calculateStopping(projectile, layer, layerIndex, EMin, dE, S);
                S_brick_out = S_brick_out / thicknessConversionFactor;

                //Calculate energy of the back scattered ion at the front of the current brick
                E_det_prev = E_det; //For calculation of the exact brick thickness after stop condition is reached
                E_det = K * E - S_brick_out * n * dx / Math.cos(Math.toRadians(beta));

                //Calculate energy of the back scattered ion at the surface
                for (int j = brickIndex-1; j>=0; j--) {
                    projectile.setE(E_det);
                    Layer topLayer = target.getLayerList().get(brickLayerIndexes[j]);
                    double thicknessConversionFactor1 = topLayer.getThicknessConversionFactor() * 1000.0;
                    double Sj = calculateStopping(projectile, topLayer, brickLayerIndexes[j], EMin, dE, S);
                    Sj = Sj / thicknessConversionFactor1;
                    E_det -= brickThicknesses[j] * Sj / Math.cos(Math.toRadians(beta));
                }

                //Calculate energy of the back scattered ion at the detector
                E_det = E_det - calculateEnergyLossThroughFoil();
            }

            //Calculate brick thickness
            double cf = (E_det -  E_det_b) / (E_det - E_det_prev);
            n -= cf;
            double brickThickness = n * dx;

            //Check if we have passed the interface to next layer
            double oldDepth = depth;
            depth += brickThickness;

            if (depth > sumLayerThicknesses && layerIndex < target.getLayerList().size()-1) {
                interfaceReached = true;
                brickThickness = sumLayerThicknesses - oldDepth;
                depth = sumLayerThicknesses;
            }

            brickThicknesses[brickIndex]  = brickThickness ;
            brickLayerIndexes[brickIndex] = layerIndex     ;

            //Calculate ion energy at the brick's back side (= next brick's front side)
            E = E_ion_front - S_brick_in * brickThickness / Math.cos(Math.toRadians(alpha));

            //Calculate brick's contribution to the spectrum
            double AD = brickThickness / thicknessConversionFactor * 1000.0;
            projectile.setE(E);
            ScreeningMode screeningMode = calculationSetup.getScreeningMode();
            double sigma = KinematicsCalculator.getBSCrossSection(projectile, Z2, M2, theta, screeningMode, 0);
            double Y_brick = 6.24E-3 * Q * AD * sigma * omega * c / Math.cos(Math.toRadians(alpha));

            if (channel < numberOfChannels-1 && channel >= 0) {

                isotopeFitData.spectra[layerIndex][(int) channel] = Y_brick;

                //Calculate current brick's straggling contribution
                str2_F = calculateStraggling(E_ion_front, E, projectile, str2_F, layer, layerIndex,
                        Z2, brickThickness, EMin, dE, S, thicknessConversionFactor, K, brickThicknesses, brickIndex,
                        brickLayerIndexes, isotopeFitData, channel);
            }

            //Set next brick's detector energies
            if (interfaceReached) {

                //Calculate energy of the back scattered ion at the front of the current brick
                E_det = K * E - S_brick_out * brickThickness / Math.cos(Math.toRadians(beta));

                //Calculate energy of the back scattered ion at the surface
                for (int j = brickIndex-1; j>=0; j--) {
                    projectile.setE(E_det);
                    Layer topLayer = target.getLayerList().get(brickLayerIndexes[j]);
                    double thicknessConversionFactor2 = topLayer.getThicknessConversionFactor() * 1000.0;
                    double Sj = calculateStopping(projectile, topLayer, brickLayerIndexes[j], EMin, dE, S);
                    Sj = Sj / thicknessConversionFactor2;
                    E_det -= brickThicknesses[j] * Sj / Math.cos(Math.toRadians(beta));
                }

                //Calculate energy of the back scattered ion at the detector
                E_det = E_det - calculateEnergyLossThroughFoil();

                layerIndex++;
                layer = target.getLayerList().get(layerIndex);
                thicknessConversionFactor = layer.getThicknessConversionFactor() * 1000.0;
                sumLayerThicknesses += layer.getThickness();
                c = isotopeFitData.concentrations[layerIndex];
                interfaceReached = false;

                //Look if this was the last layer containing the current isotope
                stopSimulation = true;
                for (int l=layerIndex; l<isotopeFitData.concentrations.length; l++) {
                    if (isotopeFitData.concentrations[l] != 0.0) {
                        stopSimulation = false;
                        break;
                    }
                }

            } else {

                E_det = E_det_b;
                E_det_b = E_det_b - a;

                channel--;
            }

            brickIndex++;
        }

        //Make convolution of simulated spectrum with straggling and detector resolution
        if (channel < numberOfChannels-1) convolveSpectrum(detectorSetup, isotopeFitData, a, b, channel);

        //Reset the projectile's initial energy
        projectile.setE(E0);
    }

    private double calculateEnergyLossThroughFoil() {
        double result = 0.0;
        //TODO: Implement proper energy loss through foil
        return result;
    }

    private double calculateStopping(Projectile projectile, Layer layer,
                                     int layerIndex, double EMin, double dE, double S[][]) {
        double result;

        StoppingCalculationMode sm = calculationSetup.getStoppingPowerCalculationMode();
        CompoundCalculationMode cm = calculationSetup.getCompoundCalculationMode();

        if (calculationSetup.isUseLookUpTable()) {
            double energyIndex = (projectile.getE()-EMin)/dE ;
            int    lowerIndex  = (int)energyIndex            ;
            int    upperIndex  = lowerIndex + 1              ;
            double E_low       = EMin + lowerIndex * dE      ;
            double E_high      = EMin + upperIndex * dE      ;
            double E           = projectile.getE()           ;
            double S_low       = S[layerIndex][lowerIndex]   ;
            double S_high      = S[layerIndex][upperIndex]   ;

            double S_inter = (S_high - S_low) * (E - E_low) / (E_high - E_low) + S_low;
            result = S_inter;
        } else {
            result= stoppingCalculator.getStoppingPower(projectile, layer, sm, cm, 2);
        }

        return result;
    }

    private double calculateStraggling(double E_ion_front, double E,
                                     Projectile projectile, double str2_F, Layer layer, int layerIndex, int Z2,
                                     double brickThickness, double EMin, double dE, double S[][],
                                     double thicknessConversionFactor, double K, double brickThicknesses[],
                                     int brickIndex, int brickLayerIndexes[], IsotopeFitData isotopeFitData,
                                     double channel) {

        if (calculationSetup.getStragglingMode() != StragglingMode.NONE) {

            double Ef = E_ion_front;
            double Eb = E;

            projectile.setE(Ef);
            double Si = calculateStopping(projectile, layer, layerIndex, EMin, dE, S);
            projectile.setE(Eb);
            double Sf = calculateStopping(projectile, layer, layerIndex, EMin, dE, S);
            double str2_Bohr = 0.26 * Math.pow(projectile.getZ(), 2) * Z2 * brickThickness / thicknessConversionFactor;
            double str2_B = Math.pow(Sf / Si, 2) * str2_F + str2_Bohr;
            str2_F = str2_B;

            double str2_B_prime = K * K * str2_B;
            double Eb_prime = K * Eb;
            projectile.setE(Eb_prime);
            Si  = calculateStopping(projectile, layer, layerIndex, EMin, dE, S);
            Si /= thicknessConversionFactor;

            double Ef_prime = Eb_prime - Si * brickThickness;
            projectile.setE(Ef_prime);
            Sf  = calculateStopping(projectile, layer, layerIndex, EMin, dE, S);
            Sf /= thicknessConversionFactor;
            double str2_F_prime = Math.pow(Sf / Si, 2) * str2_B_prime + str2_Bohr;

            for (int j = brickIndex - 1; j >= 0; j--) {
                Eb_prime = Ef_prime;
                projectile.setE(Eb_prime);
                Layer topLayer1 = target.getLayerList().get(brickLayerIndexes[j]);
                double thicknessConversionFactor2 = topLayer1.getThicknessConversionFactor() * 1000.0;
                Si  = calculateStopping(projectile, topLayer1, brickLayerIndexes[j], EMin, dE, S);
                Si /= thicknessConversionFactor2;
                Ef_prime = Eb_prime - Si * brickThicknesses[j];
                projectile.setE(Ef_prime);
                Sf  = calculateStopping(projectile, topLayer1, brickLayerIndexes[j], EMin, dE, S);
                Sf /= thicknessConversionFactor2;
                str2_Bohr  = 0.26 * Math.pow(projectile.getZ(), 2) * Z2 * brickThicknesses[j];
                str2_Bohr /= thicknessConversionFactor2;
                str2_F_prime = Math.pow(Sf / Si, 2) * str2_F_prime + str2_Bohr;
            }

            isotopeFitData.straggling[(int) channel] = str2_F_prime;
        } else {
            isotopeFitData.straggling[(int) channel] = 0.0;
        }

        return str2_F;
    }

    private void convolveSpectrum(DetectorSetup detectorSetup, IsotopeFitData isotopeFitData, double a,
                                  double b, double channel) {

        double detRes                = detectorSetup.getResolution()    ;
        double str2_det              = Math.pow(detRes / 2.355,2)       ;
        int    layerIndex            = 0                                ;
        int    sx                    = isotopeFitData.spectra.length ;
        double convolutedSpectra[][] = new double[sx][numberOfChannels] ;

        //Fill the none simulated part of the simulate isotope spectrum with last straggling value
        for (int i=(int)channel; i>=0; i--) {
            isotopeFitData.straggling[i] = isotopeFitData.straggling[(int)channel+1];
        }

        //Do convolution
        for (Layer layer : target.getLayerList()) {

            for (int ch=0; ch<numberOfChannels; ch++) {

                double str2_total = str2_det + isotopeFitData.straggling[ch]    ;
                double fact       = 1.0 / Math.sqrt(2.0*Math.PI*str2_total)     ;
                double Ei         = a * ch + b                                  ;
                double Si         = 0.0                                         ;

                for (int j=0; j<numberOfChannels; j++) {

                    double Nj = isotopeFitData.spectra[layerIndex][j];
                    double Sjj = 0.0;

                    if (Nj > 0) {
                        double Ej = a * j + b;
                        double argument = -0.5 * (Ei - Ej) * (Ei - Ej) / (str2_total);
                        if (argument > -5.0) { Sjj = Nj * Math.exp(argument); }
                    }

                    Si += Sjj;
                }
                convolutedSpectra[layerIndex][ch] = fact*Si*a;
            }
            layerIndex++;
        }

        //Replace original simulated spectrum with the convoluted one
        layerIndex = 0;
        for (Layer layer : target.getLayerList()) {
            for (int ch=0; ch<numberOfChannels; ch++) {
                isotopeFitData.spectra[layerIndex][ch] = convolutedSpectra[layerIndex][ch];
            }
            layerIndex++;
        }
    }

    private void preCalcStoppingValues(ExperimentalSetup experimentalSetup, double S[][], double EMin, double dE) {

        Projectile projectile = experimentalSetup.getProjectile() ;
        int        layerIndex = 0                                 ;
        double     EMax       = projectile.getE()                 ;

        StoppingCalculationMode sm = calculationSetup.getStoppingPowerCalculationMode() ;
        CompoundCalculationMode cm = calculationSetup.getCompoundCalculationMode()      ;

        for (Layer layer : target.getLayerList()) {
            for (int i=0; i<NUM_STEPS+2; i++) {
                double E = EMin + i*dE;
                projectile.setE(E);
                S[layerIndex][i] = stoppingCalculator.getStoppingPower(projectile, layer, sm, cm, 2);
            }
            layerIndex++;
        }

        projectile.setE(EMax);
    }

    private LinkedList<IsotopeFitData> generateIsotopeList(int numberOfLayers) {

        LinkedList<IsotopeFitData> isotopeList = new LinkedList<IsotopeFitData>();
        boolean addIt;
        int layerIndex = 0;

        for (Layer layer : target.getLayerList()) {
            for (Element element : layer.getElementList()) {
                int Z = element.getAtomicNumber();
                for (Isotope isotope : element.getIsotopeList()) {
                    double M = isotope.getMass();
                    double c = layer.getIsotopeContribution(Z, M);
                    addIt = true;
                    for (IsotopeFitData isotopeFitData : isotopeList) {
                        if (isotopeFitData.Z == Z && isotopeFitData.M == M) {
                            addIt = false;
                            isotopeFitData.concentrations[layerIndex] = c;
                            break;
                        }
                    }
                    if (addIt) {
                        double concentrations[] = new double[numberOfLayers];
                        concentrations[layerIndex] = c;
                        isotopeList.add(new IsotopeFitData(Z,M,concentrations, numberOfChannels));
                    }
                }
            }
            layerIndex++;
        }
        return isotopeList;
    }

    private LinkedList<IsotopeFitData> generateSimplifiedIsotopeList(int numberOfLayers) {

        LinkedList<IsotopeFitData> isotopeList = new LinkedList<IsotopeFitData>();
        boolean addIt;
        int layerIndex = 0;

        for (Layer layer : target.getLayerList()) {
            for (Element element : layer.getElementList()) {
                int Z = element.getAtomicNumber();

                double M = element.getAverageMass();
                double c = layer.getElementContribution(Z);
                addIt = true;
                for (IsotopeFitData isotopeFitData : isotopeList) {
                    if (isotopeFitData.Z == Z) {
                        addIt = false;
                        isotopeFitData.concentrations[layerIndex] = c;
                        break;
                    }
                }
                if (addIt) {
                    double concentrations[] = new double[numberOfLayers];
                    concentrations[layerIndex] = c;
                    isotopeList.add(new IsotopeFitData(Z,M,concentrations, numberOfChannels));
                }
            }
            layerIndex++;
        }
        return isotopeList;
    }

    private double calcLFF(){

        double temp = detectorSetup.getResolution() / detectorSetup.getCalibration().getFactor();
        temp = Math.floor(Math.floor(temp/2.0f)) * 2.0f +1.0f;
        int filterLength = (int) temp;
        if (filterLength < 4) filterLength = 4;

        SGFilter sgFilter = new SGFilter(filterLength/2, filterLength/2);
        double coeff[] = SGFilter.computeSGCoefficients(filterLength/2, filterLength/2, 3);
        smoothedSpectrum = sgFilter.smooth(experimentalSpectrum, coeff);

        double result = 0.0f;
        for (int i=startChannel; i<stopChannel; i++){
            result += Math.pow(smoothedSpectrum[i] - experimentalSpectrum[i],2);
        }
        return result;
    }

    private double calFitness(){

        double sigma2 = 0.0f;

        for (int i=startChannel; i<stopChannel; i++){
            sigma2 += Math.pow(experimentalSpectrum[i]-simulationData.getSimulatedSpectrum()[i],2);
        }

        //sigma2 = 100.0f / (Math.log(sigma2) - Math.log(LFF));
        //sigma2 = LFF / sigma2 * 100.0f;
        sigma2 = Math.log(LFF) / Math.log(sigma2) * 100.0f;
        return sigma2;
    }

    public SpectrumSimulator getDeepCopy(){

        ExperimentalSetup experimentalSetup = this.experimentalSetup.getDeepCopy();
        DetectorSetup detectorSetup = this.detectorSetup.getDeepCopy();
        Target target = this.target.getDeepCopy();
        Target foil = this.foil.getDeepCopy();
        CalculationSetup calculationSetup = this.calculationSetup.getDeepCopy();

        SpectrumSimulator spSim = new SpectrumSimulator(experimentalSetup, detectorSetup, target, foil, calculationSetup, stoppingParaFile.getDeepCopy());
        double[] exp = new double[this.experimentalSpectrum.length];
        System.arraycopy(this.experimentalSpectrum, 0, exp, 0, this.experimentalSpectrum.length);
        spSim.setStartChannel(this.startChannel);
        spSim.setStopChannel(this.stopChannel);
        spSim.setExperimentalSpectrum(exp);

        return spSim;

    }

}

