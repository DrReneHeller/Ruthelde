package com.ruthelde.Target;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.ruthelde.Helper.Helper;
import com.ruthelde.IBA.DataFile;
import com.google.gson.Gson;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.File;
import java.io.FileReader;

public class TargetView extends JFrame {

    private JPanel rootPanel;
    private JList liLayers;
    private JList liElements;
    private JList liIsotopes;
    private JButton btnAddElement;
    private JButton btnRemoveElement;
    private JButton btnNormalizeElements;
    private JButton btnNormalizeIsotopes;
    private JButton btnAddLayer;
    private JButton btnRemoveLayer;
    private JButton btnAddIsotope;
    private JButton btnRemoveIsotope;
    private JButton btnNaturalizeIsotopes;
    private JTextField tfElementRatio;
    private JTextField tfElementAD;
    private JTextField tfLayerPosition;
    private JTextField tfLayerMassDensity;
    private JTextField tfElementZ;
    private JTextField tfIsotopeMass;
    private JTextField tfIsotopeRatio;
    private JTextField tfLayerAD;
    private JTextField tfLayerThickness;
    private JLabel lblErrorMsg;
    private JTextField tfMinAD;
    private JTextField tfMaxAD;
    private JTextField tfRatioMin;
    private JTextField tfRatioMax;

    private TargetModel targetModel;
    private String lastFolder;

    public TargetView(TargetModel targetModel, String title) {
        super(title);
        initComponents();
        this.targetModel = targetModel;
        updateTarget();
    }

    public void updateTarget() {
        fillLayerList();
        liLayers.setSelectedIndex(0);
        updateLayerProperties();
        fillElementList();
        liElements.setSelectedIndex(0);
        updateElementProperties();
        fillIsotopeList();
        liIsotopes.setSelectedIndex(0);
        updateIsotopeProperties();
    }

    public void setLastFolder(String lastFolder) {
        this.lastFolder = lastFolder;
    }

    private void switchLayer() {
        updateLayerProperties();
        fillElementList();
        liElements.setSelectedIndex(0);
        updateElementProperties();
        fillIsotopeList();
        liIsotopes.setSelectedIndex(0);
        updateIsotopeProperties();
    }

    private void switchElement() {
        updateElementProperties();
        fillIsotopeList();
        liIsotopes.setSelectedIndex(0);
        updateIsotopeProperties();
    }

    private void switchIsotope() {
        updateIsotopeProperties();
    }


    private void updateLayerProperties() {
        int layerIndex = liLayers.getSelectedIndex();
        tfLayerPosition.setText(Integer.toString(layerIndex + 1));
        double massDensity = targetModel.getTarget().getLayerList().get(layerIndex).getMassDensity();
        tfLayerMassDensity.setText(Helper.dblToDecStr(massDensity, 2));
        double arealDensity = targetModel.getTarget().getLayerList().get(layerIndex).getArealDensity();
        tfLayerAD.setText(Helper.dblToDecStr(arealDensity, 2));
        double minAD = targetModel.getTarget().getLayerList().get(layerIndex).getMinAD();
        tfMinAD.setText(Helper.dblToDecStr(minAD, 2));
        double maxAD = targetModel.getTarget().getLayerList().get(layerIndex).getMaxAD();
        tfMaxAD.setText(Helper.dblToDecStr(maxAD, 2));
        double thickness = targetModel.getTarget().getLayerList().get(layerIndex).getThickness();
        tfLayerThickness.setText(Helper.dblToDecStr(thickness, 2));
    }

