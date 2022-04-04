package IBA.ExperimentalSetup;

import java.util.Observable;

public class ExperimentalSetupModel extends Observable {

    public final static String NOTIFICATION = "ExperimentalSetup";

    private ExperimentalSetup experimentalSetup;

    public ExperimentalSetupModel(ExperimentalSetup experimentalSetup) {
        this.experimentalSetup = experimentalSetup;
    }

    public ExperimentalSetup getExperimentalSetup() {
        return experimentalSetup;
    }

    public void setExperimentalSetup(ExperimentalSetup experimentalSetup) {
        this.experimentalSetup = experimentalSetup;
        setChanged();
        notifyObservers(NOTIFICATION);
    }
}
