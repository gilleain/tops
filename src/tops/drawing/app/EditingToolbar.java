package tops.drawing.app;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.Border;


/**
 * @author maclean
 *
 */
public class EditingToolbar extends JPanel implements ActionListener {

    // Activities
    public static int SELECT           = 0;
    public static int UNDO             = 1;
    public static int DELETE           = 2;
    public static int CLEAR            = 3;
    public static int ZOOM_IN          = 4;
    public static int ZOOM_OUT         = 5;
    public static int SUBMIT           = 6;
    
    // Symbols
    public static int STRAND_UP        = 7;
    public static int STRAND_DOWN      = 8;
    public static int HELIX_UP         = 9;
    public static int HELIX_DOWN       = 10;
    public static int TEMPLATE         = 11;
    
    // Arcs
    public static int H_BOND           = 12;
    public static int RIGHT_ARC        = 13;
    public static int LEFT_ARC         = 14;
    public static int RANGE            = 15;
    
    // Flips and Align
    public static int FLIP             = 16;
    public static int HORIZONTAL_ALIGN = 17;
    public static int VERTICAL_ALIGN   = 18;
    public static int FLIP_X           = 19;
    public static int FLIP_Y           = 20;
    
    private TopsEditor parentPanel;
    private JToggleButton[] buttons;
    
    private int num_of_buttons = 21;

    private JPanel activityPanel;
    private JPanel symbols_panel;
    private JPanel arcs_bonds_panel;
    private JPanel flip_panel;
    
    private static int MENU_BUTTON_PANEL_HEIGHT = 25;

