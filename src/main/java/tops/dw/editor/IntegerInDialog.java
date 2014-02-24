package tops.dw.editor;

import java.awt.*;
import java.awt.event.*;

public class IntegerInDialog extends Dialog implements ActionListener {

    private static int WIDTH = 350, HEIGHT = 150;

    private TextField tf = null;

    private int RetVal = 0;

    private Button CancelButton = null;

    public IntegerInDialog(Frame f, String Title, String Message, int Default) {

        super(f, Title, true);

        this.setFont(new Font("TimesRoman", Font.PLAIN, 12));
        this.setLayout(new BorderLayout());

        this.add("North", new Label(Message));

        this.RetVal = Default;

        this.tf = new TextField(10);
        this.tf.addActionListener(this);
        this.tf.setEditable(true);
        this.tf.setText((new Integer(Default)).toString());
        Panel p = new Panel();
        p.add(this.tf);
        this.add("Center", p);

        this.CancelButton = new Button("Cancel");
        this.CancelButton.addActionListener(this);
        this.add("South", this.CancelButton);

        this.setSize(IntegerInDialog.WIDTH, IntegerInDialog.HEIGHT);

        this.pack();

    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(IntegerInDialog.WIDTH, IntegerInDialog.HEIGHT);
    }

    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == this.tf) {
            String ans = this.tf.getText();
            int ians = 0;
            boolean failed = false;
            if (ans != null) {
                try {
                    ians = Integer.valueOf(ans).intValue();
                } catch (NumberFormatException nfe) {
                    failed = true;
                }
                if (failed) {
                    try {
                        ians = Float.valueOf(ans).intValue();
                    } catch (NumberFormatException nfe) {
                    }
                }
            }
            this.RetVal = ians;
            this.dispose();
        } else if (e.getSource() == this.CancelButton) {
            this.dispose();
        }

    }

    public int getInput() {
        return this.RetVal;
    }

}
