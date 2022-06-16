package GA.Uncertainty;

import Target.Target;

public class UncertaintyDataEntry {

    public double q_set, res_set, alpha, theta, E0;
    public Target target;
    public double calFactor, calOffset, q_fit, res_fit;

    public int parameterID, spectrumID, fitID;

    public UncertaintyDataEntry(int parameterID, int spectrumID, int fitID){

        this.parameterID = parameterID ;
        this.spectrumID  = spectrumID  ;
        this.fitID       = fitID       ;
    }

    public void setInputParameter(double q_set, double res_set, double alpha, double theta, double E0){

        this.q_set   = q_set   ;
        this.res_set = res_set ;
        this.alpha   = alpha   ;
        this.theta   = theta   ;
        this.E0      = E0      ;
    }
}
