package GA.Uncertainty;

import javax.swing.*;
import java.awt.*;

public class UncertaintyInputWindow extends JFrame {

    private JPanel pnlFitPara;
    private JTextField tf_E0_min;
    private JTextField tf_alpha_min;
    private JTextField tf_E0_max;
    private JTextField tf_alpha_max;
    private JTextField tf_theta_min;
    private JTextField tf_theta_max;
    private JPanel rootPanel;
    private JTextField tf_n_fits;
    private JTextField tf_n_spectra;
    private JTextField tf_q_min;
    private JTextField tf_q_max;
    private JTextField tf_q_var;
    private JTextField tf_dE_min;
    private JTextField tf_dE_max;
    private JTextField tf_dE_var;
    private JButton pbApply;
    private UncertaintyInput input;

    public UncertaintyInputWindow(UncertaintyInput input){

        super("Input for Uncertainty Calculation");
        initComponents();
        this.input = input;

        tf_n_fits.setText(input.numberOfFits + "");
        tf_n_spectra.setText(input.numberOfSpectra + "");
        tf_q_min.setText(String.format("%.2f" , input.q_min).replace(",","."));
        tf_q_max.setText(String.format("%.2f" , input.q_max).replace(",","."));
        tf_q_var.setText(String.format("%.2f" , input.q_var).replace(",","."));
        tf_dE_min.setText(String.format("%.2f" , input.dE_min).replace(",","."));
        tf_dE_max.setText(String.format("%.2f" , input.dE_max).replace(",","."));
        tf_dE_var.setText(String.format("%.2f" , input.dE_var).replace(",","."));
        tf_E0_min.setText(String.format("%.2f" , input.E0_min).replace(",","."));
        tf_E0_max.setText(String.format("%.2f" , input.E0_max).replace(",","."));
        tf_alpha_min.setText(String.format("%.2f" , input.alpha_min).replace(",","."));
        tf_alpha_max.setText(String.format("%.2f" , input.alpha_max).replace(",","."));
        tf_theta_min.setText(String.format("%.2f" , input.theta_min).replace(",","."));
        tf_theta_max.setText(String.format("%.2f" , input.theta_max).replace(",","."));
    }

    private void initComponents(){

        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.setContentPane(rootPanel);

        pack();
        this.setMinimumSize(new Dimension(400,200));
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - this.getWidth() / 2, dim.height / 2 - this.getHeight() / 2);
    }

    private void createUIComponents() {

        pbApply = new JButton();
        pbApply.addActionListener(e -> {
            try{
                int n_fits = Integer.parseInt(tf_n_fits.getText());
                input.numberOfFits = n_fits;

                int n_spectra = Integer.parseInt(tf_n_spectra.getText());
                input.numberOfSpectra = n_spectra;

                double q_min = Double.parseDouble(tf_q_min.getText());
                input.q_min= q_min;

                double q_max = Double.parseDouble(tf_q_max.getText());
                input.q_max= q_max;

                double q_var = Double.parseDouble(tf_q_var.getText());
                input.q_var= q_var;

                double dE_min = Double.parseDouble(tf_dE_min.getText());
                input.dE_min= dE_min;

                double dE_max = Double.parseDouble(tf_dE_max.getText());
                input.dE_max= dE_max;

                double dE_var = Double.parseDouble(tf_dE_var.getText());
                input.dE_var= dE_var;

                double E0_min = Double.parseDouble(tf_E0_min.getText());
                input.E0_min= E0_min;

                double E0_max = Double.parseDouble(tf_E0_max.getText());
                input.E0_max= E0_max;

                double alpha_min = Double.parseDouble(tf_alpha_min.getText());
                input.alpha_min= alpha_min;

                double alpha_max = Double.parseDouble(tf_alpha_max.getText());
                input.alpha_max= alpha_max;

                double theta_min = Double.parseDouble(tf_theta_min.getText());
                input.theta_min= theta_min;

                double theta_max = Double.parseDouble(tf_theta_max.getText());
                input.theta_max= theta_max;

                this.setVisible(false);

            } catch (Exception ex) {
                System.out.println("Error parsing numeric value");
                ex.printStackTrace();
            }
        });

    }
}
