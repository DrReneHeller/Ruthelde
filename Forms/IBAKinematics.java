package Forms;

import Globals.GlobalColors;
import Helper.Helper;
import IBA.CalculationSetup.CalculationSetup;
import IBA.CalculationSetup.ScreeningMode;
import IBA.ExperimentalSetup.ExperimentalSetup;
import IBA.Kinematics.KinematicsCalculator;
import Stopping.*;
import Target.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;

public class IBAKinematics extends JFrame {

    private final static int    DEFAULT_Z2     = 14      ;
    private final static double DEFAULT_M2     = 0.0d    ; // M2=0 --> use average isotope mass
    private final static double DEFAULT_DEPTH  = 100.0d  ;
    private final static String NOT_POSSIBLE   = "not possible";

    private JPanel     rootPanel             ;
    private JTextField tfZ2                  ;
    private JTextField tfDepth               ;
    private JComboBox  cBoxM2                ;
    private JLabel     lblE0Prime            ;
    private JLabel     lblBSCrossSection     ;
    private JLabel     lblBSE1               ;
    private JLabel     lblBSE1Prime          ;
    private JLabel     lblBSE2               ;
    private JLabel     lblRecoilCrossSection ;
    private JLabel     lblRecoilE1           ;
    private JLabel     lblRecoilE1Prime      ;
    private JLabel     lblRecoilE2           ;

    private int Z2, Z2old;
    private double M2, depth, depthOld;

    private ExperimentalSetup experimentalSetup;
    private Target target, foil;

    private StoppingCalculationMode stoppingPowerCalculationMode ;
    private CompoundCalculationMode compoundCalculationMode      ;
    private ScreeningMode           screeningMode                ;

    public IBAKinematics(ExperimentalSetup experimentalSetup, Target target, Target foil, CalculationSetup calculationSetup) {

        Z2       = DEFAULT_Z2    ;
        Z2old    = DEFAULT_Z2    ;
        M2       = DEFAULT_M2    ;
        depth    = DEFAULT_DEPTH ;
        depthOld = DEFAULT_DEPTH ;

        this.experimentalSetup = experimentalSetup;
        this.target = target;
        this.foil   = foil;

        stoppingPowerCalculationMode = calculationSetup.getStoppingPowerCalculationMode() ;
        compoundCalculationMode      = calculationSetup.getCompoundCalculationMode()      ;
        screeningMode                = calculationSetup.getScreeningMode()                ;

        initComponents();
        calcAndDraw();
    }

    public void setExperimentalSetup (ExperimentalSetup experimentalSetup) {
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
            stoppingPowerCalculationMode = calculationSetup.getStoppingPowerCalculationMode() ;
            compoundCalculationMode      = calculationSetup.getCompoundCalculationMode()      ;
            screeningMode                = calculationSetup.getScreeningMode()                ;
            calcAndDraw();
        }
    }

    private void calcAndDraw() {

        Projectile projectile = experimentalSetup.getProjectile();
        int    Z1 = projectile.getZ();
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
            tfDepth.setText(Helper.dblToDecStr(depth,2));
        }

        if (M2 == 0.0d) {

            element.setAtomicNumber(Z2);
            sumRatio = 0.0d;

            for (Isotope isotope: element.getIsotopeList()) {
                M2        += isotope.getMass() * isotope.getAbundance() ;
                sumRatio  += isotope.getAbundance()                     ;
            }

            M2 /= sumRatio;
        }

        BSA = false;
        BSB = false;
        RC  = false;

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
                RC  = true;
            } else if (theta < 90.0d) {
                RC  = true;
            }
        }

        foilThickness = foil.getTotalThickness();

        projectile.setE(E0);
        E0Prime  =  KinematicsCalculator.getEnergyInDepth(projectile, target, alpha, depth);
        lblE0Prime.setText(Helper.dblToSciStr(E0Prime, 3));

        if (BSA) {
            projectile.setE(E0Prime);
            E1 = KinematicsCalculator.getBSEnergyA(projectile, M2, theta);
            if (E1>0.0d) sigma = KinematicsCalculator.getBSCrossSection(projectile, Z2, M2, theta, screeningMode, 0); else sigma = 0.0d;

            projectile.setE(E1);
            E1Prime  =  KinematicsCalculator.getEnergyAtSurface(projectile, target, alpha, theta, depth);

            if (foilThickness > 0.1d) {
                projectile.setE(E1Prime);
                E2 = KinematicsCalculator.getEnergyInDepth(projectile, foil, 0.0d, foilThickness);
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
                if (E1>0.0d) sigma = KinematicsCalculator.getBSCrossSection(projectile, Z2, M2, theta, screeningMode, 1); else sigma = 0.0d;

                projectile.setE(E1);
                E1Prime  =  KinematicsCalculator.getEnergyAtSurface(projectile, target, alpha, theta, depth);

                if (foilThickness > 0.1d) {
                    projectile.setE(E1Prime);
                    E2 = KinematicsCalculator.getEnergyInDepth(projectile, foil, 0.0d, foilThickness);
                } else {
                    E2 = E1Prime;
                }

                lblBSE1.setText(lblBSE1.getText() + "/" + Helper.dblToSciStr(E1,2));
                lblBSCrossSection.setText(lblBSCrossSection.getText() + "/" + Helper.dblToSciStr(sigma, 2));
                lblBSE1Prime.setText(lblBSE1Prime.getText() + "/" + Helper.dblToSciStr(E1Prime, 2));
                lblBSE2.setText(lblBSE2.getText() + "/" + Helper.dblToSciStr(E2, 2));
            }
        }
        else {
            lblBSE1.setText(NOT_POSSIBLE);
            lblBSCrossSection.setText(NOT_POSSIBLE);
            lblBSE1Prime.setText(NOT_POSSIBLE);
            lblBSE2.setText(NOT_POSSIBLE);
        }

        if (RC) {
            projectile.setE(E0Prime);
            E1 = KinematicsCalculator.getRecoilEnergy(projectile, M2, theta);
            if (E1>0.0d) sigma = KinematicsCalculator.getRecoilCrossSection(projectile, Z2, M2,theta); else sigma = 0.0d;

            projectile.setZ(Z2);
            projectile.setM(M2);
            projectile.setE(E1);

            E1Prime  =  KinematicsCalculator.getEnergyAtSurface(projectile, target, alpha, theta, depth);

            if (foilThickness > 0.1d) {
                projectile.setE(E1Prime);
                E2 = KinematicsCalculator.getEnergyInDepth(projectile, foil, 0.0d, foilThickness);
            } else {
                E2 = E1Prime;
            }

            lblRecoilE1.setText(Helper.dblToSciStr(E1, 3));
            lblRecoilCrossSection.setText(Helper.dblToSciStr(sigma, 3));
            lblRecoilE1Prime.setText(Helper.dblToSciStr(E1Prime, 3));
            lblRecoilE2.setText(Helper.dblToSciStr(E2, 3));
        }
        else {
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

        for (Isotope isotope: element.getIsotopeList()) {
            M2        += isotope.getMass() * isotope.getAbundance() ;
            sumRatio  += isotope.getAbundance()                     ;
        }

        M2 /= sumRatio;

        String firstEntry = "Natural weight (" + Helper.dblToDecStr(M2, 2) + ")";

        lm.addElement(firstEntry);

        for (Isotope isotope: element.getIsotopeList()) {
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

        Color lblCol = GlobalColors.DEFAULT_LBL_COLOR;
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
                    M2 = element.getIsotopeList().get(cBoxM2.getSelectedIndex()-1).getMass();
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
}