    private void updateElementProperties() {
        int layerIndex = liLayers.getSelectedIndex();
        int elementIndex = liElements.getSelectedIndex();
        int Z = targetModel.getTarget().getLayerList().get(layerIndex).getElementList().get(elementIndex).getAtomicNumber();
        tfElementZ.setText(Integer.toString(Z));
        double ratio = targetModel.getTarget().getLayerList().get(layerIndex).getElementList().get(elementIndex).getRatio();
        tfElementRatio.setText(Helper.dblToDecStr(ratio, 4));
        double minRatio = targetModel.getTarget().getLayerList().get(layerIndex).getElementList().get(elementIndex).getMin_ratio();
        tfRatioMin.setText(Helper.dblToDecStr(minRatio, 4));
        double maxRatio = targetModel.getTarget().getLayerList().get(layerIndex).getElementList().get(elementIndex).getMax_ratio();
        tfRatioMax.setText(Helper.dblToDecStr(maxRatio, 4));
        double AD = targetModel.getTarget().getLayerList().get(layerIndex).getElementList().get(elementIndex).getArealDensity();
        tfElementAD.setText(Helper.dblToDecStr(AD, 4));
    }

    private void updateIsotopeProperties() {
        int layerIndex = liLayers.getSelectedIndex();
        int elementIndex = liElements.getSelectedIndex();
        int isotopeIndex = liIsotopes.getSelectedIndex();
        double mass = targetModel.getTarget().getLayerList().get(layerIndex).getElementList().get(elementIndex).getIsotopeList().get(isotopeIndex).getMass();
        tfIsotopeMass.setText(Helper.dblToDecStr(mass, 3));
        double ab = targetModel.getTarget().getLayerList().get(layerIndex).getElementList().get(elementIndex).getIsotopeList().get(isotopeIndex).getAbundance();
        tfIsotopeRatio.setText(Helper.dblToDecStr(ab, 3));
    }


    private void fillLayerList() {
        DefaultListModel lm = (DefaultListModel) liLayers.getModel();
        lm.removeAllElements();
        Iterable<Layer> layerList = targetModel.getTarget().getLayerList();
        int i = 1;
        for (Layer layer : layerList) {
            int numElements = layer.getElementList().size();
            String entry = "Layer_" + Integer.toString(i) + " [" + Integer.toString(numElements) + "]";
            lm.addElement(entry);
            i++;
        }
    }

    private void fillElementList() {
        int layerIndex = liLayers.getSelectedIndex();
        DefaultListModel lm = (DefaultListModel) liElements.getModel();
        lm.removeAllElements();
        Iterable<Element> elementList = targetModel.getTarget().getLayerList().get(layerIndex).getElementList();
        for (Element element : elementList) {
            String entry = "";
            entry += element.getName() + " (" + Helper.dblToDecStr(element.getRatio(), 4) + ") [" + Helper.dblToDecStr(element.getArealDensity(), 4) + "]";
            lm.addElement(entry);
        }
    }

    private void fillIsotopeList() {
        int layerIndex = liLayers.getSelectedIndex();
        int elementIndex = liElements.getSelectedIndex();
        DefaultListModel lm = (DefaultListModel) liIsotopes.getModel();
        lm.removeAllElements();
        Iterable<Isotope> isotopeList = targetModel.getTarget().getLayerList().get(layerIndex).getElementList().get(elementIndex).getIsotopeList();
        for (Isotope isotope : isotopeList) {
            String entry = "";
            entry += Helper.dblToDecStr(isotope.getMass(), 2) + " (" + Helper.dblToDecStr(isotope.getAbundance(), 2) + ")";
            lm.addElement(entry);
        }
    }


    private void addLayer() {
        int layerIndex = liLayers.getSelectedIndex();
        Target target = targetModel.getTarget();
        target.addLayer();
        targetModel.setTarget(target);
        fillLayerList();
        liLayers.setSelectedIndex(layerIndex);
    }

    private void removeLayer() {
        int layerIndex = liLayers.getSelectedIndex();
        Target target = targetModel.getTarget();
        target.removeLayer(layerIndex);
        targetModel.setTarget(target);
        fillLayerList();
        if (layerIndex > 0) layerIndex--;
        liLayers.setSelectedIndex(layerIndex);
        updateLayerProperties();
        fillElementList();
        liElements.setSelectedIndex(0);
        updateElementProperties();
        fillIsotopeList();
        liIsotopes.setSelectedIndex(0);
        updateIsotopeProperties();
    }

