package com.ruthelde.Stopping;

public class StoppingCorrectionEntry {

    public String release, parametrization, info, datetime;
    public int Z;
    public double scaling_factor;
    public double[] params;

    public StoppingCorrectionEntry(String release, String parametrization, String info, String datetime, int Z, double scaling_factor, double[] params){

        this.release = release;
        this.parametrization = parametrization;
        this.info = info;
        this.datetime = datetime;
        this.Z = Z;
        this.scaling_factor = scaling_factor;
        this.params = params;
    }

    public StoppingCorrectionEntry getDeepCopy() {

        double[] new_params = new double[params.length];
        System.arraycopy(params, 0, new_params, 0, params.length);
        return new StoppingCorrectionEntry(release, parametrization, info, datetime, Z, scaling_factor, new_params);
    }
}
