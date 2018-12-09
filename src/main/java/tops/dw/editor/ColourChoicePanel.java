package tops.dw.editor;

import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * a colour choice java bean just a simple text based one for now
 * 
 * @author David Westhead
 * @version 1.00 30 Apr. 1997
 */
public class ColourChoicePanel extends Panel implements ItemListener {

    
    private static final Color[] COLORS = {  Color.white, Color.blue, Color.cyan,
            Color.darkGray, Color.gray, Color.green, Color.lightGray, Color.magenta,
            Color.orange, Color.pink, Color.red, Color.yellow, Color.black }; 

    private Color currentColour;

    private PropertyChangeSupport changes = new PropertyChangeSupport(this);
    
    public ColourChoicePanel() {

        this.setFont(new Font("TimesRoman", Font.PLAIN, 12));
        this.setLayout(new GridLayout(0, 1));

        int i;
        Checkbox cb;
        CheckboxGroup colours = new CheckboxGroup();
        boolean state;
        for (i = 0, state = true; i < ColourChoicePanel.COLORS.length; i++, state = false) {
            cb = new Checkbox(ColourChoicePanel.COLORS[i].toString().toLowerCase(), colours, state);
            cb.addItemListener(this);
            this.add(cb);
        }
        this.setCurrentColour(ColourChoicePanel.COLORS[0]);

    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(200, 400);
    }

    public synchronized void setCurrentColour(Color c) {
        Color oldc = this.currentColour;
        this.currentColour = c;
        this.changes.firePropertyChange("CurrentColour", oldc, c);
    }

    public synchronized Color getCurrentColour() {
        return this.currentColour;
    }

    public void itemStateChanged(ItemEvent e) {
        this.setCurrentColour(Color.decode(e.getItem().toString()));
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        this.changes.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        this.changes.removePropertyChangeListener(l);
    }

}
