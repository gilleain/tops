package tops.drawing.app;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.Border;


public class TemplateDialog extends JDialog implements ActionListener {

    private int selectedIndex; // determines the template that is selected.

    private TopsEditor parentPanel;

    private Container container;

    private String[] templateNames = { "GreekKey", "Rossmann",
            "ImmununoGlobulin", "Plait", "JellyRoll", "TimBarrel", "KeyBarrel",
            "UbiquitinRoll", };

    private String[] templateDesc = { "----", "----", "----", "----", "----",
            "----", "----", "----", };

    private String[] templateSSE = { "NEeEeEC 1:4A 2:3A 2:5A 3:4A",
            "NEhEhEhEhEC 1:3R 1:3P 1:7P 3:5R 3:5P 5:7R 7:9R 7:9P",
            "NEeEeeEeEC 1:2A 2:6A 3:4A 3:7A 5:6A 7:8A ",
            "NEhEeHeC 1:3R 1:4A 1:6A 3:4A 4:6R ",
            "NEeEeEeEeEeC 1:2A 2:9A 3:10A 3:8A 4:7A 4:9A 5:6A 5:8A ",
            "NEhEhEhEhEhEhEhEC 1:3P 1:15P 3:5P 5:7P 7:9P 9:11P 11:13P 13:15P",
            "NEeEeEeC 1:2A 1:6A 2:5A 3:4A 3:6A 4:5A ",
            "NEeHeEC 1:2A 1:5P 2:4R 4:5A", 
    };

    private JLabel[] templateImages = {
            new JLabel(new ImageIcon(MediaCenter.getImage("greek_thumb"))),
            new JLabel(new ImageIcon(MediaCenter.getImage("rosma_thumb"))),
            new JLabel(new ImageIcon(MediaCenter.getImage("immun_thumb"))),
            new JLabel(new ImageIcon(MediaCenter.getImage("plait_thumb"))),
            new JLabel(new ImageIcon(MediaCenter.getImage("jelly_thumb"))),
            new JLabel(new ImageIcon(MediaCenter.getImage("timba_thumb"))),
            new JLabel(new ImageIcon(MediaCenter.getImage("keyba_thumb"))),
            new JLabel(new ImageIcon(MediaCenter.getImage("ubrol_thumb"))), 
    };

    private JComboBox<Object> templateNameCombo;
    private JPanel templateComboPanel;
    private JPanel middlePanel;
    private JPanel templateImagePanel;
    private JPanel innerPicPanel;
    private JPanel templateDescriptionPanel;

    private JPanel templateButtonPanel;

    private JButton submit;
    private JButton cancel;

    // labels for the description
    private JLabel templateNameLabel;

    private JLabel templateSseLabel;

    private JLabel templateDescLabel;

    // the Strings for the label
    private static final String TEMPLATE_NAME = "Template Name:  ";

    private static final String TEMPLATE_SSE = "SSE String   :  ";

    private static final String TEMPLATE_DESC = "Protein Desc :  ";

