package tops.dw.editor;

import java.awt.*;
import java.util.*;
import java.beans.*;

public class ColourChoice {

    /* START instance variables */

    private ColourChoicePanel ccp;

    private Frame f;

    /* END instance variables */

    /* START constructors */

    public ColourChoice() {
        this.f = new Frame("Colour choice");
        this.f.setFont(new Font("TimesRoman", Font.PLAIN, 12));

        this.f.setLayout(new BorderLayout());

        this.ccp = new ColourChoicePanel();

        this.f.add("Center", this.ccp);

        this.f.pack();

        this.f.setVisible(false);

    }

    /* END constructors */

    public void addColourChangeListeners(Vector ColourChListeners) {

        if (ColourChListeners == null)
            return;

        Enumeration enumeration = ColourChListeners.elements();

        if (this.ccp != null) {
            while (enumeration.hasMoreElements()) {
                this.ccp
                        .addPropertyChangeListener((PropertyChangeListener) enumeration
                                .nextElement());
            }
        }

    }

    public void setVisible(boolean vis) {
        if (this.f != null) {
            this.f.setVisible(vis);
            if (vis)
                this.f.toFront();
        }
    }

    public void dispose() {
        this.f.dispose();
    }

}
