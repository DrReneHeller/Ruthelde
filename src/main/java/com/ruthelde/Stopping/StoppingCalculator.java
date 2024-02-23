package com.ruthelde.Stopping;

import com.ruthelde.Target.Element;
import com.ruthelde.Target.Isotope;
import com.ruthelde.Target.Layer;
import com.ruthelde.Target.Projectile;

final public class StoppingCalculator {

    private static final int MAX_ATOMIC_NUMBER = 92;
    private double[][] stoppingCoefficients;

    private StoppingParaFile paraFile;

    private double[] correctionFactors;
    private String[] parametrizations;

    public StoppingCalculator(StoppingParaFile stoppingParaFile) {

        stoppingCoefficients = new double[94][55];

        for (int i=0; i<55; i++) {
            stoppingCoefficients[0 ][i] = 0.0d;
            if (i>0) {
                stoppingCoefficients[93][i] = DataTable.SCOEF[92*54+i-1];
            }
        }

        for (int i=0; i<94; i++) {
            stoppingCoefficients[i][0] = 0.0d;
        }

        for (int i=0; i<92; i++) {
            System.arraycopy(DataTable.SCOEF, i * 54, stoppingCoefficients[i + 1], 1, 54);
        }

        paraFile = stoppingParaFile;

        correctionFactors = new double[MAX_ATOMIC_NUMBER];
        parametrizations = new String[MAX_ATOMIC_NUMBER];

        if (paraFile != null) {
            for (int i=0; i<MAX_ATOMIC_NUMBER; i++) {
                correctionFactors[i] = paraFile.data[i].correction_factor;
                parametrizations[i] = paraFile.data[i].parametrization;
            }

        } else {
            for (int i=0; i<MAX_ATOMIC_NUMBER; i++) {
                correctionFactors[i] = 1.0d;
                parametrizations[i] = null;
            }
        }
    }

    /**
     * Get the total stopping power value of a projectile in a single element (Z2, M2)
     * Set index to determine particular stopping value 0=S_elect, 1=S_nucl, 2=S_tot
     */
    public double getStoppingPower(Projectile projectile, int Z2, double M2, StoppingCalculationMode mode, int index) {
        double result = 0;

        int    Z1 = projectile.getZ();
        double M1 = projectile.getM();
        double E0 = projectile.getE();

        double Se = 0;
        double Sn = 0;
        double S  = 0;

        if (Z1 >= 0 && Z1 <= MAX_ATOMIC_NUMBER && Z2>=0 && Z2 <= MAX_ATOMIC_NUMBER && M1>0 && M2>0) {
            switch (mode) {

                case ZB :
                    Se = calcElectronicStoppingZB(Z1, M1, Z2, E0, StoppingCalculationMode.ZB);
                    Se *= correctionFactors[Z2-1];
                    Sn = calcNuclearStoppingZB(Z1, M1, Z2, M2, E0);
                    S  = Se + Sn;
                    break;

                case PARA_FILE:
                    Se = calcElectronicStoppingZB(Z1, M1, Z2, E0, StoppingCalculationMode.PARA_FILE);
                    Se *= correctionFactors[Z2-1];
                    Sn = calcNuclearStoppingZB(Z1, M1, Z2, M2, E0);
                    S  = Se + Sn;
                    break;
            }
        }

        switch (index) {
            case 0:
                result = Se;
                break;
            case 1:
                result = Sn;
                break;
            case 2:
                result = S;
                break;
        }

        return result;
    }

