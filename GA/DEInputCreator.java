package GA;

import GA.Input.DEParameter;

import javax.swing.*;
import java.awt.*;

public class DEInputCreator extends JFrame {

    private JPanel rootPanel, pnlFitPara;
    private JButton pb_ok;
    private JTextField tf_ch_max;
    private JTextField tf_ch_min;
    private JTextField tf_N;
    private JTextField tf_F;
    private JTextField tf_CR;
    private JTextField tf_THR;
    private JTextField tf_t_max;
    private JTextField tf_max_gen;
    private JTextField tf_min_fit;
    private JTextField tf_iso;
    private JTextField tf_binning;

    private DEParameter deParameter;

    public DEInputCreator() {

        super("Define DE input");
        initComponents();
    }

    private void initComponents(){

        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.setContentPane(rootPanel);

        pack();
        this.setMinimumSize(new Dimension(400,200));
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - this.getWidth() / 2, dim.height / 2 - this.getHeight() / 2);
    }

    private void close(){
        this.setVisible(false);
    }

    private void createUIComponents() {

        pb_ok = new JButton();
        pb_ok.addActionListener(e -> {

            try {

                int ch_min = Integer.parseInt(tf_ch_min.getText());
                int ch_max = Integer.parseInt(tf_ch_max.getText());

                int    N   = Integer.parseInt(tf_N.getText())     ;
                double F   = Double.parseDouble(tf_F.getText())   ;
                double CR  = Double.parseDouble(tf_CR.getText())  ;
                double THR = Double.parseDouble(tf_THR.getText()) ;

                double maxT   = Double.parseDouble(tf_t_max.getText())   ;
                int    maxGen = Integer.parseInt(tf_max_gen.getText())   ;
                double minFit = Double.parseDouble(tf_min_fit.getText()) ;

                double tIso = Double.parseDouble(tf_iso.getText())   ;
                int    bins = Integer.parseInt(tf_binning.getText()) ;

                deParameter.startCH = ch_min ;
                deParameter.endCH   = ch_max ;

                deParameter.populationSize = N      ;
                deParameter.F              = F      ;
                deParameter.CR             = CR     ;
                deParameter.THR            = THR    ;
                deParameter.endTime        = maxT   ;
                deParameter.endGeneration  = maxGen ;
                deParameter.endFitness     = minFit ;
                deParameter.isotopeTime    = tIso   ;
                deParameter.numBins        = bins   ;

            } catch (Exception ex) { System.out.println("Failed to parse value: " + ex.toString()); }

            close();
        });
    }

    public void setGAInput(DEParameter deParameter){

        this.deParameter = deParameter;
        
        tf_ch_min.setText("" + deParameter.startCH);
        tf_ch_max.setText("" + deParameter.endCH);
        tf_N.setText("" + deParameter.populationSize);
        tf_max_gen.setText("" + (int)deParameter.endGeneration);
        tf_binning.setText("" + deParameter.numBins);
        tf_F.setText(String.format("%.2f" , deParameter.F).replace(",","."));
        tf_CR.setText(String.format("%.2f" , deParameter.CR).replace(",","."));
        tf_THR.setText(String.format("%.2f" , deParameter.THR).replace(",","."));
        tf_t_max.setText(String.format("%.2f" , deParameter.endTime).replace(",","."));
        tf_min_fit.setText(String.format("%.2f" , deParameter.endFitness).replace(",","."));
        tf_iso.setText(String.format("%.2f" , deParameter.isotopeTime).replace(",","."));

    }
}