    public EditingToolbar(TopsEditor parentPanel) {
        this.parentPanel = parentPanel;
        
        Border etched = BorderFactory.createEtchedBorder();
        this.setBorder(etched);
        this.setLayout(new GridLayout(2, 2));
        
        Dimension buttonSize = new Dimension(MENU_BUTTON_PANEL_HEIGHT, MENU_BUTTON_PANEL_HEIGHT);
        
        ButtonGroup buttonGroup = new ButtonGroup();
        buttons = new JToggleButton[num_of_buttons];
        
        for (int i = 0; i < buttons.length; i++) {
            Image mainImage = MediaCenter.getImage(MediaCenter.buttonImageNames[i]);
            if (mainImage != null) {
                buttons[i] = new JToggleButton(new ImageIcon(mainImage));
            } else {
                buttons[i] = new JToggleButton(new ImageIcon());
            }
        
            Image selectedImage = MediaCenter.getImage(MediaCenter.rolloverImageNames[i]);
            ImageIcon selectedIcon;
            if (selectedImage != null) {
                selectedIcon = new ImageIcon(selectedImage);
            } else {
                selectedIcon = new ImageIcon();
            }
            buttons[i].setRolloverIcon(selectedIcon);
            buttons[i].setSelectedIcon(selectedIcon);
        
            buttons[i].setPreferredSize(buttonSize);
            buttonGroup.add(buttons[i]);
            buttons[i].addActionListener(this);
        }
        
        activityPanel = new JPanel();
        symbols_panel = new JPanel();
        arcs_bonds_panel = new JPanel();
        flip_panel = new JPanel();
        
        activityPanel.add(buttons[SELECT]);
        activityPanel.add(buttons[UNDO]);
        activityPanel.add(buttons[DELETE]);
        activityPanel.add(buttons[CLEAR]);
        activityPanel.add(buttons[ZOOM_IN]);
        activityPanel.add(buttons[ZOOM_OUT]);
        activityPanel.add(buttons[SUBMIT]);
        
        activityPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Actions"));
        this.add(activityPanel);
        
        symbols_panel.add(buttons[STRAND_UP]);
        symbols_panel.add(buttons[STRAND_DOWN]);
        symbols_panel.add(buttons[HELIX_UP]);
        symbols_panel.add(buttons[HELIX_DOWN]);
        symbols_panel.add(buttons[TEMPLATE]);
        
        symbols_panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "SSEs"));
        this.add(symbols_panel);
        
        arcs_bonds_panel.add(buttons[H_BOND]);
        arcs_bonds_panel.add(buttons[RIGHT_ARC]);
        arcs_bonds_panel.add(buttons[LEFT_ARC]);
        arcs_bonds_panel.add(buttons[RANGE]);
        
        arcs_bonds_panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Arcs & Bond"));
        this.add(arcs_bonds_panel);
        
        flip_panel.add(buttons[FLIP]);
        flip_panel.add(buttons[FLIP_X]);
        flip_panel.add(buttons[FLIP_Y]);
        flip_panel.add(buttons[HORIZONTAL_ALIGN]);
        flip_panel.add(buttons[VERTICAL_ALIGN]);
        
        flip_panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Alignment"));
        this.add(flip_panel);
        
        buttons[SELECT].setToolTipText("Default (F1)");
        buttons[STRAND_UP].setToolTipText("Insert Up Strand (F2)");
        buttons[STRAND_UP].setToolTipText("Insert Down Strand (F3)");
        buttons[HELIX_UP].setToolTipText("Insert Up Helix (F4)");
        buttons[HELIX_DOWN].setToolTipText("Insert Down Strand (F5)");
        buttons[RIGHT_ARC].setToolTipText("Insert an Right Chirality Arc (F6)");
        buttons[LEFT_ARC ].setToolTipText("Insert an Left Chirality Arc (F7)");
        buttons[H_BOND].setToolTipText("Insert a Hydrogen Bond (F8)");
        buttons[RANGE].setToolTipText("Add a range to a connecter (F9)");
        buttons[SUBMIT].setToolTipText("Submit Cartoon to Database (F10)");
      
        buttons[DELETE].setToolTipText("Delete Selected SSEs");
        buttons[FLIP].setToolTipText("Flip Selected SSEs");
        buttons[HORIZONTAL_ALIGN].setToolTipText("Horizontally Allign Selected SSEs ");
        buttons[VERTICAL_ALIGN].setToolTipText("Vertically Allign Selected SSEs ");
        buttons[CLEAR].setToolTipText("Clears the canvas");
        buttons[FLIP_X].setToolTipText("Flip cartoon in the x axis");
        buttons[FLIP_Y].setToolTipText("Flip cartoon in the y axis");
        buttons[ZOOM_IN].setToolTipText("Zoom in");
        buttons[ZOOM_OUT].setToolTipText("Zoom out");
        buttons[UNDO].setToolTipText("Undo");
        buttons[TEMPLATE].setToolTipText("Load a template");
        
        buttons[SELECT].setMnemonic(KeyEvent.VK_F1);
        buttons[STRAND_UP].setMnemonic(KeyEvent.VK_F2);
        buttons[STRAND_DOWN].setMnemonic(KeyEvent.VK_F3);
        buttons[HELIX_UP].setMnemonic(KeyEvent.VK_F4);
        buttons[HELIX_DOWN].setMnemonic(KeyEvent.VK_F5);
        buttons[RIGHT_ARC].setMnemonic(KeyEvent.VK_F6);
        buttons[LEFT_ARC ].setMnemonic(KeyEvent.VK_F7);
        buttons[H_BOND].setMnemonic(KeyEvent.VK_F8);
        buttons[RANGE].setMnemonic(KeyEvent.VK_F9);
    }

    public void actionPerformed(ActionEvent ae) {

        if (ae.getSource() == buttons[SELECT]) {
            this.parentPanel.setState(SELECT);
        } else if (ae.getSource() == buttons[STRAND_UP]) {
            this.parentPanel.setState(STRAND_UP);
            this.parentPanel.deselectAll();
        } else if (ae.getSource() == buttons[STRAND_DOWN]) {
            this.parentPanel.setState(STRAND_DOWN);
            this.parentPanel.deselectAll();
        } else if (ae.getSource() == buttons[HELIX_UP]) {
            this.parentPanel.setState(HELIX_UP);
            this.parentPanel.deselectAll();
        } else if (ae.getSource() == buttons[HELIX_DOWN]) {
            this.parentPanel.setState(HELIX_DOWN);
            this.parentPanel.deselectAll();
        } else if (ae.getSource() == buttons[RIGHT_ARC]) {
            this.parentPanel.setState(RIGHT_ARC);
            this.parentPanel.deselectAll();
        } else if (ae.getSource() == buttons[LEFT_ARC]) {
            this.parentPanel.setState(LEFT_ARC);
            this.parentPanel.deselectAll();
        } else if (ae.getSource() == buttons[H_BOND]) {
            this.parentPanel.setState(H_BOND);
            this.parentPanel.deselectAll();
        } else if (ae.getSource() == buttons[RANGE]) {
            this.parentPanel.setState(RANGE);
            parentPanel.fireInsertRangesEvent();
        } else if (ae.getSource() == buttons[DELETE]) {
            this.parentPanel.setState(DELETE);
        } else if (ae.getSource() == buttons[FLIP]) {
            this.parentPanel.setState(FLIP);
        } else if (ae.getSource() == buttons[HORIZONTAL_ALIGN]) {
            this.parentPanel.fireHorizontalAlign();
        } else if (ae.getSource() == buttons[VERTICAL_ALIGN]) {
           this.parentPanel.fireVerticalAlign();
        } else if (ae.getSource() == buttons[CLEAR]) {
            parentPanel.setState(CLEAR);
            parentPanel.fireDeleteAll();
        } else if (ae.getSource() == buttons[FLIP_X]) {
            parentPanel.setState(FLIP_X);
        } else if (ae.getSource() == buttons[FLIP_Y]) {
            parentPanel.setState(FLIP_X);
        } else if (ae.getSource() == buttons[ZOOM_IN]) {
            parentPanel.setState(ZOOM_IN);
            parentPanel.zoomIn();
        } else if (ae.getSource() == buttons[ZOOM_OUT]) {
            parentPanel.setState(ZOOM_OUT);
            parentPanel.zoomOut();
        }  else if (ae.getSource() == buttons[UNDO]) {
            parentPanel.setState(UNDO);
            parentPanel.revert();
            parentPanel.setState(SELECT);
            this.buttons[UNDO].setSelected(false);
            this.buttons[SELECT].setSelected(true);
        } else if (ae.getSource() == buttons[TEMPLATE]) {
            parentPanel.setState(TEMPLATE);
            parentPanel.setTemplateDialogVisible();
        } else if (ae.getSource() == buttons[SUBMIT]) {
            parentPanel.setState(SUBMIT);
            parentPanel.setSubmitDialogVisible(true);
        }
     
    }
}
