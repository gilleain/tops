package tops.dw.app;

import java.awt.*;
import java.util.*;

/**
 * pchooser class
 * 
 * @author Daniel Hatton, updated by David Westhead, converted to JDK1.1
 * @version 3.00 10 Sept. 1997
 */

public class PChooser extends Panel {

    TextArea pdesc;

    public TextField pdbcode;

    public TextField pchain;

    Button go;

    Label l1;

    Label l2;

    Label l3;

    Label l4;

    Label l5;

    Label l6;

    TextArea cdesc;

    TextField ccode;

    TextField cchain;

    public PChooser(boolean enabled) {

        this.setBackground(Color.white);
        this.setFont(new Font("TimesRoman", Font.BOLD, Topol.LargeFontSize));

        this.setLayout(new GridLayout(2, 1));

        this.l2 = new Label("PDB code:");
        this.l2.setAlignment(Label.RIGHT);
        this.l4 = new Label("Representative PDB code:");
        this.l4.setAlignment(Label.RIGHT);
        this.l5 = new Label("Chain:");
        this.l5.setAlignment(Label.RIGHT);
        this.l6 = new Label("Chain:");
        this.l6.setAlignment(Label.RIGHT);

        this.pdbcode = new TextField();

        this.pdesc = new TextArea(6, 40);
        this.pdesc.setFont(new Font("TimesRoman", Font.PLAIN, Topol.SmallFontSize));
        this.pdesc.setEditable(false);
        this.pdesc.setText("Description of requested tops.dw.protein");
        this.pdesc.setBackground(Color.lightGray);

        this.cdesc = new TextArea(6, 40);
        this.cdesc.setEditable(false);
        this.cdesc.setFont(new Font("TimesRoman", Font.PLAIN, Topol.SmallFontSize));
        this.cdesc.setText("Description of representative tops.dw.protein");
        this.cdesc.setBackground(Color.lightGray);

        this.ccode = new TextField();
        this.ccode.setEditable(false);
        this.ccode.setBackground(Color.lightGray);

        this.pchain = new TextField();
        this.pchain.setEditable(true);
        this.pchain.setBackground(Color.lightGray);

        this.cchain = new TextField();
        this.cchain.setEditable(false);
        this.cchain.setBackground(Color.lightGray);

        this.go = new Button("Get information");
        this.go.setBackground(Color.lightGray);
        this.go.setForeground(Color.blue);

        Panel p1 = new Panel();
        p1.setLayout(new GridLayout(1, 5));

        p1.add(this.l2);
        p1.add(this.pdbcode);
        p1.add(this.l5);
        p1.add(this.pchain);
        p1.add(this.go);

        Panel p2 = new Panel();
        p2.setLayout(new BorderLayout());

        p2.add("North", p1);
        p2.add("Center", this.pdesc);

        this.add(p2);

        Panel p3 = new Panel();
        p3.setLayout(new GridLayout(1, 3));
        Panel p5 = new Panel();
        p5.setLayout(new GridLayout(1, 2));

        p3.add(this.l4);
        p5.add(this.ccode);
        p5.add(this.l6);
        p3.add(p5);
        p3.add(this.cchain);

        Panel p4 = new Panel();
        p4.setLayout(new BorderLayout());

        p4.add("North", p3);
        p4.add("Center", this.cdesc);

        this.add(p4);

        this.setEnabled(enabled);

    }

    @Override
    public void setEnabled(boolean enabled) {
        if (this.go == null)
            return;
        this.go.setEnabled(enabled);
    }

    public void setInfo(String rep_code, String rep_chain, String pcode,
            Vector<String> p_descrip, Vector<String> rep_descrip) {

        this.ccode.setText(rep_code);
        this.cchain.setText(rep_chain);

        this.pdesc.setText("INFORMATION FOR PROTEIN     " + pcode.toUpperCase()
                + "\n\n");
        Enumeration<String> lines = p_descrip.elements();
        String line;
        while (lines.hasMoreElements()) {
            line = (String) lines.nextElement();
            this.pdesc.append(line.trim() + "\n");
        }

        this.cdesc.setText("INFORMATION FOR PROTEIN     " + rep_code.toUpperCase()
                + "\n\n");
        lines = rep_descrip.elements();
        while (lines.hasMoreElements()) {
            line = (String) lines.nextElement();
            this.cdesc.append(line.trim() + "\n");
        }
    }

}
