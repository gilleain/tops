package tops.dw.editor;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import tops.dw.protein.Cartoon;
import tops.dw.protein.Protein;
import tops.port.model.DomainDefinition;
import tops.port.model.Segment;

/**
 * this class displays domain information for a Protein
 * 
 * @author David Westhead
 * @version 1.00 22 Apr. 1997
 */
public class DomainInfoPanel extends Panel {

    private Vector<Protein> proteins;

    private TopsEditor TopsEd;

    public DomainInfoPanel(TopsEditor te) {
        this.TopsEd = te;
        this.proteins = new Vector<Protein>();
        this.setLayout(new GridLayout(0, 4));
        this.setFont(new Font("TimesRoman", Font.BOLD, 12));
    }

    public void Clear() {
        this.proteins = new Vector<Protein>();
        this.removeAll();
    }

    public void addProtein(Protein p) {

        if (p == null)
            return;

        this.proteins.addElement(p);

        List<DomainDefinition> DomainDefs = p.getDomainDefs();
        List<Cartoon> Diagrams = p.getLinkedLists();

        Button b;
        Choice ch;
        Panel panel;

        int index = 0;
        for (DomainDefinition dd : DomainDefs) {

            Cartoon cartoon = Diagrams.get(index);

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

            ch.addItemListener(new DomainInfoPanelCommand(this.TopsEd, cartoon,
                    DomainInfoPanelCommand.CHANGE_EDIT_MODE_COMMAND));

            panel = new Panel();
            panel.add(ch);
            this.add(panel);
            index++;
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
            ta.append("Fragment     Start        Finish\n\n");
            
            int index = 0; // XXX generating fragment index here
            for (Segment segment : this.domain.getSegments()) {
                
                String StartLab = "N" + index;
                String EndLab = "C" + (index + 1);
                ta.append(String.format("%s", StartLab, segment, EndLab));
                ta.append("\n");
                index++;
            }
            this.InfoFrame.add("Center", ta);

            Button dismiss = new Button("Dismiss");
            this.InfoFrame.add("South", dismiss);
            dismiss.addActionListener(this);

            this.InfoFrame.pack();
            this.InfoFrame.setVisible(true);
        }

    }
}
