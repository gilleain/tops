package tops.dw.editor;

import java.awt.*;
import java.util.*;

import tops.dw.protein.*;

/**
 * This class is a java bean which displays a TopsDisplay panel and scrollbars
 * 
 * @author David Westhead
 * @version 1.00, 27 Mar 1997
 * @see TopsDiagramDisplayPanel
 */

public class TopsDisplayScroll extends ScrollPane {

    /* START class variables */

    static final int MIN_HEIGHT = 400;

    static final int MIN_WIDTH = 500;

    static final int PREF_HEIGHT = 600;

    static final int PREF_WIDTH = 1000;

    /* END class variables */

    /* START instance variables */
    private TopsDisplayPanel DisplayPanel;

    /* END instance variables */

    /* START constructors */

    /**
     * the basic constructor
     */
    public TopsDisplayScroll() {

        super(ScrollPane.SCROLLBARS_AS_NEEDED);

        Adjustable Vadjust = this.getVAdjustable();
        Adjustable Hadjust = this.getHAdjustable();
        Vadjust.setUnitIncrement(10);
        Hadjust.setUnitIncrement(10);

        this.DisplayPanel = new TopsDisplayPanel();
        this.add(this.DisplayPanel);

        Dimension ps = this.getPreferredSize();
        this.setSize(ps.width, ps.height);

    }

    /**
     * Construct for a set of Tops diagrams and labels
     * 
     * @param Diagrams -
     *            a vector of Tops diagrams
     * @param Labels -
     *            a vector of labels concurrent with the Tops diagrams
     */
    public TopsDisplayScroll(Vector<SecStrucElement> Diagrams, Vector<DomainDefinition> Labels) {
        this();
        this.setDiagrams(Diagrams, Labels);
        this.doLayout();
    }

    public TopsDisplayScroll(Protein p) {
        this();
        this.setDiagrams(p);
        this.doLayout();
    }

    /* END constructors */

    /* START methods defining preferred and minimum sizes */

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(TopsDisplayScroll.MIN_WIDTH, TopsDisplayScroll.MIN_HEIGHT);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(TopsDisplayScroll.PREF_WIDTH, TopsDisplayScroll.PREF_HEIGHT);
    }

    /* END methods defining preferred and minimum sizes */

    /* START paint/update methods */

    @Override
    public void paint(Graphics g) {
        if (this.DisplayPanel != null)
            this.DisplayPanel.repaint();
    }

    /* END paint/update methods */

    /* START methods to clear and set/re-set the panel */

    /**
     * this method clears the display
     */
    public void clear() {
        if (this.DisplayPanel != null)
            this.DisplayPanel.clear();
    }

    /**
     * method sets/re-sets the diagrams being managed
     * 
     * @param Diagrams -
     *            the new set of diagrams (all SecStrucElements)
     * @param Labels -
     *            the new set of labels
     */
    public void setDiagrams(Vector<SecStrucElement> Diagrams, Vector<DomainDefinition> Labels) {
        if (this.DisplayPanel != null)
            this.DisplayPanel.setDiagrams(Diagrams, Labels);
        this.doLayout();
    }

    /**
     * sets the set of diagrams being managed to those of tops.dw.protein p;
     * 
     * @param p -
     *            the tops.dw.protein
     */
    public void setDiagrams(Protein p) {
        if (this.DisplayPanel != null)
            this.DisplayPanel.setDiagrams(p);
        this.doLayout();
    }

    /**
     * method adds to the diagrams being managed
     * 
     * @param Diagrams -
     *            the new set of diagrams (all SecStrucElements)
     * @param Labels -
     *            the new set of labels
     */
    public void addDiagrams(Vector<SecStrucElement> Diagrams, Vector<DomainDefinition> Labels) {
        if (this.DisplayPanel != null)
            this.DisplayPanel.addDiagrams(Diagrams, Labels);
        this.doLayout();
    }

    /**
     * adds to the set of diagrams being managed those of tops.dw.protein p;
     * 
     * @param p -
     *            the tops.dw.protein
     */
    public void addDiagrams(Protein p) {
        if (this.DisplayPanel != null)
            this.DisplayPanel.addDiagrams(p);
        this.doLayout();
    }

    /**
     * rescale the display
     * 
     * @param scale -
     *            the new scale required (as percentage, 100 => no change )
     */
    public void scaleDisplay(int scale) {
        if (this.DisplayPanel != null)
            this.DisplayPanel.scaleDisplay(scale);
        this.doLayout();
    }

    /**
     * sets the edit mode for a diagram with a specific index
     * 
     * @param index -
     *            the diagram index
     * @param EditMode -
     *            the required edit mode
     */
    public void setEditMode(int index, int EditMode) {
        if (this.DisplayPanel != null)
            this.DisplayPanel.setEditMode(index, EditMode);
    }

    /**
     * sets the edit mode for specified diagram
     * 
     * @param s -
     *            the root SecStrucElement for the diagram
     * @param EditMode -
     *            the required edit mode
     */
    public void setEditMode(SecStrucElement s, int EditMode) {
        if (this.DisplayPanel != null)
            this.DisplayPanel.setEditMode(s, EditMode);
    }

    /**
     * returns the number of TopsDrawCanvases being managed by the
     * TopsDisplayPanel
     */
    public int NumberDrawCanvases() {
        int n = 0;
        if (this.DisplayPanel != null)
            n = this.DisplayPanel.numberDrawCanvases();

        return n;
    }

    /**
     * returns the TopsDrawCanvas with a given index
     * 
     * @param index -
     *            the index of the required canvas
     */
    public TopsDrawCanvas GetDrawCanvas(int index) {
        TopsDrawCanvas tdc = null;
        if (this.DisplayPanel != null)
            tdc = this.DisplayPanel.getDrawCanvas(index);

        return (tdc);
    }

    /**
     * returns the TopsDrawCanvas with a given root SecStrucElement
     * 
     * @param s -
     *            the root SecStrucElement
     */
    public TopsDrawCanvas GetDrawCanvas(SecStrucElement s) {
        TopsDrawCanvas tdc = null;
        if (this.DisplayPanel != null)
            tdc = this.DisplayPanel.getDrawCanvas(s);

        return tdc;
    }

    public Vector<TopsDrawCanvas> GetDrawCanvases() {
        if (this.DisplayPanel != null)
            return this.DisplayPanel.getDrawCanvases();
        else
            return null;
    }


    public TopsDisplayPanel getDisplayPanel() {
        return this.DisplayPanel;
    }

    /* END utility methods */

}
