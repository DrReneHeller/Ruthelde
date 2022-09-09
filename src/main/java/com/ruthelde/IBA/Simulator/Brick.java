package com.ruthelde.IBA.Simulator;

import com.ruthelde.Target.Layer;
import com.ruthelde.Helper.Helper;

public class Brick {

    public Layer layer;
    public int layerIndex;
    public double AD;
    public double E_from, E_to, sigma, Y;

    public Brick(Layer layer, double AD, int layerIndex){
        this.layer = layer;
        this.AD = AD;
        this.layerIndex = layerIndex;
    }

    public double[] getGauss(){

        double width = E_to - E_from;
        int span = 0;
        double gauss[];

        if (Y>0) {

            //System.out.print("sigma = " + Helper.dblToDecStr(sigma, 2));
            //System.out.print(", E_c = " + Helper.dblToDecStr(E_c, 2));
            //System.out.print(", width = " + Helper.dblToDecStr(width, 2));

            while (0.5 * (span * width) * (span * width) / (sigma) < 5.0f) {
                span++;
            }
            gauss = new double[2 * span + 1];
            double y;
            double fact = Y / Math.sqrt(2.0 * Math.PI * sigma);

            //System.out.print("# = " + Helper.dblToDecStr(2*span+1, 0));
            //System.out.println(", Y = " + Helper.dblToDecStr(Y, 0));

            for (int i = 0; i <= span; i++) {
                y = width * fact * Math.exp((i * width) * (i * width) / (-2.0 * sigma));
                gauss[span + i] = y;
                gauss[span - i] = y;
                //System.out.println("  y_" + i + "=" + Helper.dblToDecStr(y, 2));
            }

            //System.out.println("");
        } else {
            gauss = new double[1];
            gauss[0] = 0;
        }

        return gauss;
    }

}
