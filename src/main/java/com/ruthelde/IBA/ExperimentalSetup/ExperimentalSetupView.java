package com.ruthelde.IBA.ExperimentalSetup;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.ruthelde.Helper.Helper;
import com.ruthelde.Target.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.LinkedList;

public class ExperimentalSetupView extends JFrame {

    private JPanel rootPanel;
    private JTextField tfE0;
    private JTextField tfDeltaE0;
    private JTextField tfZ1;
    private JComboBox cBoxM1;
    private JTextField tfAlpha;
    private JTextField tfTheta;
    private JTextField tfBeta;
    private boolean blockEvents;

    private ExperimentalSetupModel experimentalSetupModel;

    public ExperimentalSetupView(ExperimentalSetupModel experimentalSetupModel) {
        super("Experimental Setup");
        this.experimentalSetupModel = experimentalSetupModel;
        initComponents();
    }

    private void setZ1() {

        ExperimentalSetup experimentalSetup = experimentalSetupModel.getExperimentalSetup();

        int Z1 = experimentalSetup.getProjectile().getZ();
        int newZ1 = Z1;
        boolean changeIt = false;
        Element element = new Element();

        try {
            newZ1 = Integer.parseInt(tfZ1.getText());
            if (newZ1 != Z1) changeIt = true;
        } catch (NumberFormatException ex) {
            if (element.setAtomicNumberByName(tfZ1.getText())) {
                newZ1 = element.getAtomicNumber();
                if (newZ1 != Z1) changeIt = true;
            }
        }

        if (changeIt) {
            experimentalSetup.getProjectile().setZ(newZ1);
            fillCBoxM1();
        } else {
            updateView();
        }

    }

    private void setE0() {
        ExperimentalSetup experimentalSetup = experimentalSetupModel.getExperimentalSetup();
        double oldE0 = experimentalSetup.getE0();
        try {
            double E0 = Double.parseDouble(tfE0.getText());
            if (E0 != oldE0) {
                experimentalSetup.setE0(E0);
                experimentalSetup.getProjectile().setE(E0);
                experimentalSetupModel.setExperimentalSetup(experimentalSetup);
            }
        } catch (NumberFormatException ex) {
        }

        updateView();
    }

    private void setDeltaE0() {
        ExperimentalSetup experimentalSetup = experimentalSetupModel.getExperimentalSetup();
        double oldDeltaE0 = experimentalSetup.getDeltaE0();
        try {
            double deltaE0 = Double.parseDouble(tfDeltaE0.getText());
            if (deltaE0 != oldDeltaE0) {
                experimentalSetup.setDeltaE0(deltaE0);
                experimentalSetupModel.setExperimentalSetup(experimentalSetup);
            }
        } catch (NumberFormatException ex) {
        }

        updateView();
    }

    private void setAlpha() {
        ExperimentalSetup experimentalSetup = experimentalSetupModel.getExperimentalSetup();
        double oldAlpha = experimentalSetup.getAlpha();
        try {
            double alpha = Double.parseDouble(tfAlpha.getText());
            if (alpha != oldAlpha) {
                experimentalSetup.setAlpha(alpha);
                experimentalSetupModel.setExperimentalSetup(experimentalSetup);
            }
        } catch (NumberFormatException ex) {
        }

        updateView();
    }

    private void setTheta() {
        ExperimentalSetup experimentalSetup = experimentalSetupModel.getExperimentalSetup();
        double oldTheta = experimentalSetup.getTheta();
        try {
            double theta = Double.parseDouble(tfTheta.getText());
            if (theta != oldTheta) {
                experimentalSetup.setTheta(theta);
                experimentalSetupModel.setExperimentalSetup(experimentalSetup);
            }
        } catch (NumberFormatException ex) {
        }

        updateView();
    }

    private void fillCBoxM1() {
        DefaultComboBoxModel lm = (DefaultComboBoxModel) cBoxM1.getModel();
        blockEvents = true;
        lm.removeAllElements();
        LinkedList<String> ll = new LinkedList<String>();

        ExperimentalSetup experimentalSetup = experimentalSetupModel.getExperimentalSetup();
        int Z1 = experimentalSetup.getProjectile().getZ();

        Element element = new Element();
        element.setAtomicNumber(Z1);

        for (Isotope isotope : element.getIsotopeList()) {
            String entry = Helper.dblToDecStr(isotope.getMass(), 3) + " (" + Helper.dblToDecStr(isotope.getAbundance(), 2) + ")";
            if (!ll.contains(entry)) {
                ll.add(entry);
                lm.addElement(entry);
            }
        }

        blockEvents = false;
        cBoxM1.setSelectedIndex(0);
    }