    /**
     * Get the stopping power value of a projectile in a compound (layer)
     * Set index to determine particular stopping value 0=S_elect, 1=S_nucl, 2=S_tot
     */
    public double getStoppingPower(Projectile projectile, Layer layer, StoppingCalculationMode calcMode,
                                   CompoundCalculationMode compMode, int index) {
        double result = 0.0d;
        double sumOfAllAtomicRatios = 0.0d;
        double sumOfAllIsotopeRatios;
        double elementContribution, isotopeContribution;
        int Z2;
        double M2;

        switch (compMode) {

            case BRAGG:
                for (Element element : layer.getElementList()) {
                    sumOfAllAtomicRatios += element.getRatio();
                }

                for (Element element : layer.getElementList()) {
                    elementContribution = element.getRatio() / sumOfAllAtomicRatios;
                    sumOfAllIsotopeRatios = 0.0d;

                    for (Isotope isotope : element.getIsotopeList()) {
                        sumOfAllIsotopeRatios += isotope.getAbundance();
                    }

                    for (Isotope isotope : element.getIsotopeList()) {
                        isotopeContribution = isotope.getAbundance() / sumOfAllIsotopeRatios;
                        Z2 = element.getAtomicNumber();
                        M2 = isotope.getMass();
                        result += elementContribution * isotopeContribution * getStoppingPower(projectile, Z2, M2, calcMode, index);
                    }
                }
                break;
        }
        return result;
    }

    /**
     * Get the electronic stopping power value for one element
     * Formalism by Ziegler-Biersack
     * All stopping power values in eV/(10^5 atoms/cm^2)
     * Energy input in keV
     *
     */
    private double calcElectronicStoppingZB(int Z1, double M1, int Z2, double E0, StoppingCalculationMode calcMode) {

        double result = 0;

        if (calcMode.equals(StoppingCalculationMode.PARA_FILE) && parametrizations[Z2-1].equals("MEE2024")){

            double _s  = paraFile.data[Z2-1].params[0];
            double _p  = paraFile.data[Z2-1].params[1];
            double _a0 = paraFile.data[Z2-1].params[2];
            double _a1 = paraFile.data[Z2-1].params[3];
            double _a2 = paraFile.data[Z2-1].params[4];
            double _a3 = paraFile.data[Z2-1].params[5];
            double _f1 = paraFile.data[Z2-1].params[6];
            double _q  = paraFile.data[Z2-1].params[7];
            double _r  = paraFile.data[Z2-1].params[8];
            double _b0 = paraFile.data[Z2-1].params[9];
            double _b1 = paraFile.data[Z2-1].params[10];
            double _b2 = paraFile.data[Z2-1].params[11];
            double _b3 = paraFile.data[Z2-1].params[12];
            double _f2 = paraFile.data[Z2-1].params[13];

            double EM = E0 / (M1*1000.0d);

            double s1 = _f1 * Math.pow(EM, _s) * Math.log(2.7183d + _p * EM);
            s1 /= _a0 +  _a1 * Math.pow(EM, 0.25d) + _a2 * Math.pow(EM, 0.5d) + _a3 * Math.pow(EM, 1.0d+_s);

            double s2 = _f2 * Math.pow(EM, _q) * Math.log(2.7183d + _r * EM);
            s2 /= _b0 +  _b1 * Math.pow(EM, 0.25d) + _b2 * Math.pow(EM, 0.5d) + _b3 * Math.pow(EM, 1.0d+_q);

            result = s1 + s2;

        } else {

            //For Protons, Deuterons and Tritons
            if (Z1 == 1) {
                result = calcElectronicStoppingHZB(Z2, E0 / M1, calcMode);
            }

            //For Helium
            if (Z1 == 2) {
                result = calcElectronicStoppingHeZB(M1, Z2, E0, calcMode);
            }

            //For heavy ions
            if (Z1 > 2) {
                result = calcElectronicStoppingHeavyZB(Z1, M1, Z2, E0, calcMode);
            }
        }

        return result;
    }

    /**
     * Get the nuclear stopping power value for one element
     * Formalism by Ziegler-Biersack
     * All stopping power values in eV/(10^5 atoms/cm^2)
     * Energy input in keV
     */
    private double calcNuclearStoppingZB(int Z1, double M1, int Z2, double M2, double E0) {

        double result = 0.0d;

        //For Protons, Deuterons and Tritons
        if (Z1 == 1) {
            result = 0.0d;
        }

        //For heavy ions
        if (Z1 > 1) {
            result = calcNuclearStoppingHeavyZB(Z1, M1, Z2, M2, E0);
        }

        return result;
    }

