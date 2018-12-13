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

    private JTextField toField;
    private JTextField fromField;

    private JButton confirm;
    private JButton cancel;
    private TopsEditor controller;

    private JCheckBox infiniteCb;
    private JCheckBox zeroRange;

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
        
        textPanel.add(new JLabel("               Please enter the range that is to be added "));
        textPanel.add(new JLabel("                           to the target connection."));
        textPanel.add(new JPanel());
        textPanel.add(new JLabel("             * You must enter two integers(larger than 0)"));
        textPanel.add(new JLabel("         * The 'To' value must be larger than the 'From' value"));
        
        mainPanel.add(textPanel);
        mainPanel.add(new JPanel());
        
        JPanel textFieldsPanel = new JPanel();
        
        this.toField = new JTextField();
        this.fromField = new JTextField();
        
        infiniteCb = new JCheckBox("Infinite Range");
        infiniteCb.setSelected(false);
        infiniteCb.addActionListener(this);
        
        zeroRange = new JCheckBox("Zero Range");
        zeroRange.setSelected(false);
        zeroRange.addActionListener(this);
        
        toField.setPreferredSize(new Dimension(40, 25));
        fromField.setPreferredSize(new Dimension(40, 25));
        
        textFieldsPanel.add(new JLabel("From: "));
        textFieldsPanel.add(fromField);
        
        textFieldsPanel.add(new JLabel("To: "));
        textFieldsPanel.add(toField);
        
        JPanel checkboxPanel = new JPanel();
        checkboxPanel.add(this.infiniteCb);
        checkboxPanel.add(this.zeroRange);
        
        Border cbEtched = BorderFactory.createEtchedBorder();
        Border cbEmpty = BorderFactory.createEmptyBorder(0, 20, 0, 20);
        Border cbCombo = BorderFactory.createCompoundBorder(cbEmpty, cbEtched);
        Border cbTitled = BorderFactory.createTitledBorder(cbCombo, "Infinite & Zero Range");
        
        checkboxPanel.setBorder(cbTitled);
        
        Border etched = BorderFactory.createEtchedBorder();
        Border empty = BorderFactory.createEmptyBorder(0, 20, 0, 20);
        Border combo = BorderFactory.createCompoundBorder(empty, etched);
        Border titled = BorderFactory.createTitledBorder(combo, "Numerical Range Selection");
        
        textFieldsPanel.setBorder(titled);
        
        mainPanel.add(new JPanel());
        mainPanel.add(textFieldsPanel);
        mainPanel.add(new JPanel());
        mainPanel.add(checkboxPanel);
        
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
        this.toField.setText(to + "");
        this.fromField.setText(from + "");
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
            String toVal = toField.getText();

            boolean inValid = false;

            String message = "";
            int toInt = -1;
            int fromInt = -1;
            
            final String RANGE_HEAD = "Range Insertion";

            // check to see what they have chosen to do.
            if (infiniteCb.isSelected()) {
                message = "Enter an infinite range onto the selected connection?";
                int i = TopsCanvas.yesNoDialog(message, RANGE_HEAD);
                if (i == JOptionPane.YES_OPTION) {
//                  connection.setLabel("*");     //TODO
                    setVisible(false);
                }
            } else if (zeroRange.isSelected()) {
                message = "Enter a zero range onto the selected connection?";
                int i = TopsCanvas.yesNoDialog(message, RANGE_HEAD);
                if (i == JOptionPane.YES_OPTION) {
//                  connection.setLabel("0");     //TODO
                    setVisible(false);
                }
            } else {

                try {
                    toInt = Integer.parseInt(toVal);
                } catch (Exception e) {
                    message = "Enter a positive number in 'To' field.\n";
                    inValid = true;
                }

                String fromVal = fromField.getText();
                try {
                    fromInt = Integer.parseInt(fromVal);
                } catch (Exception e) {
                    message += "Enter a positive number in 'From' field.\n";
                    inValid = true;
                }

                // now check that the 'from' is < than 'to'
                if (fromInt > toInt && (toInt != -1 && fromInt != -1)) {
                    message += "'To' value must be larger than 'From' field";
                    inValid = true;
                }

                if ( !(fromInt >= 0 && toInt >= 0)) {
                    message += "'To' value and 'From' value must both be positive integers.";
                    inValid = true;
                }

                if (inValid)
                    System.err.println(message);
                else {
                    int i = TopsCanvas.yesNoDialog("Are you sure you wish to insert this range", "Range Confirmation");

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
            int i = TopsCanvas.yesNoDialog("Enter the specified range onto the selected connection?", "Range Insertion");

            if (i == JOptionPane.YES_OPTION)
                setVisible(false);
        }


    if (ae.getSource() == infiniteCb) {
        if (!infiniteCb.isSelected()) { // go from true to false
            zeroRange.setSelected(false);
            toField.setEditable(true);
            fromField.setEditable(true);
        } else {// going from false to true
            //infinite_cb.setSelected(true);

            // deselect the others
            zeroRange.setSelected(false);

            toField.setText("");
            fromField.setText("");
            toField.setEditable(false);
            fromField.setEditable(false);
        }
    } else if (ae.getSource() == zeroRange) {
        if (!zeroRange.isSelected()) // go from true to false
        {
            infiniteCb.setSelected(false);

            toField.setEditable(true);
            fromField.setEditable(true);
        } else // going from false to true
        {
            infiniteCb.setSelected(false);

            toField.setText("");
            fromField.setText("");
            toField.setEditable(false);
            fromField.setEditable(false);
        }
    }
}
}
