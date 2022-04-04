package Target;

import java.io.Serializable;

public class Isotope implements Serializable {

    private static final double MAX_MASS = 238;
    private static final double DEFAULT_MASS = 1;
    private static final double DEFAULT_ABUNDANCE = 1;

    private double mass;
    private double abundance;

    public Isotope() {
        this.setMass(DEFAULT_MASS);
        this.setAbundance(DEFAULT_ABUNDANCE);
    }

    public Isotope(double mass, double abundance) {
        this.setMass(mass);
        this.setAbundance(abundance);
    }


    public void   setAbundance(double abundance) {
        this.abundance = abundance;
    }

    public double getAbundance() {
        return abundance;
    }

    public void   setMass(double mass) {
        if (mass < MAX_MASS && mass > 0) {
            this.mass = mass;
        }
    }

    public double getMass() {
        return mass;
    }

    public Isotope getDeepCopy(){

        Isotope result = new Isotope();

        result.setMass(mass);
        result.setAbundance(abundance);

        return result;

    }
}
