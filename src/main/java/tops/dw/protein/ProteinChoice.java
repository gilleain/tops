package tops.dw.protein;

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

public class ProteinChoice extends Dialog implements ActionListener {

    private static final int WIDTH = 250;
    private static final int HEIGHT = 300;

    private List<Protein> proteins = null;

    private Protein retProt = null;

    private Button cancelButton = null;

    private Button continueButton = null;

    private CheckboxGroup cbg = null;

    public ProteinChoice(Frame f, String message, List<Protein> prots) {
        super(f, "ProteinChoice", true);

        this.proteins = prots;

        this.setFont(new Font("TimesRoman", Font.BOLD, 12));
        this.setLayout(new BorderLayout());

        this.add("North", new Label(message));

        ScrollPane sp = new ScrollPane();

        Panel pan = new Panel();
        pan.setLayout(new GridLayout(0, 1));

        if (this.proteins != null) {
            String nm;
            this.cbg = new CheckboxGroup();
            boolean st = true;
            for (Protein p : proteins) {
                nm = p.getName();
                Checkbox cb = new Checkbox(nm, this.cbg, st);
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

        this.setSize(ProteinChoice.WIDTH, ProteinChoice.HEIGHT);

        this.pack();

    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(ProteinChoice.WIDTH, ProteinChoice.HEIGHT);
    }

    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == this.cancelButton) {
            this.retProt = null;
            this.dispose();
        } else if (e.getSource() == this.continueButton) {
            this.retProt = this.currentProtein();
            this.dispose();
        }

    }

    private Protein currentProtein() {
        if (this.cbg != null) {
            Checkbox cb = this.cbg.getSelectedCheckbox();
            if (cb != null) {
                String nm = cb.getLabel();
                for (Protein p : this.proteins) {
                    if (nm.equals(p.getName()))
                        return p;
                }
            }
        }

        return null;

    }

    public Protein getChoice() {
        return this.retProt;
    }

}
