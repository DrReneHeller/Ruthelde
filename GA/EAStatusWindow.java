package GA;

import javax.swing.*;
import java.awt.*;

public class EAStatusWindow extends JFrame{

    public JTextArea ta_info;
    private JPanel rootPanel;

    public EAStatusWindow(String title){

        super(title);
        initComponents();
    }

    private void initComponents(){

        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        this.setContentPane(rootPanel);

        pack();
        this.setMinimumSize(new Dimension(400,200));
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - this.getWidth() / 2, dim.height / 2 - this.getHeight() / 2);

    }
}