    private void swapLayers() {
        int layerIndex = liLayers.getSelectedIndex();
        Target target = targetModel.getTarget();
        try {
            int newLayerIndex = Integer.parseInt(tfLayerPosition.getText()) - 1;
            target.swapLayers(layerIndex, newLayerIndex);
        } catch (NumberFormatException ex) {
            lblErrorMsg.setText("Error parsing layer index.");
        }
        targetModel.setTarget(target);
        fillLayerList();
        liLayers.setSelectedIndex(layerIndex);
        updateLayerProperties();
        fillElementList();
        liElements.setSelectedIndex(0);
        updateElementProperties();
        fillIsotopeList();
        liIsotopes.setSelectedIndex(0);
        updateIsotopeProperties();
    }

    private void setLayerArealDensity() {
        int layerIndex = liLayers.getSelectedIndex();
        Target target = targetModel.getTarget();
        try {
            double AD = Double.parseDouble(tfLayerAD.getText());
            target.setLayerArealDensity(layerIndex, AD);
        } catch (NumberFormatException ex) {
            lblErrorMsg.setText("Error parsing layer AD.");
        }
        targetModel.setTarget(target);
        updateLayerProperties();
        int elementIndex = liElements.getSelectedIndex();
        fillElementList();
        liElements.setSelectedIndex(elementIndex);
        updateElementProperties();
    }

    private void setMinAD() {
        int layerIndex = liLayers.getSelectedIndex();
        Target target = targetModel.getTarget();
        try {
            double minAD = Double.parseDouble(tfMinAD.getText());
            double maxAD = target.getLayerList().get(layerIndex).getMaxAD();
            target.getLayerList().get(layerIndex).setConstrains(minAD, maxAD);
        } catch (NumberFormatException ex) {
            lblErrorMsg.setText("Error parsing layer AD constrains.");
        }
        targetModel.setTarget(target);
        updateLayerProperties();
    }

    private void setMaxAD() {
        int layerIndex = liLayers.getSelectedIndex();
        Target target = targetModel.getTarget();
        try {
            double minAD = target.getLayerList().get(layerIndex).getMinAD();
            double maxAD = Double.parseDouble(tfMaxAD.getText());
            target.getLayerList().get(layerIndex).setConstrains(minAD, maxAD);
        } catch (NumberFormatException ex) {
            lblErrorMsg.setText("Error parsing layer AD constrains.");
        }
        targetModel.setTarget(target);
        updateLayerProperties();
    }

    private void setLayerThickness() {
        int layerIndex = liLayers.getSelectedIndex();
        Target target = targetModel.getTarget();
        try {
            double thickness = Double.parseDouble(tfLayerThickness.getText());
            target.setLayerThickness(layerIndex, thickness);
        } catch (NumberFormatException ex) {
            lblErrorMsg.setText("Error parsing layer thickness.");
        }
        targetModel.setTarget(target);
        updateLayerProperties();
        int elementIndex = liElements.getSelectedIndex();
        fillElementList();
        liElements.setSelectedIndex(elementIndex);
        updateElementProperties();
    }

    private void setLayerMassDensity() {
        int layerIndex = liLayers.getSelectedIndex();
        Target target = targetModel.getTarget();
        try {
            double massDensity = Double.parseDouble(tfLayerMassDensity.getText());
            target.setLayerMassDensity(layerIndex, massDensity);
        } catch (NumberFormatException ex) {
            lblErrorMsg.setText("Error parsing layer mass density.");
        }
        targetModel.setTarget(target);
        updateLayerProperties();
    }


