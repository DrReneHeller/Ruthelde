package Helper.Plot;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

public class Symbol implements Serializable {

    public static final int RECTANGULAR_SHAPE = 1;
    public static final int CIRCULAR_SHAPE    = 2;

    public int shape;
    public int size;
    public boolean filled;

    public Symbol(int shape, int size, boolean filled){
        this.shape = shape;
        this.size = size;
        this.filled = filled;
    }

    public Shape getShape(){

        Shape result = null;

        switch (shape) {

            case CIRCULAR_SHAPE:

                result = new Ellipse2D.Double(- size / 2.0, - size / 2.0, size, size);

            break;

            case RECTANGULAR_SHAPE:

                result = new Rectangle2D.Double(- size / 2.0, - size / 2.0, size, size);
        }

        return result;

    }

}
