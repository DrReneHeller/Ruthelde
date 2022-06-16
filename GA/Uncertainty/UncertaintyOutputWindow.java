package GA.Uncertainty;

import Helper.Plot.PlotSeries;
import Helper.Plot.PlotWindow;
import Target.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;

public class UncertaintyOutputWindow extends JFrame{

    private JPanel rootPanel;
    private JTextArea ta_output;
    private PlotWindow dataPlot, statsPlot;
    private int selection;
    private UncertaintyInput lastInput;
    private String lastFolder;

    LinkedList<UncertaintyDataEntry> data;

    public UncertaintyOutputWindow(){

        super("Uncertainty Calculation Output");
        initComponents();
        dataPlot  = new PlotWindow("Uncertainty Plot - Data");
        statsPlot = new PlotWindow("Uncertainty Plot - Statistics");
        lastFolder = null;
    }

    public void setData(LinkedList<UncertaintyDataEntry> data){
        this.data = data;
    }

    public void update(UncertaintyInput input){

        lastInput = input;
        int size = data.size();
        StringBuilder sb = new StringBuilder();
        LinkedList<PlotSeries> plotSeries = new LinkedList<>();

        UncertaintyDataEntry lastEntry = data.getLast();

        sb.append("  \n\r" );
        sb.append("  Param. set \t = " + (lastEntry.parameterID+1) + "\n\r");
        sb.append("  Spectrum \t = " + (lastEntry.spectrumID+1) + " of " + input.numberOfSpectra + "\n\r");
        sb.append("  Fit \t = " + (lastEntry.fitID+1) + " of " + input.numberOfFits + "\n\r\n\r");
        sb.append("  [Q=" + String.format("%.2f" , lastEntry.q_set).replace(",",".") + ", ");
        sb.append("  dE=" + String.format("%.2f" , lastEntry.res_set).replace(",",".") + ", ");
        sb.append("  E0=" + String.format("%.2f" , lastEntry.E0).replace(",",".") + ", ");
        sb.append("  alpha=" + String.format("%.2f" , lastEntry.alpha).replace(",",".") + ", ");
        sb.append("  Theta=" + String.format("%.2f" , lastEntry.theta).replace(",",".") + "]\n\r");

        sb.append("\n\r");
        sb.append("  Statistics: \n\r");
        sb.append("\n\r");

        Target target = data.getFirst().target;

        int numPlots  = 0;
        int plotIndex = 0;

        for (Layer layer : target.getLayerList()){

            numPlots++;
            for (Element ignored : layer.getElementList()){ numPlots++; }
        }

        numPlots += 4;

        int index = 0;
        PlotSeries ps;
        float h;
        double[] x = new double[size];
        double[] y;
        double mean, std;

        for (UncertaintyDataEntry entry : data){

            switch (selection){
                case 0:
                    x[index] = entry.q_set;
                    break;
                case 1:
                    x[index] = entry.E0;
                    break;
                case 2:
                    x[index] = entry.res_set;
                    break;
                case 3:
                    x[index] = entry.alpha;
                    break;
                case 4:
                    x[index] = entry.theta;
                    break;
                default:
                    break;
            }

            index++;
        }

        int layerIndex = 0;

        for (Layer layer : target.getLayerList()){

            sb.append("    Layer " + (layerIndex+1) + " [mean: ");

            mean = 0.0d;

            y = new double[size];
            index = 0;

            for (UncertaintyDataEntry entry : data){

                double AD = entry.target.getLayerList().get(layerIndex).getArealDensity();
                mean += AD;
                y[index] = AD;
                index++;
            }

            mean /= size;

            String layerName = "Layer " + (layerIndex+1) + "AD";
            ps = new PlotSeries(layerName, x, y);
            ps.seriesProperties.showLine = true;
            ps.seriesProperties.stroke = 3;
            ps.seriesProperties.symbol.size = 8;
            ps.seriesProperties.showSymbols = true;
            h = (float) plotIndex / (float) numPlots + 0.33f;
            ps.setColor(new Color(Color.HSBtoRGB(h, 1, 1)));
            plotSeries.add(ps);
            plotIndex++;

            sb.append(String.format("%.2f" , mean).replace(",",".") + ", std: ");

            std = 0.0d;

            for (UncertaintyDataEntry entry : data){

                std += Math.pow(mean - entry.target.getLayerList().get(layerIndex).getArealDensity(),2);
            }

            std = Math.sqrt(std/size);

            sb.append(String.format("%.2f" , std).replace(",",".") + "]\n\r");

            sb.append("\n\r");

            int elementIndex = 0;

            for (Element element : layer.getElementList()){

                sb.append("        " + element.getName() + " [mean: ");

                mean = 0.0d;
                y = new double[size];
                index = 0;

                for (UncertaintyDataEntry entry : data){

                    double ratio = entry.target.getLayerList().get(layerIndex).getElementList().get(elementIndex).getRatio();
                    mean += ratio;
                    y[index] = ratio;
                    index++;
                }

                mean /= size;

                String elementName = "Layer " + (layerIndex+1) + " " + element.getName();
                ps = new PlotSeries(elementName, x, y);
                ps.seriesProperties.showLine = true;
                ps.seriesProperties.stroke = 3;
                ps.seriesProperties.symbol.size = 8;
                ps.seriesProperties.showSymbols = true;
                ps.setDashed(true);
                h = (float) plotIndex / (float) numPlots + 0.33f;
                ps.setColor(new Color(Color.HSBtoRGB(h, 1, 1)));
                plotSeries.add(ps);
                plotIndex++;

                sb.append(String.format("%.2f" , mean).replace(",",".") + ", std: ");

                std = 0.0d;

                for (UncertaintyDataEntry entry : data){

                    std += Math.pow(mean - entry.target.getLayerList().get(layerIndex).getElementList().get(elementIndex).getRatio(),2);
                }

                std = Math.sqrt(std/size);

                sb.append(String.format("%.2f" , std).replace(",",".") + "]\n\r");

                elementIndex++;
            }

            sb.append("\n\r");

            layerIndex++;
        }

        index = 0;
        y = new double[size];

        for (UncertaintyDataEntry entry : data){

            double q_fit = entry.q_fit;
            y[index] = q_fit;
            index++;
        }

        String name = "Charge";
        ps = new PlotSeries(name, x, y);
        ps.seriesProperties.showLine = true;
        ps.seriesProperties.stroke = 3;
        ps.seriesProperties.symbol.size = 8;
        ps.seriesProperties.showSymbols = true;
        h = (float) plotIndex / (float) numPlots + 0.33f;
        ps.setColor(new Color(Color.HSBtoRGB(h, 1, 1)));
        plotSeries.add(ps);
        plotIndex++;

        index = 0;
        y = new double[size];

        for (UncertaintyDataEntry entry : data){

            double dE = entry.res_fit;
            y[index] = dE;
            index++;
        }

        name = "dE";
        ps = new PlotSeries(name, x, y);
        ps.seriesProperties.showLine = true;
        ps.seriesProperties.stroke = 3;
        ps.seriesProperties.symbol.size = 8;
        ps.seriesProperties.showSymbols = true;
        h = (float) plotIndex / (float) numPlots + 0.33f;
        ps.setColor(new Color(Color.HSBtoRGB(h, 1, 1)));
        plotSeries.add(ps);
        plotIndex++;

        index = 0;
        y = new double[size];

        mean = 0.0d;
        std = 0.0d;

        for (UncertaintyDataEntry entry : data){

            double a = entry.calFactor;
            y[index] = a;
            mean += a;
            index++;
        }

        mean /= size;
        for (UncertaintyDataEntry entry : data){ std += Math.pow(mean - entry.calFactor,2); }
        std = Math.sqrt(std/size);

        sb.append("\n\r");
        sb.append("  Cal.-Factor: [mean = " + String.format("%.2f" , mean).replace(",","."));
        sb.append(", std = " + String.format("%.2f" , std).replace(",",".") + "]\n\r");

        name = "Cal.-factor";
        ps = new PlotSeries(name, x, y);
        ps.seriesProperties.showLine = true;
        ps.seriesProperties.stroke = 3;
        ps.seriesProperties.symbol.size = 8;
        ps.seriesProperties.showSymbols = true;
        h = (float) plotIndex / (float) numPlots + 0.33f;
        ps.setColor(new Color(Color.HSBtoRGB(h, 1, 1)));
        plotSeries.add(ps);
        plotIndex++;

        index = 0;
        y = new double[size];
        mean = 0.0d;
        std = 0.0d;

        for (UncertaintyDataEntry entry : data){

            double b = entry.calOffset;
            y[index] = b;
            mean += b;
            index++;
        }

        mean /= size;
        for (UncertaintyDataEntry entry : data){ std += Math.pow(mean - entry.calOffset,2); }
        std = Math.sqrt(std/size);

        sb.append("  Cal.-Offset: [mean = " + String.format("%.2f" , mean).replace(",","."));
        sb.append(", std = " + String.format("%.2f" , std).replace(",",".") + "]\n\r");

        name = "Cal.-offset";
        ps = new PlotSeries(name, x, y);
        ps.seriesProperties.showLine = true;
        ps.seriesProperties.stroke = 3;
        ps.seriesProperties.symbol.size = 8;
        ps.seriesProperties.showSymbols = true;
        h = (float) plotIndex / (float) numPlots + 0.33f;
        ps.setColor(new Color(Color.HSBtoRGB(h, 1, 1)));
        plotSeries.add(ps);
        plotIndex++;


        ta_output.setText(sb.toString());

        dataPlot.setPlotSeries(plotSeries);
        dataPlot.getPlotProperties().xAxisName = "Input Parameter";
        dataPlot.getPlotProperties().yAxisName = "Fit values";
        dataPlot.refresh();



        //Now do statistics within fits/spectra

        if (input.numberOfFits + input.numberOfSpectra > 2 &&
            data.getLast().fitID == input.numberOfFits-1 &&
            data.getLast().spectrumID == input.numberOfSpectra-1) {

            int maxParaID = data.getLast().parameterID;
            int paraID    = data.getFirst().parameterID;
            LinkedList<PlotSeries> plotSeries2 = new LinkedList<>();

            plotIndex = 0;
            layerIndex  = 0;

            x = new double[maxParaID+1];

            while (paraID <= maxParaID){

                switch (selection) {
                    case 0:
                        x[paraID] = data.get(paraID * input.numberOfFits * input.numberOfSpectra).q_set;
                        break;
                    case 1:
                        x[paraID] = data.get(paraID * input.numberOfFits * input.numberOfSpectra).E0;
                        break;
                    case 2:
                        x[paraID] = data.get(paraID * input.numberOfFits * input.numberOfSpectra).res_set;
                        break;
                    case 3:
                        x[paraID] = data.get(paraID * input.numberOfFits * input.numberOfSpectra).alpha;
                        break;
                    case 4:
                        x[paraID] = data.get(paraID * input.numberOfFits * input.numberOfSpectra).theta;
                        break;
                    default:
                        break;
                }

                paraID++;
            }

            for (Layer layer : data.getFirst().target.getLayerList()) {

                y = new double[maxParaID+1];

                paraID = data.getFirst().parameterID;

                while (paraID <= maxParaID) {

                    mean = 0.0d;
                    std = 0.0d;

                    for (UncertaintyDataEntry entry : data) {

                        if (entry.parameterID == paraID) {
                            mean += entry.target.getLayerList().get(layerIndex).getArealDensity();
                        }
                    }

                    mean /= (input.numberOfFits * input.numberOfSpectra);

                    for (UncertaintyDataEntry entry : data) {

                        if (entry.parameterID == paraID) {
                            std += Math.pow(mean - entry.target.getLayerList().get(layerIndex).getArealDensity(), 2);
                        }
                    }

                    std = Math.sqrt(std / (input.numberOfFits * input.numberOfSpectra));

                    y[paraID] = std;

                    paraID++;
                }

                name = "Layer " + layerIndex + " AD";
                ps = new PlotSeries(name, x, y);
                ps.seriesProperties.showLine = true;
                ps.seriesProperties.stroke = 3;
                ps.seriesProperties.symbol.size = 8;
                ps.seriesProperties.showSymbols = true;
                h = (float) plotIndex / (float) numPlots + 0.33f;
                ps.setColor(new Color(Color.HSBtoRGB(h, 1, 1)));
                plotSeries2.add(ps);
                plotIndex++;

                int elementIndex = 0;

                for(Element element : layer.getElementList()){

                    y = new double[maxParaID+1];

                    paraID = data.getFirst().parameterID;

                    while (paraID <= maxParaID) {

                        mean = 0.0d;
                        std = 0.0d;

                        for (UncertaintyDataEntry entry : data) {

                            if (entry.parameterID == paraID) {
                                mean += entry.target.getLayerList().get(layerIndex).getElementList().get(elementIndex).getRatio();
                            }
                        }

                        mean /= (input.numberOfFits * input.numberOfSpectra);

                        for (UncertaintyDataEntry entry : data) {

                            if (entry.parameterID == paraID) {
                                std += Math.pow(mean - entry.target.getLayerList().get(layerIndex).getElementList().get(elementIndex).getRatio(), 2);
                            }
                        }

                        std = Math.sqrt(std / (input.numberOfFits * input.numberOfSpectra));

                        y[paraID] = std;

                        paraID++;
                    }

                    name = "Layer " + layerIndex + " " + element.getName();
                    ps = new PlotSeries(name, x, y);
                    ps.seriesProperties.showLine = true;
                    ps.seriesProperties.stroke = 3;
                    ps.seriesProperties.symbol.size = 8;
                    ps.seriesProperties.showSymbols = true;
                    h = (float) plotIndex / (float) numPlots + 0.33f;
                    ps.setColor(new Color(Color.HSBtoRGB(h, 1, 1)));
                    plotSeries2.add(ps);
                    plotIndex++;

                    elementIndex++;
                }

                layerIndex++;
            }

            y = new double[maxParaID+1];

            paraID = data.getFirst().parameterID;

            while (paraID <= maxParaID) {

                mean = 0.0d;
                std = 0.0d;

                for (UncertaintyDataEntry entry : data) {

                    if (entry.parameterID == paraID) {
                        mean += entry.q_fit;
                    }
                }

                mean /= (input.numberOfFits * input.numberOfSpectra);

                for (UncertaintyDataEntry entry : data) {

                    if (entry.parameterID == paraID) {
                        std += Math.pow(mean - entry.q_fit, 2);
                    }
                }

                std = Math.sqrt(std / (input.numberOfFits * input.numberOfSpectra));

                y[paraID] = std;

                paraID++;
            }

            name = "Charge";
            ps = new PlotSeries(name, x, y);
            ps.seriesProperties.showLine = true;
            ps.seriesProperties.stroke = 3;
            ps.seriesProperties.symbol.size = 8;
            ps.seriesProperties.showSymbols = true;
            h = (float) plotIndex / (float) numPlots + 0.33f;
            ps.setColor(new Color(Color.HSBtoRGB(h, 1, 1)));
            plotSeries2.add(ps);
            plotIndex++;

            y = new double[maxParaID+1];

            paraID = data.getFirst().parameterID;

            while (paraID <= maxParaID) {

                mean = 0.0d;
                std = 0.0d;

                for (UncertaintyDataEntry entry : data) {

                    if (entry.parameterID == paraID) {
                        mean += entry.res_fit;
                    }
                }

                mean /= (input.numberOfFits * input.numberOfSpectra);

                for (UncertaintyDataEntry entry : data) {

                    if (entry.parameterID == paraID) {
                        std += Math.pow(mean - entry.res_fit, 2);
                    }
                }

                std = Math.sqrt(std / (input.numberOfFits * input.numberOfSpectra));

                y[paraID] = std;

                paraID++;
            }

            name = "dE";
            ps = new PlotSeries(name, x, y);
            ps.seriesProperties.showLine = true;
            ps.seriesProperties.stroke = 3;
            ps.seriesProperties.symbol.size = 8;
            ps.seriesProperties.showSymbols = true;
            h = (float) plotIndex / (float) numPlots + 0.33f;
            ps.setColor(new Color(Color.HSBtoRGB(h, 1, 1)));
            plotSeries2.add(ps);
            plotIndex++;

            y = new double[maxParaID+1];

            paraID = data.getFirst().parameterID;

            while (paraID <= maxParaID) {

                mean = 0.0d;
                std = 0.0d;

                for (UncertaintyDataEntry entry : data) {

                    if (entry.parameterID == paraID) {
                        mean += entry.calFactor;
                    }
                }

                mean /= (input.numberOfFits * input.numberOfSpectra);

                for (UncertaintyDataEntry entry : data) {

                    if (entry.parameterID == paraID) {
                        std += Math.pow(mean - entry.calFactor, 2);
                    }
                }

                std = Math.sqrt(std / (input.numberOfFits * input.numberOfSpectra));

                y[paraID] = std;

                paraID++;
            }

            name = "Cal.-Factor" ;
            ps = new PlotSeries(name, x, y);
            ps.seriesProperties.showLine = true;
            ps.seriesProperties.stroke = 3;
            ps.seriesProperties.symbol.size = 8;
            ps.seriesProperties.showSymbols = true;
            h = (float) plotIndex / (float) numPlots + 0.33f;
            ps.setColor(new Color(Color.HSBtoRGB(h, 1, 1)));
            plotSeries2.add(ps);
            plotIndex++;

            y = new double[maxParaID+1];

            paraID = data.getFirst().parameterID;

            while (paraID <= maxParaID) {

                mean = 0.0d;
                std = 0.0d;

                for (UncertaintyDataEntry entry : data) {

                    if (entry.parameterID == paraID) {
                        mean += entry.calOffset;
                    }
                }

                mean /= (input.numberOfFits * input.numberOfSpectra);

                for (UncertaintyDataEntry entry : data) {

                    if (entry.parameterID == paraID) {
                        std += Math.pow(mean - entry.calOffset, 2);
                    }
                }

                std = Math.sqrt(std / (input.numberOfFits * input.numberOfSpectra));

                y[paraID] = std;

                paraID++;
            }

            name = "Cal.-Offset";
            ps = new PlotSeries(name, x, y);
            ps.seriesProperties.showLine = true;
            ps.seriesProperties.stroke = 3;
            ps.seriesProperties.symbol.size = 8;
            ps.seriesProperties.showSymbols = true;
            h = (float) plotIndex / (float) numPlots + 0.33f;
            ps.setColor(new Color(Color.HSBtoRGB(h, 1, 1)));
            plotSeries2.add(ps);

            statsPlot.setPlotSeries(plotSeries2);
            statsPlot.getPlotProperties().xAxisName = "Input Parameter";
            statsPlot.getPlotProperties().yAxisName = "Std of fit values";
            statsPlot.refresh();
        }

    }

