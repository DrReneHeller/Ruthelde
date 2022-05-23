package IBA.ExperimentalSetup;

import Target.*;
import java.io.Serializable;

public class ExperimentalSetup implements Serializable {

    private final static double DEFAULT_E0         = 1700.0d ;
    private final static double DEFAULT_DELTA_E0   = 0.0d    ;
    private final static double DEFAULT_ALPHA      = 0.0d    ;
    private final static double DEFAULT_THETA      = 170.0d  ;
    private final static double DEFAULT_CHARGE     = 20.0d   ;
    private final static double DEFAULT_MIN_CHARGE = 15.0d   ;
    private final static double DEFAULT_MAX_CHARGE = 25.0d   ;

    private double     E0         ; //keV
    private double     deltaE0    ; //keV
    private double     alpha      ;
    private double     theta      ;
    private double     beta       ;
    private double     charge     ; //µC
    private double     minCharge  ;
    private double     maxCharge  ;

    private Projectile    projectile    ;

    public ExperimentalSetup() {

        this.setE0(DEFAULT_E0);
        this.setDeltaE0(DEFAULT_DELTA_E0);
        this.setAlpha(DEFAULT_ALPHA);
        this.setTheta(DEFAULT_THETA);
        this.setCharge(DEFAULT_CHARGE);
        this.setMinCharge(DEFAULT_MIN_CHARGE);
        this.setMaxCharge(DEFAULT_MAX_CHARGE);

        this.setProjectile(new Projectile());
    }

    public void setProjectile(Projectile projectile) {
        if (projectile != null) this.projectile = projectile;
    }

    public Projectile getProjectile() {
        return projectile;
    }

    public void setE0(double E0) {
        if (E0 > 0.0d) this.E0 = E0;
    }

    public double getE0() {
        return this.E0;
    }

    public void setDeltaE0(double deltaE0) {
        if (deltaE0 >= 0.0d) this.deltaE0 = deltaE0;
    }

    public double getDeltaE0() {
        return deltaE0;
    }

    public void setAlpha(double alpha) {
        if (alpha >= -90.0d && alpha < 90.0d) {

            double temp_beta = Math.abs(180.0d - alpha - theta);

            if (temp_beta < 90.0d) {
                this.alpha = alpha;
                this.beta = temp_beta;
            }
        }
    }

    public double getAlpha() {
        return alpha;
    }

    public void setTheta(double theta) {
        if (theta > 90 && theta <= 180) {

            double temp_beta = Math.abs(180.0d - alpha - theta);

            if (temp_beta < 90.0d) {
                this.theta =theta;
                this.beta = temp_beta;
            }
        }
    }

    public double getTheta() {
        return theta;
    }

    public double getBeta() {
        return beta;
    }


    public void setCharge(double charge) {
        if (charge > 0.0) {
            this.charge = charge;
            if (charge > maxCharge) maxCharge = charge;
            if (charge < minCharge) minCharge = charge;
        }
    }

    public void setMinCharge(double minCharge) {

        if (minCharge > 0.0 && minCharge <= charge) {
                this.minCharge = minCharge;
        }
    }

    public void setMaxCharge(double maxCharge) {


        if (maxCharge > 0.0 && maxCharge >= charge) {

            this.maxCharge = maxCharge;
        }
    }

    public double getMinCharge(){return minCharge;}

    public double getMaxCharge(){return maxCharge;}

    public double getCharge() {
        return charge;
    }

    public ExperimentalSetup getDeepCopy(){

        ExperimentalSetup result = new ExperimentalSetup();

        result.setE0(this.E0);
        result.setDeltaE0(this.deltaE0);
        result.setAlpha(this.alpha);
        result.setTheta(this.theta);
        result.setCharge(this.charge);
        result.setMinCharge(this.minCharge);
        result.setMaxCharge(this.maxCharge);
        result.setProjectile(new Projectile(this.projectile.getZ(), this.projectile.getM(), this.projectile.getE()));

        return result;

    }

}
