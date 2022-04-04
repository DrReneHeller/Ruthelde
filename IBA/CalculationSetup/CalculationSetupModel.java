package IBA.CalculationSetup;

import java.util.Observable;

public class CalculationSetupModel extends Observable {

    public final static String NOTIFICATION = "CalculationSetup";

    private CalculationSetup calculationSetup;

    public CalculationSetupModel(CalculationSetup calculationSetup) {
        this.calculationSetup = calculationSetup;
    }

    public CalculationSetup getCalculationSetup() {
        return calculationSetup;
    }

    public void setCalculationSetup(CalculationSetup calculationSetup) {
        this.calculationSetup = calculationSetup;
        setChanged();
        notifyObservers(NOTIFICATION);
    }
}
