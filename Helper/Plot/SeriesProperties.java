package Helper.Plot;

import java.awt.*;
import java.io.Serializable;

public class SeriesProperties implements Serializable {

    public String name;
    public boolean showLine, showSymbols, dashed;
    public int stroke;
    public Symbol symbol;
    public Color color;

    public SeriesProperties(){

        name = "New Series";
        showLine = true;
        showSymbols = false;
        symbol = new Symbol(Symbol.CIRCULAR_SHAPE, 6, true);
        dashed = false;
        stroke = 1;
        color = Color.BLUE;
    }

    public SeriesProperties(String name){

        this.name = name;
        showLine = true;
        showSymbols = false;
        symbol = new Symbol(Symbol.CIRCULAR_SHAPE, 6, true);
        dashed = false;
        stroke = 1;
        color = Color.BLUE;
    }
}
