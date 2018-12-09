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
import java.util.ArrayList;
import java.util.List;

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

    private List<Protein> proteins;

    private TopsEditor topsEditor;

    public DomainInfoPanel(TopsEditor te) {
        this.topsEditor = te;
        this.proteins = new ArrayList<>();
        this.setLayout(new GridLayout(0, 4));
        this.setFont(new Font("TimesRoman", Font.BOLD, 12));
    }

    public void clear() {
        this.proteins.clear();
        this.removeAll();
    }

    public void addProtein(Protein p) {

        if (p == null)
            return;

        this.proteins.add(p);

        List<DomainDefinition> domainDefs = p.getDomainDefs();
        List<Cartoon> diagrams = p.getLinkedLists();

        Button b;
        Choice ch;
        Panel panel;

        int index = 0;
        for (DomainDefinition dd : domainDefs) {

            Cartoon cartoon = diagrams.get(index);

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

            ch.addItemListener(new DomainInfoPanelCommand(this.topsEditor, cartoon,
                    DomainInfoPanelCommand.CHANGE_EDIT_MODE_COMMAND));

            panel = new Panel();
            panel.add(ch);
            this.add(panel);
            index++;
        }
    }

}

/**
 * a class to act as listener for DomainInfoPanel button presses sets up a frame
 * containing the info
 */
class DomainInfoCommand implements ActionListener {

    DomainDefinition domain = null;

    Frame infoFrame = null;

    public DomainInfoCommand(DomainDefinition d) {
        this.domain = d;
    }

    public void actionPerformed(ActionEvent e) {

        if (e.getActionCommand() == "Dismiss") {
            if (this.infoFrame != null) {
                this.infoFrame.dispose();
                this.infoFrame = null;
            }
        } else {
            this.infoFrame = new Frame("Protein domain information for " + this.domain);
            this.infoFrame.setLayout(new BorderLayout());
            this.infoFrame.setFont(new Font("Courier", Font.PLAIN, 12));

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
                
                String startLab = "N" + index;
                String endLab = "C" + (index + 1);
                ta.append(String.format("%s %s %s", startLab, segment, endLab));
                ta.append("\n");
                index++;
            }
            this.infoFrame.add("Center", ta);

            Button dismiss = new Button("Dismiss");
            this.infoFrame.add("South", dismiss);
            dismiss.addActionListener(this);

            this.infoFrame.pack();
            this.infoFrame.setVisible(true);
        }

    }
}
