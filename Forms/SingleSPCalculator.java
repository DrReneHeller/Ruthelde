package Forms;

import Globals.GlobalColors;
import Helper.Helper;
import IBA.CalculationSetup.CalculationSetup;
import Stopping.*;
import Target.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;

public class SingleSPCalculator extends JFrame {

    private final static int    DEFAULT_Z2     = 14      ;
    private final static double DEFAULT_M2     = 0.0d    ; // M2=0 --> use average isotope mass

    private JTextField tfZ2         ;
    private JLabel     lblResultA   ;
    private JLabel     lblResultB   ;
    private JLabel     lblResultC   ;
    private JPanel     rootPanel    ;
    private JComboBox  cBoxM2       ;

    private Projectile projectile;
    private int Z2, Z2old;
    private double M2;

    private StoppingCalculationMode mode;

    public SingleSPCalculator(Projectile projectile, CalculationSetup calculationSetup) {

        super("Elemental Stopping");

        Z2       = DEFAULT_Z2    ;
        Z2old    = DEFAULT_Z2    ;
        M2       = DEFAULT_M2    ;

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

            for (Isotope isotope: element.getIsotopeList()) {
                M2        += isotope.getMass() * isotope.getAbundance() ;
                sumRatio  += isotope.getAbundance()                     ;
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
        result +=    "Se=" + Helper.dblToDecStr(Se, 1) + "(" + Helper.dblToDecStr(Se / (Se + Sn) * 100, 1) + "%)";
        result += " / Sn=" + Helper.dblToDecStr(Sn, 1) + "(" + Helper.dblToDecStr(Sn / (Se + Sn) * 100, 1) + "%)";
        lblResultB.setText(result);

        Layer layer = new Layer();
        while (layer.getElementList().size() > 1) layer.removeElement(0);
        layer.setElementAtomicNumber(0,Z2);
        while (layer.getElementList().get(0).getIsotopeList().size() > 1) layer.getElementList().get(0).removeIsotope(0);
        layer.setIsotopeRatio(0,0,1);
        layer.setIsotopeMass(0,0,M2);
        double S = (Se + Sn) / layer.getThicknessConversionFactor() / 1000.0d;
        result = "";
        result += "S=" + Helper.dblToDecStr(S,2) + "keV/nm @ rho=" + Helper.dblToDecStr(layer.getMassDensity(), 2) +"g/cm3";
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

    private void initComponents() {

        tfZ2.setText(Integer.toString(Z2));
        fillCBoxM2();

        Color lblColor = GlobalColors.DEFAULT_LBL_COLOR;

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
                    M2 = element.getIsotopeList().get(cBoxM2.getSelectedIndex()-1).getMass();
                }
                calculateStopping();
            }
        });
    }
}