    /**
     * Get electronic stopping for H/D/T by Ziegler/Biersack
     * EM = E0 / M1 = E0 (M1=1)
     */
    private double calcElectronicStoppingHZB(int Z2, double EM, StoppingCalculationMode calcMode) {

        double result = 0.0d;

        double CA,CB;
        double C1, C2, C3, C4, C5, C6, C7, C8, C9, C10, C11, C12;

        switch (calcMode) {

            case ZB:

                C1   = stoppingCoefficients[Z2][ 9];
                C2   = stoppingCoefficients[Z2][10];
                C3   = stoppingCoefficients[Z2][11];
                C4   = stoppingCoefficients[Z2][12];
                C5   = stoppingCoefficients[Z2][13];
                C6   = stoppingCoefficients[Z2][14];
                C7   = stoppingCoefficients[Z2][15];
                C8   = stoppingCoefficients[Z2][16];
                C9   = stoppingCoefficients[Z2][17];
                C10  = stoppingCoefficients[Z2][18];
                C11  = stoppingCoefficients[Z2][19];
                C12  = stoppingCoefficients[Z2][20];

                if (EM >= 10.0d && EM < 10000.0d) {
                    double S_Low  = C1*Math.pow(EM,C2) + C3*Math.pow(EM,C4);
                    double S_High = C5/(Math.pow(EM,C6))*Math.log(C7/EM + C8*EM);
                    result = S_Low * S_High / (S_Low + S_High);
                }

                if (EM >= 10000.0d) {
                    double x = Math.log(EM)/EM;
                    result = C9 + C10*x + C11*x*x + C12/x;
                }

                if (EM < 10.0d) {

                    double y;

                    if (Z2 > 6){
                        y = 0.45d;
                    } else {
                        y = 0.35d;
                    }

                    double S_Low_10  = C1*Math.pow(10,C2) + C3*Math.pow(10,C4);
                    double S_High_10 = C5/(Math.pow(10,C6))*Math.log(C7/10 + C8*10);
                    double S_elec_10 = S_Low_10 * S_High_10 / (S_Low_10 + S_High_10);

                    result = S_elec_10 * Math.pow(EM/10,y);
                }

                break;

            case PARA_FILE:

               switch(parametrizations[Z2-1]){

                   case "ZBL+BG":

                       CA  = paraFile.data[Z2-1].params[0];
                       CB  = paraFile.data[Z2-1].params[1];

                       C1  = paraFile.data[Z2-1].params[2];
                       C2  = paraFile.data[Z2-1].params[3];
                       C3  = paraFile.data[Z2-1].params[4];
                       C4  = paraFile.data[Z2-1].params[5];
                       C5  = paraFile.data[Z2-1].params[6];
                       C6  = paraFile.data[Z2-1].params[7];
                       C7  = paraFile.data[Z2-1].params[8];
                       C8  = paraFile.data[Z2-1].params[9];
                       C9  = paraFile.data[Z2-1].params[10];
                       C10 = paraFile.data[Z2-1].params[11];
                       C11 = paraFile.data[Z2-1].params[12];
                       C12 = paraFile.data[Z2-1].params[13];

                       if (EM >= 10.0d && EM < 10000.0d) {
                           double S_Low  = C1*Math.pow(EM,C2) + C3*Math.pow(EM,C4);
                           double S_High = C5/(Math.pow(EM,C6))*Math.log(C7/EM + C8*EM);
                           result = S_Low * S_High / (S_Low + S_High);
                       }

                       if (EM >= 10000.0d) {
                           double x = Math.log(EM)/EM;
                           result = C9 + C10*x + C11*x*x + C12/x;
                       }

                       if (EM < 10.0d) {

                           double y;

                           if (Z2 > 6){
                               y = 0.45d;
                           } else {
                               y = 0.35d;
                           }

                           double S_Low_10  = C1*Math.pow(10,C2) + C3*Math.pow(10,C4);
                           double S_High_10 = C5/(Math.pow(10,C6))*Math.log(C7/10 + C8*10);
                           double S_elec_10 = S_Low_10 * S_High_10 / (S_Low_10 + S_High_10);

                           result = S_elec_10 * Math.pow(EM/10,y);

                           result += CA / Math.pow(EM,CB);
                       }

                       break;
               }

                break;

            default:

                break;
        }

        return result;
    }

