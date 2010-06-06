package tops.drawing.app;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import tops.drawing.symbols.CartoonConnector;


public class InsertEditRangeDialog extends JDialog implements ActionListener {

    private CartoonConnector connection;
    private Container container;

    private JPanel mainPanel;     // the panel that tells the user what this dialog box is used for
    private JPanel buttonPanel;  // the panel that contians the buttons

    private JTextField to_field;
    private JTextField from_field;

    private JButton confirm;
    private JButton cancel;
    private TopsEditor controller;

    private JCheckBox infinite_cb;
    private JCheckBox zero_range;
    
    public static void main(String[] args) {
        InsertEditRangeDialog r = new InsertEditRangeDialog(null);
        r.setVisible(true);
    }

    public InsertEditRangeDialog(TopsEditor controller) {
        this.controller = controller;

        this.setTitle("Range Instructions");

        this.setSize(350, 350);
        container = this.getContentPane();
        mainPanel = new JPanel();
        buttonPanel = new JPanel();
        
        // panel layouts
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        buttonPanel.setLayout(new FlowLayout());
        confirm = new JButton("Confirm");
        cancel = new JButton("Cancel");
        //// add the panels to the dialog
        container.add(mainPanel);
        container.add(buttonPanel);
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        
        //mainPanel.add(new JPanel());
        textPanel.add(new JLabel("               Please enter the range that is to be added "));
        textPanel.add(new JLabel("                           to the target connection."));
        textPanel.add(new JPanel());
        textPanel.add(new JLabel("             * You must enter two integers(larger than 0)"));
        textPanel.add(new JLabel("         * The 'To' value must be larger than the 'From' value"));
        
        mainPanel.add(textPanel);
        mainPanel.add(new JPanel());
        
        JPanel textFieldsPanel = new JPanel();
        
        this.to_field = new JTextField();
        this.from_field = new JTextField();
        
        infinite_cb = new JCheckBox("Infinite Range");
        infinite_cb.setSelected(false);
        infinite_cb.addActionListener(this);
        
        zero_range = new JCheckBox("Zero Range");
        zero_range.setSelected(false);
        zero_range.addActionListener(this);
        
        to_field.setPreferredSize(new Dimension(40, 25));
        from_field.setPreferredSize(new Dimension(40, 25));
        
        textFieldsPanel.add(new JLabel("From: "));
        textFieldsPanel.add(from_field);
        
        textFieldsPanel.add(new JLabel("To: "));
        textFieldsPanel.add(to_field);
        
        JPanel checkbox_panel = new JPanel();
        checkbox_panel.add(this.infinite_cb);
        checkbox_panel.add(this.zero_range);
        
        Border cb_etched = BorderFactory.createEtchedBorder();
        Border cb_empty = BorderFactory.createEmptyBorder(0, 20, 0, 20);
        Border cb_combo = BorderFactory.createCompoundBorder(cb_empty, cb_etched);
        Border cb_titled = BorderFactory.createTitledBorder(cb_combo, "Infinite & Zero Range");
        
        checkbox_panel.setBorder(cb_titled);
        
        Border etched = BorderFactory.createEtchedBorder();
        Border empty = BorderFactory.createEmptyBorder(0, 20, 0, 20);
        Border combo = BorderFactory.createCompoundBorder(empty, etched);
        Border titled = BorderFactory.createTitledBorder(combo, "Numerical Range Selection");
        
        textFieldsPanel.setBorder(titled);
        
        mainPanel.add(new JPanel());
        mainPanel.add(textFieldsPanel);
        mainPanel.add(new JPanel());
        mainPanel.add(checkbox_panel);
        
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        
        JPanel temp1 = new JPanel();
        temp1.setLayout(new FlowLayout(3));
        
        JPanel temp2 = new JPanel();
        temp2.add(confirm);
        temp2.add(cancel);
        
        buttonPanel.add(temp1);
        buttonPanel.add(temp2);
        confirm.addActionListener(this);
        cancel.addActionListener(this);
    }
    