    private void addElement() {
        int layerIndex = liLayers.getSelectedIndex();
        int elementIndex = liElements.getSelectedIndex();
        Target target = targetModel.getTarget();
        target.addElement(layerIndex);
        targetModel.setTarget(target);
        updateLayerProperties();
        fillElementList();
        liElements.setSelectedIndex(elementIndex);
        updateElementProperties();
        fillIsotopeList();
        liIsotopes.setSelectedIndex(0);
        updateIsotopeProperties();
    }

    private void removeElement() {
        int layerIndex = liLayers.getSelectedIndex();
        int elementIndex = liElements.getSelectedIndex();
        Target target = targetModel.getTarget();
        target.removeElement(layerIndex, elementIndex);
        targetModel.setTarget(target);
        updateLayerProperties();
        fillElementList();
        if (elementIndex > 0) elementIndex--;
        liElements.setSelectedIndex(elementIndex);
        updateElementProperties();
        fillIsotopeList();
        liIsotopes.setSelectedIndex(0);
        updateIsotopeProperties();
    }

    private void normalizeElements() {
        int layerIndex = liLayers.getSelectedIndex();
        int elementIndex = liElements.getSelectedIndex();
        Target target = targetModel.getTarget();
        target.normalizeElements(layerIndex);
        targetModel.setTarget(target);
        fillElementList();
        liElements.setSelectedIndex(elementIndex);
        updateElementProperties();
    }

    private void setElementArealDensity() {
        int layerIndex = liLayers.getSelectedIndex();
        int elementIndex = liElements.getSelectedIndex();
        Target target = targetModel.getTarget();
        try {
            double AD = Double.parseDouble(tfElementAD.getText());
            target.setElementArealDensity(layerIndex, elementIndex, AD);
        } catch (NumberFormatException ex) {
            lblErrorMsg.setText("Error parsing element AD.");
        }
        targetModel.setTarget(target);
        updateLayerProperties();
        fillElementList();
        liElements.setSelectedIndex(elementIndex);
        updateElementProperties();
    }

    private void setElementRatio() {

        int layerIndex = liLayers.getSelectedIndex();
        int elementIndex = liElements.getSelectedIndex();
        Target target = targetModel.getTarget();
        try {
            double ratio = Double.parseDouble(tfElementRatio.getText());
            target.setElementRatio(layerIndex, elementIndex, ratio);
        } catch (NumberFormatException ex) {
            lblErrorMsg.setText("Error parsing element ratio.");
        }
        targetModel.setTarget(target);
        updateLayerProperties();
        fillElementList();
        liElements.setSelectedIndex(elementIndex);
        updateElementProperties();
    }

    private void setMinRatio() {

        int layerIndex = liLayers.getSelectedIndex();
        int elementIndex = liElements.getSelectedIndex();
        Target target = targetModel.getTarget();
        try {
            double minRatio = Double.parseDouble(tfRatioMin.getText());
            double maxRatio = target.getLayerList().get(layerIndex).getElementList().get(elementIndex).getMax_ratio();
            target.getLayerList().get(layerIndex).getElementList().get(elementIndex).setConstrains(minRatio, maxRatio);
        } catch (NumberFormatException ex) {
            lblErrorMsg.setText("Error parsing element ratio constrains.");
        }
        targetModel.setTarget(target);
        updateElementProperties();
    }

    private void setMaxRatio() {

        int layerIndex = liLayers.getSelectedIndex();
        int elementIndex = liElements.getSelectedIndex();
        Target target = targetModel.getTarget();
        try {
            double minRatio = target.getLayerList().get(layerIndex).getElementList().get(elementIndex).getMin_ratio();
            double maxRatio = Double.parseDouble(tfRatioMax.getText());
            target.getLayerList().get(layerIndex).getElementList().get(elementIndex).setConstrains(minRatio, maxRatio);
        } catch (NumberFormatException ex) {
            lblErrorMsg.setText("Error parsing element ratio constrains.");
        }
        targetModel.setTarget(target);
        updateElementProperties();
    }

