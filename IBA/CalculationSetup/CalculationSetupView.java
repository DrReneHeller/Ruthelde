package IBA.CalculationSetup;

import Stopping.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CalculationSetupView extends JFrame {

    private JPanel    rootPanel                   ;
    private JComboBox cBoxStoppingCalculationMode ;
    private JComboBox cBoxCompoundCorrectionMode  ;
    private JComboBox cBoxStraggling              ;
    private JComboBox cBoxScreening               ;
    private JComboBox cBoxChargeFraction          ;
    private JCheckBox chBoxElementalSpectra       ;
    private JCheckBox chBoxIsotopeSpectra         ;
    private JCheckBox chBoxLayerContribution      ;

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
                StoppingCalculationMode scm = (StoppingCalculationMode)cBoxStoppingCalculationMode.getSelectedItem();
                CalculationSetup calculationSetup = calculationSetupModel.getCalculationSetup();
                calculationSetup.setStoppingPowerCalculationMode(scm);
                calculationSetupModel.setCalculationSetup(calculationSetup);
            }
        });

        cBoxCompoundCorrectionMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CompoundCalculationMode cm = (CompoundCalculationMode)cBoxCompoundCorrectionMode.getSelectedItem();
                CalculationSetup calculationSetup = calculationSetupModel.getCalculationSetup();
                calculationSetup.setCompoundCalculationMode(cm);
                calculationSetupModel.setCalculationSetup(calculationSetup);
            }
        });

        cBoxScreening.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ScreeningMode sm = (ScreeningMode)cBoxScreening.getSelectedItem();
                CalculationSetup calculationSetup = calculationSetupModel.getCalculationSetup();
                calculationSetup.setScreeningMode(sm);
                calculationSetupModel.setCalculationSetup(calculationSetup);
            }
        });

        cBoxStraggling.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StragglingMode sm = (StragglingMode)cBoxStraggling.getSelectedItem();
                CalculationSetup calculationSetup = calculationSetupModel.getCalculationSetup();
                calculationSetup.setStragglingMode(sm);
                calculationSetupModel.setCalculationSetup(calculationSetup);
            }
        });

        cBoxChargeFraction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ChargeFractionMode cfm = (ChargeFractionMode)cBoxChargeFraction.getSelectedItem();
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
}
