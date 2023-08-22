package com.ruthelde.Forms;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.ruthelde.Globals.Globals;
import com.ruthelde.Helper.Helper;
import com.ruthelde.IBA.CalculationSetup.CalculationSetup;
import com.ruthelde.Stopping.*;
import com.ruthelde.Target.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;

public class SingleSPCalculator extends JFrame {

    private final static int DEFAULT_Z2 = 14;
    private final static double DEFAULT_M2 = 0.0d; // M2=0 --> use average isotope mass

    private JTextField tfZ2;
    private JLabel lblResultA;
    private JLabel lblResultB;
    private JLabel lblResultC;
    private JPanel rootPanel;
    private JComboBox cBoxM2;

    private Projectile projectile;
    private int Z2, Z2old;
    private double M2;

    private StoppingCalculationMode mode;

    public SingleSPCalculator(Projectile projectile, CalculationSetup calculationSetup) {

        super("Elemental Stopping");

        Z2 = DEFAULT_Z2;
        Z2old = DEFAULT_Z2;
        M2 = DEFAULT_M2;

        this.projectile = projectile;
        mode = calculationSetup.getStoppingPowerCalculationMode();
        initComponents();
        calculateStopping();
    }

    public void setProjectile(Projectile projectile) {
        if (projectile != null) {
            this.projectile = projectile;
            calculateStopping();
        }
    }

    public void setStoppingMode(StoppingCalculationMode stoppingMode) {
        if (stoppingMode != null) {
            this.mode = stoppingMode;
            calculateStopping();
        }
    }

    private void calculateStopping() {

        Element element = new Element();

        if (M2 == 0.0d) {

            element.setAtomicNumber(Z2);
            double sumRatio = 0.0d;

            for (Isotope isotope : element.getIsotopeList()) {
                M2 += isotope.getMass() * isotope.getAbundance();
                sumRatio += isotope.getAbundance();
            }

            M2 /= sumRatio;
        }

        StoppingCalculator calculator = new StoppingCalculator();
        double Se = calculator.getStoppingPower(projectile, Z2, M2, mode, 0);
        double Sn = calculator.getStoppingPower(projectile, Z2, M2, mode, 1);

        String result;

        result = "S=" + Helper.dblToDecStr(Se + Sn, 2) + "eV/(10^15at/cm)";
        lblResultA.setText(result);

        result = "";
        result += "Se=" + Helper.dblToDecStr(Se, 1) + "(" + Helper.dblToDecStr(Se / (Se + Sn) * 100, 1) + "%)";
        result += " / Sn=" + Helper.dblToDecStr(Sn, 1) + "(" + Helper.dblToDecStr(Sn / (Se + Sn) * 100, 1) + "%)";
        lblResultB.setText(result);

        Layer layer = new Layer();
        while (layer.getElementList().size() > 1) layer.removeElement(0);
        layer.setElementAtomicNumber(0, Z2);
        while (layer.getElementList().get(0).getIsotopeList().size() > 1)
            layer.getElementList().get(0).removeIsotope(0);
        layer.setIsotopeRatio(0, 0, 1);
        layer.setIsotopeMass(0, 0, M2);
        double S = (Se + Sn) / layer.getThicknessConversionFactor() / 1000.0d;
        result = "";
        result += "S=" + Helper.dblToDecStr(S, 2) + "keV/nm @ rho=" + Helper.dblToDecStr(layer.getMassDensity(), 2) + "g/cm3";
        lblResultC.setText(result);
    }

    private void parseZ2() {
        Element element = new Element();
        try {
            Z2 = Integer.parseInt(tfZ2.getText());
        } catch (NumberFormatException ex) {
            if (element.setAtomicNumberByName(tfZ2.getText())) {
                Z2 = element.getAtomicNumber();
            } else {
                Z2 = Z2old;
                tfZ2.setText(Integer.toString(Z2));
            }
        }
        if (Z2 > 0 && Z2 <= Element.MAX_ATOMIC_NUMBER && Z2 != Z2old) {
            Z2old = Z2;
            fillCBoxM2();
            calculateStopping();
        } else {
            Z2 = Z2old;
            tfZ2.setText(Integer.toString(Z2));
        }
    }

    private void fillCBoxM2() {
        DefaultComboBoxModel lm = (DefaultComboBoxModel) cBoxM2.getModel();
        lm.removeAllElements();
        LinkedList<String> ll = new LinkedList<String>();

        Element element = new Element();
        element.setAtomicNumber(Z2);

        double sumRatio = 0.0d;
        M2 = 0.0d;

        for (Isotope isotope : element.getIsotopeList()) {
            M2 += isotope.getMass() * isotope.getAbundance();
            sumRatio += isotope.getAbundance();
        }

        M2 /= sumRatio;

        String firstEntry = "Natural weight (" + Helper.dblToDecStr(M2, 2) + ")";

        lm.addElement(firstEntry);

        for (Isotope isotope : element.getIsotopeList()) {
            String entry = Helper.dblToDecStr(isotope.getMass(), 3) + " (" + Helper.dblToDecStr(isotope.getAbundance(), 2) + ")";
            if (!ll.contains(entry)) {
                ll.add(entry);
                lm.addElement(entry);
            }
        }

        cBoxM2.setSelectedIndex(0);
        M2 = 0.0d;
    }

    private void initComponents() {

        tfZ2.setText(Integer.toString(Z2));
        fillCBoxM2();

        Color lblColor = Globals.DEFAULT_LBL_COLOR;

        lblResultA.setForeground(lblColor);
        lblResultB.setForeground(lblColor);
        lblResultC.setForeground(lblColor);

        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setContentPane(rootPanel);
        pack();
        setResizable(false);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - this.getWidth() / 2, dim.height / 2 - this.getHeight() / 2);

        tfZ2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parseZ2();
            }
        });

        tfZ2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                parseZ2();
            }
        });

        tfZ2.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                parseZ2();
            }
        });

        cBoxM2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cBoxM2.getSelectedIndex() < 1) {
                    M2 = 0.0d;
                } else {
                    Element element = new Element();
                    element.setAtomicNumber(Z2);
                    M2 = element.getIsotopeList().get(cBoxM2.getSelectedIndex() - 1).getMass();
                }
                calculateStopping();
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
        rootPanel.setLayout(new GridLayoutManager(5, 3, new Insets(5, 5, 5, 5), -1, -1));
        final JLabel label1 = new JLabel();
        label1.setText("M2");
        rootPanel.add(label1, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblResultA = new JLabel();
        lblResultA.setText("Label");
        rootPanel.add(lblResultA, new GridConstraints(2, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblResultB = new JLabel();
        lblResultB.setText("Label");
        rootPanel.add(lblResultB, new GridConstraints(3, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblResultC = new JLabel();
        lblResultC.setText("Label");
        rootPanel.add(lblResultC, new GridConstraints(4, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfZ2 = new JTextField();
        tfZ2.setText("14");
        rootPanel.add(tfZ2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Z2");
        rootPanel.add(label2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cBoxM2 = new JComboBox();
        rootPanel.add(cBoxM2, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(175, -1), null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }
}
