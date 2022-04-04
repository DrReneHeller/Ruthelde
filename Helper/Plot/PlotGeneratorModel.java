package Helper.Plot;

import java.util.Observable;

public class PlotGeneratorModel extends Observable{

    public static final String NOTIFICATION_MESSAGE = "PlotGenerator";

    private MyPlotGenerator myPlotGenerator;

    public PlotGeneratorModel(MyPlotGenerator myPlotGenerator) {
        this.myPlotGenerator = myPlotGenerator;
    }

    public MyPlotGenerator getPlotGenerator() {
        return myPlotGenerator;
    }

    public void setPlotGenerator(MyPlotGenerator myPlotGenerator) {
        this.myPlotGenerator = myPlotGenerator;
        setChanged();
        notifyObservers(NOTIFICATION_MESSAGE);
    }
}