    private void setElementAtomicNumber() {
        int layerIndex = liLayers.getSelectedIndex();
        int elementIndex = liElements.getSelectedIndex();
        Target target = targetModel.getTarget();
        try {
            int Z = Integer.parseInt(tfElementZ.getText());
            target.setElementAtomicNumber(layerIndex, elementIndex, Z);
        } catch (NumberFormatException ex) {
            if (!target.setElementAtomicNumberByName(layerIndex, elementIndex, tfElementZ.getText())) {
                lblErrorMsg.setText("Error parsing element's Z.");
            }
        }
        targetModel.setTarget(target);
        updateLayerProperties();
        fillElementList();
        liElements.setSelectedIndex(elementIndex);
        updateElementProperties();
        fillIsotopeList();
        liIsotopes.setSelectedIndex(0);
        updateIsotopeProperties();
    }


    private void addIsotope() {
        int layerIndex = liLayers.getSelectedIndex();
        int elementIndex = liElements.getSelectedIndex();
        int isotopeIndex = liIsotopes.getSelectedIndex();
        Target target = targetModel.getTarget();
        target.addIsotope(layerIndex, elementIndex);
        targetModel.setTarget(target);
        fillIsotopeList();
        liIsotopes.setSelectedIndex(isotopeIndex);
    }

    private void removeIsotope() {
        int layerIndex = liLayers.getSelectedIndex();
        int elementIndex = liElements.getSelectedIndex();
        int isotopeIndex = liIsotopes.getSelectedIndex();
        Target target = targetModel.getTarget();
        target.removeIsotope(layerIndex, elementIndex, isotopeIndex);
        targetModel.setTarget(target);
        updateLayerProperties();
        fillIsotopeList();
        if (isotopeIndex > 0) isotopeIndex--;
        liIsotopes.setSelectedIndex(isotopeIndex);
        updateIsotopeProperties();
    }

    private void normalizeIsotopes() {
        int layerIndex = liLayers.getSelectedIndex();
        int elementIndex = liElements.getSelectedIndex();
        int isotopeIndex = liIsotopes.getSelectedIndex();
        Target target = targetModel.getTarget();
        target.normalizeIsotopes(layerIndex, elementIndex);
        targetModel.setTarget(target);
        updateLayerProperties();
        fillIsotopeList();
        liIsotopes.setSelectedIndex(isotopeIndex);
        updateIsotopeProperties();
    }

    private void naturalizeIsotopes() {
        int layerIndex = liLayers.getSelectedIndex();
        int elementIndex = liElements.getSelectedIndex();
        Target target = targetModel.getTarget();
        target.naturalizeIsotopes(layerIndex, elementIndex);
        targetModel.setTarget(target);
        updateLayerProperties();
        fillIsotopeList();
        liIsotopes.setSelectedIndex(0);
        updateIsotopeProperties();
    }

    private void setIsotopeMass() {
        int layerIndex = liLayers.getSelectedIndex();
        int elementIndex = liElements.getSelectedIndex();
        int isotopeIndex = liIsotopes.getSelectedIndex();
        Target target = targetModel.getTarget();
        try {
            double isotopeMass = Double.parseDouble(tfIsotopeMass.getText());
            target.setIsotopeMass(layerIndex, elementIndex, isotopeIndex, isotopeMass);
        } catch (NumberFormatException ex) {
            lblErrorMsg.setText("Error parsing isotope mass.");
        }
        targetModel.setTarget(target);
        updateLayerProperties();
        fillIsotopeList();
        liIsotopes.setSelectedIndex(isotopeIndex);
        updateIsotopeProperties();
    }

