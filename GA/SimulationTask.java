package GA;

public class SimulationTask implements Runnable {

    private Individual individual;

    public SimulationTask(Individual individual){
        this.individual = individual;
    }

    public void run() {
        try {
            individual.simulate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