    public void clear(){

        LinkedList<PlotSeries> plotSeries = new LinkedList<>();

        dataPlot.setPlotSeries(plotSeries);
        dataPlot.getPlotProperties().xAxisName = "Input Parameter";
        dataPlot.getPlotProperties().yAxisName = "Fit values";
        dataPlot.refresh();

        statsPlot.setPlotSeries(plotSeries);
        statsPlot.getPlotProperties().xAxisName = "Input Parameter";
        statsPlot.getPlotProperties().yAxisName = "Std of fit values";
        statsPlot.refresh();

        ta_output.setText("");
    }

    private void exportData() {

        StringBuilder sb = new StringBuilder();

        sb.append("Uncertainty Data File \n\r\n\r");
        sb.append("Columns  \n\r\n\r");

        int columnCounter = 1;

        sb.append("  Column " + columnCounter + " \t = Parameter ID \n\r");
        columnCounter++;
        sb.append("  Column " + columnCounter + " \t = Spectrum ID \n\r");
        columnCounter++;
        sb.append("  Column " + columnCounter + " \t = Fit ID \n\r");
        columnCounter++;
        sb.append("  Column " + columnCounter + " \t = Charge (set value) \n\r");
        columnCounter++;
        sb.append("  Column " + columnCounter + " \t = dE (set value) \n\r");
        columnCounter++;
        sb.append("  Column " + columnCounter + " \t = E0 (set value) \n\r");
        columnCounter++;
        sb.append("  Column " + columnCounter + " \t = alpha (set value) \n\r");
        columnCounter++;
        sb.append("  Column " + columnCounter + " \t = Theta (set value) \n\r");
        columnCounter++;
        sb.append("  Column " + columnCounter + " \t = Cal.-Factor (fitted value) \n\r");
        columnCounter++;
        sb.append("  Column " + columnCounter + " \t = Cal.-Offset (fitted value) \n\r");
        columnCounter++;
        sb.append("  Column " + columnCounter + " \t = Charge (fitted value) \n\r");
        columnCounter++;
        sb.append("  Column " + columnCounter + " \t = dE (fitted value) \n\r");
        columnCounter++;

        int layerIndex = 1;

        for (Layer layer : data.getFirst().target.getLayerList()){

            sb.append("  Column " + columnCounter + " \t = Layer " + layerIndex + " AD (fitted value) \n\r");
            columnCounter++;

            for (Element element : layer.getElementList()){

                sb.append("  Column " + columnCounter + " \t = Layer " + layerIndex + " " + element.getName() + " ratio (fitted value) \n\r");
                columnCounter++;
            }

            layerIndex++;
        }

        sb.append("\n\r\n\r");
        sb.append("Data \n\r\n\r");

        for (UncertaintyDataEntry entry : data){

            sb.append(String.format("%.4e" , (double) entry.parameterID ).replace(",",".") + "\t");
            sb.append(String.format("%.4e" , (double) entry.spectrumID  ).replace(",",".") + "\t");
            sb.append(String.format("%.4e" , (double) entry.fitID       ).replace(",",".") + "\t");

            sb.append(String.format("%.4e" , entry.q_set    ).replace(",",".") + "\t");
            sb.append(String.format("%.4e" , entry.res_set  ).replace(",",".") + "\t");
            sb.append(String.format("%.4e" , entry.E0       ).replace(",",".") + "\t");
            sb.append(String.format("%.4e" , entry.alpha    ).replace(",",".") + "\t");
            sb.append(String.format("%.4e" , entry.theta    ).replace(",",".") + "\t");
            sb.append(String.format("%.4e" , entry.calFactor).replace(",",".") + "\t");
            sb.append(String.format("%.4e" , entry.calOffset).replace(",",".") + "\t");
            sb.append(String.format("%.4e" , entry.q_fit    ).replace(",",".") + "\t");
            sb.append(String.format("%.4e" , entry.res_fit  ).replace(",",".") + "\t");

            for (Layer layer : entry.target.getLayerList()){

                sb.append(String.format("%.4e" , layer.getArealDensity()).replace(",",".") + "\t");

                for (Element element : layer.getElementList()){

                    sb.append(String.format("%.4e" , element.getRatio()).replace(",",".") + "\t");
                }
            }

            sb.append("\n\r");
        }

        try {
            final JFileChooser fc;
            if (lastFolder != null) fc = new JFileChooser(lastFolder);
            else fc = new JFileChooser();
            int returnVal = fc.showSaveDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {

                File file = fc.getSelectedFile();
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                writer.write(sb.toString());
                writer.close();
                lastFolder = file.getParent();
            }

        } catch (Exception ex) {
            System.out.println("Error writing ASCII file: " + ex.getMessage());
        }
    }

