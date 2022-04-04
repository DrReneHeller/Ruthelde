package Helper.Plot;

public class PlotProperties {

    public String title, xAxisName, yAxisName;

    public boolean autoScaleX, autoScaleY, logX, logY;

    public double xMin, xMax, yMin, yMax;

    public PlotProperties()
    {
        title = "";
        xAxisName = "";
        yAxisName = "";

        autoScaleX = true;
        autoScaleY = true;
        logX = false;
        logY = false;

        xMin = 0;
        xMax = 100;
        yMin = 0;
        yMax = 100;
    }

}
