package com.ruthelde.Forms;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.ruthelde.Globals.Globals;
import com.ruthelde.Helper.Helper;
import com.ruthelde.IBA.CalculationSetup.CalculationSetup;
import com.ruthelde.IBA.CalculationSetup.ScreeningMode;
import com.ruthelde.IBA.ExperimentalSetup.ExperimentalSetup;
import com.ruthelde.IBA.Kinematics.KinematicsCalculator;
import com.ruthelde.Stopping.*;
import com.ruthelde.Target.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;

public class IBAKinematics extends JFrame {

    private final static int DEFAULT_Z2 = 14;
    private final static double DEFAULT_M2 = 0.0d; // M2=0 --> use average isotope mass
    private final static double DEFAULT_DEPTH = 100.0d;
    private final static String NOT_POSSIBLE = "not possible";

    private JPanel rootPanel;
    private JTextField tfZ2;
    private JTextField tfDepth;
    private JComboBox cBoxM2;
    private JLabel lblE0Prime;
    private JLabel lblBSCrossSection;
    private JLabel lblBSE1;
    private JLabel lblBSE1Prime;
    private JLabel lblBSE2;
    private JLabel lblRecoilCrossSection;
    private JLabel lblRecoilE1;
    private JLabel lblRecoilE1Prime;
    private JLabel lblRecoilE2;

    private int Z2, Z2old;
    private double M2, depth, depthOld;

    private ExperimentalSetup experimentalSetup;
    private Target target, foil;

    private StoppingCalculationMode stoppingPowerCalculationMode;
    private CompoundCalculationMode compoundCalculationMode;
    private ScreeningMode screeningMode;

    private StoppingParaFile stoppingParaFile;

    public IBAKinematics(ExperimentalSetup experimentalSetup, Target target, Target foil, CalculationSetup calculationSetup, StoppingParaFile stoppingParaFile) {

        Z2 = DEFAULT_Z2;
        Z2old = DEFAULT_Z2;
        M2 = DEFAULT_M2;
        depth = DEFAULT_DEPTH;
        depthOld = DEFAULT_DEPTH;

        this.experimentalSetup = experimentalSetup;
        this.target = target;
        this.foil = foil;
        this.stoppingParaFile = stoppingParaFile;

        stoppingPowerCalculationMode = calculationSetup.getStoppingPowerCalculationMode();
        compoundCalculationMode = calculationSetup.getCompoundCalculationMode();
        screeningMode = calculationSetup.getScreeningMode();

        initComponents();
        calcAndDraw();
    }

    public void setStoppingParaFile(StoppingParaFile stoppingParaFile) {

        this.stoppingParaFile = stoppingParaFile;
    }


    public void setExperimentalSetup(ExperimentalSetup experimentalSetup) {
        if (experimentalSetup != null) {
            this.experimentalSetup = experimentalSetup;
            calcAndDraw();
        }
    }

    public void setTarget(Target target) {
        if (target != null) {
            this.target = target;
            calcAndDraw();
        }
    }

    public void setFoil(Target foil) {
        if (foil != null) {
            this.foil = foil;
            calcAndDraw();
        }
    }

    public void setCalculationSetup(CalculationSetup calculationSetup) {
        if (calculationSetup != null) {
            stoppingPowerCalculationMode = calculationSetup.getStoppingPowerCalculationMode();
            compoundCalculationMode = calculationSetup.getCompoundCalculationMode();
            screeningMode = calculationSetup.getScreeningMode();
            calcAndDraw();
        }
    }

