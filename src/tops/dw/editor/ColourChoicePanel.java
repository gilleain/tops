package tops.dw.editor;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;

/**
 * a colour choice java bean just a simple text based one for now
 * 
 * @author David Westhead
 * @version 1.00 30 Apr. 1997
 */
public class ColourChoicePanel extends Panel implements ItemListener {

    /* START class variables */

    private static final String[] ColourStrings = { "white", "blue", "cyan",
            "darkGray", "gray", "green", "lightGray", "magenta", "orange",
            "pink", "red", "yellow", "black" };

    /* END class variables */

    /* START instance variables */

    private Color CurrentColour;

    private PropertyChangeSupport changes = new PropertyChangeSupport(this);

    /* END instance variables */

    /* START constructors */
    public ColourChoicePanel() {

        this.setFont(new Font("TimesRoman", Font.PLAIN, 12));
        this.setLayout(new GridLayout(0, 1));

        int i;
        Checkbox cb;
        CheckboxGroup Colours = new CheckboxGroup();
        boolean state;
        for (i = 0, state = true; i < ColourChoicePanel.ColourStrings.length; i++, state = false) {
            cb = new Checkbox(ColourChoicePanel.ColourStrings[i], Colours, state);
            cb.addItemListener(this);
            this.add(cb);
        }
        this.setCurrentColour(this.StringToColor(ColourChoicePanel.ColourStrings[0]));

    }

    /* END constructors */

    /* START size methods */

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(200, 400);
    }

    /* END size methods */

    /* START property get/set methods */

    public synchronized void setCurrentColour(Color c) {
        Color oldc = this.CurrentColour;
        this.CurrentColour = c;
        this.changes.firePropertyChange("CurrentColour", oldc, c);
    }

    public synchronized Color getCurrentColour() {
        return this.CurrentColour;
    }

    /* END property get/set methods */

    /* START the ItemListener interface */

    public void itemStateChanged(ItemEvent e) {
        this.setCurrentColour(this.StringToColor(e.getItem().toString()));
    }

    /* END the ItemListener interface */

    /* START methods which add and remove PropertyChangeListeners */

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        this.changes.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        this.changes.removePropertyChangeListener(l);
    }

    /* END methods to add and remove PropertyChangeListeners */

    /* START utility methods */

    private Color StringToColor(String s) {

        Color c = null;

        if (s.equals("black"))
            c = Color.black;
        else if (s.equals("blue"))
            c = Color.blue;
        else if (s.equals("cyan"))
            c = Color.cyan;
        else if (s.equals("darkGray"))
            c = Color.darkGray;
        else if (s.equals("gray"))
            c = Color.gray;
        else if (s.equals("green"))
            c = Color.green;
        else if (s.equals("lightGray"))
            c = Color.lightGray;
        else if (s.equals("magenta"))
            c = Color.magenta;
        else if (s.equals("orange"))
            c = Color.orange;
        else if (s.equals("pink"))
            c = Color.pink;
        else if (s.equals("red"))
            c = Color.red;
        else if (s.equals("white"))
            c = Color.white;
        else if (s.equals("yellow"))
            c = Color.yellow;
        else
            c = Color.white;
        return c;

    }

    /* END utility methods */

}
