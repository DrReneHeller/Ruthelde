package com.ruthelde.Stopping;

public class StoppingCorrectionEntry {

    public String info, parametrization;
    public int Z;
    public double correction_factor;
    public double[] params;

    public StoppingCorrectionEntry(String parametrization, String info, int Z, double scaling_factor, double[] params){

        this.parametrization = parametrization;
        this.info = info;
        this.Z = Z;
        this.correction_factor = scaling_factor;
        this.params = params;
    }

    public StoppingCorrectionEntry getDeepCopy() {

        double[] new_params = new double[params.length];
        System.arraycopy(params, 0, new_params, 0, params.length);
        return new StoppingCorrectionEntry(parametrization, info, Z, correction_factor, new_params);
    }
}
