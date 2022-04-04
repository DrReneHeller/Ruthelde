package Helper;

import java.io.Serializable;
import java.util.Random;

public class Spectrum implements Serializable {

    public int length;
    public String name;
    public String dateTme;
    public String comments;

    public double offset, factor, quadraticTerm;
    public double[][] data;

    public Spectrum(String name, int length){

        this.name   = name   ;
        this.length = length ;

        offset = 0.0;
        factor = 1.0;
        quadraticTerm = 0.0;

        data = new double[2][length];
        for (int i=0; i<length; i++) {
            data[0][i] = i;
            data[1][i] = 0;
        }

        dateTme  = "" ;
        comments = "" ;
    }

    public void addGaussPeak(int intensity, double position, double width){

        Random r  = new Random();

        for (int i=0; i<intensity; i++){

            int channel = (int) (r.nextGaussian()*width + position);
            if ((channel >= 0) && (channel < length)) data[1][channel]++;
        }
    }

    public void addSpectrum(Spectrum spectrum){

        if (spectrum.length == this.length){
            for (int i=0; i< length; i++) data[1][i] += spectrum.data[1][i];
        }
    }

}

