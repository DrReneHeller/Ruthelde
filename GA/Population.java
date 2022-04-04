package GA;

import IBA.Simulator.SpectrumSimulator;
import java.util.LinkedList;

public class Population {

    private final LinkedList<Individual> individualList;

    public Population(SpectrumSimulator spectrumSimulator, int size){

        individualList = new LinkedList<>();
        for (int i = 0; i< size; i++){ individualList.add(new Individual(spectrumSimulator)); }
    }

    public LinkedList<Individual> getIndividualList(){
        return individualList;
    }

    public int getBestFitnessIndex(){

        int result = 0;
        int index  = 0;

        for (Individual individual : individualList){

            if (individual.getFitness() > individualList.get(result).getFitness()){
                result = index;
            }
            index++;
        }

        return result;
    }

    public double getAverageFitness(){

        double result = 0;
        for (Individual individual : individualList){ result += individual.getFitness(); }
        result /= individualList.size();
        return result;
    }
}