    private void initComponents(){

        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.setContentPane(rootPanel);

        createUIComponents();

        pack();
        this.setMinimumSize(new Dimension(400,200));
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - this.getWidth() / 2, dim.height / 2 - this.getHeight() / 2);
        this.setSize(new Dimension(460, 560));
    }

    private void createUIComponents() {

        JMenuBar jmb = new JMenuBar();

        JMenu fileMenu = new JMenu("File");

        JMenuItem itemExportData = new JMenuItem("Export Data");
        itemExportData.addActionListener(e -> exportData());

        fileMenu.add(itemExportData);

        JMenu plotMenu = new JMenu("Plot");

        JMenuItem itemShowData  = new JMenuItem("Show Data");
        itemShowData.addActionListener(e -> dataPlot.setVisible(true));

        JMenuItem itemShowStats = new JMenuItem("Show Statistics");
        itemShowStats.addActionListener(e -> statsPlot.setVisible(true));

        JMenu range               = new JMenu("Range axis")           ;
        ButtonGroup ranges        = new ButtonGroup()                 ;
        JRadioButtonMenuItem ch   = new JRadioButtonMenuItem("Charge");
        JRadioButtonMenuItem e0   = new JRadioButtonMenuItem("E0")    ;
        JRadioButtonMenuItem de   = new JRadioButtonMenuItem("dE")    ;
        JRadioButtonMenuItem al   = new JRadioButtonMenuItem("alpha") ;
        JRadioButtonMenuItem th   = new JRadioButtonMenuItem("Theta") ;

        selection = 0;
        ch.setSelected(true);

        ch.addActionListener(e -> {
            selection = 0;
            update(lastInput);
        });
        e0.addActionListener(e -> {
            selection = 1;
            update(lastInput);
        });
        de.addActionListener(e -> {
            selection = 2;
            update(lastInput);
        });
        al.addActionListener(e -> {
            selection = 3;
            update(lastInput);
        });
        th.addActionListener(e -> {
            selection = 4;
            update(lastInput);
        });

        range.add(ch);
        range.add(e0);
        range.add(de);
        range.add(al);
        range.add(th);

        ranges.add(ch);
        ranges.add(e0);
        ranges.add(de);
        ranges.add(al);
        ranges.add(th);

        plotMenu.add(range);
        plotMenu.add(itemShowData);
        plotMenu.add(itemShowStats);

        jmb.add(fileMenu);
        jmb.add(plotMenu);

        this.setJMenuBar(jmb);
    }
}
