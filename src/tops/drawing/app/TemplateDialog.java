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

    private String[] template_names = { "GreekKey", "Rossmann",
            "ImmununoGlobulin", "Plait", "JellyRoll", "TimBarrel", "KeyBarrel",
            "UbiquitinRoll", };

    private String[] template_desc = { "----", "----", "----", "----", "----",
            "----", "----", "----", };

    private String[] template_sse = { "NEeEeEC 1:4A 2:3A 2:5A 3:4A",
            "NEhEhEhEhEC 1:3R 1:3P 1:7P 3:5R 3:5P 5:7R 7:9R 7:9P",
            "NEeEeeEeEC 1:2A 2:6A 3:4A 3:7A 5:6A 7:8A ",
            "NEhEeHeC 1:3R 1:4A 1:6A 3:4A 4:6R ",
            "NEeEeEeEeEeC 1:2A 2:9A 3:10A 3:8A 4:7A 4:9A 5:6A 5:8A ",
            "NEhEhEhEhEhEhEhEC 1:3P 1:15P 3:5P 5:7P 7:9P 9:11P 11:13P 13:15P",
            "NEeEeEeC 1:2A 1:6A 2:5A 3:4A 3:6A 4:5A ",
            "NEeHeEC 1:2A 1:5P 2:4R 4:5A", 
    };

    private JLabel[] template_images = {
            new JLabel(new ImageIcon(MediaCenter.getImage("greek_thumb"))),
            new JLabel(new ImageIcon(MediaCenter.getImage("rosma_thumb"))),
            new JLabel(new ImageIcon(MediaCenter.getImage("immun_thumb"))),
            new JLabel(new ImageIcon(MediaCenter.getImage("plait_thumb"))),
            new JLabel(new ImageIcon(MediaCenter.getImage("jelly_thumb"))),
            new JLabel(new ImageIcon(MediaCenter.getImage("timba_thumb"))),
            new JLabel(new ImageIcon(MediaCenter.getImage("keyba_thumb"))),
            new JLabel(new ImageIcon(MediaCenter.getImage("ubrol_thumb"))), 
    };

    private JLabel templateName_label;
    private JComboBox templateName_combo;
    private JPanel templateComboPanel;
    private JPanel middle_panel, template_image_panel, inner_pic_panel;
    private JPanel template_description_panel;

    private JPanel template_button_panel;

    private JButton submit, cancel;

    // labels for the description
    private JLabel template_name_label;

    private JLabel template_sse_label;

    private JLabel template_desc_label;

    // the Strings for the label
    private String TEMPLATE_NAME = "Template Name:  ";

    private String TEMPLATE_SSE = "SSE String   :  ";

    private String TEMPLATE_DESC = "Protein Desc :  ";

    /** Creates a new instance of TemplateDialog */
    public TemplateDialog(TopsEditor parentPanel) {

        this.parentPanel = parentPanel;

        container = getContentPane();

        templateName_label = new JLabel();
        templateName_combo = new JComboBox();
        templateComboPanel = new JPanel();

        template_image_panel = new JPanel();
        inner_pic_panel = new JPanel();
        
        // this contains the imae and the description
        middle_panel = new JPanel(); 

        template_description_panel = new JPanel();

        template_button_panel = new JPanel();
        submit = new JButton("Load Template");
        cancel = new JButton("Cancel");

        template_name_label = new JLabel(TEMPLATE_NAME);
        template_sse_label = new JLabel(TEMPLATE_SSE);
        template_desc_label = new JLabel(TEMPLATE_DESC);
        
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
        templateName_label.setText(templateName);
        templateName_label.setForeground(Color.black);
        
        // combo box settings
        templateName_combo.setModel(new DefaultComboBoxModel(this.template_names));
        templateName_combo.setSelectedIndex(0);
        templateName_combo.setForeground(Color.black);
        
        templateComboPanel.setBorder(titled);
        templateComboPanel.add(templateName_label);
        templateComboPanel.add(templateName_combo);
        
        container.add(templateComboPanel);
        
        
        Border etched1 = BorderFactory.createEtchedBorder();
        Border empty_border = BorderFactory.createEmptyBorder(0, 20, 10, 20);
        Border compound = BorderFactory.createCompoundBorder(etched1, empty_border);
        Border titled1 = BorderFactory.createTitledBorder(compound,
                "Template Picture & Description");
        Border line = BorderFactory.createLineBorder(Color.black);
        
        Border empty_outer = BorderFactory.createEmptyBorder(0, 0, 0, 0);
        Border empty_inner = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        Border empty_line = BorderFactory.createCompoundBorder(empty_outer,
                line);
        Border empty_line_empty_border = BorderFactory.createCompoundBorder(
                empty_line, empty_inner);
        
        // middle panel settings
        middle_panel.setLayout(new BoxLayout(middle_panel, BoxLayout.Y_AXIS));
        middle_panel.setBorder(titled1);
        middle_panel.setPreferredSize(new Dimension(100, 300));
        
        // the template image panel and its containing inner panel
        template_image_panel.setLayout(new BoxLayout(template_image_panel,
                BoxLayout.X_AXIS));
        inner_pic_panel.setPreferredSize(new Dimension(30, 200));
        inner_pic_panel.setBorder(empty_line_empty_border);
        inner_pic_panel.setBackground(Color.white);
        
        // the panel containing the description
        template_description_panel.setLayout(new BoxLayout(
                template_description_panel, BoxLayout.Y_AXIS));
        template_description_panel.setLayout(new GridLayout(3, 1));
        
        template_description_panel.setBorder(empty_line_empty_border);
        
        // adding everything together
        
        JPanel dummyPanel = new JPanel();
        dummyPanel.setPreferredSize(new Dimension(30, 20));
        
        template_image_panel.add(inner_pic_panel);
        
        // add the template name, SSE String equivalent, description to the
        // template_image_panel
        template_description_panel.add(template_name_label);
        template_description_panel.add(template_sse_label);
        // template_description_panel.add(template_desc_label);
        
        // now add the picture & desc panels to the middle panel
        middle_panel.add(template_image_panel);
        middle_panel.add(dummyPanel);
        middle_panel.add(template_description_panel);
        
        // add the middle to the container
        container.add(middle_panel);
        
        updateTemplateDescription();
        template_button_panel.setPreferredSize(new Dimension(100, 70));
        // border settings
        Border etched2 = BorderFactory.createEtchedBorder();
        Border titled2 = BorderFactory.createTitledBorder(etched2, "");
        
        // add the buttons
        template_button_panel.add(this.submit);
        template_button_panel.add(this.cancel);
        template_button_panel.setBorder(titled2);
        
        container.add(template_button_panel);
        
        
        submit.addActionListener(this);
        cancel.addActionListener(this);
        templateName_combo.addActionListener(this);

        selectedIndex = 0;
        updateTemplatePicture_at_start();
        updateTemplateDescription();
        setVisible(false);
    }

    public void resetAll() {
        selectedIndex = 0;
    }

    private void updateTemplatePicture() {
        this.inner_pic_panel.removeAll();
        inner_pic_panel.add(template_images[selectedIndex]);
        inner_pic_panel.repaint();
        this.repaint();
        this.setVisible(false);
        this.setVisible(true);
    }

    // dont have the setVisible true and false
    private void updateTemplatePicture_at_start() {
        this.inner_pic_panel.removeAll();
        inner_pic_panel.add(template_images[selectedIndex]);
        inner_pic_panel.repaint();
        this.repaint();
    }

    // update the picture panel with picture templatePics[selectedIndex]
    private void updateTemplateDescription() {
        template_name_label.setText(TEMPLATE_NAME + template_names[selectedIndex]);
        template_desc_label.setText(TEMPLATE_DESC + template_desc[selectedIndex]);
        template_sse_label.setText(TEMPLATE_SSE + template_sse[selectedIndex]);
    }

    public void actionPerformed(ActionEvent ae) {

        if (ae.getSource() == submit) {
            // parentPanel.fireTemplateSelected(templateNum) // maybe a
            // Template Object?
            int template_ID_index = templateName_combo.getSelectedIndex();
            String template_ID = template_names[template_ID_index];

            String filename = "";
            if (template_ID.equals("GreekKey"))
                filename = "greek.tops";
            else if (template_ID.equals("ImmunoGlobulin"))
                filename = "immun.tops";
            else if (template_ID.equals("KeyBarrel"))
                filename = "keyba.tops";
            else if (template_ID.equals("Plait"))
                filename = "plait.tops";
            else if (template_ID.equals("Rossmann"))
                filename = "rosma.tops";
            else if (template_ID.equals("TimBarrel"))
                filename = "timba.tops";
            else if (template_ID.equals("UbiquitinRoll"))
                filename = "ubrol.tops";
            else if (template_ID.equals("JellyRoll"))
                filename = "jelly.tops";

            parentPanel.fireLoadTemplateEvent("templates/" + filename);
            setVisible(false);
        } else if (ae.getSource() == this.templateName_combo) {
            selectedIndex = templateName_combo.getSelectedIndex();
            updateTemplatePicture();
            updateTemplateDescription();
        } else {
            resetAll(); // place back to first index
            setVisible(false);
        }
    }
}
