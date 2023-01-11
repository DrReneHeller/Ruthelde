package com.ruthelde.Main;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.ruthelde.Forms.*;
import com.ruthelde.GA.*;
import com.ruthelde.GA.Input.*;
import com.ruthelde.GA.Uncertainty.UncertaintyEngine;
import com.ruthelde.Helper.*;
import com.ruthelde.Helper.Plot.*;
import com.ruthelde.IBA.CalculationSetup.*;
import com.ruthelde.IBA.DataFile;
import com.ruthelde.IBA.Detector.DetectorSetup;
import com.ruthelde.IBA.ExperimentalSetup.*;
import com.ruthelde.IBA.Simulator.*;
import com.ruthelde.Stopping.*;
import com.ruthelde.Target.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ruthelde.DataFileReader.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import static com.ruthelde.DataFileReader.FileType.*;

public class MainWindow extends JFrame implements Observer {

    private final String TARGET_NOTIFICATION = "TargetModel";
    private final String FOIL_NOTIFICATION = "FoilModel";

    //------------ Global Fields -------------------------------------------------------------------------------------//
    private JPanel rootPanel;
    private JComboBox cBoxExpM1;
    private JTextField tfExpE0;
    private JTextField tfExpDE0;
    private JTextField tfExpZ1;
    private JTextField tfExpCharge;
    private JTextField tfExpAlpha;
    private JTextField tfExpTheta;
    private JTextField tFExpBeta;
    private JTextField tfSPEMin;
    private JTextField tfSPEMax;
    private JComboBox cBoxSPUnit;
    private JComboBox cBoxDPUnitX;
    private JComboBox cBoxDPUnitY;
    private JTextField tfDetDE;
    private JTextField tfDetCalOffset;
    private JTextField tfDetCalFactor;
    private JTextField tfDetSolidAngle;
    private JLabel lblStatus;
    private JPanel pnlSetup;
    private JLabel lbl_fitness;
    private JTextField tf_ch_min;
    private JTextField tf_ch_max;
    private JTextField tfExpChargeMin;
    private JTextField tfExpChargeMax;
    private JTextField tfDetDEMin;
    private JTextField tfDetDEMax;
    private JTextField tfDetCalOffsetMin;
    private JTextField tfDetCalOffsetMax;
    private JTextField tfDetCalFactorMin;
    private JTextField tfDetCalFactorMax;

    private ExperimentalSetup experimentalSetup;
    private DetectorSetup detectorSetup;
    private TargetModel targetModel;
    private TargetModel foilModel;
    private TargetView targetView;
    private TargetView foilView;
    private CalculationSetup calculationSetup;
    private PlotWindow spectraPlotWindow;
    private PlotWindow stoppingPlotWindow;
    private PlotWindow depthPlotWindow;
    private PlotWindow fitnessPlotWindow;
    private PlotWindow parameterPlotWindow;
    private EAStatusWindow eaStatusWindow;

    private SimulationResultPlotter simulationResultPlotter;
    private SingleSPCalculator singleSPCalculator;
    private StoppingPlotter stoppingPlotter;
    private DepthPlotter depthPlotter;
    private IBAKinematics ibaKinematics;
    private SpectrumSimulator spectrumSimulator;

    private GAEngineWorker gaEngineWorker;
    private DEParameter deParameter;
    private boolean gaRunning;
    private GABatch gaBatch;
    private DEInputCreator deInputCreator;
    private UncertaintyEngine uncertaintyEngine;

    private boolean blockEvents;
    private boolean console;
    private String lastFolder;
    private String currentFileName;

    //------------ Constructor ---------------------------------------------------------------------------------------//

    public MainWindow(String args[]) {

        experimentalSetup = new ExperimentalSetup();
        detectorSetup = new DetectorSetup();
        targetModel = new TargetModel(new Target(), TARGET_NOTIFICATION);
        targetView = new TargetView(targetModel, "Target Configurator");
        foilModel = new TargetModel(new Target(), FOIL_NOTIFICATION);
        foilModel.getTarget().setLayerThickness(0, 0.1d);
        foilView = new TargetView(foilModel, "Foil Configurator");
        calculationSetup = new CalculationSetup();

        Projectile pr = experimentalSetup.getProjectile();
        Target tg = targetModel.getTarget();
        Target fo = foilModel.getTarget();

        singleSPCalculator = new SingleSPCalculator(pr, calculationSetup);
        stoppingPlotter = new StoppingPlotter(pr, tg, calculationSetup);
        depthPlotter = new DepthPlotter(pr, tg, calculationSetup);
        ibaKinematics = new IBAKinematics(experimentalSetup, tg, fo, calculationSetup);
        spectrumSimulator = new SpectrumSimulator(experimentalSetup, detectorSetup, tg, fo, calculationSetup);
        simulationResultPlotter = new SimulationResultPlotter(calculationSetup);

        initComponents();

        targetModel.addObserver(this);
        foilModel.addObserver(this);

        spectrumSimulator.setTarget(targetModel.getTarget()); //Just to have an initial plot drawn
        uncertaintyEngine = new UncertaintyEngine();

        if (args.length >= 3) {

            System.out.println("Importing parameters from simulation file.");
            loadSimulation(args[0]);

            FileType fileType = null;
            if (args[2].equals("ASCII_ONE")) fileType = ONE_COLUMN_ASCII;
            if (args[2].equals("ASCII_TWO")) fileType = TWO_COLUMN_ASCII;
            if (args[2].equals("IBC_RBS")) fileType = IBC_RBS;
            if (args[2].equals("IBC_3MV_SINGLE")) fileType = IBC_3MV_SINGLE;
            if (args[2].equals("IBC_3MV_MULTI")) fileType = IBC_3MV_MULTI;
            if (args[2].equals("IMEC")) fileType = IMEC;
            if (args[2].equals("IBA_SIM")) fileType = IBA_SIM;

            if (fileType != null) {
                System.out.println("Parsed file type to be *" + fileType.toString() + "*");

                File files[] = new File[args.length - 2];
                boolean error = false;

                for (int i = 0; i < (args.length - 2); i++) {
                    files[i] = new File(args[i + 2]);
                    if (files[i] == null) {
                        error = true;
                        break;
                    }
                }

                if (!error) {

                    System.out.println("Parsed " + files.length + " file names:");
                    for (int i = 0; i < files.length; i++) {
                        System.out.println("  " + files[i].getParent() + "/" + files[i].getName());
                    }

                    System.out.println("Starting DE algorithm.");
                    spectraPlotWindow.setVisible(true);
                    spectraPlotWindow.setSize(800, 500);
                    eaStatusWindow.setVisible(true);
                    fitnessPlotWindow.setVisible(true);
                    console = true;
                    doBatchGASimulation(files, fileType);
                }
            }
        } else {

            if (args.length == 1 && args[0].equals("help")) {

                System.out.println("Usage:");
                System.out.println("");
                System.out.println("  java -jar IBA.jar input fileType spectrum_1 spectrum_2 ... spectrum_N");
                System.out.println("");
                System.out.println("    input - (absolute) path to IBA simulation file which is used to extract");
                System.out.println("    input parameters like target model and  experimental constrains from.");
                System.out.println("");
                System.out.println("    fileType - specifies the tye of following iba spectra. Allowed values:");
                System.out.println("    ASCII_ONE, ASCII_TWO, IBC_RBS, IBC_3MV_SINGLE, IBC_3MV_MULTI, IMEC, IBA_SIM");
                System.out.println("");
                System.out.println("    spectrum_1 ... spectrum_N - (absolute) file paths to spectra files");

                System.exit(0);
            }
        }
    }

    public static void main(String args[]) {

        try {
            UIManager.setLookAndFeel("com.jtattoo.plaf.hifi.HiFiLookAndFeel");
        } catch (Exception ex) {
            System.out.println("Problem(s) with loading Look&Feel ...");
        }

        EventQueue.invokeLater(() -> new MainWindow(args));
    }

    //------------ Update Routine ------------------------------------------------------------------------------------//

    @Override
    public void update(Observable o, Object arg) {

        if (arg.equals(TARGET_NOTIFICATION)) {

            Target tg = targetModel.getTarget();
            spectrumSimulator.setTarget(tg);
            ibaKinematics.setTarget(tg);
            stoppingPlotter.setTarget(tg);
            depthPlotter.setTarget(tg);
            updateOpenPlotWindows();
        }

        if (arg.equals(FOIL_NOTIFICATION)) {

            Target foil = foilModel.getTarget();
            ibaKinematics.setFoil(foil);
            spectrumSimulator.setFoil(foil);
            updateOpenPlotWindows();
        }

    }

    private void updateSimulation() {

        if (!gaRunning) {

            spectrumSimulator.setStartChannel(Integer.parseInt(tf_ch_min.getText()));
            spectrumSimulator.setStopChannel(Integer.parseInt(tf_ch_max.getText()));

            spectrumSimulator.setTarget(targetModel.getTarget());

            SimulationData simulationData = spectrumSimulator.simulate();

            long time = simulationData.getSimulationTime();
            lblStatus.setText("t=" + time + "ms");

            Target target = targetModel.getTarget();
            LinkedList<PlotSeries> plotList = simulationResultPlotter.makePlots(simulationData, target);
            spectraPlotWindow.setPlotSeries(plotList);
            spectraPlotWindow.refresh();

            lbl_fitness.setText(Helper.dblToDecStr(simulationData.getFitness(), 2));
        }
    }

    private void updateStoppingCalculation() {

        MyPlotGenerator pg = stoppingPlotter.getPlot();
        stoppingPlotWindow.setPlotSeries(pg.plotSeries);
        stoppingPlotWindow.getPlotProperties().xAxisName = pg.plotProperties.xAxisName;
        stoppingPlotWindow.getPlotProperties().yAxisName = pg.plotProperties.yAxisName;
        stoppingPlotWindow.refresh();
    }

    private void updateDepthCalculation() {

        MyPlotGenerator pg = depthPlotter.getPlot();
        depthPlotWindow.getPlotProperties().xAxisName = pg.plotProperties.xAxisName;
        depthPlotWindow.getPlotProperties().yAxisName = pg.plotProperties.yAxisName;
        depthPlotWindow.setPlotSeries(pg.plotSeries);
        depthPlotWindow.refresh();
    }

