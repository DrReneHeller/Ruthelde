package com.ruthelde.Target;

import java.io.Serializable;

public class Projectile implements Serializable {

    private static final int DEFAULT_Z =       2;
    private static final int DEFAULT_M =       4;
    private static final int DEFAULT_E =    1700;

    int Z;
    double M, E;

    public Projectile() {
        this.setParameter(DEFAULT_Z, DEFAULT_M, DEFAULT_E);
    }

    public Projectile(int Z, double M, double E) {
        if (!this.setParameter(Z, M, E)) {
            this.setParameter(DEFAULT_Z, DEFAULT_M, DEFAULT_E);
        }
    }

    public void setZ(int Z) {
        if (Z>0 && Z<=Element.MAX_ATOMIC_NUMBER) {
            this.Z = Z;
        }
    }

    public int getZ() {
        return Z;
    }

    public void setM(double M) {
        if (M>0) {
            this.M = M;
        }
    }

    public double getM() {
        return M;
    }

    public void setE(double E) {
        if (E >= 0) {
            this.E = E;
        }
    }

    public double getE() {
        return this.E;
    }

    private boolean setParameter(int Z, double M, double E) {
        boolean result = false;
        if (Z>0 && Z<=Element.MAX_ATOMIC_NUMBER && M>0 && E>0) {
            this.Z = Z;
            this.M = M;
            this.E = E;
            result = true;
        }
        return result;
    }
}
