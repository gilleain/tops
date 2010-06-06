package tops.dw.app;

import java.awt.*;
import java.awt.event.ActionListener;

/**
 * pdbpanel class
 * 
 * @author Daniel Hatton, updated by David Westhead
 * @version 2.00 10 Sept. 1997
 */

public class PDBPanel extends Panel {

    private Button pdbclick;

    private Button cathclick;

    private Button mmdbclick;

    private Label pdblabel;

    public PDBPanel() {

        this.setBackground(Color.white);
        this.setFont(new Font("TimesRoman", Font.PLAIN, Topol.LargeFontSize));

        this.setLayout(new BorderLayout());

        this.pdbclick = new Button("BPDB");
        this.pdbclick.setBackground(Color.lightGray);
        this.pdbclick.setForeground(Color.blue);
        Label lpdb = new Label(
                "The Brookhaven tops.dw.protein databank mirror at the E.B.I.");
        lpdb.setAlignment(Label.LEFT);

        this.cathclick = new Button("UCL");
        this.cathclick.setBackground(Color.lightGray);
        this.cathclick.setForeground(Color.blue);
        Label lcath = new Label(
                "The PDB summary files at University College London");
        lcath.setAlignment(Label.LEFT);

        this.mmdbclick = new Button("MMDB");
        this.mmdbclick.setBackground(Color.lightGray);
        this.mmdbclick.setForeground(Color.blue);
        Label lmmdb = new Label("The MMDB structure summary at the N.C.B.I.");
        lmmdb.setAlignment(Label.LEFT);

        this.pdblabel = new Label(
                "Other sites' information on your chosen tops.dw.protein:");
        this.add("North", this.pdblabel);

        Panel p1 = new Panel();
        p1.setLayout(new GridLayout(0, 1));
        p1.add(this.pdbclick);
        p1.add(this.cathclick);
        // p1.add(mmdbclick);
        this.add("West", p1);

        Panel p2 = new Panel();
        p2.setLayout(new GridLayout(0, 1));
        p2.add(lpdb);
        p2.add(lcath);
        // p2.add(lmmdb);
        this.add("Center", p2);

    }
    
    public void addActionListener(ActionListener listener) {
    	this.pdbclick.addActionListener(listener);
        this.cathclick.addActionListener(listener);
        this.mmdbclick.addActionListener(listener);
    }
}
