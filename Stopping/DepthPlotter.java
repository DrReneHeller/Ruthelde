package Stopping;

import Helper.Plot.MyPlotGenerator;
import Helper.Plot.PlotSeries;
import IBA.CalculationSetup.CalculationSetup;
import Target.*;

import java.awt.*;

public class DepthPlotter {

    private final static double DEFAULT_E_CUTOFF = 1E-4 ;
    private final static int    DEFAULT_UNIT_X   = 0    ;
    private final static int    DEFAULT_UNIT_Y   = 0    ;

    private Projectile projectile;
    private Target target;
    private StoppingCalculator stoppingCalculator;

    private int unitX, unitY;
    private double ECutOff;
    private StoppingCalculationMode stoppingPowerCalculationMode;
    private CompoundCalculationMode compoundCalculationMode;

    public DepthPlotter (Projectile projectile, Target target, CalculationSetup calculationSetup) {

        stoppingCalculator = new StoppingCalculator();

        ECutOff = DEFAULT_E_CUTOFF ;
        unitX   = DEFAULT_UNIT_X   ;
        unitY   = DEFAULT_UNIT_Y   ;

        stoppingPowerCalculationMode = calculationSetup.getStoppingPowerCalculationMode() ;
        compoundCalculationMode      = calculationSetup.getCompoundCalculationMode()      ;

        this.projectile = projectile;
        this.target = target;
    }

    public void setProjectile(Projectile projectile) {
        if (projectile != null) {
            this.projectile = projectile;
        }
    }

    public void setTarget(Target target) {
        if (target != null) {
            this.target = target;
        }
    }

    public void setCalculationSetup(CalculationSetup calculationSetup) {
        if (calculationSetup != null) {
            stoppingPowerCalculationMode = calculationSetup.getStoppingPowerCalculationMode() ;
            compoundCalculationMode      = calculationSetup.getCompoundCalculationMode()      ;
        }
    }

    public void setUnitX(int unitX) {
        if (unitX >=0) {
            this.unitX = unitX;
        }
    }

    public int getUnitX() {
        return unitX;
    }

    public void setUnitY(int unitY) {
        if (unitY >= 0) {
            this.unitY = unitY;
        }
    }

    public int getUnitY() {
        return unitY;
    }

    public MyPlotGenerator getPlot() {

        double E0 = projectile.getE();

        final int NUM_POINTS_PER_LAYER = 1024                         ;
        double    depth, thickness, E, S, layerArealDensity, stepSize ;
        int       layerIndex                                          ;
        String    xLabel, yLabel                                      ;

        MyPlotGenerator plotGenerator = new MyPlotGenerator();

        int numberOfLayers = target.getLayerList().size();

        double x[][] = new double[numberOfLayers][NUM_POINTS_PER_LAYER+1];
        double y[][] = new double[numberOfLayers][NUM_POINTS_PER_LAYER+1];

        switch (unitX) {
            case 0:
                xLabel = "depth (nm)";
                break;
            case 1:
                xLabel = "Areal density (10^15 at/cm)";
                break;
            default:
                xLabel = "";
                break;
        }

        switch (unitY) {
            case 0:
                yLabel = "E (keV)";
                break;
            case 1:
                yLabel = "dE/dx (keV/nm)";
                break;
            case 2:
                yLabel = "dE/dx (eV/10^15 at/cm2)";
                break;
            default:
                yLabel = "";
                break;
        }

        plotGenerator.plotProperties.xAxisName = xLabel;
        plotGenerator.plotProperties.yAxisName = yLabel;

        depth      = 0.0d              ;
        thickness  = 0.0d              ;
        E          = projectile.getE() ;
        layerIndex = 0                 ;

        for (Layer layer: target.getLayerList()) {
            layerArealDensity = layer.getArealDensity();
            stepSize = layerArealDensity / NUM_POINTS_PER_LAYER;

            for (int i=0; i<=NUM_POINTS_PER_LAYER; i++) {

                S = stoppingCalculator.getStoppingPower(projectile, layer, stoppingPowerCalculationMode, compoundCalculationMode, 2);

                switch (unitX) {
                    case 0:
                        x[layerIndex][i] = thickness;
                        break;
                    case 1:
                        x[layerIndex][i] = depth;
                        break;
                }

                switch (unitY) {
                    case 0:
                        y[layerIndex][i] = E;
                        break;
                    case 1:
                        y[layerIndex][i] = S / (layer.getThicknessConversionFactor() * 1000.0d);
                        break;
                    case 2:
                        y[layerIndex][i] = S;
                        break;
                }

                if (E > ECutOff) {
                    projectile.setE(E);
                    depth += stepSize;
                    thickness += stepSize * layer.getThicknessConversionFactor();
                    E -= S * stepSize / 1000.0d;
                    if (E<0) E=ECutOff;
                }
            }
            layerIndex++;
        }

        x[0][0] = x[0][1];
        y[0][0] = y[0][1];

        plotGenerator.plotSeries.clear();

        for (int i=0; i<numberOfLayers; i++) {
            String seriesName = "Layer_" + i;
            PlotSeries ps = new PlotSeries(seriesName, x[i], y[i]);
            ps.setStroke(4);
            float h = (float) i / (float) numberOfLayers + 0.33f;
            ps.setColor(new Color(Color.HSBtoRGB(h, 1, 1)));
            plotGenerator.plotSeries.add(ps);
        }

        projectile.setE(E0);
       return plotGenerator;
    }
}
