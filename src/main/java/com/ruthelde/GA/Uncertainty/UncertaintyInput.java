package com.ruthelde.GA.Uncertainty;

public class UncertaintyInput {

    public double E0_min, E0_max, alpha_min, alpha_max, theta_min, theta_max;
    public double q_min, q_max, q_var, dE_min, dE_max, dE_var;
    public int numberOfFits, numberOfSpectra;

    public UncertaintyInput()
    {
        numberOfFits    =       1 ;
        numberOfSpectra =       1 ;
        q_min           =   18.0f ;
        q_max           =   22.0f ;
        q_var           =   10.0f ;
        dE_min          =   14.0f ;
        dE_max          =   16.0f ;
        dE_var          =   20.0f ;
        E0_min          = 1650.0f ;
        E0_max          = 1750.0f ;
        alpha_min       =   -2.0f ;
        alpha_max       =    2.0f ;
        theta_min       =  165.0f ;
        theta_max       =  175.0f ;
    }

}
