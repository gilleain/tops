package tops.dw.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Panel;
import java.util.List;
import java.util.Vector;

import tops.dw.protein.Cartoon;
import tops.dw.protein.Protein;
import tops.port.model.DomainDefinition;
import tops.web.display.applet.TopsDrawCanvas;

/**
 * This class is a java bean which displays a number of Tops diagrams in a panel
 * with a grid layout.
 * 
 * @author David Westhead
 * @version 1.00, 17 Apr 1997
 * @see TopsDrawCanvas
 */
public class TopsDisplayPanel extends Panel {

    private Vector<TopsDrawCanvas> drawCanvases;

    private Vector<Panel> dcPanels;


    private Dimension canvasDimension;

    private int nColumns;

//    private float LastScale = 1.0f;

    /**
     * the basic constructor
     */
    public TopsDisplayPanel() {

        super();

//        LastScale = 1.0f;

        this.setBackground(Color.white);
        this.drawCanvases = new Vector<TopsDrawCanvas>();
        this.dcPanels = new Vector<Panel>();

        this.canvasDimension = new Dimension(0, 0);
//        		TopsDrawCanvas.PREF_WIDTH,  TODO
//                TopsDrawCanvas.PREF_HEIGHT);
        this.nColumns = 1;

        this.setLayout(new GridLayout(0, this.nColumns));

        Dimension ps = this.getPreferredSize();
        this.setSize(ps.width, ps.height);

    }

    /**
     * Construct for a set of Tops diagrams and labels
     * 
     * @param diagrams - a vector of Tops diagrams
     * @param labels - a vector of labels concurrent with the Tops diagrams
     */
    public TopsDisplayPanel(List<Cartoon> diagrams, List<DomainDefinition> labels) {
        this();
        this.setDiagrams(diagrams, labels);
    }

    /**
     * Construct for a tops.dw.protein
     */
    public TopsDisplayPanel(Protein p) {
        this(p.getLinkedLists(), p.getDomainDefs());
    }

    /* END constructors */

    /* START methods defining sizes */

    @Override
    public Dimension getPreferredSize() {
        return this.getRequiredSize();
    }

    public Dimension getRequiredSize() {

        int UnitWidth = this.canvasDimension.width;
        int UnitHeight = this.canvasDimension.height;

        int n = this.dcPanels.size();

        int width = this.nColumns * UnitWidth;
        int height = (1 + (n - 1) / this.nColumns) * UnitHeight;

        return new Dimension(width, height);

    }

    @Override
    public void paint(Graphics g) {

        int i;

        for (i = 0; i < this.numberDrawCanvases(); i++) {
            this.drawCanvases.get(i).repaint();
        }

    }

    /**
     * this method clears the display
     */
    public void clear() {

        this.removeAll();
        this.drawCanvases = new Vector<TopsDrawCanvas>();
        this.dcPanels = new Vector<Panel>();

        Dimension ps = this.getPreferredSize();
        this.setSize(ps.width, ps.height);

        this.repaint();

    }

    /**
     * this method adds a diagram to the display
     * 
     * @param s - the root secondary structure for the diagram
     * @param Label - the label for the diagram
     */
    private void addDiagram(Cartoon cartoon, DomainDefinition Label) {
        if (cartoon != null) {
        	String lab = (Label == null)? "Tops Diagram" : Label.toString();
        	TopsDrawCanvas tdc = new TopsDrawCanvas(cartoon, lab);
            this.drawCanvases.addElement(tdc);
            tdc.setSize(this.canvasDimension);
            Panel p = new Panel();
            this.dcPanels.addElement(p);
            p.add(tdc);
            this.add(p);
        }
    }

    /**
     * method sets/re-sets the diagrams being managed
     * 
     * @param Diagrams -
     *            the new set of diagrams (all SecStrucElements)
     * @param Labels -
     *            the new set of labels
     */
    public void setDiagrams(List<Cartoon> diagrams, List<DomainDefinition> labels) {
        this.clear();
        this.addDiagrams(diagrams, labels);
    }

