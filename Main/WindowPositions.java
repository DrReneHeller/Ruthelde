package Main;

public class WindowPositions {

    public WindowProperties spectrumWindow;
    public WindowProperties stoppingPlotWindow;
    public WindowProperties depthPlotWindow;
    public WindowProperties eaStatusWindow;
    public WindowProperties eaFitnessWindow;

    public WindowPositions(){
        spectrumWindow     = new WindowProperties();
        stoppingPlotWindow = new WindowProperties();
        depthPlotWindow    = new WindowProperties();
        eaStatusWindow     = new WindowProperties();
        eaFitnessWindow    = new WindowProperties();

    }
}
