package com.ruthelde.GA.Input;

import com.ruthelde.DataFileReader.FileType;
import com.ruthelde.GA.GAEngine;
import com.ruthelde.IBA.Detector.DetectorSetup;
import com.ruthelde.IBA.ExperimentalSetup.ExperimentalSetup;
import com.ruthelde.Target.*;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GABatch {

    public boolean running;
    public int counter, length;
    public File[] files;
    public FileType spectrumType;
    public File simResultFolder, simPNGFolder, simFitsFolder, parameterFileFolder, fitnessFileFolder;

    public GABatch(FileType spectrumType, File[] files, DEParameter deParameter, ExperimentalSetup experimentalSetup, DetectorSetup detectorSetup, Target target){

        this.spectrumType = spectrumType;
        this. files = files;

        length = files.length;
        counter = 0;
        running = true;

        if (files.length > 0){
            if (files[0] != null) {
                String folderName = files[0].getParent() + "/SimulationResults";
                simResultFolder = new File(folderName);
                if (!simResultFolder.exists()) {
                    simResultFolder.mkdir();
                }

                folderName = files[0].getParent() + "/SimulationResults/SimulationData";
                simResultFolder = new File(folderName);
                if (!simResultFolder.exists()) {
                    simResultFolder.mkdir();
                }

                folderName = files[0].getParent() + "/SimulationResults/Images";
                simPNGFolder = new File(folderName);
                if (!simPNGFolder.exists()) {
                    simPNGFolder.mkdir();
                }

                folderName = files[0].getParent() + "/SimulationResults/FitResults";
                simFitsFolder = new File(folderName);
                if (!simFitsFolder.exists()) {
                    simFitsFolder.mkdir();
                }

                folderName = files[0].getParent() + "/SimulationResults/Para";
                parameterFileFolder = new File(folderName);
                if (!parameterFileFolder.exists()) {
                    parameterFileFolder.mkdir();
                }

                folderName = files[0].getParent() + "/SimulationResults/Fitness";
                fitnessFileFolder = new File(folderName);
                if (!fitnessFileFolder.exists()) {
                    fitnessFileFolder.mkdir();
                }

                double Q         = experimentalSetup.getCharge();
                double Q_min     = experimentalSetup.getMinCharge();
                double Q_max     = experimentalSetup.getMaxCharge();
                double a         = detectorSetup.getCalibration().getFactor();
                double a_min     = detectorSetup.getCalibration().getFactorMin();
                double a_max     = detectorSetup.getCalibration().getFactorMax();
                double b         = detectorSetup.getCalibration().getOffset();
                double b_min     = detectorSetup.getCalibration().getOffsetMin();
                double b_max     = detectorSetup.getCalibration().getOffsetMax();
                double res       = detectorSetup.getResolution();
                double res_min   = detectorSetup.getMinRes();
                double res_max   = detectorSetup.getMaxRes();
                double E0        = experimentalSetup.getE0();
                double dE0       = experimentalSetup.getDeltaE0();
                int Z            = experimentalSetup.getProjectile().getZ();
                double M         = experimentalSetup.getProjectile().getM();
                double sigma     = detectorSetup.getSolidAngle();
                double alpha     = experimentalSetup.getAlpha();
                double theta     = experimentalSetup.getTheta();
                double beta      = experimentalSetup.getBeta();
                int startChannel = deParameter.startCH;
                int stopChannel  = deParameter.endCH;
                int numBins      = deParameter.numBins;
                double stopTime  = deParameter.endTime;
                double stopFit   = deParameter.endFitness;
                double stopGen   = deParameter.endGeneration;

                StringBuilder sb = new StringBuilder();

                sb.append("<header>\n\n");

                Date date = new Date(System.currentTimeMillis());
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                sb.append("  Start Time       = " + formatter.format(date)    + "\n\n");

                sb.append("  Experimental Setup" + "\n\n");
                sb.append("    E[keV]         = " + dblStr(2,E0)    + "\n");
                sb.append("    dE[keV]        = " + dblStr(2,dE0)   + "\n");
                sb.append("    Z              = " + dblStr(0,Z)     + "\n");
                sb.append("    M [u]          = " + dblStr(2,M)     + "\n");
                sb.append("    sigma [msr]    = " + dblStr(2,sigma) + "\n");
                sb.append("    alpha [°]      = " + dblStr(2,alpha) + "\n");
                sb.append("    theta [°]      = " + dblStr(2,theta) + "\n");
                sb.append("    beta [°]       = " + dblStr(2,beta)  + "\n\n");

                sb.append("    Q[µC]          = " + dblStr(2,Q)   + " [" + dblStr(2,Q_min)   + "," + dblStr(2,Q_max)   + "]\n");
                sb.append("    a[keV/ch]      = " + dblStr(2,a)   + " [" + dblStr(2,a_min)   + "," + dblStr(2,a_max)   + "]\n");
                sb.append("    b[keV]         = " + dblStr(2,b)   + " [" + dblStr(2,b_min)   + "," + dblStr(2,b_max)   + "]\n");
                sb.append("    Det.-Res.[keV] = " + dblStr(2,res) + " [" + dblStr(2,res_min) + "," + dblStr(2,res_max) + "]\n\n");

                sb.append("  DE Parameters" + "\n\n");
                sb.append("    From Channel   = " + dblStr(0,startChannel) + "\n");
                sb.append("    To Channel     = " + dblStr(0,stopChannel)  + "\n");
                sb.append("    Num. Bins      = " + dblStr(0,numBins)      + "\n");
                sb.append("    Stop Time [s]  = " + dblStr(0,stopTime)     + "\n");
                sb.append("    Stop Fitness   = " + dblStr(2,stopFit)      + "\n");
                sb.append("    Stop Gen.      = " + dblStr(0,stopGen)      + "\n\n");

                sb.append("  Target Model Constrains" + "\n\n");

                int layerIndex = 1;

                for (Layer layer : target.getLayerList()) {

                    sb.append("    Layer " + layerIndex + "\t");
                    sb.append(dblStr(2,layer.getMinAD()) + "\t");
                    sb.append(dblStr(2,layer.getMaxAD()) + "\t");
                    sb.append("[");

                    for (Element element : layer.getElementList()){
                        sb.append(element.getName() + ",");
                    }
                    sb.deleteCharAt(sb.length()-1);
                    sb.append("]\n");
                    layerIndex++;
                }

                sb.append("\n");
                sb.append("  Data Columns \n\n");

                sb.append("    01     - Step \n");
                sb.append("    02     - Start Date & Time \n");
                sb.append("    03     - Simulation Time [s] \n");
                sb.append("    04     - Fitness \n");
                sb.append("    05     - # Generations \n");
                sb.append("    06     - Calibration Factor \n");
                sb.append("    07     - Calibration Offset \n");
                sb.append("    08     - Detector Resolution \n");
                sb.append("    09     - Charge [µC] \n");

                int columnCounter = 10;

                layerIndex = 1;

                for (Layer layer : target.getLayerList()){

                    sb.append("\n    " + dblStr(0,columnCounter) + "     - Layer " + layerIndex + " - Areal Density [tfu] \n");
                    columnCounter++;

                    for (Element element : layer.getElementList()){

                        sb.append("    " + dblStr(0,columnCounter) + "     - Layer " + layerIndex + " - " + element.getName() + " (Ratio) \n");
                        columnCounter++;
                        sb.append("    " + dblStr(0,columnCounter) + "     - Layer " + layerIndex + " - " + element.getName() + " (Areal Density [tfu]) \n");
                        columnCounter++;
                    }
                    layerIndex++;
                }

                sb.append("\n    " + dblStr(2,columnCounter) + "     - File Name " + "\n\n");

                sb.append("</header>\n\n");

                File reportFile = new File(simResultFolder.getParent() + "/" + "BatchReport.txt");

                try {
                    if(!reportFile.exists()) {reportFile.createNewFile();}
                    FileWriter fw = new FileWriter(reportFile);
                    fw.write(sb.toString());
                    fw.close();
                } catch (Exception ex) {System.out.println(ex.getMessage());}
            }
        }
    }

    public File getCurrentFile(){

        return files[counter];
    }

    public boolean hasMoreElements(){

        return counter < length;
    }

    public void addReportEntry(GAEngine gaEngine){

        StringBuilder sb = new StringBuilder();

        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        double t_total = (double)gaEngine.getTotalTime();
        double fitness = gaEngine.getBest().getFitness();
        int genCount = gaEngine.getGenerationCount();
        double calibrationFactor = gaEngine.getBest().getCalibrationFactor() / gaEngine.getDeParameter().numBins;
        double calibrationOffset = gaEngine.getBest().getCalibrationOffset();
        double detRes = gaEngine.getBest().getResolution();
        double charge = gaEngine.getBest().getCharge();

        sb.append(String.format("%05d" , counter+1)  + "\t");
        sb.append(formatter.format(date) + "\t");
        sb.append(String.format("%.4e" , t_total/1000.0f)  + "\t");
        sb.append(String.format("%.4e" , fitness)  + "\t");
        sb.append(String.format("%06d" , genCount)  + "\t");
        sb.append(String.format("%.4e" , calibrationFactor)  + "\t");
        sb.append(String.format("%.4e" , calibrationOffset)  + "\t");
        sb.append(String.format("%.4e" , detRes)  + "\t");
        sb.append(String.format("%.4e" , charge)  + "\t");

        Target target = gaEngine.getBest().getTarget().getDeepCopy();

        for (Layer layer : target.getLayerList()){

            sb.append(String.format("%.4e" , layer.getArealDensity())  + "\t");
            layer.normalizeElements();

            for(Element element : layer.getElementList()){

                sb.append(String.format("%.4e" , element.getRatio())  + "\t");
                sb.append(String.format("%.4e" , layer.getArealDensity() * element.getRatio())  + "\t");

            }
        }

        sb.append(files[counter].getName() + "\n");

        File reportFile = new File(simResultFolder.getParent() + "/" + "BatchReport.txt");

        try {
            FileWriter fw = new FileWriter(reportFile,true);
            fw.write(sb.toString());
            fw.close();
        } catch (Exception ex) {System.out.println(ex.getMessage());}
    }

    private String dblStr(int digits, double value){

        String pattern = "%." + digits + "f";
        return String.format(pattern,value).replace(",",".");
    }
}