    public void reset(int to, int from) {
        this.to_field.setText(to + "");
        this.from_field.setText(from + "");
        this.setVisible(true);
    }

    /**
     * Getter for property connection.
     * @return Value of property connection.
     */
    public CartoonConnector getConnection() {
        return connection;
    }

    /**
     * Setter for property connection.
     * @param connection New value of property connection.
     */
    public void setConnection(CartoonConnector connection) {
        this.connection = connection;
    }

    public void actionPerformed(ActionEvent ae) {

        if (ae.getSource() == confirm) {
            // get the to from values
            String to_val = to_field.getText();

            boolean inValid = false;

            String message = "";
            int to_int = -1;
            int from_int = -1;

            // check to see what they have chosen to do.
            if (infinite_cb.isSelected()) {
                message = "Enter an infinite range onto the selected connection?";
                int i = TopsCanvas.yes_no_dialog(message, "Range Insertion");
                if (i == JOptionPane.YES_OPTION) {
//                  connection.setLabel("*");     //TODO
                    setVisible(false);
                }
            } else if (zero_range.isSelected()) {
                message = "Enter a zero range onto the selected connection?";
                int i = TopsCanvas.yes_no_dialog(message, "Range Insertion");
                if (i == JOptionPane.YES_OPTION) {
//                  connection.setLabel("0");     //TODO
                    setVisible(false);
                }
            } else {

                try {
                    to_int = Integer.parseInt(to_val);
                } catch (Exception e) {
                    message = "Enter a positive number in 'To' field.\n";
                    inValid = true;
                }

                String from_val = from_field.getText();
                try {
                    from_int = Integer.parseInt(from_val);
                } catch (Exception e) {
                    message += "Enter a positive number in 'From' field.\n";
                    inValid = true;
                }

                // now check that the 'from' is < than 'to'
                if (from_int > to_int && (to_int != -1 && from_int != -1)) {
                    message += "'To' value must be larger than 'From' field";
                    inValid = true;
                }

                if ( !(from_int >= 0 && to_int >= 0)) {
                    message += "'To' value and 'From' value must both be positive integers.";
                    inValid = true;
                }

                if (inValid)
                    System.err.println(message);
                else {
                    int i = TopsCanvas.yes_no_dialog("Are you sure you wish to insert this range", "Range Confirmation");

                    if (i == JOptionPane.YES_OPTION) {
//                      connection.setLabel(from_int + ":" + to_int); //TODO
                        setVisible(false);
                    }
                }
            }
            // update the canvas

            // now need to re-enable the save function
            controller.setAsUnSaved();
            controller.repaint();
        } else if (ae.getSource() == cancel) {
            int i = TopsCanvas.yes_no_dialog("Enter the specified range onto the selected connection?", "Range Insertion");

            if (i == JOptionPane.YES_OPTION)
                setVisible(false);
        }


    if (ae.getSource() == infinite_cb) {
        if (!infinite_cb.isSelected()) { // go from true to false
            zero_range.setSelected(false);
            to_field.setEditable(true);
            from_field.setEditable(true);
        } else {// going from false to true
            //infinite_cb.setSelected(true);

            // deselect the others
            zero_range.setSelected(false);

            to_field.setText("");
            from_field.setText("");
            to_field.setEditable(false);
            from_field.setEditable(false);
        }
    } else if (ae.getSource() == zero_range) {
        if (!zero_range.isSelected()) // go from true to false
        {
            infinite_cb.setSelected(false);

            to_field.setEditable(true);
            from_field.setEditable(true);
        } else // going from false to true
        {
            infinite_cb.setSelected(false);

            to_field.setText("");
            from_field.setText("");
            to_field.setEditable(false);
            from_field.setEditable(false);
        }
    }
}
}
