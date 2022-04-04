package IBA.Detector;

import java.util.Observable;

public class DetectorSetupModel extends Observable{

    public final static String NOTIFICATION = "DetectorSetup";

    private DetectorSetup detectorSetup;

    public DetectorSetupModel (DetectorSetup detectorSetup) {
        this.detectorSetup = detectorSetup;
    }

    public DetectorSetup getDetectorSetup() {
        return detectorSetup;
    }

    public void setDetectorSetup(DetectorSetup detectorSetup) {
        this.detectorSetup = detectorSetup;
        setChanged();
        notifyObservers(NOTIFICATION);
    }

}
