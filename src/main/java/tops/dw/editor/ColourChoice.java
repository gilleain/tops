package tops.dw.editor;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.beans.PropertyChangeListener;
import java.util.List;

public class ColourChoice {

    private ColourChoicePanel ccp;

    private Frame frame;

    public ColourChoice() {
        this.frame = new Frame("Colour choice");
        this.frame.setFont(new Font("TimesRoman", Font.PLAIN, 12));

        this.frame.setLayout(new BorderLayout());

        this.ccp = new ColourChoicePanel();

        this.frame.add("Center", this.ccp);
        this.frame.pack();
        this.frame.setVisible(false);
    }

    public void addColourChangeListeners(List<? extends PropertyChangeListener> listeners) {

        if (listeners == null) {
            return;
        }

        if (this.ccp != null) {
            for (PropertyChangeListener listener : listeners) {
                this.ccp.addPropertyChangeListener(listener);
            }
        }
    }

    public void setVisible(boolean vis) {
        if (this.frame != null) {
            this.frame.setVisible(vis);
            if (vis) {
                this.frame.toFront();
            }
        }
    }

    public void dispose() {
        this.frame.dispose();
    }

}
