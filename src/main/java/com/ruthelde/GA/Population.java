package com.ruthelde.GA;

import com.ruthelde.IBA.Simulator.SpectrumSimulator;
import java.util.LinkedList;

public class Population {

    private final LinkedList<Individual> individualList;

    public Population(SpectrumSimulator spectrumSimulator, int size){

        individualList = new LinkedList<>();

        for (int i = 0; i < size; i++){
            if (i < 3*size/4) {
                individualList.add(new Individual(spectrumSimulator.getDeepCopy(), 0.01d));
            } else{
                individualList.add(new Individual(spectrumSimulator.getDeepCopy(), 1.0d));
            }
        }

        for (Individual individual : individualList){
            individual.simulate();
        }
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


