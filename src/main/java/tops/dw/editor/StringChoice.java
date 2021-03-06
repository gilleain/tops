package tops.dw.editor;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class StringChoice extends Dialog implements ActionListener {

    private static final int WIDTH = 250;
    private static final int HEIGHT = 300;

    private List<String> strings = null;

    private String retString = null;

    private int retInt = -1;

    private Button cancelButton = null;

    private Button continueButton = null;

    private CheckboxGroup cbg = null;

    public StringChoice(Frame f, String message, List<String> strs) {
        super(f, "StringChoice", true);

        this.strings = strs;

        this.setFont(new Font("TimesRoman", Font.BOLD, 12));
        this.setLayout(new BorderLayout());

        this.add("North", new Label(message));

        ScrollPane sp = new ScrollPane();

        Panel pan = new Panel();
        pan.setLayout(new GridLayout(0, 1));

        if (this.strings != null) {
            this.cbg = new CheckboxGroup();
            boolean st = true;
            for (String str : strings) {
                Checkbox cb = new Checkbox(str, this.cbg, st);
                pan.add(cb);
                st = false;
            }

        }

        sp.add(pan);
        this.add("Center", sp);

        Panel bpn = new Panel();
        this.cancelButton = new Button("Cancel");
        this.cancelButton.addActionListener(this);
        bpn.add(this.cancelButton);
        this.continueButton = new Button("Continue");
        this.continueButton.addActionListener(this);
        bpn.add(this.continueButton);

        this.add("South", bpn);

        this.setSize(StringChoice.WIDTH, StringChoice.HEIGHT);

        this.pack();

    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(StringChoice.WIDTH, StringChoice.HEIGHT);
    }

    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == this.cancelButton) {
            this.retString = null;
            this.dispose();
        } else if (e.getSource() == this.continueButton) {
            this.setRetString();
            this.dispose();
        }

    }

    private void setRetString() {

        int i = -1;

        if (this.cbg != null) {
            Checkbox cb = this.cbg.getSelectedCheckbox();
            if (cb != null) {
                String nm = cb.getLabel();
                i = 0;
                for (String str : strings) {
                    if (nm.equals(str)) {
                        this.retString = str;
                        this.retInt = i;
                        return;
                    }
                    i++;
                }
            }
        }

        this.retString = null;
        this.retInt = -1;

    }

    public String getChoice() {
        return this.retString;
    }

    public int getChoiceNumber() {
        return this.retInt;
    }

}
