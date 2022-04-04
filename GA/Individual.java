package GA;

import Helper.Helper;
import IBA.Detector.DetectorCalibration;
import IBA.Simulator.SimulationData;
import IBA.Simulator.SpectrumSimulator;
import Target.Element;
import Target.Layer;
import Target.Target;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Individual {

    private Target target;
    private final DetectorCalibration detectorCalibration;
    private double charge, resolution;
    private double fitness;
    private SimulationData simulationData;
    private final SpectrumSimulator spectrumSimulator;

    public Individual(SpectrumSimulator spectrumSimulator){

        this.spectrumSimulator = spectrumSimulator;

        Random rand = new Random();

        this.target = spectrumSimulator.getTarget().getDeepCopy();
        this.target.randomize();

        this.detectorCalibration = spectrumSimulator.getDetectorCalibration().getDeepCopy();
        this.detectorCalibration.randomize();

        this.charge     = spectrumSimulator.getExperimentalSetup().getMinCharge() + rand.nextDouble() * (spectrumSimulator.getExperimentalSetup().getMaxCharge() - spectrumSimulator.getExperimentalSetup().getMinCharge());
        this.resolution = spectrumSimulator.getDetectorSetup().getMinRes() + rand.nextDouble() * (spectrumSimulator.getDetectorSetup().getMaxRes() - spectrumSimulator.getDetectorSetup().getMinRes());
    }

    public SimulationData simulate(){

        spectrumSimulator.setTarget(target);
        spectrumSimulator.setDetectorCalibration(detectorCalibration);

        double oldCharge = spectrumSimulator.getCharge();
        double oldResolution = spectrumSimulator.getDetectorSetup().getResolution();
        spectrumSimulator.setCharge(charge);
        spectrumSimulator.getDetectorSetup().setResolution(resolution);

        //SimulationData simulationData = spectrumSimulator.simulate();
        simulationData = spectrumSimulator.simulate();
        fitness = simulationData.getFitness();

        spectrumSimulator.setCharge(oldCharge);
        spectrumSimulator.getDetectorSetup().setResolution(oldResolution);

        return simulationData;
    }

    public void setSimulationData(SimulationData simulationData){
        this.simulationData = simulationData;
    }

    public SimulationData getSimulationData(){return simulationData;}

    public double getFitness(){
        return fitness;
    }

    public void setFitness(double fitness){
        this.fitness = fitness;
    }

    public void setTarget(Target target){
        this.target = target;
    }

    public void setCharge(double charge){
        this.charge = charge;
    }

    public double getCharge() {
        return charge;
    }

    public void setResolution(double resolution){this.resolution = resolution;}

    public double getResolution(){return resolution;}

    public void setCalibrationFactor(double calibrationFactor){
        detectorCalibration.setFactor(calibrationFactor);
    }

    public double getCalibrationFactor(){
        return detectorCalibration.getFactor();
    }

    public void setCalibrationOffset(double calibrationOffset){
        detectorCalibration.setOffset(calibrationOffset);
    }

    public double getCalibrationOffset(){
        return detectorCalibration.getOffset();
    }

    public void setDetectorCalibration(DetectorCalibration detectorCalibration){
        this.detectorCalibration.setFactor(detectorCalibration.getFactor());
        this.detectorCalibration.setOffset(detectorCalibration.getOffset());
    }

    public Target getTarget(){
        return target;
    }

    public LinkedList<Gene> getGenes(){

        LinkedList<Gene> genes = new LinkedList<>();
        double min, max, val;

        //Charge
        min = spectrumSimulator.getExperimentalSetup().getMinCharge();
        max = spectrumSimulator.getExperimentalSetup().getMaxCharge();
        val = charge;
        genes.add(new Gene(min,max,val));

        //Calibration Factor
        min = spectrumSimulator.getDetectorCalibration().getFactorMin();
        max = spectrumSimulator.getDetectorCalibration().getFactorMax();
        val = detectorCalibration.getFactor();
        genes.add(new Gene(min,max,val));

        //Calibration Offset
        min = spectrumSimulator.getDetectorCalibration().getOffsetMin();
        max = spectrumSimulator.getDetectorCalibration().getOffsetMax();
        val = detectorCalibration.getOffset();
        genes.add(new Gene(min,max,val));

        //Detector Resolution
        min = spectrumSimulator.getDetectorSetup().getMinRes();
        max = spectrumSimulator.getDetectorSetup().getMaxRes();
        val = resolution;
        genes.add(new Gene(min,max,val));

        //Target Model
        for (Layer layer : target.getLayerList()){

            min = layer.getMinAD();
            max = layer.getMaxAD();
            val = layer.getArealDensity();
            genes.add(new Gene(min,max,val));

            for (Element element : layer.getElementList()){

                min = element.getMin_ratio();
                max = element.getMax_ratio();
                val = element.getRatio();
                genes.add(new Gene(min,max,val));
            }
        }

        return genes;
    }

    public void setGenes(List<Gene> genes){

        int geneIndex = 0;

        //Charge
        charge = genes.get(geneIndex).val;
        geneIndex++;

        //Calibration Factor
        detectorCalibration.setFactor(genes.get(geneIndex).val);
        geneIndex++;

        //Calibration Offset
        detectorCalibration.setOffset(genes.get(geneIndex).val);
        geneIndex++;

        //Detector Resolution
        resolution = genes.get(geneIndex).val;
        geneIndex++;

        //Target Model
        for (Layer layer : target.getLayerList()){

            layer.setArealDensity(genes.get(geneIndex).val);
            geneIndex++;

            for (Element element : layer.getElementList()){

                element.setRatio(genes.get(geneIndex).val);
                geneIndex++;
            }
        }
    }

    public void replace(Individual individual){

        setGenes(individual.getGenes());
        setFitness(individual.getFitness());
        setSimulationData(individual.getSimulationData().getDeepCopy());
    }

    public Individual getDeepCopy(){

        Individual result = new Individual(spectrumSimulator);

        result.setTarget(target.getDeepCopy());
        result.setCharge(charge);
        result.setResolution(resolution);
        result.setDetectorCalibration(detectorCalibration);
        result.setFitness(fitness);

        return result;
    }

    public void getInfo(StringBuilder sb, double binningFactor){

        sb.append("Charge \t = ").append(Helper.dblToDecStr(charge, 4)).append("\r\n");
        sb.append("Resolution \t = ").append(Helper.dblToDecStr(resolution, 4)).append("\r\n");
        sb.append("Cal.-Factor \t = ").append(Helper.dblToDecStr(detectorCalibration.getFactor()/binningFactor, 4)).append("\r\n");
        sb.append("Cal.-Offset \t = ").append(Helper.dblToDecStr(detectorCalibration.getOffset(), 4)).append("\r\n\n\r");

        Target temp = target.getDeepCopy();
        temp.getInfo(sb);
    }

}