    /**
     * method sets/re-sets the diagrams being managed
     * 
     * @param p -
     *            the tops.dw.protein
     */
    public void setDiagrams(Protein p) {
        this.setDiagrams(p.getLinkedLists(), p.getDomainDefs());
    }

    /**
     * method adds to the diagrams being managed
     * 
     * @param Diagrams -
     *            the new set of diagrams (all Cartoons)
     * @param Labels -
     *            the new set of labels
     */
    public void addDiagrams(List<Cartoon> Diagrams, List<DomainDefinition> Labels) {

        int i;

        /*
         * this is an unpleasant thing to have to do - would be better to change
         * it at some point
         */
        for (i = 0; i < this.numberDrawCanvases(); i++) {
            this.getDrawCanvas(i).setCCodeCoordinates();
        }

        for (i = 0; i < Diagrams.size(); i++) {
            Cartoon el = Diagrams.get(i);
            DomainDefinition lab = null;
            if (i < Labels.size())
            	lab = Labels.get(i);
            this.addDiagram(el, lab);
        }

        float MinScale = 1.0F, scale;
        for (i = 0; i < this.numberDrawCanvases(); i++) {
            if ((scale = this.getDrawCanvas(i).getMaxScale()) < MinScale)
                MinScale = scale;
        }
        for (i = 0; i < this.numberDrawCanvases(); i++) {
            this.getDrawCanvas(i).setCanvasCoordinates(MinScale);
        }

        Dimension d = this.getRequiredSize();
        this.setSize(d.width, d.height);
        this.repaint();

    }

    /**
     * set the diagrams on display to those of a given tops.dw.protein
     * 
     * @param tops.dw.protein -
     *            the tops.dw.protein
     */
    public void addDiagrams(Protein p) {
        this.addDiagrams(p.getLinkedLists(), p.getDomainDefs());
    }

    /**
     * rescale the display
     * 
     * @param scale -
     *            the new scale required (as percentage, 100 => no change ) THIS
     *            DOES NOT WORK YET SO COMMENTED OUT
     */
    public void scaleDisplay(int scale) {

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
        TopsDrawCanvas dc = this.getDrawCanvas(index);
        if (dc != null)
            dc.setEditMode(EditMode);
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
        TopsDrawCanvas dc = this.getDrawCanvas(s);
        if (dc != null)
            dc.setEditMode(EditMode);
    }


    /**
     * returns the number of TopsDrawCanvases being managed by the
     * TopsDisplayPanel
     */
    public int numberDrawCanvases() {
        return this.drawCanvases.size();
    }

    /**
     * returns the TopsDrawCanvas with a given index
     * 
     * @param index -
     *            the index of the required canvas
     */
    public TopsDrawCanvas getDrawCanvas(int index) {

        TopsDrawCanvas tdc = null;

        if ((index >= 0) && (index < this.numberDrawCanvases())) {
            tdc = this.drawCanvases.elementAt(index);
        }

        return tdc;

    }

    /**
     * returns the TopsDrawCanvas with a given index
     * 
     * @param index - the index of the required canvas
     */
    public Panel getDCPanel(int index) {

        Panel p = null;

        if ((index >= 0) && (index < this.numberDrawCanvases())) {
            p = (Panel) this.dcPanels.elementAt(index);
        }

        return (p);

    }

    /**
     * returns the TopsDrawCanvas with a given root SecStrucElement
     * 
     * @param s -
     *            the root SecStrucElement
     */
    public TopsDrawCanvas getDrawCanvas(Cartoon s) {

        TopsDrawCanvas tdc = null;

        int i;
        for (i = 0; i < this.numberDrawCanvases(); i++) {
            TopsDrawCanvas tdc1 = this.getDrawCanvas(i);
            if (s == tdc1.getCartoon()) {
                tdc = tdc1;
                break;
            }
        }

        return tdc;

    }

    public Vector<TopsDrawCanvas> getDrawCanvases() {
        return this.drawCanvases;
    }

    /* END utility methods */

}
