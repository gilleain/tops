package tops.dw.editor;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import tops.dw.protein.*;

/**
 * this class displays domain information for a Protein
 * 
 * @author David Westhead
 * @version 1.00 22 Apr. 1997
 */
public class DomainInfoPanel extends Panel {

    private Vector proteins;

    private TopsEditor TopsEd;

    public DomainInfoPanel(TopsEditor te) {
        this.TopsEd = te;
        this.proteins = new Vector();
        this.setLayout(new GridLayout(0, 4));
        this.setFont(new Font("TimesRoman", Font.BOLD, 12));
    }

    public void Clear() {
        this.proteins = new Vector();
        this.removeAll();
    }

    public void addProtein(Protein p) {

        if (p == null)
            return;

        this.proteins.addElement(p);

        Enumeration DomainDefs = p.GetDomainDefs().elements();
        Enumeration Diagrams = p.GetLinkedLists().elements();

        DomainDefinition dd;
        SecStrucElement ss;
        Button b;
        Choice ch;
        Panel panel;

        while (DomainDefs.hasMoreElements()) {

            dd = (DomainDefinition) DomainDefs.nextElement();

            ss = null;
            if (Diagrams.hasMoreElements())
                ss = (SecStrucElement) Diagrams.nextElement();

            b = new Button("Domain information");
            b.addActionListener(new DomainInfoCommand(dd));
            panel = new Panel();
            panel.add(b);
            this.add(panel);

            panel = new Panel();
            panel.add(new Label("Edit mode", Label.CENTER));
            this.add(panel);

            ch = new Choice();
            ch.addItem("Display information");
            ch.addItem("Colour symbols");
            ch.addItem("Move symbols");
            ch.addItem("Move fixed structures");
            ch.addItem("Redraw connections");
            ch.addItem("Delete symbols");
            ch.addItem("Align X direction");
            ch.addItem("Align Y direction");
            ch.addItem("Rotate 180 about x");
            ch.addItem("Rotate 180 about y");
            ch.addItem("Rotate 180 about z");
            ch.addItem("Reflect in xy plane");
            ch.addItem("Toggle size display");
            ch.addItem("Add label");
            ch.addItem("Delete label");
            ch.addItem("Move label");
            ch.addItem("Add arrow");
            ch.addItem("Delete arrow");

            ch.addItemListener(new DomainInfoPanelCommand(this.TopsEd, ss,
                    DomainInfoPanelCommand.CHANGE_EDIT_MODE_COMMAND));

            panel = new Panel();
            panel.add(ch);
            this.add(panel);
        }
    }

    /* END get and set methods */

}

/**
 * a class to act as listener for DomainInfoPanel button presses sets up a frame
 * containing the info
 */
class DomainInfoCommand implements ActionListener {

    /* START instance variables */

    DomainDefinition domain = null;

    Frame InfoFrame = null;

    /* END instance variables */

    /* START constructors */

    public DomainInfoCommand(DomainDefinition d) {
        this.domain = d;
    }

    /* END constructors */

    /* START the ActionListener interface */
    public void actionPerformed(ActionEvent e) {

        if (e.getActionCommand() == "Dismiss") {
            if (this.InfoFrame != null) {
                this.InfoFrame.dispose();
                this.InfoFrame = null;
            }
        } else {
            this.InfoFrame = new Frame("Protein domain information for " + this.domain);
            this.InfoFrame.setLayout(new BorderLayout());
            this.InfoFrame.setFont(new Font("Courier", Font.PLAIN, 12));

            TextArea ta = new TextArea();
            ta.setEditable(false);
            ta.setRows(10);
            ta.setColumns(50);

            ta.append("\n");
            ta.append("Domain " + this.domain
                    + " contains the following fragments\n");
            ta.append("\n");

            Enumeration sfrags = this.domain.getSequenceFragments();
            Enumeration fragis = this.domain.getFragmentIndices();

            int index;
            IntegerInterval interval;
            String StartLab, EndLab, StartRes, EndRes;
            String s;
            char chain = this.domain.getChain();
            ta.append("Fragment     Start        Finish\n\n");
            while (sfrags.hasMoreElements()) {

                index = 0;
                if ((fragis != null) && fragis.hasMoreElements()) {
                    index = ((Integer) fragis.nextElement()).intValue();
                }
                StartLab = "N" + index;
                EndLab = "C" + (index + 1);

                interval = (IntegerInterval) sfrags.nextElement();
                if ((chain != ' ') && (chain != '0')) {
                    StartRes = "" + chain + interval.getLower();
                    EndRes = "" + chain + interval.getUpper();
                } else {
                    StartRes = "" + interval.getLower();
                    EndRes = "" + interval.getUpper();
                }

                s = this.SetOutputString(index, StartLab, StartRes, EndLab, EndRes);
                ta.append(s);
                ta.append("\n");

            }
            this.InfoFrame.add("Center", ta);

            Button dismiss = new Button("Dismiss");
            this.InfoFrame.add("South", dismiss);
            dismiss.addActionListener(this);

            this.InfoFrame.pack();
            this.InfoFrame.setVisible(true);
        }

    }

    /* surely there is a better way to do formatted output in java then this!!! */
    /* MUST look into it, leave it for now */
    private String SetOutputString(int index, String slab, String sres,
            String elab, String eres) {

        int BUFFER_LEN = 80;
        StringBuffer sb = new StringBuffer();

        int i;
        for (i = 0; i < BUFFER_LEN; i++)
            sb.append(' ');

        String ind = (new Integer(index)).toString();
        for (i = 0; i < ind.length(); i++)
            sb.setCharAt(i + 3, ind.charAt(i));

        for (i = 0; i < slab.length(); i++)
            sb.setCharAt(i + 12, slab.charAt(i));

        sb.setCharAt(14, '(');
        for (i = 0; i < sres.length(); i++)
            sb.setCharAt(i + 15, sres.charAt(i));
        sb.setCharAt(20, ')');

        for (i = 0; i < elab.length(); i++)
            sb.setCharAt(i + 25, elab.charAt(i));

        sb.setCharAt(27, '(');
        for (i = 0; i < eres.length(); i++)
            sb.setCharAt(i + 28, eres.charAt(i));
        sb.setCharAt(33, ')');

        return sb.toString();

    }

}
