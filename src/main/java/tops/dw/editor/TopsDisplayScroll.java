package tops.dw.editor;

import java.awt.Adjustable;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.ScrollPane;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import tops.dw.protein.Cartoon;
import tops.dw.protein.Protein;
import tops.port.model.DomainDefinition;
import tops.web.display.applet.TopsDrawCanvas;

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

    private TopsDisplayPanel displayPanel;


    /**
     * the basic constructor
     */
    public TopsDisplayScroll() {

        super(ScrollPane.SCROLLBARS_AS_NEEDED);

        Adjustable Vadjust = this.getVAdjustable();
        Adjustable Hadjust = this.getHAdjustable();
        Vadjust.setUnitIncrement(10);
        Hadjust.setUnitIncrement(10);

        this.displayPanel = new TopsDisplayPanel();
        this.add(this.displayPanel);

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
    public TopsDisplayScroll(List<Cartoon> Diagrams, Vector<DomainDefinition> Labels) {
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
        if (this.displayPanel != null)
            this.displayPanel.repaint();
    }

    /* END paint/update methods */

    /* START methods to clear and set/re-set the panel */

    /**
     * this method clears the display
     */
    public void clear() {
        if (this.displayPanel != null)
            this.displayPanel.clear();
    }

    /**
     * method sets/re-sets the diagrams being managed
     * 
     * @param Diagrams -
     *            the new set of diagrams (all SecStrucElements)
     * @param Labels -
     *            the new set of labels
     */
    public void setDiagrams(List<Cartoon> Diagrams, Vector<DomainDefinition> Labels) {
        if (this.displayPanel != null)
            this.displayPanel.setDiagrams(Diagrams, Labels);
        this.doLayout();
    }

    /**
     * sets the set of diagrams being managed to those of tops.dw.protein p;
     * 
     * @param p -
     *            the tops.dw.protein
     */
    public void setDiagrams(Protein p) {
        if (this.displayPanel != null)
            this.displayPanel.setDiagrams(p);
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
    public void addDiagrams(List<Cartoon> Diagrams, Vector<DomainDefinition> Labels) {
        if (this.displayPanel != null)
            this.displayPanel.addDiagrams(Diagrams, Labels);
        this.doLayout();
    }

    /**
     * adds to the set of diagrams being managed those of tops.dw.protein p;
     * 
     * @param p -
     *            the tops.dw.protein
     */
    public void addDiagrams(Protein p) {
        if (this.displayPanel != null)
            this.displayPanel.addDiagrams(p);
        this.doLayout();
    }

    /**
     * rescale the display
     * 
     * @param scale -
     *            the new scale required (as percentage, 100 => no change )
     */
    public void scaleDisplay(int scale) {
        if (this.displayPanel != null)
            this.displayPanel.scaleDisplay(scale);
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
        if (this.displayPanel != null)
            this.displayPanel.setEditMode(index, EditMode);
    }

    /**
     * sets the edit mode for specified diagram
     * 
     * @param s -
     *            the root SecStrucElement for the diagram
     * @param EditMode -
     *            the required edit mode
     */
    public void setEditMode(Cartoon s, int EditMode) {
        if (this.displayPanel != null)
            this.displayPanel.setEditMode(s, EditMode);
    }

    /**
     * returns the number of TopsDrawCanvases being managed by the
     * TopsDisplayPanel
     */
    public int NumberDrawCanvases() {
        int n = 0;
        if (this.displayPanel != null)
            n = this.displayPanel.numberDrawCanvases();

        return n;
    }

    /**
     * returns the TopsDrawCanvas with a given index
     * 
     * @param index -
     *            the index of the required canvas
     */
    public TopsDrawCanvas getDrawCanvas(int index) {
        TopsDrawCanvas tdc = null;
        if (this.displayPanel != null)
            tdc = this.displayPanel.getDrawCanvas(index);

        return tdc;
    }

    /**
     * returns the TopsDrawCanvas with a given root SecStrucElement
     * 
     * @param s -
     *            the root SecStrucElement
     */
    public TopsDrawCanvas getDrawCanvas(Cartoon s) {
        TopsDrawCanvas tdc = null;
        if (this.displayPanel != null)
            tdc = this.displayPanel.getDrawCanvas(s);

        return tdc;
    }

    public List<TopsDrawCanvas> getDrawCanvases() {
        if (this.displayPanel != null)
            return this.displayPanel.getDrawCanvases();
        else
            return new ArrayList<>();
    }


    public TopsDisplayPanel getDisplayPanel() {
        return this.displayPanel;
    }

}