    /** Creates a new instance of TemplateDialog */
    public TemplateDialog(TopsEditor parentPanel) {

        this.parentPanel = parentPanel;

        container = getContentPane();

        templateNameLabel = new JLabel();
        templateNameCombo = new JComboBox<>();
        templateComboPanel = new JPanel();

        templateImagePanel = new JPanel();
        innerPicPanel = new JPanel();
        
        // this contains the imae and the description
        middlePanel = new JPanel(); 

        templateDescriptionPanel = new JPanel();

        templateButtonPanel = new JPanel();
        submit = new JButton("Load Template");
        cancel = new JButton("Cancel");

        templateNameLabel = new JLabel(TEMPLATE_NAME);
        templateSseLabel = new JLabel(TEMPLATE_SSE);
        templateDescLabel = new JLabel(TEMPLATE_DESC);
        
        // the main settings
        setSize(400, 500);
        setTitle("Template Selection Dialog");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setModal(false);
        
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        
        templateComboPanel.setPreferredSize(new Dimension(100, 70));
        
        // border settings
        Border etched = BorderFactory.createEtchedBorder();
        Border titled = BorderFactory.createTitledBorder(etched, "Select Template");
        
        // JLabel Settings
        String templateName = "Template Name:         ";
        templateNameLabel.setText(templateName);
        templateNameLabel.setForeground(Color.black);
        
        // combo box settings
        templateNameCombo.setModel(new DefaultComboBoxModel<Object>(this.templateNames));
        templateNameCombo.setSelectedIndex(0);
        templateNameCombo.setForeground(Color.black);
        
        templateComboPanel.setBorder(titled);
        templateComboPanel.add(templateNameLabel);
        templateComboPanel.add(templateNameCombo);
        
        container.add(templateComboPanel);
        
        
        Border etched1 = BorderFactory.createEtchedBorder();
        Border emptyBorder = BorderFactory.createEmptyBorder(0, 20, 10, 20);
        Border compound = BorderFactory.createCompoundBorder(etched1, emptyBorder);
        Border titled1 = BorderFactory.createTitledBorder(compound,
                "Template Picture & Description");
        Border line = BorderFactory.createLineBorder(Color.black);
        
        Border emptyOuter = BorderFactory.createEmptyBorder(0, 0, 0, 0);
        Border emptyInner = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        Border emptyLine = BorderFactory.createCompoundBorder(emptyOuter, line);
        Border emptyLineEmptyBorder = BorderFactory.createCompoundBorder(emptyLine, emptyInner);
        
        // middle panel settings
        middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
        middlePanel.setBorder(titled1);
        middlePanel.setPreferredSize(new Dimension(100, 300));
        
        // the template image panel and its containing inner panel
        templateImagePanel.setLayout(new BoxLayout(templateImagePanel, BoxLayout.X_AXIS));
        innerPicPanel.setPreferredSize(new Dimension(30, 200));
        innerPicPanel.setBorder(emptyLineEmptyBorder);
        innerPicPanel.setBackground(Color.white);
        
        // the panel containing the description
        templateDescriptionPanel.setLayout(new BoxLayout(
                templateDescriptionPanel, BoxLayout.Y_AXIS));
        templateDescriptionPanel.setLayout(new GridLayout(3, 1));
        
        templateDescriptionPanel.setBorder(emptyLineEmptyBorder);
        
        // adding everything together
        
        JPanel dummyPanel = new JPanel();
        dummyPanel.setPreferredSize(new Dimension(30, 20));
        
        templateImagePanel.add(innerPicPanel);
        
        // add the template name, SSE String equivalent, description to the
        // template_image_panel
        templateDescriptionPanel.add(templateNameLabel);
        templateDescriptionPanel.add(templateSseLabel);
        
        // now add the picture & desc panels to the middle panel
        middlePanel.add(templateImagePanel);
        middlePanel.add(dummyPanel);
        middlePanel.add(templateDescriptionPanel);
        
        // add the middle to the container
        container.add(middlePanel);
        
        updateTemplateDescription();
        templateButtonPanel.setPreferredSize(new Dimension(100, 70));
        // border settings
        Border etched2 = BorderFactory.createEtchedBorder();
        Border titled2 = BorderFactory.createTitledBorder(etched2, "");
        
        // add the buttons
        templateButtonPanel.add(this.submit);
        templateButtonPanel.add(this.cancel);
        templateButtonPanel.setBorder(titled2);
        
        container.add(templateButtonPanel);
        
        
        submit.addActionListener(this);
        cancel.addActionListener(this);
        templateNameCombo.addActionListener(this);

        selectedIndex = 0;
        updateTemplatePictureAtStart();
        updateTemplateDescription();
        setVisible(false);
    }

    public void resetAll() {
        selectedIndex = 0;
    }

    private void updateTemplatePicture() {
        this.innerPicPanel.removeAll();
        innerPicPanel.add(templateImages[selectedIndex]);
        innerPicPanel.repaint();
        this.repaint();
        this.setVisible(false);
        this.setVisible(true);
    }

    // dont have the setVisible true and false
    private void updateTemplatePictureAtStart() {
        this.innerPicPanel.removeAll();
        innerPicPanel.add(templateImages[selectedIndex]);
        innerPicPanel.repaint();
        this.repaint();
    }

    // update the picture panel with picture templatePics[selectedIndex]
    private void updateTemplateDescription() {
        templateNameLabel.setText(TEMPLATE_NAME + templateNames[selectedIndex]);
        templateDescLabel.setText(TEMPLATE_DESC + templateDesc[selectedIndex]);
        templateSseLabel.setText(TEMPLATE_SSE + templateSSE[selectedIndex]);
    }

    public void actionPerformed(ActionEvent ae) {

        if (ae.getSource() == submit) {
            // parentPanel.fireTemplateSelected(templateNum) // maybe a
            // Template Object?
            int templateIDIndex = templateNameCombo.getSelectedIndex();
            String templateID = templateNames[templateIDIndex];

            String filename = "";
            if (templateID.equals("GreekKey"))
                filename = "greek.tops";
            else if (templateID.equals("ImmunoGlobulin"))
                filename = "immun.tops";
            else if (templateID.equals("KeyBarrel"))
                filename = "keyba.tops";
            else if (templateID.equals("Plait"))
                filename = "plait.tops";
            else if (templateID.equals("Rossmann"))
                filename = "rosma.tops";
            else if (templateID.equals("TimBarrel"))
                filename = "timba.tops";
            else if (templateID.equals("UbiquitinRoll"))
                filename = "ubrol.tops";
            else if (templateID.equals("JellyRoll"))
                filename = "jelly.tops";

            parentPanel.fireLoadTemplateEvent("templates/" + filename);
            setVisible(false);
        } else if (ae.getSource() == this.templateNameCombo) {
            selectedIndex = templateNameCombo.getSelectedIndex();
            updateTemplatePicture();
            updateTemplateDescription();
        } else {
            resetAll(); // place back to first index
            setVisible(false);
        }
    }
}
