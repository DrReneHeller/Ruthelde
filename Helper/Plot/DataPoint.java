package Helper.Plot;

import java.io.Serializable;

public class DataPoint implements Serializable {

    public double x;
    public double y;

    public DataPoint(double x, double y){
        this.x = x;
        this.y = y;
    }

}