    /**
     * Get electronic stopping for He by Ziegler/Biersack
     */
    private double calcElectronicStoppingHeZB(double M1, int Z2, double E0, StoppingCalculationMode calcMode) {

        double C0   =  0.286500d;
        double C1   =  0.126600d;
        double C2   = -0.001429d;
        double C3   =  0.024020d;
        double C4   = -0.011350d;
        double C5   =  0.001475d;

        double EM = E0/M1;
        double HE0 = 1.0d;
        double HE = Math.max(HE0, EM);
        double B = Math.log(HE);
        double A = 0.0d;

        A += C0*Math.pow(B,0.0d);
        A += C1*Math.pow(B,1.0d);
        A += C2*Math.pow(B,2.0d);
        A += C3*Math.pow(B,3.0d);
        A += C4*Math.pow(B,4.0d);
        A += C5*Math.pow(B,5.0d);

        if (A > 30) {
            A = 30;
        }

        double HEH = 1.0d - Math.exp(-A);

        if (HE < 1) {
            HE=1;
        }

        A = (1.0d + (0.007d + 0.00005*Z2)*Math.exp(-Math.pow(7.6d - Math.log(HE),2)));
        HEH = HEH * A * A;

        double S_p = calcElectronicStoppingHZB(Z2, E0 / M1, calcMode);
        double result = (S_p * HEH * 4.0d);

        if (EM <= HE0) {
            result = result * Math.sqrt(EM/HE0);
        }

        return result;
    }

