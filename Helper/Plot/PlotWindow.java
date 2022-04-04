package Helper.Plot;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

public class PlotWindow extends JFrame {

    private JPanel rootPanel;
    private JPanel plotPanel;

    private MyPlotGenerator plotGenerator;
    private String lastFolder;

    public PlotWindow(String title){

        super(title);
        plotGenerator = new MyPlotGenerator();
        lastFolder = null;
        initComponents();
    }

    public MyPlotGenerator getPlotGenerator(){
        return plotGenerator;
    }

    public void setPlotSeries(LinkedList<PlotSeries> plotSeries) {
        this.plotGenerator.plotSeries = plotSeries;
    }

    public PlotProperties getPlotProperties(){
        return plotGenerator.plotProperties;
    }

    public LinkedList<PlotSeries> getPlotSeries() {
        return plotGenerator.plotSeries;
    }

    public void setLastFolder(String lastFolder){
        this.lastFolder = lastFolder;
    }

    public void refresh(){

        plotGenerator.render(plotPanel);
    }

    public void saveImage(File file){

        if (file == null) {

            final JFileChooser fc;
            if (lastFolder != null) fc = new JFileChooser(lastFolder);
            else fc = new JFileChooser();
            int returnVal = fc.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = fc.getSelectedFile();
                lastFolder = file.getParent();
            }
        }

        if(file != null){

            BufferedImage image = new BufferedImage(plotPanel.getWidth(), plotPanel.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics g = image.getGraphics();
            plotPanel.paint(g);

            try {
                ImageIO.write(image, "png", file);
            } catch (IOException ex) {
                System.out.println("Error when saving graphics: " + ex.getMessage());
            }
        }

    }

    public void exportAscii(File _file){

        LinkedList<PlotSeries> pss = plotGenerator.plotSeries;

        int numPlots = pss.size();
        int length   = pss.getFirst().data.size();

        Double[][] data = new Double[2*numPlots+1][length];
        StringBuilder sb = new StringBuilder();

        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateTime = formatter.format(date);

        sb.append("<Header> \n");

        sb.append("  " + dateTime + "\n");
        sb.append("  " + this.getTitle() + "\n\n");

        sb.append("  Column 1: \t Line Number \n");

        for (int i=0; i<length; i++) data[0][i] = (double)(i+1);

        int plotIndex = 1;

        for (PlotSeries ps : pss){

            sb.append("  Column " + (2*plotIndex+0) + ": \t " + ps.seriesProperties.name + " (x-Values) \n");
            sb.append("  Column " + (2*plotIndex+1) + ": \t " + ps.seriesProperties.name + " (y-Values) \n");

            for (int i=0; i<length; i++){data[2*plotIndex-1][i]   = ps.data.get(i).x;}
            for (int i=0; i<length; i++){data[2*plotIndex][i] = ps.data.get(i).y;}

            plotIndex++;
        }

        sb.append("</Header> \n\n");

        for (int i=0; i<length; i++){

            sb.append(String.format("%.4e" , (double)(i+1)) + "\t\t" );

            for (int j=0; j<numPlots; j++){
                sb.append(String.format("%.4e" , data[2*j+1][i])   + "\t\t" );
                sb.append(String.format("%.4e" , data[2*j+2][i]) + "\t\t" );
            }

            sb.append("\n");
        }

        if (_file == null) {

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
        } else {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(_file));
                writer.write(sb.toString());
                writer.close();
                lastFolder = _file.getParent();
            } catch (Exception ex) {
                System.out.println("Error writing ASCII file: " + ex.getMessage());
            }

        }
    }

    private void buildMenu(){

        JMenuBar jmb = new JMenuBar();

        JMenu fileMenu = new JMenu("File");

        JMenuItem itemExportAscii = new JMenuItem("Export ASCII");
        itemExportAscii.addActionListener(e -> exportAscii(null));
        fileMenu.add(itemExportAscii);

        JMenuItem itemSavePNG =  new JMenuItem("Save as PNG");
        itemSavePNG.addActionListener(e -> saveImage(null));
        fileMenu.add(itemSavePNG);

        jmb.add(fileMenu);

        JMenu plotMenu = new JMenu("Plot");

        JCheckBoxMenuItem jcbAutoScaleX = new JCheckBoxMenuItem("Auto Scale X");
        jcbAutoScaleX.setSelected(plotGenerator.plotProperties.autoScaleX);
        jcbAutoScaleX.addActionListener(e -> {

            plotGenerator.plotProperties.autoScaleX = jcbAutoScaleX.isSelected();
            refresh();

        });
        plotMenu.add(jcbAutoScaleX);

        JCheckBoxMenuItem jcbAutoScaleY = new JCheckBoxMenuItem("Auto Scale Y");
        jcbAutoScaleY.setSelected(plotGenerator.plotProperties.autoScaleY);
        jcbAutoScaleY.addActionListener(e -> {

            plotGenerator.plotProperties.autoScaleY = jcbAutoScaleY.isSelected();
            refresh();

        });
        plotMenu.add(jcbAutoScaleY);

        plotMenu.add(new JSeparator());

        JCheckBoxMenuItem jcbLogX = new JCheckBoxMenuItem("Log X-Scale");
        jcbLogX.setSelected(plotGenerator.plotProperties.logX);
        JCheckBoxMenuItem jcbLogY = new JCheckBoxMenuItem("Log Y-Scale");
        jcbLogY.setSelected(plotGenerator.plotProperties.logY);
        jcbLogX.addActionListener(e -> {

            plotGenerator.plotProperties.logX = jcbLogX.isSelected();
            refresh();

        });
        jcbLogY.addActionListener(e -> {

            plotGenerator.plotProperties.logY = jcbLogY.isSelected();
            refresh();

        });
        plotMenu.add(jcbLogX);
        plotMenu.add(jcbLogY);

        jmb.add(plotMenu);

        this.setJMenuBar(jmb);
    }

    private void initComponents(){

        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.setContentPane(rootPanel);
        buildMenu();
        pack();
        this.setMinimumSize(new Dimension(400,200));
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - this.getWidth() / 2, dim.height / 2 - this.getHeight() / 2);
    }
}
