package tops.dw.editor;

import java.awt.*;
import java.awt.event.*;

public class IntegerInDialog extends Dialog implements ActionListener {

    private static final int WIDTH = 350;
    private static final int HEIGHT = 150;

    private TextField tf = null;

    private int retVal = 0;

    private Button cancelButton = null;

    public IntegerInDialog(Frame f, String title, String message, int defaultValue) {

        super(f, title, true);

        this.setFont(new Font("TimesRoman", Font.PLAIN, 12));
        this.setLayout(new BorderLayout());

        this.add("North", new Label(message));

        this.retVal = defaultValue;

        this.tf = new TextField(10);
        this.tf.addActionListener(this);
        this.tf.setEditable(true);
        this.tf.setText(String.valueOf(defaultValue));
        Panel p = new Panel();
        p.add(this.tf);
        this.add("Center", p);

        this.cancelButton = new Button("Cancel");
        this.cancelButton.addActionListener(this);
        this.add("South", this.cancelButton);

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
                    ians = Integer.parseInt(ans);
                } catch (NumberFormatException nfe) {
                    failed = true;
                }
                if (failed) {
                    try {
                        ians = Float.valueOf(ans).intValue();
                    } catch (NumberFormatException nfe) {
                        // still failed
                    }
                }
            }
            this.retVal = ians;
            this.dispose();
        } else if (e.getSource() == this.cancelButton) {
            this.dispose();
        }

    }

    public int getInput() {
        return this.retVal;
    }

}