    private void calcAndDraw() {

        Projectile projectile = experimentalSetup.getProjectile();
        int Z1 = projectile.getZ();
        double M1 = projectile.getM();
        double E0 = projectile.getE();
        double theta = experimentalSetup.getTheta();
        double alpha = experimentalSetup.getAlpha();

        KinematicsCalculator.setStoppingPowerCalculationMode(stoppingPowerCalculationMode);
        KinematicsCalculator.setCompoundCalculationMode(compoundCalculationMode);

        double E0Prime, E1, E1Prime, E2;
        double sigma, sumRatio;
        double foilThickness;
        Element element = new Element();
        boolean BSA, BSB, RC;

        if (depth > target.getTotalThickness()) {
            depth = target.getTotalThickness();
            tfDepth.setText(Helper.dblToDecStr(depth, 2));
        }

        if (M2 == 0.0d) {

            element.setAtomicNumber(Z2);
            sumRatio = 0.0d;

            for (Isotope isotope : element.getIsotopeList()) {
                M2 += isotope.getMass() * isotope.getAbundance();
                sumRatio += isotope.getAbundance();
            }

            M2 /= sumRatio;
        }

        BSA = false;
        BSB = false;
        RC = false;

        if (M1 < M2) {
            BSA = true;
            if (theta < 90.0d) {
                RC = true;
            }
        } else if (M1 == M2) {
            if (theta < 90.0d) {
                BSA = true;
                RC = true;
            }
        } else if (M1 > M2) {
            double thetaMax = KinematicsCalculator.getMaxScatteringAngle(M1, M2);
            if (theta < thetaMax) {
                BSA = true;
                BSB = true;
                RC = true;
            } else if (theta < 90.0d) {
                RC = true;
            }
        }

        foilThickness = foil.getTotalThickness();

        projectile.setE(E0);
        E0Prime = KinematicsCalculator.getEnergyInDepth(projectile, target, alpha, depth, stoppingParaFile);
        lblE0Prime.setText(Helper.dblToSciStr(E0Prime, 3));

        if (BSA) {
            projectile.setE(E0Prime);
            E1 = KinematicsCalculator.getBSEnergyA(projectile, M2, theta);
            if (E1 > 0.0d) sigma = KinematicsCalculator.getBSCrossSection(projectile, Z2, M2, theta, screeningMode, 0);
            else sigma = 0.0d;

            projectile.setE(E1);
            E1Prime = KinematicsCalculator.getEnergyAtSurface(projectile, target, alpha, theta, depth, stoppingParaFile);

            if (foilThickness > 0.1d) {
                projectile.setE(E1Prime);
                E2 = KinematicsCalculator.getEnergyInDepth(projectile, foil, 0.0d, foilThickness, stoppingParaFile);
            } else {
                E2 = E1Prime;
            }

            lblBSE1.setText(Helper.dblToSciStr(E1, 3));
            lblBSCrossSection.setText(Helper.dblToSciStr(sigma, 3));
            lblBSE1Prime.setText(Helper.dblToSciStr(E1Prime, 3));
            lblBSE2.setText(Helper.dblToSciStr(E2, 3));

            if (BSB) {
                projectile.setE(E0Prime);
                E1 = KinematicsCalculator.getBSEnergyB(projectile, M2, theta);
                if (E1 > 0.0d)
                    sigma = KinematicsCalculator.getBSCrossSection(projectile, Z2, M2, theta, screeningMode, 1);
                else sigma = 0.0d;

                projectile.setE(E1);
                E1Prime = KinematicsCalculator.getEnergyAtSurface(projectile, target, alpha, theta, depth, stoppingParaFile);

                if (foilThickness > 0.1d) {
                    projectile.setE(E1Prime);
                    E2 = KinematicsCalculator.getEnergyInDepth(projectile, foil, 0.0d, foilThickness, stoppingParaFile);
                } else {
                    E2 = E1Prime;
                }

                lblBSE1.setText(lblBSE1.getText() + "/" + Helper.dblToSciStr(E1, 2));
                lblBSCrossSection.setText(lblBSCrossSection.getText() + "/" + Helper.dblToSciStr(sigma, 2));
                lblBSE1Prime.setText(lblBSE1Prime.getText() + "/" + Helper.dblToSciStr(E1Prime, 2));
                lblBSE2.setText(lblBSE2.getText() + "/" + Helper.dblToSciStr(E2, 2));
            }
        } else {
            lblBSE1.setText(NOT_POSSIBLE);
            lblBSCrossSection.setText(NOT_POSSIBLE);
            lblBSE1Prime.setText(NOT_POSSIBLE);
            lblBSE2.setText(NOT_POSSIBLE);
        }

        if (RC) {
            projectile.setE(E0Prime);
            E1 = KinematicsCalculator.getRecoilEnergy(projectile, M2, theta);
            if (E1 > 0.0d) sigma = KinematicsCalculator.getRecoilCrossSection(projectile, Z2, M2, theta);
            else sigma = 0.0d;

            projectile.setZ(Z2);
            projectile.setM(M2);
            projectile.setE(E1);

            E1Prime = KinematicsCalculator.getEnergyAtSurface(projectile, target, alpha, theta, depth, stoppingParaFile);

            if (foilThickness > 0.1d) {
                projectile.setE(E1Prime);
                E2 = KinematicsCalculator.getEnergyInDepth(projectile, foil, 0.0d, foilThickness, stoppingParaFile);
            } else {
                E2 = E1Prime;
            }

            lblRecoilE1.setText(Helper.dblToSciStr(E1, 3));
            lblRecoilCrossSection.setText(Helper.dblToSciStr(sigma, 3));
            lblRecoilE1Prime.setText(Helper.dblToSciStr(E1Prime, 3));
            lblRecoilE2.setText(Helper.dblToSciStr(E2, 3));
        } else {
            lblRecoilE1.setText(NOT_POSSIBLE);
            lblRecoilCrossSection.setText(NOT_POSSIBLE);
            lblRecoilE1Prime.setText(NOT_POSSIBLE);
            lblRecoilE2.setText(NOT_POSSIBLE);
        }

        projectile.setZ(Z1);
        projectile.setM(M1);
        projectile.setE(E0);
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

    private void parseDepth() {
        try {
            depth = Double.parseDouble(tfDepth.getText());
        } catch (NumberFormatException ex) {
            depth = depthOld;
            tfDepth.setText(Helper.dblToDecStr(depth, 2));
        }

        if (depth != depthOld) {
            depthOld = depth;
            calcAndDraw();
        } else {
            depth = depthOld;
            tfDepth.setText(Helper.dblToDecStr(depth, 2));
        }
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
            calcAndDraw();
        } else {
            Z2 = Z2old;
            tfZ2.setText(Integer.toString(Z2));
        }
    }