    private void setIsotopeRatio() {
        int layerIndex = liLayers.getSelectedIndex();
        int elementIndex = liElements.getSelectedIndex();
        int isotopeIndex = liIsotopes.getSelectedIndex();
        Target target = targetModel.getTarget();
        try {
            double ratio = Double.parseDouble(tfIsotopeRatio.getText());
            target.setIsotopeRatio(layerIndex, elementIndex, isotopeIndex, ratio);
        } catch (NumberFormatException ex) {
            lblErrorMsg.setText("Error parsing isotope ratio.");
        }
        targetModel.setTarget(target);
        updateLayerProperties();
        fillIsotopeList();
        liIsotopes.setSelectedIndex(isotopeIndex);
        updateIsotopeProperties();
    }

    private void initComponents() {

        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.setContentPane(rootPanel);
        pack();
        this.setMinimumSize(getSize());
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - this.getWidth() / 2, dim.height / 2 - this.getHeight() / 2);

        this.lastFolder = null;


        liLayers.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                switchLayer();
            }
        });

        liElements.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                switchElement();
            }
        });

        liIsotopes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                switchIsotope();
            }
        });


        btnAddLayer.addActionListener(e -> addLayer());

        btnRemoveLayer.addActionListener(e -> removeLayer());

        btnAddElement.addActionListener(e -> addElement());

        btnRemoveElement.addActionListener(e -> removeElement());

        btnNormalizeElements.addActionListener(e -> normalizeElements());

        btnAddIsotope.addActionListener(e -> addIsotope());

        btnRemoveIsotope.addActionListener(e -> removeIsotope());

        btnNormalizeIsotopes.addActionListener(e -> normalizeIsotopes());

        btnNaturalizeIsotopes.addActionListener(e -> naturalizeIsotopes());


        tfLayerAD.addActionListener(e -> setLayerArealDensity());

        tfLayerAD.addMouseWheelListener(e -> {
            int stepSize = -e.getUnitsToScroll();
            if (stepSize > 10) stepSize = 10;
            double AD = Double.parseDouble(tfLayerAD.getText());
            AD += (double) stepSize / 100.0 * AD;
            if (AD < 0.0) AD = 0.0;
            tfLayerAD.setText(Helper.dblToDecStr(AD, 2));
            setLayerArealDensity();
        });

        tfMinAD.addActionListener(e -> setMinAD());

        tfMaxAD.addActionListener(e -> setMaxAD());

        tfLayerMassDensity.addActionListener(e -> setLayerMassDensity());

        tfElementAD.addActionListener(e -> setElementArealDensity());

        tfElementRatio.addActionListener(e -> setElementRatio());

        tfRatioMin.addActionListener(e -> setMinRatio());

        tfRatioMax.addActionListener(e -> setMaxRatio());

        tfElementRatio.addMouseWheelListener(e -> {
            int stepSize = -e.getUnitsToScroll();
            if (stepSize > 10) stepSize = 10;
            double ratio = Double.parseDouble(tfElementRatio.getText());
            ratio += (double) stepSize / 100.0 * ratio;
            if (ratio < 0.0) ratio = 0.0;
            tfElementRatio.setText(Helper.dblToDecStr(ratio, 4));
            setElementRatio();
        });

        tfElementZ.addActionListener(e -> setElementAtomicNumber());

        tfIsotopeMass.addActionListener(e -> setIsotopeMass());

        tfIsotopeRatio.addActionListener(e -> setIsotopeRatio());

        tfLayerThickness.addActionListener(e -> setLayerThickness());

        tfLayerThickness.addMouseWheelListener(e -> {
            int stepSize = -e.getUnitsToScroll();
            if (stepSize > 10) stepSize = 10;
            double thickness = Double.parseDouble(tfLayerThickness.getText());
            thickness += (double) stepSize / 100.0 * thickness;
            if (thickness < 0.0) thickness = 0.0;
            tfLayerThickness.setText(Helper.dblToDecStr(thickness, 2));
            setLayerThickness();
        });

        tfLayerPosition.addActionListener(e -> swapLayers());

        JMenuBar menuBar = new JMenuBar();
        JMenu menuFile = new JMenu("File");
        JMenuItem miLoadTarget = new JMenuItem("Import from simulation file");
        JMenuItem miPlotTarget = new JMenuItem("Plot");

        menuFile.add(miLoadTarget);
        menuFile.add(miPlotTarget);
        menuBar.add(menuFile);
        this.setJMenuBar(menuBar);

        miLoadTarget.addActionListener(e -> {

            Target target = targetModel.getTarget();

            try {
                Gson gson = new Gson();

                final JFileChooser fc;
                if (lastFolder != null) fc = new JFileChooser(lastFolder);
                else fc = new JFileChooser();
                int returnVal = fc.showOpenDialog(this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {

                    File file = fc.getSelectedFile();
                    lastFolder = file.getParent();

                    FileReader fr = new FileReader(file);
                    target = (gson.fromJson(fr, DataFile.class)).target.getDeepCopy();

                    lastFolder = fc.getSelectedFile().getParent();
                }

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            targetModel.setTarget(target);
            updateTarget();
        });

        miPlotTarget.addActionListener(e -> {

            StringBuilder sb = new StringBuilder();
            targetModel.getTarget().getInfo(sb);
            String text = sb.toString();
            JOptionPane.showMessageDialog(null, text);
            StringSelection stringSelection = new StringSelection(text);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
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
        rootPanel.setLayout(new GridLayoutManager(5, 6, new Insets(5, 5, 5, 5), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        rootPanel.add(panel1, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(166, 169), null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder(null, "Layers", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        liLayers = new JList();
        final DefaultListModel defaultListModel1 = new DefaultListModel();
        defaultListModel1.addElement("Layer 1");
        defaultListModel1.addElement("Layer 2");
        defaultListModel1.addElement("Layer 3");
        liLayers.setModel(defaultListModel1);
        panel1.add(liLayers, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        rootPanel.add(panel2, new GridConstraints(0, 2, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(166, 169), null, 0, false));
        panel2.setBorder(BorderFactory.createTitledBorder(null, "Elements", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        liElements = new JList();
        final DefaultListModel defaultListModel2 = new DefaultListModel();
        defaultListModel2.addElement("Si (0.66) [5000]");
        defaultListModel2.addElement("O (0.30) [3000]");
        defaultListModel2.addElement("Ta (0.02) [35]");
        liElements.setModel(defaultListModel2);
        panel2.add(liElements, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        rootPanel.add(panel3, new GridConstraints(0, 4, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(166, 169), null, 0, false));
        panel3.setBorder(BorderFactory.createTitledBorder(null, "Isotopes", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        liIsotopes = new JList();
        final DefaultListModel defaultListModel3 = new DefaultListModel();
        defaultListModel3.addElement("28.03 (85.54%)");
        defaultListModel3.addElement("29.12 (12.30%)");
        defaultListModel3.addElement("30.00 (1.87%)");
        liIsotopes.setModel(defaultListModel3);
        panel3.add(liIsotopes, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        btnRemoveElement = new JButton();
        btnRemoveElement.setText("Remove");
        rootPanel.add(btnRemoveElement, new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnAddElement = new JButton();
        btnAddElement.setText("Add");
        rootPanel.add(btnAddElement, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnAddLayer = new JButton();
        btnAddLayer.setText("Add");
        rootPanel.add(btnAddLayer, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnRemoveLayer = new JButton();
        btnRemoveLayer.setText("Remove");
        rootPanel.add(btnRemoveLayer, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnNormalizeElements = new JButton();
        btnNormalizeElements.setText("Normalize");
        rootPanel.add(btnNormalizeElements, new GridConstraints(4, 2, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnAddIsotope = new JButton();
        btnAddIsotope.setText("Add");
        rootPanel.add(btnAddIsotope, new GridConstraints(3, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnRemoveIsotope = new JButton();
        btnRemoveIsotope.setText("Remove");
        rootPanel.add(btnRemoveIsotope, new GridConstraints(3, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnNormalizeIsotopes = new JButton();
        btnNormalizeIsotopes.setText("Normalize");
        rootPanel.add(btnNormalizeIsotopes, new GridConstraints(4, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnNaturalizeIsotopes = new JButton();
        btnNaturalizeIsotopes.setText("Naturalize");
        rootPanel.add(btnNaturalizeIsotopes, new GridConstraints(4, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(6, 2, new Insets(5, 5, 5, 5), -1, -1));
        rootPanel.add(panel4, new GridConstraints(1, 2, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel4.setBorder(BorderFactory.createTitledBorder(null, "Properties", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label1 = new JLabel();
        label1.setText("Z");
        panel4.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Areal Density");
        panel4.add(label2, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfElementZ = new JTextField();
        panel4.add(tfElementZ, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Ratio");
        panel4.add(label3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfElementRatio = new JTextField();
        panel4.add(tfElementRatio, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        tfElementAD = new JTextField();
        panel4.add(tfElementAD, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("");
        panel4.add(label4, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfRatioMin = new JTextField();
        tfRatioMin.setForeground(new Color(-14905123));
        panel4.add(tfRatioMin, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        tfRatioMax = new JTextField();
        tfRatioMax.setForeground(new Color(-14905123));
        panel4.add(tfRatioMax, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setForeground(new Color(-14905123));
        label5.setText("   [min]");
        panel4.add(label5, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setForeground(new Color(-14905123));
        label6.setText("   [max]");
        panel4.add(label6, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel4.add(spacer1, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(4, 3, new Insets(5, 5, 5, 5), -1, -1));
        rootPanel.add(panel5, new GridConstraints(1, 4, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel5.setBorder(BorderFactory.createTitledBorder(null, "Properties", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label7 = new JLabel();
        label7.setText("Mass");
        panel5.add(label7, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("");
        panel5.add(label8, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfIsotopeMass = new JTextField();
        panel5.add(tfIsotopeMass, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Ratio");
        panel5.add(label9, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfIsotopeRatio = new JTextField();
        panel5.add(tfIsotopeRatio, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("");
        panel5.add(label10, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel5.add(spacer2, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(6, 2, new Insets(5, 5, 5, 5), -1, -1));
        rootPanel.add(panel6, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel6.setBorder(BorderFactory.createTitledBorder(null, "Properties", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label11 = new JLabel();
        label11.setText("Position");
        panel6.add(label11, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfLayerPosition = new JTextField();
        panel6.add(tfLayerPosition, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        final JLabel label12 = new JLabel();
        label12.setText("Mass density");
        panel6.add(label12, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label13 = new JLabel();
        label13.setText("Areal Density");
        panel6.add(label13, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfLayerMassDensity = new JTextField();
        panel6.add(tfLayerMassDensity, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        final JLabel label14 = new JLabel();
        label14.setText("Thickness");
        panel6.add(label14, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfLayerAD = new JTextField();
        panel6.add(tfLayerAD, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        tfLayerThickness = new JTextField();
        panel6.add(tfLayerThickness, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        tfMinAD = new JTextField();
        tfMinAD.setForeground(new Color(-14905123));
        panel6.add(tfMinAD, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        tfMaxAD = new JTextField();
        tfMaxAD.setForeground(new Color(-14905123));
        panel6.add(tfMaxAD, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        final JLabel label15 = new JLabel();
        label15.setForeground(new Color(-14905123));
        label15.setText("   [min]");
        panel6.add(label15, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label16 = new JLabel();
        label16.setForeground(new Color(-14905123));
        label16.setText("   [max]");
        panel6.add(label16, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lblErrorMsg = new JLabel();
        lblErrorMsg.setText("");
        rootPanel.add(lblErrorMsg, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        rootPanel.add(spacer3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }
}