    private void updateOpenPlotWindows() {
        if (spectraPlotWindow.isVisible()) updateSimulation();
        if (stoppingPlotWindow.isVisible()) updateStoppingCalculation();
        if (depthPlotWindow.isVisible()) updateDepthCalculation();
        if (ibaKinematics.isVisible()) ibaKinematics.setCalculationSetup(calculationSetup);
    }

    //------------ Load & Save ---------------------------------------------------------------------------------------//

    private void readDataFile(FileType fileType, File file) {

        File spectrumFile = null;

        if (file == null) {
            final JFileChooser fc;
            if (lastFolder != null) fc = new JFileChooser(lastFolder);
            else fc = new JFileChooser();
            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                spectrumFile = fc.getSelectedFile();
                lastFolder = fc.getSelectedFile().getParent();
                setLastFolder(lastFolder);
                currentFileName = spectrumFile.getParent() + '/' + spectrumFile.getName();
                spectraPlotWindow.setTitle("Spectra - " + currentFileName);
            }
        } else spectrumFile = file;

        if (spectrumFile != null) {

            System.out.println(spectrumFile.getParent() + "/" + spectrumFile.getName());

            double[] experimentalSpectrum = null;

            switch (fileType) {

                case ONE_COLUMN_ASCII:
                    experimentalSpectrum = DataFileReader.readASCIIFileOneColumn(spectrumFile);
                    break;

                case TWO_COLUMN_ASCII:
                    experimentalSpectrum = DataFileReader.readASCIIFileTwoColumn(spectrumFile);
                    break;

                case IBC_RBS:
                    experimentalSpectrum = DataFileReader.readIBCDataFile(spectrumFile);
                    break;

                case IBC_3MV_SINGLE:
                    Gson gson = new Gson();
                    try {
                        FileReader fr = new FileReader(spectrumFile);
                        Spectrum spectrum = gson.fromJson(fr, Spectrum.class);
                        experimentalSpectrum = new double[spectrum.length];

                        for (int i = 0; i < experimentalSpectrum.length; i++) {
                            experimentalSpectrum[i] = spectrum.data[1][i];
                        }
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }
                    break;

                case IBC_3MV_MULTI:

                    String indexStr = JOptionPane.showInputDialog(
                            this,
                            "Select Spectrum",
                            "Select Spectrum",
                            JOptionPane.QUESTION_MESSAGE
                    );

                    if (indexStr != null) {
                        try {
                            int index = Integer.parseInt(indexStr);
                            if (index >= 1) {
                                experimentalSpectrum = DataFileReader.read3MVAllDataFile(spectrumFile, index);
                            } else {
                                experimentalSpectrum = DataFileReader.read3MVAllDataFile(spectrumFile, 1);
                            }
                        } catch (Exception ex) {
                            System.out.println(ex.getMessage());
                        }
                    }


                    break;

                case IMEC:
                    experimentalSpectrum = DataFileReader.readIMECDataFile(spectrumFile);
                    break;

                case IBA_SIM:
                    experimentalSpectrum = DataFileReader.readExpSpectrumFromSimulationFile(spectrumFile);
                    break;
            }

            int ch_min = Integer.parseInt(tf_ch_min.getText());
            int ch_max = Integer.parseInt(tf_ch_max.getText());

            int length = experimentalSpectrum.length;

            if (ch_min >= length) {
                ch_min = 0;
                tf_ch_min.setText("" + ch_min);
                spectrumSimulator.setStartChannel(ch_min);
            }

            if (ch_max >= length) {
                ch_min = 0;
                ch_max = length - 1;
                tf_ch_min.setText("" + ch_min);
                tf_ch_max.setText("" + ch_max);
                spectrumSimulator.setStartChannel(ch_min);
                spectrumSimulator.setStopChannel(ch_max);
            }

            spectrumSimulator.setExperimentalSpectrum(experimentalSpectrum);
            updateSimulation();
        }
    }

    private void saveSimulation(File file) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        if (file == null) {

            final JFileChooser fc;
            if (lastFolder != null) fc = new JFileChooser(lastFolder);
            else fc = new JFileChooser();
            int returnVal = fc.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {

                file = fc.getSelectedFile();
                lastFolder = fc.getSelectedFile().getParent();
                setLastFolder(lastFolder);
            }
        }

        if (file != null) {

            Target target = targetModel.getTarget();
            //Target foil = foilModel.getTarget(); //TODO: Include Foil

            WindowPositions wp = new WindowPositions();

            wp.spectrumWindow.visible = spectraPlotWindow.isVisible();
            wp.spectrumWindow.height = spectraPlotWindow.getHeight();
            wp.spectrumWindow.width = spectraPlotWindow.getWidth();
            wp.spectrumWindow.x = spectraPlotWindow.getX();
            wp.spectrumWindow.y = spectraPlotWindow.getY();

            wp.stoppingPlotWindow.visible = stoppingPlotWindow.isVisible();
            wp.stoppingPlotWindow.height = stoppingPlotWindow.getHeight();
            wp.stoppingPlotWindow.width = stoppingPlotWindow.getWidth();
            wp.stoppingPlotWindow.x = stoppingPlotWindow.getX();
            wp.stoppingPlotWindow.y = stoppingPlotWindow.getY();

            wp.depthPlotWindow.visible = depthPlotWindow.isVisible();
            wp.depthPlotWindow.height = depthPlotWindow.getHeight();
            wp.depthPlotWindow.width = depthPlotWindow.getWidth();
            wp.depthPlotWindow.x = depthPlotWindow.getX();
            wp.depthPlotWindow.y = depthPlotWindow.getY();

            wp.eaFitnessWindow.visible = fitnessPlotWindow.isVisible();
            wp.eaFitnessWindow.height = fitnessPlotWindow.getHeight();
            wp.eaFitnessWindow.width = fitnessPlotWindow.getWidth();
            wp.eaFitnessWindow.x = fitnessPlotWindow.getX();
            wp.eaFitnessWindow.y = fitnessPlotWindow.getY();

            wp.eaStatusWindow.visible = eaStatusWindow.isVisible();
            wp.eaStatusWindow.height = eaStatusWindow.getHeight();
            wp.eaStatusWindow.width = eaStatusWindow.getWidth();
            wp.eaStatusWindow.x = eaStatusWindow.getX();
            wp.eaStatusWindow.y = eaStatusWindow.getY();

            DataFile dataFile = new DataFile(target, experimentalSetup, calculationSetup, detectorSetup, deParameter,
                    spectrumSimulator.experimentalSpectrum, currentFileName, wp);

            try {
                FileWriter fw = new FileWriter(file);

                gson.toJson(dataFile, fw);
                fw.flush();
                fw.close();
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }


    }

    private void loadSimulation(String fileName) {

        File file = null;

        try {

            if (fileName == null) {

                final JFileChooser fc;
                if (lastFolder != null) fc = new JFileChooser(lastFolder);
                else fc = new JFileChooser();
                int returnVal = fc.showOpenDialog(this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    file = fc.getSelectedFile();
                }

            } else {

                file = new File(fileName);
            }

            if (file != null) {

                Gson gson = new Gson();
                lastFolder = file.getParent();

                FileReader fr = new FileReader(file);
                DataFile df = gson.fromJson(fr, DataFile.class);

                tfExpE0.setText(Helper.dblToDecStr(df.experimentalSetup.getE0(), 2));
                experimentalSetup.setE0(df.experimentalSetup.getE0());

                tfExpDE0.setText(Helper.dblToDecStr(df.experimentalSetup.getDeltaE0(), 2));
                experimentalSetup.setDeltaE0(df.experimentalSetup.getDeltaE0());

                int z = df.experimentalSetup.getProjectile().getZ();
                double m = df.experimentalSetup.getProjectile().getM();
                tfExpZ1.setText(Integer.toString(z));
                experimentalSetup.getProjectile().setZ(z);
                experimentalSetup.getProjectile().setM(m);
                fillCBoxExpM1(m);

                experimentalSetup.setCharge(df.experimentalSetup.getCharge());
                experimentalSetup.setMinCharge(df.experimentalSetup.getMinCharge());
                experimentalSetup.setMaxCharge(df.experimentalSetup.getMaxCharge());
                updateChargeValues();

                detectorSetup.setResolution(df.detectorSetup.getResolution());
                detectorSetup.setMinRes(df.detectorSetup.getMinRes());
                detectorSetup.setMaxRes(df.detectorSetup.getMaxRes());
                updateDetDE();

                detectorSetup.setCalibrationFactor(df.detectorSetup.getCalibration().getFactor());
                detectorSetup.getCalibration().setFactorMin(df.detectorSetup.getCalibration().getFactorMin());
                detectorSetup.getCalibration().setFactorMax(df.detectorSetup.getCalibration().getFactorMax());

                detectorSetup.setCalibrationOffset(df.detectorSetup.getCalibration().getOffset());
                detectorSetup.getCalibration().setOffsetMin(df.detectorSetup.getCalibration().getOffsetMin());
                detectorSetup.getCalibration().setOffsetMax(df.detectorSetup.getCalibration().getOffsetMax());
                updateCalibration();

                tfDetSolidAngle.setText(Helper.dblToDecStr(df.detectorSetup.getSolidAngle(), 2));
                detectorSetup.setSolidAngle(df.detectorSetup.getSolidAngle());

                tfExpAlpha.setText(Helper.dblToDecStr(df.experimentalSetup.getAlpha(), 2));
                tFExpBeta.setText(Helper.dblToDecStr(df.experimentalSetup.getBeta(), 2));
                tfExpTheta.setText(Helper.dblToDecStr(df.experimentalSetup.getTheta(), 2));

                experimentalSetup.setAlpha(df.experimentalSetup.getAlpha());
                experimentalSetup.setTheta(df.experimentalSetup.getTheta());

                calculationSetup.setShowElements(df.calculationSetup.isShowElements());
                calculationSetup.setShowIsotopes(df.calculationSetup.isShowIsotopes());
                calculationSetup.setShowLayers(df.calculationSetup.isShowLayers());
                calculationSetup.setSimulateIsotopes(df.calculationSetup.isSimulateIsotopes());
                calculationSetup.setCorrectionFactors(df.calculationSetup.getCorrectionFactors());

                spectrumSimulator.setCalculationSetup(calculationSetup);

                blockEvents = true;
                buildMenu();
                blockEvents = false;

                deParameter = df.deParameter;

                spectrumSimulator.setStartChannel(deParameter.startCH);
                spectrumSimulator.setStopChannel(deParameter.endCH);
                tf_ch_min.setText("" + (int) df.deParameter.startCH);
                tf_ch_max.setText("" + (int) df.deParameter.endCH);
                spectrumSimulator.setExperimentalSpectrum(df.experimentalSpectrum);

                targetModel.setTarget(df.target);
                targetView.updateTarget();

                //TODO: Include Foil

                spectraPlotWindow.setLocation(new Point((int) df.windowPositions.spectrumWindow.x, (int) df.windowPositions.spectrumWindow.y));
                spectraPlotWindow.setSize(new Dimension((int) df.windowPositions.spectrumWindow.width, (int) df.windowPositions.spectrumWindow.height));
                spectraPlotWindow.setVisible(df.windowPositions.spectrumWindow.visible);

                depthPlotWindow.setLocation(new Point((int) df.windowPositions.depthPlotWindow.x, (int) df.windowPositions.depthPlotWindow.y));
                depthPlotWindow.setSize(new Dimension((int) df.windowPositions.depthPlotWindow.width, (int) df.windowPositions.depthPlotWindow.height));
                depthPlotWindow.setVisible(df.windowPositions.depthPlotWindow.visible);

                stoppingPlotWindow.setLocation(new Point((int) df.windowPositions.stoppingPlotWindow.x, (int) df.windowPositions.stoppingPlotWindow.y));
                stoppingPlotWindow.setSize(new Dimension((int) df.windowPositions.stoppingPlotWindow.width, (int) df.windowPositions.stoppingPlotWindow.height));
                stoppingPlotWindow.setVisible(df.windowPositions.stoppingPlotWindow.visible);

                eaStatusWindow.setLocation(new Point((int) df.windowPositions.eaStatusWindow.x, (int) df.windowPositions.eaStatusWindow.y));
                eaStatusWindow.setSize(new Dimension((int) df.windowPositions.eaStatusWindow.width, (int) df.windowPositions.eaStatusWindow.height));
                eaStatusWindow.setVisible(df.windowPositions.eaStatusWindow.visible);

                fitnessPlotWindow.setLocation(new Point((int) df.windowPositions.eaFitnessWindow.x, (int) df.windowPositions.eaFitnessWindow.y));
                fitnessPlotWindow.setSize(new Dimension((int) df.windowPositions.eaFitnessWindow.width, (int) df.windowPositions.eaFitnessWindow.height));
                fitnessPlotWindow.setVisible(df.windowPositions.eaFitnessWindow.visible);

                currentFileName = file.getName();
                spectraPlotWindow.setTitle("Spectra - " + currentFileName);

                updateOpenPlotWindows();

                lastFolder = file.getParent();
                setLastFolder(lastFolder);

                updateSimulation();
            }

        } catch (Exception ex) {
            System.out.println("Error loading simulation file: ");
            ex.printStackTrace();
        }


    }

    private void makeAndSaveExpSpectrum() {

        Random rand = new Random();

        //StringBuilder sb = new StringBuilder();

        SimulationData simData = spectrumSimulator.simulate();
        int length = simData.getNumberOfChannels();

        double[] experimentalSpectrum = new double[length];

        for (int i = 0; i < length; i++) {

            //sb.append(String.format("%.4e" , (double)(i+1)).replace(",",".") + "\t" );

            //double E = simData.getEnergy()[i];
            //sb.append(String.format("%.4e" , E).replace(",",".")   + "\t" );

            double simValue = simData.getSimulatedSpectrum()[i];
            //sb.append(String.format("%.4e" , simValue).replace(",",".")   + "\t" );

            double sqrt = Math.sqrt(simValue);


            //double simExpValue = simValue - sqrt * 2.0d*(rand.nextDouble()-0.5d);
            double simExpValue = sqrt * rand.nextGaussian() + simValue;
            //double simExpValue = simValue * (0.96d + 0.08d * rand.nextDouble());
            //double simExpValue = simValue;
            //sb.append(String.format("%.4e" , simExpValue).replace(",",".")   + "\n" );
            experimentalSpectrum[i] = simExpValue;
        }

        spectrumSimulator.setExperimentalSpectrum(experimentalSpectrum);
        updateSimulation();

        /*
        try {
            final JFileChooser fc;
            if (lastFolder != null) fc=new JFileChooser(lastFolder); else fc = new JFileChooser();
            int returnVal = fc.showSaveDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {

                File file = fc.getSelectedFile();
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                writer.write(sb.toString());
                writer.close();
                lastFolder = file.getParent();
            }

        } catch (Exception ex){System.out.println(ex.getMessage());}
        */

    }

    private void setLastFolder(String lastFolder) {

        spectraPlotWindow.setLastFolder(lastFolder);
        depthPlotWindow.setLastFolder(lastFolder);
        stoppingPlotWindow.setLastFolder(lastFolder);

        targetView.setLastFolder(lastFolder);
        foilView.setLastFolder(lastFolder);
    }

    //------------ Actions for "Experimental Setup" ------------------------------------------------------------------//

    private void setExpZ1() {

        int Z1 = experimentalSetup.getProjectile().getZ();
        int newZ1 = Z1;
        boolean changeIt = false;
        Element element = new Element();

        try {
            newZ1 = Integer.parseInt(tfExpZ1.getText());
            if (newZ1 != Z1) changeIt = true;
        } catch (NumberFormatException ex) {
            if (element.setAtomicNumberByName(tfExpZ1.getText())) {
                newZ1 = element.getAtomicNumber();
                if (newZ1 != Z1) changeIt = true;
            }
        }

        if (changeIt) {
            experimentalSetup.getProjectile().setZ(newZ1);
            fillCBoxExpM1(0.0f);
        } else {
            blockEvents = true;
            tfExpZ1.setText(Integer.toString(experimentalSetup.getProjectile().getZ()));
            blockEvents = false;
        }

    }

    private void fillCBoxExpM1(double m) {

        DefaultComboBoxModel lm = (DefaultComboBoxModel) cBoxExpM1.getModel();
        blockEvents = true;
        lm.removeAllElements();
        LinkedList<String> ll = new LinkedList<String>();

        int Z1 = experimentalSetup.getProjectile().getZ();

        Element element = new Element();
        element.setAtomicNumber(Z1);

        if (m == 0.0f) {
            double highest = 0.0f;
            for (Isotope isotope : element.getIsotopeList()) {
                if (isotope.getAbundance() > highest) {
                    highest = isotope.getAbundance();
                    m = isotope.getMass();
                }
            }
        }

        int index = 0;
        int bestIndex = 0;
        double delta = 500.0f;

        for (Isotope isotope : element.getIsotopeList()) {
            String entry = Helper.dblToDecStr(isotope.getMass(), 3) + " (" + Helper.dblToDecStr(isotope.getAbundance(), 2) + ")";
            if (!ll.contains(entry)) {
                ll.add(entry);
                lm.addElement(entry);
                double tempDelta = Math.abs(isotope.getMass() - m);
                if (tempDelta <= delta) {
                    bestIndex = index;
                    delta = tempDelta;
                }
                index++;
            }
        }

        blockEvents = false;
        cBoxExpM1.setSelectedIndex(bestIndex);
    }

    private void setExpE0() {

        double oldE0 = experimentalSetup.getE0();
        Double E0;
        try {
            E0 = Double.parseDouble(tfExpE0.getText());
            if (E0 != oldE0) {
                experimentalSetup.setE0(E0);
                experimentalSetup.getProjectile().setE(E0);
                updateOpenPlotWindows();
            }
        } catch (NumberFormatException ex) {
        }

        blockEvents = true;
        E0 = experimentalSetup.getE0();
        tfExpE0.setText(Helper.dblToDecStr(E0, 2));
        blockEvents = false;
    }

    private void setExpDE0() {

        double oldDeltaE0 = experimentalSetup.getDeltaE0();
        double deltaE0;
        try {
            deltaE0 = Double.parseDouble(tfExpDE0.getText());
            if (deltaE0 != oldDeltaE0) {
                experimentalSetup.setDeltaE0(deltaE0);
                updateOpenPlotWindows();
            }
        } catch (NumberFormatException ex) {
        }

        blockEvents = true;
        deltaE0 = experimentalSetup.getDeltaE0();
        tfExpDE0.setText(Helper.dblToDecStr(deltaE0, 2));
        blockEvents = false;
    }

    private void setExpAlpha() {

        double oldAlpha = experimentalSetup.getAlpha();
        double alpha;
        try {
            alpha = Double.parseDouble(tfExpAlpha.getText());
            if (alpha != oldAlpha) {
                experimentalSetup.setAlpha(alpha);
                updateOpenPlotWindows();
            }
        } catch (NumberFormatException ex) {
        }

        blockEvents = true;
        alpha = experimentalSetup.getAlpha();
        tfExpAlpha.setText(Helper.dblToDecStr(alpha, 2));
        tFExpBeta.setText(Helper.dblToDecStr(experimentalSetup.getBeta(), 2));
        blockEvents = false;
    }

    private void setExpTheta() {

        double oldTheta = experimentalSetup.getTheta();
        double theta;
        try {
            theta = Double.parseDouble(tfExpTheta.getText());
            if (theta != oldTheta) {
                experimentalSetup.setTheta(theta);
                updateOpenPlotWindows();
            }
        } catch (NumberFormatException ex) {
        }

        blockEvents = true;
        theta = experimentalSetup.getTheta();
        tfExpTheta.setText(Helper.dblToDecStr(theta, 2));
        tFExpBeta.setText(Helper.dblToDecStr(experimentalSetup.getBeta(), 2));
        blockEvents = false;
    }

    private void updateChargeValues() {
        double charge = experimentalSetup.getCharge();
        double minCharge = experimentalSetup.getMinCharge();
        double maxCharge = experimentalSetup.getMaxCharge();

        tfExpCharge.setText(Helper.dblToDecStr(charge, 2));
        tfExpChargeMin.setText(Helper.dblToDecStr(minCharge, 2));
        tfExpChargeMax.setText(Helper.dblToDecStr(maxCharge, 2));
    }

    private void setExpCharge() {

        double oldCharge = experimentalSetup.getCharge();
        double charge;
        try {
            charge = Double.parseDouble(tfExpCharge.getText());
            if (charge != oldCharge) {
                experimentalSetup.setCharge(charge);
                updateOpenPlotWindows();
            }
        } catch (NumberFormatException ex) {
        }

        blockEvents = true;
        updateChargeValues();
        blockEvents = false;
    }

    private void setExpMinCharge() {

        double oldValue = experimentalSetup.getMinCharge();
        double minCharge;
        try {
            minCharge = Double.parseDouble(tfExpChargeMin.getText());
            if (minCharge != oldValue) {
                experimentalSetup.setMinCharge(minCharge);
            }
        } catch (NumberFormatException ex) {
        }

        blockEvents = true;
        updateChargeValues();
        blockEvents = false;
    }

    private void setExpMaxCharge() {

        double oldValue = experimentalSetup.getMaxCharge();
        double maxCharge;
        try {
            maxCharge = Double.parseDouble(tfExpChargeMax.getText());
            if (maxCharge != oldValue) {
                experimentalSetup.setMaxCharge(maxCharge);
            }
        } catch (NumberFormatException ex) {
        }

        blockEvents = true;
        updateChargeValues();
        blockEvents = false;
    }

    //------------ Actions for "Detector Setup" ----------------------------------------------------------------------//

    private void setTfDetSolidAngle() {

        System.out.println("Here");

        double oldSolidAngle = detectorSetup.getSolidAngle();
        Double solidAngle;
        try {
            solidAngle = Double.parseDouble(tfDetSolidAngle.getText());
            if (solidAngle != oldSolidAngle) {
                detectorSetup.setSolidAngle(solidAngle);
                updateOpenPlotWindows();
            }
        } catch (NumberFormatException ex) {
        }

        blockEvents = true;
        solidAngle = detectorSetup.getSolidAngle();
        tfDetSolidAngle.setText(Helper.dblToDecStr(solidAngle, 2));
        blockEvents = false;
    }

    private void updateDetDE() {

        double res = detectorSetup.getResolution();
        double minRes = detectorSetup.getMinRes();
        double maxRes = detectorSetup.getMaxRes();

        tfDetDE.setText(Helper.dblToDecStr(res, 2));
        tfDetDEMin.setText(Helper.dblToDecStr(minRes, 2));
        tfDetDEMax.setText(Helper.dblToDecStr(maxRes, 2));
    }

    private void updateCalibration() {

        double factor = detectorSetup.getCalibration().getFactor();
        double factorMin = detectorSetup.getCalibration().getFactorMin();
        double factorMax = detectorSetup.getCalibration().getFactorMax();
        double offset = detectorSetup.getCalibration().getOffset();
        double offsetMin = detectorSetup.getCalibration().getOffsetMin();
        double offsetMax = detectorSetup.getCalibration().getOffsetMax();

        tfDetCalFactor.setText(Helper.dblToDecStr(factor, 2));
        tfDetCalFactorMin.setText(Helper.dblToDecStr(factorMin, 2));
        tfDetCalFactorMax.setText(Helper.dblToDecStr(factorMax, 2));
        tfDetCalOffset.setText(Helper.dblToDecStr(offset, 2));
        tfDetCalOffsetMin.setText(Helper.dblToDecStr(offsetMin, 2));
        tfDetCalOffsetMax.setText(Helper.dblToDecStr(offsetMax, 2));
    }

    private void setDetDE() {

        double oldDE = detectorSetup.getResolution();
        Double dE;
        try {
            dE = Double.parseDouble(tfDetDE.getText());
            if (dE != oldDE) {
                detectorSetup.setResolution(dE);
                updateOpenPlotWindows();
            }
        } catch (NumberFormatException ex) {
        }

        blockEvents = true;
        updateDetDE();
        blockEvents = false;
    }

    private void setDetDEMin() {

        double oldValue = detectorSetup.getMinRes();
        Double minDE;
        try {
            minDE = Double.parseDouble(tfDetDEMin.getText());
            if (minDE != oldValue) {
                detectorSetup.setMinRes(minDE);
            }
        } catch (NumberFormatException ex) {
        }

        blockEvents = true;
        updateDetDE();
        blockEvents = false;
    }

    private void setDetDEMax() {

        double oldValue = detectorSetup.getMaxRes();
        Double maxDE;
        try {
            maxDE = Double.parseDouble(tfDetDEMax.getText());
            if (maxDE != oldValue) {
                detectorSetup.setMaxRes(maxDE);
            }
        } catch (NumberFormatException ex) {
        }

        blockEvents = true;
        updateDetDE();
        blockEvents = false;
    }

    private void setDetFactor() {

        double oldA = detectorSetup.getCalibration().getFactor();
        Double a;
        try {
            a = Double.parseDouble(tfDetCalFactor.getText());
            if (a != oldA) {
                detectorSetup.getCalibration().setFactor(a);
                updateOpenPlotWindows();
            }
        } catch (NumberFormatException ex) {
        }

        blockEvents = true;
        updateCalibration();
        blockEvents = false;
    }

    private void setDetFactorMin() {

        double oldValue = detectorSetup.getCalibration().getFactorMin();
        Double newValue;
        try {
            newValue = Double.parseDouble(tfDetCalFactorMin.getText());
            if (newValue != oldValue) {
                detectorSetup.getCalibration().setFactorMin(newValue);
            }
        } catch (NumberFormatException ex) {
        }

        blockEvents = true;
        updateCalibration();
        blockEvents = false;
    }

    private void setDetFactorMax() {

        double oldValue = detectorSetup.getCalibration().getFactorMax();
        Double newValue;
        try {
            newValue = Double.parseDouble(tfDetCalFactorMax.getText());
            if (newValue != oldValue) {

                detectorSetup.getCalibration().setFactorMax(newValue);
            }
        } catch (NumberFormatException ex) {
        }

        blockEvents = true;
        updateCalibration();
        blockEvents = false;
    }

    private void setDetOffset() {

        double oldOffset = detectorSetup.getCalibration().getOffset();
        Double offset;
        try {
            offset = Double.parseDouble(tfDetCalOffset.getText());
            if (offset != oldOffset) {
                detectorSetup.getCalibration().setOffset(offset);
                updateOpenPlotWindows();
            }
        } catch (NumberFormatException ex) {
        }

        blockEvents = true;
        updateCalibration();
        blockEvents = false;
    }

    private void setDetOffsetMin() {

        double oldValue = detectorSetup.getCalibration().getOffsetMin();
        Double newValue;
        try {
            newValue = Double.parseDouble(tfDetCalOffsetMin.getText());
            if (newValue != oldValue) {
                detectorSetup.getCalibration().setOffsetMin(newValue);
            }
        } catch (NumberFormatException ex) {
        }

        blockEvents = true;
        updateCalibration();
        blockEvents = false;
    }

    private void setDetOffsetMax() {

        double oldValue = detectorSetup.getCalibration().getOffsetMax();
        Double newValue;
        try {
            newValue = Double.parseDouble(tfDetCalOffsetMax.getText());
            if (newValue != oldValue) {
                detectorSetup.getCalibration().setOffsetMax(newValue);
            }
        } catch (NumberFormatException ex) {
        }

        blockEvents = true;
        updateCalibration();
        blockEvents = false;
    }


    //------------ Actions for "Misc Setup" --------------------------------------------------------------------------//

    private void setSPEMin() {

        double oldEMin = stoppingPlotter.getEMin();
        double EMin;
        try {
            EMin = Double.parseDouble(tfSPEMin.getText());
            if (EMin != oldEMin) {
                stoppingPlotter.setEMin(EMin);
            }
        } catch (NumberFormatException ex) {
        }

        blockEvents = true;
        EMin = stoppingPlotter.getEMin();
        tfSPEMin.setText(Helper.dblToDecStr(EMin, 2));
        blockEvents = false;

        updateStoppingCalculation();

    }

    private void setSPEMax() {

        double oldEMax = stoppingPlotter.getEMax();
        double EMax;
        try {
            EMax = Double.parseDouble(tfSPEMax.getText());
            if (EMax != oldEMax) {
                stoppingPlotter.setEMax(EMax);
            }
        } catch (NumberFormatException ex) {
        }

        blockEvents = true;
        EMax = stoppingPlotter.getEMax();
        tfSPEMax.setText(Helper.dblToDecStr(EMax, 2));
        blockEvents = false;

        updateStoppingCalculation();
    }

    private void setSPUnit() {
        stoppingPlotter.setUnit(cBoxSPUnit.getSelectedIndex());

        updateStoppingCalculation();
    }

    private void setDPUnitX() {
        depthPlotter.setUnitX(cBoxDPUnitX.getSelectedIndex());
        updateDepthCalculation();
    }

    private void setDPUnitY() {
        depthPlotter.setUnitY(cBoxDPUnitY.getSelectedIndex());
        updateDepthCalculation();
    }


    //------------ Actions for "EA"         --------------------------------------------------------------------------//
    private void doGASimulation() {

        gaRunning = true;
        lbl_fitness.setText("---");
        lblStatus.setText("---");
        if (gaBatch.running) lblStatus.setText("EA-Batch " + (gaBatch.counter + 1) + " of " + gaBatch.length);

        GAEngine gaEngine;

        if (uncertaintyEngine.running) {
            gaEngine = new GAEngine(uncertaintyEngine.getSpectrumSimulator(), deParameter, calculationSetup);
        } else {
            gaEngine = new GAEngine(spectrumSimulator, deParameter, calculationSetup);
        }

        gaEngineWorker = new GAEngineWorker(gaEngine, spectraPlotWindow, fitnessPlotWindow, parameterPlotWindow, eaStatusWindow.ta_info);
        gaEngineWorker.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {

                stopGASimulation();

                if (gaBatch.running) {

                    File f = new File(gaBatch.simResultFolder.getAbsolutePath() + "/" + gaBatch.getCurrentFile().getName());

                    File simulationFile = Helper.changeExtension(f, ".json", null);
                    saveSimulation(simulationFile);

                    f = new File(gaBatch.simPNGFolder.getAbsolutePath() + "/" + gaBatch.getCurrentFile().getName());
                    File imageFile = Helper.changeExtension(f, ".png", null);
                    spectraPlotWindow.saveImage(imageFile);

                    f = new File(gaBatch.simFitsFolder.getAbsolutePath() + "/" + gaBatch.getCurrentFile().getName());
                    File simResultFile = Helper.changeExtension(f, ".txt", null);
                    spectraPlotWindow.exportAscii(simResultFile);

                    f = new File(gaBatch.parameterFileFolder.getAbsolutePath() + "/" + gaBatch.getCurrentFile().getName());
                    File parameterFile = Helper.changeExtension(f, ".txt", null);
                    parameterPlotWindow.exportAscii(parameterFile);

                    f = new File(gaBatch.fitnessFileFolder.getAbsolutePath() + "/" + gaBatch.getCurrentFile().getName());
                    File fitnessFile = Helper.changeExtension(f, ".txt", null);
                    fitnessPlotWindow.exportAscii(fitnessFile);

                    gaBatch.addReportEntry(gaEngine);
                    gaBatch.counter++;

                    if (gaBatch.hasMoreElements()) {

                        readDataFile(gaBatch.spectrumType, gaBatch.getCurrentFile());
                        currentFileName = gaBatch.getCurrentFile().getParent() + '/' + gaBatch.getCurrentFile().getName();
                        spectraPlotWindow.setTitle("Spectra - " + currentFileName);
                        doGASimulation();

                    } else {

                        gaBatch.running = false;
                        lblStatus.setText("GA-Batch finished");
                        if (console) {
                            System.out.println("All simulations done. stopping application.");
                            System.out.println();
                            System.exit(0);
                        }
                    }
                } else if (uncertaintyEngine.running) {

                    doGASimulation();
                }
            }
        });
        gaEngineWorker.execute();
    }

    private void doBatchGASimulation(File files[], FileType fileType) {

        if (files == null) {
            try {
                final JFileChooser fc;
                if (lastFolder != null) fc = new JFileChooser(lastFolder);
                else fc = new JFileChooser();
                fc.setMultiSelectionEnabled(true);
                int returnVal = fc.showOpenDialog(this);
                if (returnVal == JFileChooser.APPROVE_OPTION) files = fc.getSelectedFiles();
            } catch (Exception ex) {
                System.out.println("Error in reading file list for batch mode: " + ex.toString());
            }
        }

        if (files.length > 0) {

            lastFolder = files[0].getParent();
            setLastFolder(lastFolder);

            if (fileType == null) {

                JComboBox cBox = new JComboBox(FileType.values());
                Object[] message = {"Spectrum File-Type :", cBox};
                int option = JOptionPane.showConfirmDialog(null, message, "Batch - File Type Selection", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) fileType = (FileType) cBox.getSelectedItem();
            }

            gaBatch = new GABatch(fileType, files, deParameter, experimentalSetup, detectorSetup, targetModel.getTarget());
            readDataFile(gaBatch.spectrumType, gaBatch.getCurrentFile());
            currentFileName = gaBatch.getCurrentFile().getParent() + '/' + gaBatch.getCurrentFile().getName();
            spectraPlotWindow.setTitle("Spectra - " + currentFileName);

            doGASimulation();
        }
    }

    private void doUncertaintyGASimulation() {

        spectraPlotWindow.setTitle("Spectra - Artificial");
        uncertaintyEngine.initialize(spectrumSimulator);
        uncertaintyEngine.running = true;
        doGASimulation();
    }

    private void stopGASimulation() {

        if (gaRunning) {

            gaEngineWorker.stop();
            while (!gaEngineWorker.isFinished()) try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
            if (!uncertaintyEngine.running) {
                copyBestCandidate();
                updateSimulation();
            } else {
                uncertaintyEngine.updateOutput(gaEngineWorker.getGaEngine().getBest(), deParameter.numBins);
                uncertaintyEngine.prepareNextSimulation(deParameter.numBins);
            }
            gaEngineWorker.stop();
        }

        gaRunning = false;

    }

    private void copyBestCandidate() {

        Individual individual = gaEngineWorker.getGaEngine().getBest();

        targetModel.setTargetSilent(individual.getTarget().getDeepCopy());
        targetView.updateTarget();
        experimentalSetup.setCharge(individual.getCharge());
        detectorSetup.setResolution(individual.getResolution());
        detectorSetup.setCalibrationFactor(individual.getCalibrationFactor() / deParameter.numBins);
        detectorSetup.setCalibrationOffset(individual.getCalibrationOffset());

        blockEvents = true;

        tfDetCalFactor.setText(Helper.dblToDecStr(individual.getCalibrationFactor() / deParameter.numBins, 4));
        tfDetCalOffset.setText(Helper.dblToDecStr(individual.getCalibrationOffset(), 2));
        tfExpCharge.setText(Helper.dblToDecStr(individual.getCharge(), 2));
        tfDetDE.setText(Helper.dblToDecStr(individual.getResolution(), 2));

        setExpCharge();
        blockEvents = false;
    }

    private void makeGACsFromCurrentSetting() {

        deInputCreator.setGAInput(deParameter);
        deInputCreator.setVisible(true);
    }

    //------------ GUI initialization --------------------------------------------------------------------------------//

    private void initComponents() {

        this.setTitle("Ruthelde V7.6.3 - 2022_11_08 (C) R. Heller");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setContentPane(rootPanel);
        pack();
        //this.setLocation(50,50);
        this.setResizable(false);
        this.setVisible(true);

        this.lastFolder = null;
        this.gaEngineWorker = null;
        this.deParameter = new DEParameter();
        this.gaRunning = false;

        spectraPlotWindow = new PlotWindow("Spectra");
        spectraPlotWindow.getPlotGenerator().plotProperties.xAxisName = "Energy (keV)";
        spectraPlotWindow.getPlotGenerator().plotProperties.yAxisName = "counts";
        stoppingPlotWindow = new PlotWindow("Stopping Plotter");
        depthPlotWindow = new PlotWindow("Penetration into target");

        fitnessPlotWindow = new PlotWindow("DE Fitness evolution");
        fitnessPlotWindow.getPlotGenerator().plotProperties.xAxisName = "Generation";
        fitnessPlotWindow.getPlotGenerator().plotProperties.yAxisName = "fitness";

        parameterPlotWindow = new PlotWindow("DE Fit Results");
        parameterPlotWindow.getPlotGenerator().plotProperties.xAxisName = "Generation";
        parameterPlotWindow.getPlotGenerator().plotProperties.yAxisName = "quantity";

        eaStatusWindow = new EAStatusWindow("DE Status");
        gaBatch = new GABatch(null, new File[1], deParameter, experimentalSetup, detectorSetup, targetModel.getTarget());
        gaBatch.running = false;
        currentFileName = "No spectrum file loaded";
        spectraPlotWindow.setTitle("Spectra - " + currentFileName);

        deInputCreator = new DEInputCreator();

        buildMenu();

        tfExpZ1.setText(Integer.toString(experimentalSetup.getProjectile().getZ()));
        fillCBoxExpM1(0.0f);
        tfExpE0.setText(Helper.dblToDecStr(experimentalSetup.getE0(), 2));
        tfExpDE0.setText(Helper.dblToDecStr(experimentalSetup.getDeltaE0(), 2));
        tfExpAlpha.setText(Helper.dblToDecStr(experimentalSetup.getAlpha(), 2));
        tfExpTheta.setText(Helper.dblToDecStr(experimentalSetup.getTheta(), 2));
        tFExpBeta.setText(Helper.dblToDecStr(experimentalSetup.getBeta(), 2));
        updateChargeValues();

        tfDetSolidAngle.setText(Helper.dblToDecStr(detectorSetup.getSolidAngle(), 2));
        updateDetDE();
        updateCalibration();

        tfSPEMin.setText(Helper.dblToDecStr(stoppingPlotter.getEMin(), 2));
        tfSPEMax.setText(Helper.dblToDecStr(stoppingPlotter.getEMax(), 2));
        cBoxSPUnit.setSelectedIndex(stoppingPlotter.getUnitIndex());
        cBoxDPUnitX.setSelectedIndex(depthPlotter.getUnitX());
        cBoxDPUnitY.setSelectedIndex(depthPlotter.getUnitY());

        tfExpZ1.addActionListener(e -> setExpZ1());

        tfExpZ1.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                setExpZ1();
            }
        });

        cBoxExpM1.addActionListener(e -> {

            if (!blockEvents) {

                int Z1 = experimentalSetup.getProjectile().getZ();

                Element element = new Element();
                element.setAtomicNumber(Z1);
                int M1Index = cBoxExpM1.getSelectedIndex();
                double M1 = element.getIsotopeList().get(M1Index).getMass();
                experimentalSetup.getProjectile().setM(M1);
                updateOpenPlotWindows();
            }
        });

        tfExpE0.addActionListener(e -> {
            if (!blockEvents) setExpE0();
        });

        tfExpE0.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (!blockEvents) setExpE0();
            }
        });

        tfExpDE0.addActionListener(e -> {
            if (!blockEvents) setExpDE0();
        });

        tfExpDE0.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (!blockEvents) setExpDE0();
            }
        });

        tfExpE0.addMouseWheelListener(e -> {
            int stepSize = -e.getUnitsToScroll();
            if (stepSize > 10) stepSize = 10;
            double E0 = experimentalSetup.getE0();
            //E0 += (double)stepSize / 100.0 * E0;
            E0 += stepSize;
            if (E0 < 0.0) E0 = 0.0;
            experimentalSetup.setE0(E0);
            updateOpenPlotWindows();
            experimentalSetup.getProjectile().setE(E0);
            blockEvents = true;
            tfExpE0.setText(Helper.dblToDecStr(E0, 2));
            blockEvents = false;
        });

        tfExpDE0.addMouseWheelListener(e -> {
            int stepSize = -e.getUnitsToScroll();
            if (stepSize > 10) stepSize = 10;
            double dE0 = experimentalSetup.getDeltaE0();
            dE0 += (double) stepSize / 100.0 * dE0;
            if (dE0 == 0.0) dE0 = 1.0;
            if (dE0 < 0.0) dE0 = 0.0;
            experimentalSetup.setDeltaE0(dE0);
            updateOpenPlotWindows();
            blockEvents = true;
            tfExpDE0.setText(Helper.dblToDecStr(dE0, 2));
            blockEvents = false;
        });

        tfExpAlpha.addActionListener(e -> {
            if (!blockEvents) setExpAlpha();
        });

        tfExpAlpha.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (!blockEvents) setExpAlpha();
            }
        });

        tfExpTheta.addActionListener(e -> {
            if (!blockEvents) setExpTheta();
        });

        tfExpTheta.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (!blockEvents) setExpTheta();
            }
        });

        tfExpCharge.addActionListener(e -> {
            if (!blockEvents) setExpCharge();
        });

        tfExpCharge.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (!blockEvents) setExpCharge();
            }
        });

        tfExpCharge.addMouseWheelListener(e -> {
            int stepSize = -e.getUnitsToScroll();
            if (stepSize > 10) stepSize = 10;
            double charge = experimentalSetup.getCharge();
            charge += (double) stepSize / 100.0 * charge;
            if (charge == 0.0) charge = 1.0;
            if (charge < 0.0) charge = 0.1;
            experimentalSetup.setCharge(charge);
            updateOpenPlotWindows();
            blockEvents = true;
            updateChargeValues();
            blockEvents = false;
        });

        tfExpChargeMin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!blockEvents) {
                    setExpMinCharge();
                }
            }
        });

        tfExpChargeMax.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!blockEvents) {
                    setExpMaxCharge();
                }
            }
        });

        tfDetSolidAngle.addActionListener(e -> {
            if (!blockEvents) setTfDetSolidAngle();
        });

        tfDetSolidAngle.addMouseWheelListener(e -> {
            int stepSize = -e.getUnitsToScroll();
            if (stepSize > 10) stepSize = 10;
            double solidAngle = detectorSetup.getSolidAngle();
            solidAngle += (double) stepSize / 100.0 * solidAngle;
            if (solidAngle == 0.0) solidAngle = 1.0;
            if (solidAngle < 0.0) solidAngle = 0.0;
            detectorSetup.setSolidAngle(solidAngle);
            updateOpenPlotWindows();
            blockEvents = true;
            tfDetSolidAngle.setText(Helper.dblToDecStr(solidAngle, 2));
            blockEvents = false;
        });

        tfDetDE.addActionListener(e -> {
            if (!blockEvents) setDetDE();
        });

        tfDetDE.addMouseWheelListener(e -> {
            int stepSize = -e.getUnitsToScroll();
            if (stepSize > 10) stepSize = 10;
            double dE = detectorSetup.getResolution();
            dE += (double) stepSize / 100.0 * dE;
            if (dE == 0.0) dE = 1.0;
            if (dE < 0.0) dE = 0.0;
            detectorSetup.setResolution(dE);
            updateOpenPlotWindows();
            blockEvents = true;
            updateDetDE();
            blockEvents = false;
        });

        tfDetDEMin.addActionListener(e -> setDetDEMin());

        tfDetDEMax.addActionListener(e -> setDetDEMax());

        tfDetCalOffset.addActionListener(e -> {
            if (!blockEvents) setDetOffset();
        });

        tfDetCalOffset.addMouseWheelListener(e -> {
            int stepSize = -e.getUnitsToScroll();
            if (stepSize > 10) stepSize = 10;
            double offset = detectorSetup.getCalibration().getOffset();
            offset += (double) stepSize / 100.0 * offset;
            if (offset == 0.0) offset = 1.0 * stepSize;
            detectorSetup.getCalibration().setOffset(offset);
            updateOpenPlotWindows();
            blockEvents = true;
            updateCalibration();
            blockEvents = false;
        });

        tfDetCalOffsetMin.addActionListener(e -> setDetOffsetMin());

        tfDetCalOffsetMax.addActionListener(e -> setDetOffsetMax());

        tfDetCalFactor.addActionListener(e -> {
            if (!blockEvents) setDetFactor();
        });

        tfDetCalFactor.addMouseWheelListener(e -> {
            int stepSize = -e.getUnitsToScroll();
            if (stepSize > 10) stepSize = 10;
            double a = detectorSetup.getCalibration().getFactor();
            a += (double) stepSize / 100.0 * a;
            if (a == 0.0) a = 1.0;
            if (a < 0.0) a = 0.0;
            detectorSetup.getCalibration().setFactor(a);
            updateOpenPlotWindows();
            blockEvents = true;
            updateCalibration();
            blockEvents = false;
        });

        tfDetCalFactorMin.addActionListener(e -> setDetFactorMin());

        tfDetCalFactorMax.addActionListener(e -> setDetFactorMax());

        tfSPEMin.addActionListener(e -> {
            if (!blockEvents) setSPEMin();
        });

        tfSPEMin.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (!blockEvents) setSPEMin();
            }
        });

        tfSPEMax.addActionListener(e -> {
            if (!blockEvents) setSPEMax();
        });

        tfSPEMax.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (!blockEvents) setSPEMax();
            }
        });

        cBoxSPUnit.addActionListener(e -> {
            if (!blockEvents) setSPUnit();
        });

        cBoxDPUnitX.addActionListener(e -> {
            if (!blockEvents) setDPUnitX();
        });

        cBoxDPUnitY.addActionListener(e -> {
            if (!blockEvents) setDPUnitY();
        });
    }

    private void buildMenu() {

        JMenuBar jmb = new JMenuBar();

        JMenu fileMenu = new JMenu("File");

        JMenu jmLoad = new JMenu("Import Spectrum");

        JMenuItem itemLoadASCIISpectrumOne = new JMenuItem("One Column ASCII");
        itemLoadASCIISpectrumOne.addActionListener(e -> readDataFile(ONE_COLUMN_ASCII, null));
        jmLoad.add(itemLoadASCIISpectrumOne);

        JMenuItem itemLoadASCIISpectrumTwo = new JMenuItem("Multi Column ASCII");
        itemLoadASCIISpectrumTwo.addActionListener(e -> readDataFile(TWO_COLUMN_ASCII, null));
        jmLoad.add(itemLoadASCIISpectrumTwo);

        JMenuItem itemLoadOldIBCSpectrum = new JMenuItem("IBC Spectrum (vdG)");
        itemLoadOldIBCSpectrum.addActionListener(e -> readDataFile(IBC_RBS, null));
        jmLoad.add(itemLoadOldIBCSpectrum);

        JMenuItem itemLoadNewIBCSpectrum = new JMenuItem("IBC Spectrum (3MV) Single");
        itemLoadNewIBCSpectrum.addActionListener(e -> readDataFile(IBC_3MV_SINGLE, null));
        jmLoad.add(itemLoadNewIBCSpectrum);

        JMenuItem itemLoadNewIBCAllSpectra = new JMenuItem("IBC Spectrum (3MV) Multiple");
        itemLoadNewIBCAllSpectra.addActionListener(e -> readDataFile(FileType.IBC_3MV_MULTI, null));
        jmLoad.add(itemLoadNewIBCAllSpectra);

        JMenuItem itemLoadIMECSpectrum = new JMenuItem("IMEC Spectrum");
        itemLoadIMECSpectrum.addActionListener(e -> readDataFile(IMEC, null));
        jmLoad.add(itemLoadIMECSpectrum);

        fileMenu.add(jmLoad);

        fileMenu.add(new JSeparator());

        JMenuItem itemLoadSimulation = new JMenuItem("Load Simulation");
        itemLoadSimulation.addActionListener(e -> loadSimulation(null));
        fileMenu.add(itemLoadSimulation);

        JMenuItem itemSaveSpectrum = new JMenuItem("Save Simulation");
        itemSaveSpectrum.addActionListener(e -> saveSimulation(null));
        itemSaveSpectrum.setAccelerator(KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        fileMenu.add(itemSaveSpectrum);

        fileMenu.add(new JSeparator());

        /*
        JMenuItem itemLoadInputData = new JMenuItem("Load Input Data");
        itemLoadInputData.addActionListener(e -> loadInputData(null));
        fileMenu.add(itemLoadInputData);
        */

        //TODO: Comment out later
        JMenuItem itemMakeExpSpectrum = new JMenuItem("Generate Measured Spectrum");
        itemMakeExpSpectrum.addActionListener(e -> makeAndSaveExpSpectrum());
        fileMenu.add(itemMakeExpSpectrum);

        jmb.add(fileMenu);

        JMenu calcMenu = new JMenu("Calculation");

        JCheckBoxMenuItem jcbUseLookUpTable = new JCheckBoxMenuItem("Use Stopping-Look-up Table");
        jcbUseLookUpTable.addActionListener(e -> {
            if (!blockEvents) {
                calculationSetup.setUseLookUpTable(jcbUseLookUpTable.isSelected());
                updateOpenPlotWindows();
            }
        });
        calcMenu.add(jcbUseLookUpTable);

        JMenu stopCalcMethod = new JMenu("Stopping Calculation");
        ButtonGroup stoppingMethods = new ButtonGroup();
        JRadioButtonMenuItem mrb_st_zb = new JRadioButtonMenuItem("Ziegler Biersack");
        mrb_st_zb.setSelected(true);
        mrb_st_zb.addActionListener(e -> {
            calculationSetup.setStoppingPowerCalculationMode(StoppingCalculationMode.ZB);
            updateOpenPlotWindows();
        });
        stoppingMethods.add(mrb_st_zb);
        stopCalcMethod.add(mrb_st_zb);
        calcMenu.add(stopCalcMethod);

        JMenu compCorrMethod = new JMenu("Compound Correction");
        ButtonGroup compCorrMethods = new ButtonGroup();
        JRadioButtonMenuItem mrb_comp_bragg = new JRadioButtonMenuItem("Bragg Rule");
        mrb_comp_bragg.setSelected(true);
        mrb_comp_bragg.addActionListener(e -> {
            calculationSetup.setCompoundCalculationMode(CompoundCalculationMode.BRAGG);
            updateOpenPlotWindows();
        });
        compCorrMethods.add(mrb_comp_bragg);
        compCorrMethod.add(mrb_comp_bragg);
        calcMenu.add(compCorrMethod);

        calcMenu.add(new JSeparator());

        JMenu stragglingModel = new JMenu("Straggling Model");
        ButtonGroup stragglingModels = new ButtonGroup();
        JRadioButtonMenuItem mrb_straggling_none = new JRadioButtonMenuItem("None");
        JRadioButtonMenuItem mrb_straggling_bohr = new JRadioButtonMenuItem("Bohr");
        JRadioButtonMenuItem mrb_straggling_chu = new JRadioButtonMenuItem("Chu");
        mrb_straggling_none.addActionListener(e -> {
            calculationSetup.setStragglingMode(StragglingMode.NONE);
            updateOpenPlotWindows();
        });
        mrb_straggling_bohr.addActionListener(e -> {
            calculationSetup.setStragglingMode(StragglingMode.BOHR);
            updateOpenPlotWindows();
        });
        mrb_straggling_chu.addActionListener(e -> {
            calculationSetup.setStragglingMode(StragglingMode.CHU);
            updateOpenPlotWindows();
        });
        mrb_straggling_chu.setSelected(true);
        stragglingModel.add(mrb_straggling_none);
        stragglingModels.add(mrb_straggling_none);
        stragglingModel.add(mrb_straggling_bohr);
        stragglingModels.add(mrb_straggling_bohr);
        stragglingModel.add(mrb_straggling_chu);
        stragglingModels.add(mrb_straggling_chu);
        calcMenu.add(stragglingModel);

        JMenu screeningModel = new JMenu("Screening Model");
        ButtonGroup screeningModels = new ButtonGroup();
        JRadioButtonMenuItem mrb_screening_none = new JRadioButtonMenuItem("None");
        JRadioButtonMenuItem mrb_screening_LEcuyer = new JRadioButtonMenuItem("L'Ecuyer");
        JRadioButtonMenuItem mrb_screening_anderson = new JRadioButtonMenuItem("Anderson");
        mrb_screening_none.addActionListener(e -> {
            calculationSetup.setScreeningMode(ScreeningMode.NONE);

            updateOpenPlotWindows();
        });
        mrb_screening_LEcuyer.addActionListener(e -> {
            calculationSetup.setScreeningMode(ScreeningMode.LECUYER);
            updateOpenPlotWindows();
        });
        mrb_screening_anderson.addActionListener(e -> {
            calculationSetup.setScreeningMode(ScreeningMode.ANDERSON);
            updateOpenPlotWindows();
        });
        mrb_screening_anderson.setSelected(true);
        screeningModel.add(mrb_screening_none);
        screeningModels.add(mrb_screening_none);
        screeningModel.add(mrb_screening_LEcuyer);
        screeningModels.add(mrb_screening_LEcuyer);
        screeningModel.add(mrb_screening_anderson);
        screeningModels.add(mrb_screening_anderson);
        calcMenu.add(screeningModel);

        JMenu cfModel = new JMenu("Charge Fraction");
        ButtonGroup cfModels = new ButtonGroup();
        JRadioButtonMenuItem mrb_cf_none = new JRadioButtonMenuItem("None");
        JRadioButtonMenuItem mrb_cf_fixed = new JRadioButtonMenuItem("Fixed");
        JRadioButtonMenuItem mrb_cf_lin = new JRadioButtonMenuItem("Linear");
        mrb_cf_none.addActionListener(e -> {
            calculationSetup.setChargeFractionMode(ChargeFractionMode.NONE);
            updateOpenPlotWindows();
        });
        mrb_cf_fixed.addActionListener(e -> {
            calculationSetup.setChargeFractionMode(ChargeFractionMode.FIXED);
            updateOpenPlotWindows();
        });
        mrb_cf_lin.addActionListener(e -> {
            calculationSetup.setChargeFractionMode(ChargeFractionMode.LINEAR);
            updateOpenPlotWindows();
        });
        mrb_cf_none.setSelected(true);
        mrb_cf_fixed.setEnabled(false);
        mrb_cf_lin.setEnabled(false);
        cfModel.add(mrb_cf_none);
        cfModels.add(mrb_cf_none);
        cfModel.add(mrb_cf_fixed);
        cfModels.add(mrb_cf_fixed);
        cfModel.add(mrb_cf_lin);
        cfModels.add(mrb_cf_lin);
        calcMenu.add(cfModel);

        calcMenu.add(new JSeparator());

        JCheckBoxMenuItem jcbSimIso = new JCheckBoxMenuItem("Simulate Isotopes");
        jcbSimIso.addActionListener(e -> {
            if (!blockEvents) {
                calculationSetup.setSimulateIsotopes(jcbSimIso.isSelected());
                updateOpenPlotWindows();
            }
        });
        calcMenu.add(jcbSimIso);

        jmb.add(calcMenu);

        JMenu targetMenu = new JMenu("Target");

        JMenuItem itemShowTargetConfigurator = new JMenuItem("Show target configurator");
        itemShowTargetConfigurator.addActionListener(e -> targetView.setVisible(true));
        targetMenu.add(itemShowTargetConfigurator);

        JMenuItem itemShowFoilConfigurator = new JMenuItem("Show foil configurator");
        itemShowFoilConfigurator.addActionListener(e -> foilView.setVisible(true));
        targetMenu.add(itemShowFoilConfigurator);

        jmb.add(targetMenu);

        JMenu plotMenu = new JMenu("Plot");

        JCheckBoxMenuItem jcbPlotElements = new JCheckBoxMenuItem("Show Element Contributions");
        jcbPlotElements.addActionListener(e -> {
            if (!blockEvents) {
                calculationSetup.setShowElements(jcbPlotElements.isSelected());
                updateOpenPlotWindows();
            }
        });
        plotMenu.add(jcbPlotElements);

        JCheckBoxMenuItem jcbPlotElementsIsotopes = new JCheckBoxMenuItem("Show Isotope Contributions");
        jcbPlotElementsIsotopes.addActionListener(e -> {
            if (!blockEvents) {
                calculationSetup.setShowIsotopes(jcbPlotElementsIsotopes.isSelected());
                updateOpenPlotWindows();
            }
        });
        plotMenu.add(jcbPlotElementsIsotopes);

        JCheckBoxMenuItem jcbPlotLayers = new JCheckBoxMenuItem("Show Layer Contributions");
        jcbPlotLayers.addActionListener(e -> {
            if (!blockEvents) {
                calculationSetup.setShowLayers(jcbPlotLayers.isSelected());
                updateOpenPlotWindows();
            }
        });
        plotMenu.add(jcbPlotLayers);

        plotMenu.add(new JSeparator());

        JMenuItem itemShowSpectra = new JMenuItem("Show Spectrum Window");
        itemShowSpectra.addActionListener(e -> {
            spectraPlotWindow.setVisible(true);
            updateOpenPlotWindows();
        });
        plotMenu.add(itemShowSpectra);

        JMenuItem itemShowStopping = new JMenuItem("Show Stopping Plotter");
        itemShowStopping.addActionListener(e -> {
            stoppingPlotWindow.setVisible(true);
            updateOpenPlotWindows();
        });
        plotMenu.add(itemShowStopping);

        JMenuItem itemShowPenetration = new JMenuItem("Show Depth Plotter");
        itemShowPenetration.addActionListener(e -> {
            depthPlotWindow.setVisible(true);
            updateOpenPlotWindows();
        });
        plotMenu.add(itemShowPenetration);

        jmb.add(plotMenu);

        JMenu miscMenu = new JMenu("Misc");

        JMenuItem itemKinematics = new JMenuItem("IBA Kinematics");
        itemKinematics.addActionListener(e -> {
            if (!ibaKinematics.isVisible()) {
                ibaKinematics.setVisible(true);
            }
        });
        miscMenu.add(itemKinematics);

        JMenuItem itemStoppingElement = new JMenuItem("Stopping in Elements");
        itemStoppingElement.addActionListener(e -> {
            if (!singleSPCalculator.isVisible()) {
                singleSPCalculator.setVisible(true);
            }
        });
        miscMenu.add(itemStoppingElement);

        jmb.add(miscMenu);

        JMenu gaMenu = new JMenu("DE");

        JMenuItem itemStartGA = new JMenuItem("Start");
        itemStartGA.addActionListener(e -> doGASimulation());
        gaMenu.add(itemStartGA);

        JMenuItem itemStopGA = new JMenuItem("Stop");
        itemStopGA.addActionListener(e -> stopGASimulation());
        gaMenu.add(itemStopGA);

        JMenuItem itemBatchGA = new JMenuItem("Run Batch");
        itemBatchGA.addActionListener(e -> doBatchGASimulation(null, null));
        gaMenu.add(itemBatchGA);

        gaMenu.add(new JSeparator());

        JMenuItem itemMakeGAConstrains = new JMenuItem("Settings");
        itemMakeGAConstrains.addActionListener(e -> makeGACsFromCurrentSetting());
        gaMenu.add(itemMakeGAConstrains);

        gaMenu.add(new JSeparator());

        JMenuItem itemShowEAStatus = new JMenuItem("Show status");
        itemShowEAStatus.addActionListener(e -> eaStatusWindow.setVisible(true));
        gaMenu.add(itemShowEAStatus);

        JMenuItem itemShowFitness = new JMenuItem("Show fitness trend");
        itemShowFitness.addActionListener(e -> {
            fitnessPlotWindow.setVisible(true);
        });
        gaMenu.add(itemShowFitness);

        JMenuItem itemShowParameters = new JMenuItem("Show fit results");
        itemShowParameters.addActionListener(e -> {
            parameterPlotWindow.setVisible(true);
        });
        gaMenu.add(itemShowParameters);

        JMenu uncertaintyMenu = new JMenu("Uncertainty calculation");

        JMenuItem itemShowUncertaintySettings = new JMenuItem("Settings");
        itemShowUncertaintySettings.addActionListener(e -> uncertaintyEngine.showInputWindow());
        JMenuItem itemStartUncertaintyCalculation = new JMenuItem("Start Calculation");
        itemStartUncertaintyCalculation.addActionListener(e -> doUncertaintyGASimulation());
        JMenuItem itemStopUncertaintyCalculation = new JMenuItem("Stop Calculation");
        itemStopUncertaintyCalculation.addActionListener(e -> stopGASimulation());
        JMenuItem itemShowUncertaintyPlotWindow = new JMenuItem("Show Output Window");
        itemShowUncertaintyPlotWindow.addActionListener(e -> uncertaintyEngine.showPlotWindow());

        uncertaintyMenu.add(itemShowUncertaintySettings);
        uncertaintyMenu.add(itemStartUncertaintyCalculation);
        uncertaintyMenu.add(itemStopUncertaintyCalculation);
        uncertaintyMenu.add(itemShowUncertaintyPlotWindow);

        gaMenu.add(new JSeparator());
        gaMenu.add(uncertaintyMenu);

        jmb.add(gaMenu);

        this.setJMenuBar(jmb);

        jcbPlotElements.setSelected(calculationSetup.isShowElements());
        jcbPlotElementsIsotopes.setSelected(calculationSetup.isShowIsotopes());
        jcbPlotLayers.setSelected(calculationSetup.isShowLayers());
        jcbUseLookUpTable.setSelected(calculationSetup.isUseLookUpTable());
        jcbSimIso.setSelected(calculationSetup.isSimulateIsotopes());
    }


    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        rootPanel = new JPanel();
        rootPanel.setLayout(new GridLayoutManager(3, 1, new Insets(5, 5, 5, 5), -1, -1));
        pnlSetup = new JPanel();
        pnlSetup.setLayout(new GridLayoutManager(3, 2, new Insets(5, 5, 5, 5), -1, -1));
        rootPanel.add(pnlSetup, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pnlSetup.setBorder(BorderFactory.createTitledBorder(null, "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(6, 3, new Insets(5, 5, 5, 5), -1, -1));
        pnlSetup.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-39936)), "Incident Beam", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, new Color(-4473925)));
        tfExpE0 = new JTextField();
        tfExpE0.setText("");
        panel1.add(tfExpE0, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("E0 (keV)");
        panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("dE0 (keV)");
        panel1.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfExpDE0 = new JTextField();
        panel1.add(tfExpDE0, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Z1");
        panel1.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfExpZ1 = new JTextField();
        panel1.add(tfExpZ1, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("M1");
        panel1.add(label4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cBoxExpM1 = new JComboBox();
        panel1.add(cBoxExpM1, new GridConstraints(3, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Charge (µC)");
        panel1.add(label5, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfExpCharge = new JTextField();
        panel1.add(tfExpCharge, new GridConstraints(4, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        tfExpChargeMin = new JTextField();
        tfExpChargeMin.setForeground(new Color(-14905123));
        panel1.add(tfExpChargeMin, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfExpChargeMax = new JTextField();
        tfExpChargeMax.setForeground(new Color(-14905123));
        panel1.add(tfExpChargeMax, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setForeground(new Color(-14905123));
        label6.setText("  [min,max]");
        panel1.add(label6, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(3, 2, new Insets(5, 5, 5, 5), -1, -1));
        pnlSetup.add(panel2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-39936)), "Geometry", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$(null, -1, -1, panel2.getFont()), new Color(-4473925)));
        final JLabel label7 = new JLabel();
        label7.setText("alpha");
        panel2.add(label7, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfExpAlpha = new JTextField();
        panel2.add(tfExpAlpha, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("beta");
        panel2.add(label8, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tFExpBeta = new JTextField();
        tFExpBeta.setEnabled(false);
        panel2.add(tFExpBeta, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Theta");
        panel2.add(label9, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfExpTheta = new JTextField();
        panel2.add(tfExpTheta, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(7, 3, new Insets(5, 5, 5, 5), -1, -1));
        panel3.setVisible(true);
        pnlSetup.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-39936)), "Detector", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$(null, -1, -1, panel3.getFont()), new Color(-4473925)));
        final JLabel label10 = new JLabel();
        label10.setText("dE (keV)");
        panel3.add(label10, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfDetDE = new JTextField();
        panel3.add(tfDetDE, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setText("Calib.-Offset (keV)");
        panel3.add(label11, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfDetCalOffset = new JTextField();
        panel3.add(tfDetCalOffset, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label12 = new JLabel();
        label12.setText("Calib.-Factor (keV)");
        panel3.add(label12, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfDetCalFactor = new JTextField();
        panel3.add(tfDetCalFactor, new GridConstraints(4, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label13 = new JLabel();
        label13.setText("Solid Angle (msr)");
        panel3.add(label13, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfDetSolidAngle = new JTextField();
        panel3.add(tfDetSolidAngle, new GridConstraints(6, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        tfDetDEMin = new JTextField();
        tfDetDEMin.setForeground(new Color(-14905123));
        panel3.add(tfDetDEMin, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfDetDEMax = new JTextField();
        tfDetDEMax.setForeground(new Color(-14905123));
        panel3.add(tfDetDEMax, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label14 = new JLabel();
        label14.setForeground(new Color(-14905123));
        label14.setText("  [min,max]");
        panel3.add(label14, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfDetCalOffsetMin = new JTextField();
        tfDetCalOffsetMin.setForeground(new Color(-14905123));
        panel3.add(tfDetCalOffsetMin, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfDetCalOffsetMax = new JTextField();
        tfDetCalOffsetMax.setForeground(new Color(-14905123));
        panel3.add(tfDetCalOffsetMax, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfDetCalFactorMin = new JTextField();
        tfDetCalFactorMin.setForeground(new Color(-14905123));
        panel3.add(tfDetCalFactorMin, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfDetCalFactorMax = new JTextField();
        tfDetCalFactorMax.setForeground(new Color(-14905123));
        panel3.add(tfDetCalFactorMax, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label15 = new JLabel();
        label15.setForeground(new Color(-14905123));
        label15.setText("  [min,max]");
        panel3.add(label15, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label16 = new JLabel();
        label16.setForeground(new Color(-14905123));
        label16.setText("  [min,max]");
        panel3.add(label16, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(3, 2, new Insets(5, 5, 5, 5), -1, -1));
        panel4.setVisible(true);
        pnlSetup.add(panel4, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-39936)), "Penetration into target", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, new Color(-4473925)));
        final JLabel label17 = new JLabel();
        label17.setText("Unit X");
        panel4.add(label17, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cBoxDPUnitX = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("nm");
        defaultComboBoxModel1.addElement("10^15 at/cm2");
        cBoxDPUnitX.setModel(defaultComboBoxModel1);
        panel4.add(cBoxDPUnitX, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel4.add(spacer1, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        cBoxDPUnitY = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel2 = new DefaultComboBoxModel();
        defaultComboBoxModel2.addElement("keV");
        defaultComboBoxModel2.addElement("keV/nm");
        defaultComboBoxModel2.addElement("eV/10^15 at/cm2");
        cBoxDPUnitY.setModel(defaultComboBoxModel2);
        panel4.add(cBoxDPUnitY, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label18 = new JLabel();
        label18.setText("Unit Y");
        panel4.add(label18, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(4, 2, new Insets(5, 5, 5, 5), -1, -1));
        panel5.setVisible(true);
        pnlSetup.add(panel5, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel5.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-39936)), "Stopping in Compounds (Layer 0)", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$(null, -1, -1, panel5.getFont()), new Color(-4473925)));
        final JLabel label19 = new JLabel();
        label19.setText("Emin (keV)");
        panel5.add(label19, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfSPEMin = new JTextField();
        tfSPEMin.setText("");
        panel5.add(tfSPEMin, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label20 = new JLabel();
        label20.setText("Emax (keV)");
        panel5.add(label20, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfSPEMax = new JTextField();
        panel5.add(tfSPEMax, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label21 = new JLabel();
        label21.setText("Unit");
        panel5.add(label21, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cBoxSPUnit = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel3 = new DefaultComboBoxModel();
        defaultComboBoxModel3.addElement("eV/10^15 at/cm2");
        defaultComboBoxModel3.addElement("keV/nm");
        cBoxSPUnit.setModel(defaultComboBoxModel3);
        panel5.add(cBoxSPUnit, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel5.add(spacer2, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(4, 2, new Insets(5, 5, 5, 5), -1, -1));
        panel6.setVisible(true);
        pnlSetup.add(panel6, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel6.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-39936)), "Fit Quality", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$(null, -1, -1, panel6.getFont()), new Color(-4473925)));
        final JLabel label22 = new JLabel();
        label22.setText("ch_min");
        panel6.add(label22, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tf_ch_min = new JTextField();
        tf_ch_min.setText("300");
        panel6.add(tf_ch_min, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label23 = new JLabel();
        label23.setText("ch_max");
        panel6.add(label23, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tf_ch_max = new JTextField();
        tf_ch_max.setText("900");
        panel6.add(tf_ch_max, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label24 = new JLabel();
        label24.setText("Fitness");
        panel6.add(label24, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lbl_fitness = new JLabel();
        lbl_fitness.setText("---");
        panel6.add(lbl_fitness, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label25 = new JLabel();
        label25.setText("t_sim");
        panel6.add(label25, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblStatus = new JLabel();
        lblStatus.setText("---");
        panel6.add(lblStatus, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        rootPanel.add(spacer3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label26 = new JLabel();
        label26.setText("___");
        rootPanel.add(label26, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }

}