    private void initComponents() {

        setTitle("IBA kinematics calculator");
        setContentPane(rootPanel);
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        tfZ2.setText(Integer.toString(Z2));
        tfDepth.setText(Helper.dblToDecStr(depth, 3));

        fillCBoxM2();

        Color lblCol = Globals.DEFAULT_LBL_COLOR;
        lblE0Prime.setForeground(lblCol);
        lblBSCrossSection.setForeground(lblCol);
        lblRecoilCrossSection.setForeground(lblCol);
        lblBSE1.setForeground(lblCol);
        lblRecoilE1.setForeground(lblCol);
        lblBSE1Prime.setForeground(lblCol);
        lblRecoilE1Prime.setForeground(lblCol);
        lblBSE2.setForeground(lblCol);
        lblRecoilE2.setForeground(lblCol);

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
                calcAndDraw();
            }
        });

        tfDepth.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parseDepth();
            }
        });

        tfDepth.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                parseDepth();
            }
        });

        tfDepth.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                parseDepth();
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
        rootPanel.setLayout(new GridLayoutManager(3, 1, new Insets(5, 5, 5, 5), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(5, 3, new Insets(5, 5, 5, 5), -1, -1));
        rootPanel.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder(null, "Scattered projectile", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label1 = new JLabel();
        label1.setText("E0'");
        panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("sigma");
        panel1.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("E1");
        panel1.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("E1'");
        panel1.add(label4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("E2");
        panel1.add(label5, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblBSE2 = new JLabel();
        lblBSE2.setHorizontalAlignment(4);
        lblBSE2.setHorizontalTextPosition(10);
        lblBSE2.setText("----");
        panel1.add(lblBSE2, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(150, -1), null, null, 0, false));
        lblBSE1Prime = new JLabel();
        lblBSE1Prime.setHorizontalAlignment(4);
        lblBSE1Prime.setHorizontalTextPosition(10);
        lblBSE1Prime.setText("----");
        panel1.add(lblBSE1Prime, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(150, -1), null, null, 0, false));
        lblBSE1 = new JLabel();
        lblBSE1.setHorizontalAlignment(4);
        lblBSE1.setHorizontalTextPosition(10);
        lblBSE1.setText("----");
        panel1.add(lblBSE1, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(150, -1), null, null, 0, false));
        lblBSCrossSection = new JLabel();
        lblBSCrossSection.setHorizontalAlignment(4);
        lblBSCrossSection.setHorizontalTextPosition(10);
        lblBSCrossSection.setText("----");
        panel1.add(lblBSCrossSection, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(150, -1), null, null, 0, false));
        lblE0Prime = new JLabel();
        lblE0Prime.setForeground(new Color(-1));
        lblE0Prime.setHorizontalAlignment(4);
        lblE0Prime.setHorizontalTextPosition(10);
        lblE0Prime.setText("----");
        panel1.add(lblE0Prime, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(150, -1), null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("keV");
        panel1.add(label6, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("keV");
        panel1.add(label7, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("keV");
        panel1.add(label8, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("mb/sr");
        panel1.add(label9, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("keV");
        panel1.add(label10, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(3, 2, new Insets(5, 5, 5, 5), -1, -1));
        rootPanel.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel2.setBorder(BorderFactory.createTitledBorder(null, "Input", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label11 = new JLabel();
        label11.setText("depth (nm)");
        panel2.add(label11, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label12 = new JLabel();
        label12.setText("Z2");
        panel2.add(label12, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label13 = new JLabel();
        label13.setText("M2");
        panel2.add(label13, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cBoxM2 = new JComboBox();
        panel2.add(cBoxM2, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(175, -1), null, null, 0, false));
        tfDepth = new JTextField();
        panel2.add(tfDepth, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(75, -1), null, 0, false));
        tfZ2 = new JTextField();
        tfZ2.setText("");
        panel2.add(tfZ2, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(75, -1), null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(4, 3, new Insets(5, 5, 5, 5), -1, -1));
        rootPanel.add(panel3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel3.setBorder(BorderFactory.createTitledBorder(null, "Recoil", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label14 = new JLabel();
        label14.setText("sigma");
        panel3.add(label14, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label15 = new JLabel();
        label15.setText("E1");
        panel3.add(label15, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label16 = new JLabel();
        label16.setText("E1'");
        panel3.add(label16, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label17 = new JLabel();
        label17.setText("E2");
        panel3.add(label17, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblRecoilE2 = new JLabel();
        lblRecoilE2.setHorizontalAlignment(4);
        lblRecoilE2.setHorizontalTextPosition(10);
        lblRecoilE2.setText("----");
        panel3.add(lblRecoilE2, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(150, -1), null, null, 0, false));
        lblRecoilE1Prime = new JLabel();
        lblRecoilE1Prime.setHorizontalAlignment(4);
        lblRecoilE1Prime.setHorizontalTextPosition(10);
        lblRecoilE1Prime.setText("----");
        panel3.add(lblRecoilE1Prime, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(150, -1), null, null, 0, false));
        lblRecoilE1 = new JLabel();
        lblRecoilE1.setHorizontalAlignment(4);
        lblRecoilE1.setHorizontalTextPosition(10);
        lblRecoilE1.setText("----");
        panel3.add(lblRecoilE1, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(150, -1), null, null, 0, false));
        lblRecoilCrossSection = new JLabel();
        lblRecoilCrossSection.setHorizontalAlignment(4);
        lblRecoilCrossSection.setHorizontalTextPosition(10);
        lblRecoilCrossSection.setText("----");
        panel3.add(lblRecoilCrossSection, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(150, -1), null, null, 0, false));
        final JLabel label18 = new JLabel();
        label18.setText("keV");
        panel3.add(label18, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label19 = new JLabel();
        label19.setText("keV");
        panel3.add(label19, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label20 = new JLabel();
        label20.setText("keV");
        panel3.add(label20, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label21 = new JLabel();
        label21.setText("mb/sr");
        panel3.add(label21, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }

}
