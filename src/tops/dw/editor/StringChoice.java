package tops.dw.editor;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class StringChoice extends Dialog implements ActionListener {

    private static int WIDTH = 250, HEIGHT = 300;

    private Vector<String> Strings = null;

    private String RetString = null;

    private int RetInt = -1;

    private Button CancelButton = null;

    private Button ContinueButton = null;

    private CheckboxGroup cbg = null;

    public StringChoice(Frame f, String Message, Vector<String> strngs) {
        super(f, "StringChoice", true);

        this.Strings = strngs;

        this.setFont(new Font("TimesRoman", Font.BOLD, 12));
        this.setLayout(new BorderLayout());

        this.add("North", new Label(Message));

        ScrollPane sp = new ScrollPane();

        Panel pan = new Panel();
        pan.setLayout(new GridLayout(0, 1));

        if (this.Strings != null) {
            Enumeration<String> en = this.Strings.elements();
            String str;
            this.cbg = new CheckboxGroup();
            boolean st = true;
            while (en.hasMoreElements()) {
                str = en.nextElement();
                Checkbox cb = new Checkbox(str, this.cbg, st);
                pan.add(cb);
                st = false;
            }

        }

        sp.add(pan);
        this.add("Center", sp);

        Panel bpn = new Panel();
        this.CancelButton = new Button("Cancel");
        this.CancelButton.addActionListener(this);
        bpn.add(this.CancelButton);
        this.ContinueButton = new Button("Continue");
        this.ContinueButton.addActionListener(this);
        bpn.add(this.ContinueButton);

        this.add("South", bpn);

        this.setSize(StringChoice.WIDTH, StringChoice.HEIGHT);

        this.pack();

    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(StringChoice.WIDTH, StringChoice.HEIGHT);
    }

    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == this.CancelButton) {
            this.RetString = null;
            this.dispose();
        } else if (e.getSource() == this.ContinueButton) {
            this.SetRetString();
            this.dispose();
        }

    }

    private void SetRetString() {

        int i = -1;

        if (this.cbg != null) {
            Checkbox cb = this.cbg.getSelectedCheckbox();
            if (cb != null) {
                String nm = cb.getLabel();
                Enumeration<String> en = this.Strings.elements();
                String str;
                i = 0;
                while (en.hasMoreElements()) {
                    str = (String) en.nextElement();

                    if (nm.equals(str)) {
                        this.RetString = str;
                        this.RetInt = i;
                        return;
                    }
                    i++;
                }
            }
        }

        this.RetString = null;
        this.RetInt = -1;

    }

    public String getChoice() {
        return this.RetString;
    }

    public int getChoiceNumber() {
        return this.RetInt;
    }

}