    /**
     * Get electronic stopping for heavy projectiles by Ziegler/Biersack
     */
    private double calcElectronicStoppingHeavyZB(int Z1, double M1, int Z2, double E0, StoppingCalculationMode calcMode) {

        double EM = E0/M1;
        double YRMIN = 0.13d;
        double VRMIN = 1.00d;
        double VFERMI1 = stoppingCoefficients[Z2][7];
        double V = Math.sqrt(EM/25.0d) / VFERMI1;

        double SE, SP, EEE, VMIN, LAMBDA0, LAMBDA1, A, YR, Q, L, ZETA0, ZETA;
        double EION, HIPOWER, VFCORR0, VFCORR1, VR;
        int index;

        if (V<1) {
            VR = (0.75d * VFERMI1) * (1.0d + (0.667d*V*V) - V*V*V*V/15.0d);
        } else {
            VR = V * VFERMI1 * (1.0d + 1.0d/(5.0d*V*V));
        }

        YR = VR/Math.pow(Z1, 0.6667d);
        if (YR < YRMIN) {YR = YRMIN;}
        A = VRMIN / Math.pow(Z1, 0.6667d);
        if (YR < A) {YR = A;}
        A = -0.803d * Math.pow(YR,0.3d) + 1.3167d * Math.pow(YR,0.6d) + 0.38157d * YR + 0.008983d * YR * YR;
        if (A>50) {A=50;}
        Q = 1 - Math.exp(-A);
        if (Q<0){Q=0;}
        if (Q>1){Q=1;}

        index = 0;

        for (int i=22; i<40; i++) {
            if (Q < stoppingCoefficients[93][i]){
                index = i-1;
                break;
            }
        }

        if (index<22) {index = 22;}
        if (index>38) {index = 38;}
        LAMBDA0 = stoppingCoefficients[Z1][index];
        LAMBDA1 = (Q - stoppingCoefficients[93][index]) * (stoppingCoefficients[Z1][index+1] - stoppingCoefficients[Z1][index]) / (stoppingCoefficients[93][index+1] - stoppingCoefficients[93][index]);
        L = (LAMBDA0 + LAMBDA1) / Math.pow((double)Z1, 0.33333d);

        ZETA0 = Q + (1.0d / (2.0d * VFERMI1 * VFERMI1))*(1.0d - Q) * Math.log(1.0d + Math.pow(4.0d*L*VFERMI1/1.919d,2.0d));
        A = Math.log(EM);
        if (A<0.0d){A=0.0d;}
        ZETA = ZETA0 * (1.0d + (1.0d / ((double)Z1*(double)Z1)) * (0.08d + 0.0015d*(double)Z2) * Math.exp(-Math.pow(7.6d-A,2.0d)));

        A = VRMIN / Math.pow((double)Z1, 0.6667d);
        if (A<YRMIN) {A=YRMIN;}

        if (YR <= A) {

            //Low velocity
            A = YRMIN * Math.pow((double)Z1, 0.6667d); if (VRMIN < A) {VRMIN = A;}
            A = VRMIN*VRMIN - 0.8d*VFERMI1*VFERMI1; if (A<0) {A=0;}
            VMIN = 0.5d * (VRMIN + Math.sqrt(A));
            EEE = 25 * VMIN * VMIN;
            SP = calcElectronicStoppingHZB(Z2, EEE, calcMode);
             /*Add Fermi Velocity Correction to Low Energy value*/
            if(EEE<=9999)EION=EEE;else EION=9999; /*Correction is only valid for E <1E4 keV/amu*/
            index = 0;

            for(int i=41;i<=53;i++) {
                if (stoppingCoefficients[93][i]<EION) {
                    index = i-1;
                    break;
                }
            }

            if(index<41){index=41;}else if(index>53){index=53;}
            VFCORR0 = stoppingCoefficients[Z2][index];
            VFCORR1 = (EION- stoppingCoefficients[93][index])*(stoppingCoefficients[Z2][index+1]- stoppingCoefficients[Z2][index])/(stoppingCoefficients[93][index+1]- stoppingCoefficients[93][index]);
            if(Z1==3){HIPOWER = 0.55d;}
            else if(Z2<7){HIPOWER = 0.375d;}
            else if(Z1<18 && (Z2==14 || Z2==32)){HIPOWER = 0.375d;}
            else {HIPOWER = 0.47d;}
            SE=SP*Math.pow(ZETA*(double)Z1,2.0d)*(VFCORR0+VFCORR1)*Math.pow((EM/EEE),HIPOWER);

        } else {

            //Not Low velocity
            SP= calcElectronicStoppingHZB(Z2, EM, calcMode);
            if(EM<=9999){EION=EM;}else {EION=9999;}
            index = 0;

            for(int i=41; i<=53; i++) {
                if (stoppingCoefficients[93][i]<EION) {
                    index = i-1;
                    break;
                }
            }

            if(index<41){index=41;}else if(index>53){index=53;}
            VFCORR0= stoppingCoefficients[Z2][index];
            VFCORR1=(EION- stoppingCoefficients[93][index])*(stoppingCoefficients[Z2][index+1]- stoppingCoefficients[Z2][index])/(stoppingCoefficients[93][index+1]- stoppingCoefficients[93][index]);
            SE=SP*Math.pow(ZETA*(double)Z1,2.0d)*(VFCORR0+VFCORR1);
        }

        return SE;
    }

    /**
     * Get nuclear stopping for heavy elements by Ziegler/Biersack
     */
    private double calcNuclearStoppingHeavyZB(int Z1, double M1, int Z2, double M2, double E0) {

        double K1 = 32.53f * M2 * E0;
        double K2 = Z1*Z2*(M1+M2);
        double K3 = Math.pow(Z1,0.23f) + Math.pow(Z2,0.23f);

        double eps = K1/(K2*K3);
        double sn;

        if (eps <= 30) {
            double A1 = Math.log(1 + 1.1383f * eps);
            double A2 = 1*eps;
            double A3 = 0.01321f*Math.pow(eps,0.21226f);
            double A4 = 0.19593f*Math.pow(eps,0.50000f);
            sn = A1 / (2.0f*(A2+A3+A4));
        } else {
            sn = Math.log(eps) / (2*eps);
        }

        double B1 = 8.462f * Z1 * Z2 * M1;
        double B2 = M1 + M2;
        double B3 = Math.pow(Z1,0.23f) + Math.pow(Z2,0.23f);

        double S_nucl = sn * B1 / (B2*B3);

        if (E0>0) {
            return S_nucl;
        } else {
            return 0.0d;
        }
    }
}
