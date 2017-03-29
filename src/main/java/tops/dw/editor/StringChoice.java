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

    private static int WIDTH = 250, HEIGHT = 300;

    private List<String> Strings = null;

    private String RetString = null;

    private int RetInt = -1;

    private Button CancelButton = null;

    private Button ContinueButton = null;

    private CheckboxGroup cbg = null;

    public StringChoice(Frame f, String Message, List<String> strs) {
        super(f, "StringChoice", true);

        this.Strings = strs;

        this.setFont(new Font("TimesRoman", Font.BOLD, 12));
        this.setLayout(new BorderLayout());

        this.add("North", new Label(Message));

        ScrollPane sp = new ScrollPane();

        Panel pan = new Panel();
        pan.setLayout(new GridLayout(0, 1));

        if (this.Strings != null) {
            this.cbg = new CheckboxGroup();
            boolean st = true;
            for (String str : Strings) {
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
                i = 0;
                for (String str : Strings) {
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
