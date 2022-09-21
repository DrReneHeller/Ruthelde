package com.ruthelde.IBA.CalculationSetup;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.ruthelde.Stopping.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CalculationSetupView extends JFrame {

    private JPanel rootPanel;
    private JComboBox cBoxStoppingCalculationMode;
    private JComboBox cBoxCompoundCorrectionMode;
    private JComboBox cBoxStraggling;
    private JComboBox cBoxScreening;
    private JComboBox cBoxChargeFraction;
    private JCheckBox chBoxElementalSpectra;
    private JCheckBox chBoxIsotopeSpectra;
    private JCheckBox chBoxLayerContribution;

    private CalculationSetupModel calculationSetupModel;

    public CalculationSetupView(CalculationSetupModel calculationSetupModel) {
        super("Calc. Setup");

        this.calculationSetupModel = calculationSetupModel;

        initComponents();
    }

    private void initComponents() {

        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setContentPane(rootPanel);

        cBoxStoppingCalculationMode.setModel(new DefaultComboBoxModel(StoppingCalculationMode.values()));
        cBoxCompoundCorrectionMode.setModel(new DefaultComboBoxModel(CompoundCalculationMode.values()));
        cBoxScreening.setModel(new DefaultComboBoxModel(ScreeningMode.values()));
        cBoxStraggling.setModel(new DefaultComboBoxModel(StragglingMode.values()));
        cBoxChargeFraction.setModel(new DefaultComboBoxModel(ChargeFractionMode.values()));

        cBoxStoppingCalculationMode.setSelectedItem(calculationSetupModel.getCalculationSetup().getStoppingPowerCalculationMode());
        cBoxCompoundCorrectionMode.setSelectedItem(calculationSetupModel.getCalculationSetup().getCompoundCalculationMode());
        cBoxScreening.setSelectedItem(calculationSetupModel.getCalculationSetup().getScreeningMode());
        cBoxStraggling.setSelectedItem(calculationSetupModel.getCalculationSetup().getStragglingMode());
        cBoxChargeFraction.setSelectedItem(calculationSetupModel.getCalculationSetup().getChargeFractionMode());

        chBoxElementalSpectra.setSelected(calculationSetupModel.getCalculationSetup().isShowElements());
        chBoxIsotopeSpectra.setSelected(calculationSetupModel.getCalculationSetup().isShowIsotopes());
        chBoxLayerContribution.setSelected(calculationSetupModel.getCalculationSetup().isShowLayers());

        pack();
        setMinimumSize(getSize());
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - this.getWidth() / 2, dim.height / 2 - this.getHeight() / 2);

        cBoxStoppingCalculationMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StoppingCalculationMode scm = (StoppingCalculationMode) cBoxStoppingCalculationMode.getSelectedItem();
                CalculationSetup calculationSetup = calculationSetupModel.getCalculationSetup();
                calculationSetup.setStoppingPowerCalculationMode(scm);
                calculationSetupModel.setCalculationSetup(calculationSetup);
            }
        });

        cBoxCompoundCorrectionMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CompoundCalculationMode cm = (CompoundCalculationMode) cBoxCompoundCorrectionMode.getSelectedItem();
                CalculationSetup calculationSetup = calculationSetupModel.getCalculationSetup();
                calculationSetup.setCompoundCalculationMode(cm);
                calculationSetupModel.setCalculationSetup(calculationSetup);
            }
        });

        cBoxScreening.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ScreeningMode sm = (ScreeningMode) cBoxScreening.getSelectedItem();
                CalculationSetup calculationSetup = calculationSetupModel.getCalculationSetup();
                calculationSetup.setScreeningMode(sm);
                calculationSetupModel.setCalculationSetup(calculationSetup);
            }
        });

        cBoxStraggling.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StragglingMode sm = (StragglingMode) cBoxStraggling.getSelectedItem();
                CalculationSetup calculationSetup = calculationSetupModel.getCalculationSetup();
                calculationSetup.setStragglingMode(sm);
                calculationSetupModel.setCalculationSetup(calculationSetup);
            }
        });

        cBoxChargeFraction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ChargeFractionMode cfm = (ChargeFractionMode) cBoxChargeFraction.getSelectedItem();
                CalculationSetup calculationSetup = calculationSetupModel.getCalculationSetup();
                calculationSetup.setChargeFractionMode(cfm);
                calculationSetupModel.setCalculationSetup(calculationSetup);
            }
        });

        chBoxElementalSpectra.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CalculationSetup calculationSetup = calculationSetupModel.getCalculationSetup();
                calculationSetup.setShowElements(chBoxElementalSpectra.isSelected());
                calculationSetupModel.setCalculationSetup(calculationSetup);
            }
        });

        chBoxIsotopeSpectra.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CalculationSetup calculationSetup = calculationSetupModel.getCalculationSetup();
                calculationSetup.setShowIsotopes(chBoxIsotopeSpectra.isSelected());
                calculationSetupModel.setCalculationSetup(calculationSetup);
            }
        });

        chBoxLayerContribution.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CalculationSetup calculationSetup = calculationSetupModel.getCalculationSetup();
                calculationSetup.setShowLayers(chBoxLayerContribution.isSelected());
                calculationSetupModel.setCalculationSetup(calculationSetup);
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
        panel1.setLayout(new GridLayoutManager(4, 1, new Insets(5, 5, 5, 5), -1, -1));
        rootPanel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder(null, "Stopping", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label1 = new JLabel();
        label1.setText("Calculation Methode");
        panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cBoxStoppingCalculationMode = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("Ziegler-Biersack");
        cBoxStoppingCalculationMode.setModel(defaultComboBoxModel1);
        panel1.add(cBoxStoppingCalculationMode, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Compound Correction");
        panel1.add(label2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cBoxCompoundCorrectionMode = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel2 = new DefaultComboBoxModel();
        defaultComboBoxModel2.addElement("Bragg-Rule");
        cBoxCompoundCorrectionMode.setModel(defaultComboBoxModel2);
        panel1.add(cBoxCompoundCorrectionMode, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(6, 1, new Insets(5, 5, 5, 5), -1, -1));
        panel2.setEnabled(true);
        rootPanel.add(panel2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel2.setBorder(BorderFactory.createTitledBorder(null, "Simulation", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label3 = new JLabel();
        label3.setEnabled(true);
        label3.setText("Energy Straggling");
        panel2.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cBoxStraggling = new JComboBox();
        cBoxStraggling.setEnabled(true);
        final DefaultComboBoxModel defaultComboBoxModel3 = new DefaultComboBoxModel();
        defaultComboBoxModel3.addElement("Bohr");
        defaultComboBoxModel3.addElement("Chu");
        defaultComboBoxModel3.addElement("Chu&Yang");
        cBoxStraggling.setModel(defaultComboBoxModel3);
        panel2.add(cBoxStraggling, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setEnabled(true);
        label4.setText("Screening Correction");
        panel2.add(label4, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cBoxScreening = new JComboBox();
        cBoxScreening.setEnabled(true);
        final DefaultComboBoxModel defaultComboBoxModel4 = new DefaultComboBoxModel();
        defaultComboBoxModel4.addElement("None");
        defaultComboBoxModel4.addElement("Anderson");
        cBoxScreening.setModel(defaultComboBoxModel4);
        panel2.add(cBoxScreening, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setEnabled(true);
        label5.setText("Charge Fraction");
        panel2.add(label5, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cBoxChargeFraction = new JComboBox();
        cBoxChargeFraction.setEnabled(true);
        final DefaultComboBoxModel defaultComboBoxModel5 = new DefaultComboBoxModel();
        defaultComboBoxModel5.addElement("None");
        defaultComboBoxModel5.addElement("Fixed Fraction");
        defaultComboBoxModel5.addElement("Linear decreasing");
        defaultComboBoxModel5.addElement("etc.");
        cBoxChargeFraction.setModel(defaultComboBoxModel5);
        panel2.add(cBoxChargeFraction, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panel3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel3.setBorder(BorderFactory.createTitledBorder(null, "Plot", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        chBoxElementalSpectra = new JCheckBox();
        chBoxElementalSpectra.setText("Elemental Spectra");
        panel3.add(chBoxElementalSpectra, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        chBoxIsotopeSpectra = new JCheckBox();
        chBoxIsotopeSpectra.setText("Isotpes Spectra");
        panel3.add(chBoxIsotopeSpectra, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        chBoxLayerContribution = new JCheckBox();
        chBoxLayerContribution.setText("Layer Contibutions");
        panel3.add(chBoxLayerContribution, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }
}
