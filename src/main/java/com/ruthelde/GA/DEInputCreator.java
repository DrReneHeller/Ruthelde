package com.ruthelde.GA;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.ruthelde.GA.Input.DEParameter;

import javax.swing.*;
import javax.swing.border.TitledBorder;
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
        $$$setupUI$$$();
        initComponents();
    }

    private void initComponents() {

        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.setContentPane(rootPanel);

        pack();
        this.setMinimumSize(new Dimension(400, 200));
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - this.getWidth() / 2, dim.height / 2 - this.getHeight() / 2);
    }

    private void close() {
        this.setVisible(false);
    }

    private void createUIComponents() {

        pb_ok = new JButton();
        pb_ok.addActionListener(e -> {

            try {

                int ch_min = Integer.parseInt(tf_ch_min.getText());
                int ch_max = Integer.parseInt(tf_ch_max.getText());

                int N = Integer.parseInt(tf_N.getText());
                double F = Double.parseDouble(tf_F.getText());
                double CR = Double.parseDouble(tf_CR.getText());
                double THR = Double.parseDouble(tf_THR.getText());

                double maxT = Double.parseDouble(tf_t_max.getText());
                int maxGen = Integer.parseInt(tf_max_gen.getText());
                double minFit = Double.parseDouble(tf_min_fit.getText());

                double tIso = Double.parseDouble(tf_iso.getText());
                int bins = Integer.parseInt(tf_binning.getText());

                deParameter.startCH = ch_min;
                deParameter.endCH = ch_max;

                deParameter.populationSize = N;
                deParameter.F = F;
                deParameter.CR = CR;
                deParameter.THR = THR;
                deParameter.endTime = maxT;
                deParameter.endGeneration = maxGen;
                deParameter.endFitness = minFit;
                deParameter.isotopeTime = tIso;
                deParameter.numBins = bins;

            } catch (Exception ex) {
                System.out.println("Failed to parse value: " + ex.toString());
            }

            close();
        });
    }

    public void setGAInput(DEParameter deParameter) {

        this.deParameter = deParameter;

        tf_ch_min.setText("" + deParameter.startCH);
        tf_ch_max.setText("" + deParameter.endCH);
        tf_N.setText("" + deParameter.populationSize);
        tf_max_gen.setText("" + (int) deParameter.endGeneration);
        tf_binning.setText("" + deParameter.numBins);
        tf_F.setText(String.format("%.2f", deParameter.F).replace(",", "."));
        tf_CR.setText(String.format("%.2f", deParameter.CR).replace(",", "."));
        tf_THR.setText(String.format("%.2f", deParameter.THR).replace(",", "."));
        tf_t_max.setText(String.format("%.2f", deParameter.endTime).replace(",", "."));
        tf_min_fit.setText(String.format("%.2f", deParameter.endFitness).replace(",", "."));
        tf_iso.setText(String.format("%.2f", deParameter.isotopeTime).replace(",", "."));

    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        rootPanel = new JPanel();
        rootPanel.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
        pnlFitPara = new JPanel();
        pnlFitPara.setLayout(new GridLayoutManager(6, 4, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(pnlFitPara, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        pnlFitPara.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-1)), "DE input", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label1 = new JLabel();
        label1.setText("N");
        pnlFitPara.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tf_N = new JTextField();
        tf_N.setText("20");
        pnlFitPara.add(tf_N, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("CR");
        pnlFitPara.add(label2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tf_CR = new JTextField();
        pnlFitPara.add(tf_CR, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("ch_min");
        pnlFitPara.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tf_ch_min = new JTextField();
        pnlFitPara.add(tf_ch_min, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("ch_max");
        pnlFitPara.add(label4, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tf_ch_max = new JTextField();
        pnlFitPara.add(tf_ch_max, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("F");
        pnlFitPara.add(label5, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tf_F = new JTextField();
        pnlFitPara.add(tf_F, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        tf_THR = new JTextField();
        pnlFitPara.add(tf_THR, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("THR");
        pnlFitPara.add(label6, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("max. time (s)");
        pnlFitPara.add(label7, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tf_t_max = new JTextField();
        pnlFitPara.add(tf_t_max, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("max. gen.");
        pnlFitPara.add(label8, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tf_max_gen = new JTextField();
        pnlFitPara.add(tf_max_gen, new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("min. fitness");
        pnlFitPara.add(label9, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tf_min_fit = new JTextField();
        pnlFitPara.add(tf_min_fit, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("isotopes (s)");
        pnlFitPara.add(label10, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tf_iso = new JTextField();
        pnlFitPara.add(tf_iso, new GridConstraints(4, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setText("binning");
        pnlFitPara.add(label11, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tf_binning = new JTextField();
        pnlFitPara.add(tf_binning, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        pb_ok.setText("Apply");
        rootPanel.add(pb_ok, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        rootPanel.add(spacer1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        rootPanel.add(spacer2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }
}