    void updateView() {

        ExperimentalSetup experimentalSetup = experimentalSetupModel.getExperimentalSetup();

        int Z1 = experimentalSetup.getProjectile().getZ();
        double E0 = experimentalSetup.getE0();
        double deltaE0 = experimentalSetup.getDeltaE0();
        double alpha = experimentalSetup.getAlpha();
        double theta = experimentalSetup.getTheta();
        double beta = Math.abs(180.0d - alpha - theta);

        blockEvents = true;

        tfZ1.setText(Integer.toString(Z1));
        tfE0.setText(Helper.dblToDecStr(E0, 2));
        tfDeltaE0.setText(Helper.dblToDecStr(deltaE0, 2));
        tfAlpha.setText(Helper.dblToDecStr(alpha, 2));
        tfTheta.setText(Helper.dblToDecStr(theta, 2));
        tfBeta.setText(Helper.dblToDecStr(beta, 2));

        blockEvents = false;
    }

    private void initComponents() {

        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.setContentPane(rootPanel);
        pack();
        this.setMinimumSize(getSize());
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - this.getWidth() / 2, dim.height / 2 - this.getHeight() / 2);

        fillCBoxM1();
        updateView();

        tfZ1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setZ1();
            }
        });

        tfZ1.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                setZ1();
            }
        });

        cBoxM1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (!blockEvents) {

                    ExperimentalSetup experimentalSetup = experimentalSetupModel.getExperimentalSetup();

                    int Z1 = experimentalSetup.getProjectile().getZ();

                    Element element = new Element();
                    element.setAtomicNumber(Z1);
                    int M1Index = cBoxM1.getSelectedIndex();
                    double M1 = element.getIsotopeList().get(M1Index).getMass();
                    experimentalSetup.getProjectile().setM(M1);

                    experimentalSetupModel.setExperimentalSetup(experimentalSetup);
                    updateView();
                }
            }
        });

        tfE0.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!blockEvents) setE0();
            }
        });

        tfE0.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (!blockEvents) setE0();
            }
        });

        tfDeltaE0.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!blockEvents) setDeltaE0();
            }
        });

        tfDeltaE0.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (!blockEvents) setDeltaE0();
            }
        });

        tfAlpha.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!blockEvents) setAlpha();
            }
        });

        tfAlpha.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (!blockEvents) setAlpha();
            }
        });

        tfTheta.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!blockEvents) setTheta();
            }
        });

        tfTheta.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (!blockEvents) setTheta();
            }
        });
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
        rootPanel.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panel1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder(null, "Incident beam", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label1 = new JLabel();
        label1.setText("E0 (keV)");
        panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfE0 = new JTextField();
        panel1.add(tfE0, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("dE0 (keV)");
        panel1.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfDeltaE0 = new JTextField();
        panel1.add(tfDeltaE0, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Z1");
        panel1.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfZ1 = new JTextField();
        panel1.add(tfZ1, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("M1");
        panel1.add(label4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cBoxM1 = new JComboBox();
        panel1.add(cBoxM1, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panel2, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel2.setBorder(BorderFactory.createTitledBorder(null, "Geometry", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label5 = new JLabel();
        label5.setText("alpha");
        panel2.add(label5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfAlpha = new JTextField();
        panel2.add(tfAlpha, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Theta");
        panel2.add(label6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfTheta = new JTextField();
        panel2.add(tfTheta, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("beta");
        panel2.add(label7, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfBeta = new JTextField();
        tfBeta.setEnabled(false);
        tfBeta.setText("");
        panel2.add(tfBeta, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panel3, new GridConstraints(0, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel3.setBorder(BorderFactory.createTitledBorder(null, "Scheme", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label8 = new JLabel();
        label8.setIcon(new ImageIcon(getClass().getResource("/pics/Scheme_RBS.png")));
        label8.setText("");
        panel3.add(label8, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }
}
