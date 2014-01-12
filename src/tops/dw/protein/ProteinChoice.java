package tops.dw.protein;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class ProteinChoice extends Dialog implements ActionListener {

    private static int WIDTH = 250, HEIGHT = 300;

    private Vector<Protein> proteins = null;

    private Protein RetProt = null;

    private Button CancelButton = null;

    private Button ContinueButton = null;

    private CheckboxGroup cbg = null;

    public ProteinChoice(Frame f, String Message, Vector<Protein> prots) {
        super(f, "ProteinChoice", true);

        this.proteins = prots;

        this.setFont(new Font("TimesRoman", Font.BOLD, 12));
        this.setLayout(new BorderLayout());

        this.add("North", new Label(Message));

        ScrollPane sp = new ScrollPane();

        Panel pan = new Panel();
        pan.setLayout(new GridLayout(0, 1));

        if (this.proteins != null) {
            Enumeration<Protein> en = this.proteins.elements();
            Protein p;
            String nm;
            this.cbg = new CheckboxGroup();
            boolean st = true;
            while (en.hasMoreElements()) {
                p = (Protein) en.nextElement();
                nm = p.Name;
                Checkbox cb = new Checkbox(nm, this.cbg, st);
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

        this.setSize(ProteinChoice.WIDTH, ProteinChoice.HEIGHT);

        this.pack();

    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(ProteinChoice.WIDTH, ProteinChoice.HEIGHT);
    }

    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == this.CancelButton) {
            this.RetProt = null;
            this.dispose();
        } else if (e.getSource() == this.ContinueButton) {
            this.RetProt = this.CurrentProtein();
            this.dispose();
        }

    }

    private Protein CurrentProtein() {
        if (this.cbg != null) {
            Checkbox cb = this.cbg.getSelectedCheckbox();
            if (cb != null) {
                String nm = cb.getLabel();
                Enumeration<Protein> en = this.proteins.elements();
                Protein p;
                while (en.hasMoreElements()) {
                    p = (Protein) en.nextElement();
                    if (nm.equals(p.Name))
                        return p;
                }
            }
        }

        return null;

    }

    public Protein getChoice() {
        return this.RetProt;
    }

}
