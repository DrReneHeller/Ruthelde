package Helper.Plot;

import java.awt.*;
import java.io.Serializable;
import java.util.LinkedList;

public class PlotSeries implements Serializable {

    public SeriesProperties seriesProperties;
    public LinkedList<DataPoint> data;

    public PlotSeries(){

        seriesProperties = new SeriesProperties();
        seriesProperties.name = "New Series";
        data = new LinkedList<>();
    }

    public PlotSeries(String name, double[] xValues, double[] yValues){

        seriesProperties = new SeriesProperties();
        seriesProperties.name = name;
        data = new LinkedList<>();
        for (int i=0; i<xValues.length; i++){
            data.add(new DataPoint(xValues[i], yValues[i]));
        }
    }

    public void setColor(Color color){
        seriesProperties.color = color;
    }

    public void setDashed(boolean dashed){
        seriesProperties.dashed = dashed;
    }

    public void setShowSysmbol(boolean symbol){
        seriesProperties.showSymbols = symbol;
    }


    public void setStroke(int stroke){
        seriesProperties.stroke = stroke;
    }

    public void setName(String name){
        seriesProperties.name = name;
    }
}
