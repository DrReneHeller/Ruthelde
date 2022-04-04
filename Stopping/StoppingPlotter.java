package Stopping;

import Helper.Plot.MyPlotGenerator;
import Helper.Plot.PlotSeries;
import IBA.CalculationSetup.CalculationSetup;
import Target.*;
import java.awt.*;

public class StoppingPlotter {

    private final static double DEFAULT_E_MIN = 10.0d   ;
    private final static double DEFAULT_E_MAX = 2000.0d ;
    private final static int    DEFAULT_UNIT  = 0       ;

    private Target target;
    private StoppingCalculator stoppingCalculator;
    private int unit; // 0 = eV/10^15 at/cm2  --- 1 = keV/nm

    private double EMin, EMax;
    private StoppingCalculationMode stoppingPowerCalculationMode;
    private CompoundCalculationMode compoundCalculationMode;
    private Projectile projectile;

    public StoppingPlotter(Projectile projectile, Target target, CalculationSetup calculationSetup) {

        stoppingCalculator = new StoppingCalculator();

        EMin = DEFAULT_E_MIN ;
        EMax = DEFAULT_E_MAX ;
        unit = DEFAULT_UNIT  ;

        this.projectile = projectile;
        this.target = target;

        stoppingPowerCalculationMode = calculationSetup.getStoppingPowerCalculationMode();
        compoundCalculationMode      = calculationSetup.getCompoundCalculationMode();
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

    public void setEMin(double EMin) {
        if (EMin > 0.0 && EMin < EMax) {
            this.EMin = EMin;
        }
    }

    public double getEMin() {
        return EMin;
    }

    public void setEMax(double EMax) {
        if (EMax > 0.0 && EMax > EMin) {
            this.EMax = EMax;
        }
    }

    public double getEMax() {
        return EMax;
    }

    public void setUnit(int unit) {
        if (unit >= 0) {
            this.unit = unit;
        }
    }

    public int getUnitIndex() {
        return unit;
    }

    public MyPlotGenerator getPlot() {

        double E0Org = projectile.getE();

        final int NUM_POINTS = 100;
        double conversionFactor;
        double E0[] = new double[NUM_POINTS];
        double Se[] = new double[NUM_POINTS];
        double Sn[] = new double[NUM_POINTS];
        double St[] = new double[NUM_POINTS];

        Layer layer = target.getLayerList().get(0);

        String xAxisName, yAxisName;

        if (unit == 0) {
            xAxisName = "E (keV)";
            yAxisName = "S (eV/10^15 at/cm2)";
            conversionFactor = 1.0d;
        } else {
            xAxisName = "E (keV)";
            yAxisName = "S (keV/nm)";
            conversionFactor = layer.getThicknessConversionFactor() * 1000;
        }

        double logE = Math.log10(EMin);
        double increment = (Math.log10(EMax) - Math.log10(EMin)) / NUM_POINTS;

        for (int i=0; i<NUM_POINTS; i++) {
            logE += increment;
            E0[i] = Math.pow(10, logE);
            projectile.setE(E0[i]);

            Se[i] = stoppingCalculator.getStoppingPower(projectile, layer,
                    stoppingPowerCalculationMode, compoundCalculationMode,0);
            Se[i] /= conversionFactor;
            Sn[i] = stoppingCalculator.getStoppingPower(projectile, layer,
                    stoppingPowerCalculationMode, compoundCalculationMode,1);
            Sn[i] /= conversionFactor;
            St[i] = Se[i] + Sn[i];
        }

        MyPlotGenerator plotGenerator = new MyPlotGenerator();

        plotGenerator.plotProperties.xAxisName = xAxisName;
        plotGenerator.plotProperties.yAxisName = yAxisName;

        plotGenerator.plotSeries.clear();

        PlotSeries ps1 = new PlotSeries("Se", E0, Se);
        ps1.seriesProperties.color = Color.BLUE;
        ps1.seriesProperties.dashed = true;
        ps1.setStroke(4);
        plotGenerator.plotSeries.add(ps1);

        PlotSeries ps2 = new PlotSeries("Sn", E0, Sn);
        ps2.seriesProperties.color = Color.GREEN;
        ps2.seriesProperties.dashed = true;
        ps2.setStroke(4);
        plotGenerator.plotSeries.add(ps2);

        PlotSeries ps3 = new PlotSeries("S_total", E0, St);
        ps3.seriesProperties.color = Color.RED;
        ps3.seriesProperties.dashed = false;
        ps3.setStroke(4);
        plotGenerator.plotSeries.add(ps3);

        projectile.setE(E0Org);

        return plotGenerator;
    }
}
