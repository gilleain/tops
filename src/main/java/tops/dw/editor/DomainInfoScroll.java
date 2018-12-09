package tops.dw.editor;

import java.awt.*;
import tops.dw.protein.*;

/**
 * this class displays domain information for a Protein in scrollable container
 * 
 * @author David Westhead
 * @version 1.00 14 July 1997
 */
public class DomainInfoScroll extends ScrollPane {

    /* START class variables */

    static final int MIN_HEIGHT = 150;

    static final int MIN_WIDTH = 500;

    static final int PREF_HEIGHT = 150;

    static final int PREF_WIDTH = 500;

    /* END class variables */

    /* START instance variables */

    DomainInfoPanel DomInfPanel;

    /* END instance variables */

    /* START constructors */

    public DomainInfoScroll(TopsEditor te) {

        super(ScrollPane.SCROLLBARS_AS_NEEDED);

        Adjustable Vadjust = this.getVAdjustable();
        Adjustable Hadjust = this.getHAdjustable();
        Vadjust.setUnitIncrement(10);
        Hadjust.setUnitIncrement(10);

        this.DomInfPanel = new DomainInfoPanel(te);
        this.add(this.DomInfPanel);

    }

    /* END constructors */

    /* START methods defining preferred and minimum sizes */

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(DomainInfoScroll.MIN_WIDTH, DomainInfoScroll.MIN_HEIGHT);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(DomainInfoScroll.PREF_WIDTH, DomainInfoScroll.PREF_HEIGHT);
    }

    /* END methods defining preferred and minimum sizes */

    /* START get/set and Clear methods */

    public void Clear() {
        if (this.DomInfPanel != null)
            this.DomInfPanel.clear();
    }

    public void addProtein(Protein p) {
        if (this.DomInfPanel != null)
            this.DomInfPanel.addProtein(p);
    }

    /* END get and set methods */

}
