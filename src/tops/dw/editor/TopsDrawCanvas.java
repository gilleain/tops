package tops.dw.editor;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;

import tops.dw.protein.*;

/**
 * This class is a java bean which displays and permits editing of a single Tops
 * diagram
 * 
 * @author David Westhead
 * @version 1.00, 27 Mar 1997
 * @see TopsDiagramDisplayPanel
 */
public class TopsDrawCanvas extends Canvas implements MouseListener,
        MouseMotionListener, PropertyChangeListener {

    /* START class variables */

    public static int BORDER = 50;

    public static int MIN_HEIGHT = 400;

    public static int MIN_WIDTH = 500;

    public static int PREF_HEIGHT = 400;

    public static int PREF_WIDTH = 500;

    static final int INFO_MODE = 0;

    static final int COLOUR_SYMBOLS_MODE = 1;

    static final int MOVE_SYMBOLS_MODE = 2;

    static final int MOVE_FIXEDS_MODE = 3;

    static final int REDRAW_CONNECTIONS_MODE = 4;

    static final int DELETE_SYMBOLS_MODE = 5;

    static final int ROTATE_X_MODE = 6;

    static final int ROTATE_Y_MODE = 7;

    static final int ROTATE_Z_MODE = 8;

    static final int REFLECT_XY_MODE = 9;

    static final int ADD_USER_LABEL = 11;

    static final int DELETE_USER_LABEL = 12;

    static final int MOVE_USER_LABEL = 13;

    static final int ADD_USER_ARROW = 14;

    static final int DELETE_USER_ARROW = 15;

    static final int ALIGN_X_MODE = 16;

    static final int ALIGN_Y_MODE = 17;

    private SecStrucElement RootSecStruc = null;

    private String Label = "Tops diagram";

    private SecStrucElement SelectedSymbol;

    private Float Scale = new Float(1.0);

    private String InfoString = null;

    private Point InfoStringPos = new Point();

    private PropertyChangeSupport changes = new PropertyChangeSupport(this);

    private int EditMode;

    private Color CurrentColour = Color.white;

    private Dimension OffDimension = new Dimension();

    private Image OffScreenImage = null;

    private Graphics OffGraphics = null;

    private Font Font18, Font12;

    private Vector UserLabels = new Vector();

    private Vector UserArrows = new Vector();

    private boolean LabellingActive = false;

    private UserLabel SelectedLabel = null;

    private UserArrow SelectedArrow = null;

    private boolean AlignXActive = false;

    private boolean AlignYActive = false;

    private int AlignXLevel = -1;

    private int AlignYLevel = -1;

    private boolean UseBorder = true;

    /* END of private instance variables */

    /* START Constructors */
    public TopsDrawCanvas() {
        this.setBackground(Color.white);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);

        this.EditMode = TopsDrawCanvas.INFO_MODE;

        Dimension ps = this.getPreferredSize();
        this.setSize(ps.width, ps.height);
        
        this.Font12 = new Font("TimesRoman", Font.PLAIN, 12);
        this.Font18 = new Font("TimesRoman", Font.PLAIN, 18);
    }

    /**
     * @param sse
     *            The SecStructElement serving as linked list root for the Tops
     *            diagram
     */
    public TopsDrawCanvas(SecStrucElement sse) {
        this();
        this.RootSecStruc = sse;
    }

    /**
     * @param sse
     *            The SecStructElement serving as linked list root for the Tops
     *            diagram
     * @param lab
     *            the Label string for the diagram
     */
    public TopsDrawCanvas(SecStrucElement sse, String lab) {
        this(sse);
        this.Label = lab;
    }

    /* END of Constructors */

    /* START methods defining preferred and minimum sizes */

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(TopsDrawCanvas.MIN_WIDTH, TopsDrawCanvas.MIN_HEIGHT);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(TopsDrawCanvas.PREF_WIDTH, TopsDrawCanvas.PREF_HEIGHT);
    }

    /* END methods defining preferred and minimum sizes */

    /* START methods to get and set properties */
    /* generally synchronized for multi-threaded access */

    /* Label - not a bound property */
    /**
     * @return the Label for the diagram
     */
    public synchronized String getLabel() {
        return this.Label;
    }

    /**
     * @param the
     *            Label for the diagram
     */
    public synchronized void setLabel(String l) {
        this.Label = l;
        this.repaint();
    }

    /**
     * @param use -
     *            sets whether a border should be drawn or not
     */
    public synchronized void setUseBorder(boolean use) {
        this.UseBorder = use;
    }

    /**
     * sets the colour for new colour change edits
     */
    public synchronized void setCurrentColour(Color c) {
        this.CurrentColour = c;
    }

    /**
     * returns the colour for new colour change edits
     */
    public synchronized Color getCurrentColour() {
        return this.CurrentColour;
    }

    /* RootSecStruc - not bound property */
    /**
     * @param Root -
     *            the root secondary structure for the diagram
     */
    public synchronized void setRootSecStruc(SecStrucElement Root) {
        this.RootSecStruc = Root;
        this.repaint();
    }

    /**
     * @return the root secondary structure for the diagram
     */
    public synchronized SecStrucElement getRootSecStruc() {
        return this.RootSecStruc;
    }

    /* the Scale - a bound property */
    /**
     * @param scale -
     *            the scaling value to apply to get from input coordinates to
     *            screen coordinates
     */
    public synchronized void setScale(float scale) {
        Float oldf = this.Scale;
        this.Scale = new Float(scale);
        this.changes.firePropertyChange("Scale", oldf, this.Scale);
        this.repaint();
    }

    /**
     * @return - the scaling value to apply to get from input coordinates to
     *         screen coordinates
     */
    public synchronized float getScale() {
        return this.Scale.floatValue();
    }

    /**
     * sets the editor mode for mouse operations
     * 
     * @param -
     *            the required editor mode (see class variables *_MODE)
     */
    public synchronized void setEditMode(int em) {
        this.EditMode = em;
        this.AlignXActive = false;
        this.AlignYActive = false;
        this.UnSelect();
    }

    /**
     * returns the current editor mode
     */
    public synchronized int getEditMode() {
        return this.EditMode;
    }

    /**
     * sets the information string
     * 
     * @param s -
     *            the new information string
     */
    public void setInfoString(String s) {
        this.InfoString = s;
    }

    /**
     * returns the current information string
     */
    public String getInfoString() {
        return this.InfoString;
    }

    /**
     * sets the information string position
     */
    public void setInfoStringPos(Point p) {
        this.InfoStringPos.x = p.x;
        this.InfoStringPos.y = p.y;
    }

    /**
     * returns the information string position
     */
    public Point getInfoStringPos() {
        return this.InfoStringPos;
    }

    /* END of methods to get and set properties */

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

    /* START the PropertyChangeListener interface */

    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals("CurrentColour")) {
            this.setCurrentColour((Color) e.getNewValue());
        }
    }

    /* END the PropertyChangeListener interface */

    /* START the MouseListener interface */

    public void mousePressed(MouseEvent e) {
        switch (this.EditMode) {
            case INFO_MODE:
                this.SelectByPosition(e.getPoint());
                if (this.SelectedSymbol != null) {
                    this.setInfoString(this.SelectedSymbol.toString());
                    this.setInfoStringPos(e.getPoint());
                    this.repaint();
                }
                break;
            case MOVE_SYMBOLS_MODE:
                this.SelectByPosition(e.getPoint());
                break;
            case MOVE_FIXEDS_MODE:
                this.SelectByPosition(e.getPoint());
                break;
            case MOVE_USER_LABEL:
                this.SelectedLabel = this.nearestUserLabel(e.getPoint());
                break;
            case ADD_USER_ARROW:
                if (this.UserArrows == null)
                    this.UserArrows = new Vector();
                UserArrow ua = new UserArrow();
                this.UserArrows.addElement(ua);
                this.SelectedArrow = ua;
                this.SelectedArrow.setStart(e.getPoint());
                this.SelectedArrow.setEnd(e.getPoint());
                this.repaint();
                break;
        }
    }

    public void mouseReleased(MouseEvent e) {
        switch (this.EditMode) {
            case INFO_MODE:
                this.UnSelect();
                this.setInfoString(null);
                this.repaint();
                break;
            case MOVE_SYMBOLS_MODE:
                this.UnSelect();
                this.repaint();
                break;
            case MOVE_FIXEDS_MODE:
                this.UnSelect();
                this.repaint();
                break;
            case MOVE_USER_LABEL:
                this.SelectedLabel = null;
                break;
            case ADD_USER_ARROW:
                this.SelectedArrow.setEnd(e.getPoint());
                this.SelectedArrow = null;
                this.repaint();
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
        switch (this.EditMode) {
            case DELETE_SYMBOLS_MODE:
                this.SelectByPosition(e.getPoint());
                this.DeleteSelected();
                this.UnSelect();
                this.repaint();
                break;
            case COLOUR_SYMBOLS_MODE:
                this.SelectByPosition(e.getPoint());
                if (this.SelectedSymbol != null) {
                    this.SelectedSymbol.setColour(this.CurrentColour);
                    this.UnSelect();
                    this.repaint();
                }
                break;
            case REDRAW_CONNECTIONS_MODE:
                if (this.SelectedSymbol == null) {
                    this.SelectByPosition(e.getPoint());
                    if (this.SelectedSymbol != null) {
                        this.SelectedSymbol.ClearConnectionTo();
                    }
                } else {
                    /*
                     * NB for double clicks two events arrive, one with
                     * ClickCount = 1 the other with ClickCount = 2
                     */
                    if (e.getClickCount() == 1) {
                        this.SelectedSymbol.AddConnectionTo(e.getPoint());
                        this.repaint();
                    } else {
                        this.UnSelect();
                    }
                }
                break;
            case ROTATE_X_MODE:
                this.RotateX();
                break;
            case ROTATE_Y_MODE:
                this.RotateY();
                break;
            case ROTATE_Z_MODE:
                this.RotateZ();
                break;
            case REFLECT_XY_MODE:
                if (this.RootSecStruc != null) {
                    this.RootSecStruc.ReflectXY();
                    this.centerDiagram();
                    this.repaint();
                }
                break;
            case ALIGN_X_MODE:
                if (e.getClickCount() > 1) {
                    if (!this.AlignXActive) {
                        this.SelectByPosition(e.getPoint());
                        if (this.SelectedSymbol != null) {
                            this.AlignXActive = true;
                            this.AlignXLevel = this.SelectedSymbol.GetPosition().x;
                        }
                    } else {
                        this.AlignXActive = false;
                    }
                } else {
                    if (this.AlignXActive) {
                        this.SelectByPosition(e.getPoint());
                        if (this.SelectedSymbol != null) {
                            this.SelectedSymbol.PlaceElementX(this.AlignXLevel);
                            this.repaint();
                        }
                    }
                }
                break;
            case ALIGN_Y_MODE:
                if (e.getClickCount() > 1) {
                    if (!this.AlignYActive) {
                        this.SelectByPosition(e.getPoint());
                        if (this.SelectedSymbol != null) {
                            this.AlignYActive = true;
                            this.AlignYLevel = this.SelectedSymbol.GetPosition().y;
                        }
                    } else {
                        this.AlignYActive = false;
                    }
                } else {
                    if (this.AlignYActive) {
                        this.SelectByPosition(e.getPoint());
                        if (this.SelectedSymbol != null) {
                            this.SelectedSymbol.PlaceElementY(this.AlignYLevel);
                            this.repaint();
                        }
                    }
                }
                break;
            case ADD_USER_LABEL:
                if (this.UserLabels == null)
                    this.UserLabels = new Vector();

                if (!this.LabellingActive) {
                    this.LabellingActive = true;
                    UserLabel ul = new UserLabel();
                    this.UserLabels.addElement(ul);
                    ul.setPosition(e.getPoint().x, e.getPoint().y);

                    LabelInput li = new LabelInput();
                    String lab = li.getLabel();
                    if (lab != null) {
                        ul.append(lab);
                    } else {
                        this.UserLabels.removeElement(ul);
                        ul = null;
                    }

                    this.LabellingActive = false;

                    this.repaint();
                }
                break;
            case DELETE_USER_LABEL:
                UserLabel ul = this.nearestUserLabel(e.getPoint());
                if ((this.UserLabels != null) && (ul != null))
                    this.UserLabels.removeElement(ul);
                this.repaint();
                break;
            case DELETE_USER_ARROW:
                UserArrow ua = this.nearestUserArrow(e.getPoint());
                if (ua != null) {
                    if (this.UserArrows != null)
                        this.UserArrows.removeElement(ua);
                    this.repaint();
                }
                break;
        }

    }

    public void RotateX() {
        if (this.RootSecStruc != null) {
            this.RootSecStruc.RotateX();
            this.centerDiagram();
            this.repaint();
        }
    }

    public void RotateY() {
        if (this.RootSecStruc != null) {
            this.RootSecStruc.RotateY();
            this.centerDiagram();
            this.repaint();
        }
    }

    public void RotateZ() {
        if (this.RootSecStruc != null) {
            this.RootSecStruc.RotateZ();
            this.centerDiagram();
            this.repaint();
        }
    }

    /* END the MouseListener interface */

    /* START the MouseMotionListener interface */

    public void mouseDragged(MouseEvent e) {
        switch (this.EditMode) {
            case INFO_MODE:
                this.setInfoStringPos(e.getPoint());
                this.repaint();
                break;
            case MOVE_SYMBOLS_MODE:
                if (this.SelectedSymbol != null) {
                    this.SelectedSymbol.SetPosition(e.getPoint());
                    this.repaint();
                }
                break;
            case MOVE_FIXEDS_MODE:
                if (this.SelectedSymbol != null) {
                    Point oldp = this.SelectedSymbol.GetPosition();
                    Point newp = e.getPoint();
                    this.SelectedSymbol.TranslateFixed(newp.x - oldp.x, newp.y
                            - oldp.y);
                    this.repaint();
                }
                break;
            case MOVE_USER_LABEL:
                Point p = e.getPoint();
                if (this.SelectedLabel != null) {
                    this.SelectedLabel.setPosition(p.x, p.y);
                    this.repaint();
                }
                break;
            case ADD_USER_ARROW:
                if (this.SelectedArrow != null) {
                    this.SelectedArrow.setEnd(e.getPoint());
                    this.repaint();
                }
        }

    }

    public void mouseMoved(MouseEvent e) {
    }

    /* END the MouseMotionListener interface */

    /* START methods used in editing with the mouse */

    public synchronized void DeleteSelected() {
        if (this.SelectedSymbol != null)
            this.RootSecStruc = this.SelectedSymbol.Delete();
    }

    public synchronized void UnSelect() {
        this.SelectedSymbol = null;
    }

    public synchronized void SelectByPosition(Point p) {

        SecStrucElement selected = null;

        if (p != null) {

            SecStrucElement s;
            double minsep = Double.POSITIVE_INFINITY;
            double sep;
            for (s = this.RootSecStruc; s != null; s = s.GetTo()) {
                sep = s.separation(p);
                if ((sep < s.GetSymbolRadius()) && (sep < minsep)) {
                    minsep = sep;
                    selected = s;
                }
            }

        }

        this.SelectedSymbol = selected;

    }

    /* END methods used in editing with the mouse */

    /* START methods used in transformation between input and screen coordinates */

    /**
     * this method applies the current scale value to the diagram, it does not
     * do a repaint
     */
    public synchronized void ScaleDiagram() {
        float scale = this.getScale();
        if (scale <= 0.0)
            scale = 1.0F;
        this.RootSecStruc.ApplyScale(scale);
    }

    /**
     * this methods applies the inverse of the scale to the diagram, it does not
     * do a repaint
     */
    public void InvertScale() {
        float scale = this.getScale();
        if (scale > 0.0)
            scale = 1.0F / scale;
        else
            scale = 1.0F;
        this.RootSecStruc.ApplyScale(scale);
    }
    

    /**
     * this method sets up screen coordinates from the input ones it also sets
     * the scale
     * 
     * @param scale -
     *            the scaling value to apply for size
     * @see SetCCodeCoordinates
     */
    public synchronized void SetCanvasCoordinates(float scale) {
        this.setScale(scale);
        this.SetCanvasCoordinates();
    }

    /**
     * this method sets up screen coordinates from the input ones it uses the
     * current scale value
     * 
     * @see SetCCodeCoordinates
     */
    public synchronized void SetCanvasCoordinates() {
        this.ScaleDiagram();

        /*
         * inversion of y direction is required because the canvas coordinate
         * system changes the handedness of the input diagram
         */
        this.RootSecStruc.InvertY();

        this.centerDiagram();
    }

  
    /**
     * this method re-sets the coordinates to those used by the C code prior to
     * writing the file
     * 
     * @see SetCanvasCoordinates
     */
    public void SetCCodeCoordinates() {
        this.InvertScale();
        this.RootSecStruc.InvertY();
    }

    /**
     * a method to center the diagram on the canvas
     */
    public void centerDiagram() {

        try {
            Point cent = this.RootSecStruc.TopsCentroid();
            int dx = -cent.x + this.getSize().width / 2;
            int dy = -cent.y + this.getSize().height / 2;
            this.RootSecStruc.TranslateDiagram(dx, dy);
        } catch (TopsLinkedListException e) {
        }

    }

    /**
     * this method returns the largest scaling factor which will fit the diagram
     * onto the current canvas size
     */
    public synchronized float getMaxScale() {
        Rectangle bb;
        float s = 1.0F;
        Dimension cd = this.getSize();
        float cheight = (cd.height - 2 * TopsDrawCanvas.BORDER);
        float cwidth = (cd.width - 2 * TopsDrawCanvas.BORDER);

        if (this.RootSecStruc != null) {
            try {
                bb = this.RootSecStruc.TopsBoundingBox();
            } catch (TopsLinkedListException e) {
                return s;
            }

            float s1 = cwidth / (bb.width);
            float s2 = cheight / (bb.height);
            s = Math.min(s1, s2);
            if (s > 1.0F)
                s = 1.0F;
        }

        return s;

    }
    
    public Image getImage() {
        int w = this.getSize().width;
        int h = this.getSize().height;

        Image img = this.createImage(w, h);
        Graphics g = img.getGraphics();

        g.setColor(this.getBackground());
        g.fillRect(0, 0, w, h);

        this.paint(g);

        return img;
    }

    public Vector getEPS() {

        if (this.RootSecStruc == null)
            return null;

        int w = this.getSize().width;
        int h = this.getSize().height;

        Vector EPS = new Vector();

        EPS = PostscriptFactory.makeEPSHeader(EPS, 0, 0, w, h);
        this.RootSecStruc.getEPS(w, h, EPS);
        
        // draw user labels
        if (this.UserLabels != null) {
            Enumeration labs = this.UserLabels.elements();
            while (labs.hasMoreElements()) {
                UserLabel ul = (UserLabel) labs.nextElement();
                EPS = ul.Draw(EPS, h);
            }
        }

        // draw user arrows
        if (this.UserArrows != null) {
            Enumeration arrows = this.UserArrows.elements();
            while (arrows.hasMoreElements()) {
                UserArrow ua = (UserArrow) arrows.nextElement();
                EPS = ua.Draw(EPS, h);
            }
        }

        // determine actual bounding box
        Rectangle bbox = this.RootSecStruc.EPSBoundingBox(h);
        bbox = this.expandEPSBoundingBox(bbox, h);
        EPS = PostscriptFactory.addBoundingBox(EPS, bbox.x, bbox.y, bbox.x
                + bbox.width, bbox.y + bbox.height);

        EPS.addElement(PostscriptFactory.showpage());
        EPS.addElement(PostscriptFactory.EndDocument());
        EPS.addElement(PostscriptFactory.EOF());

        return EPS;

    }

    private Rectangle expandEPSBoundingBox(Rectangle r, int h) {

        int xmin = r.x;
        int xmax = r.x + r.width;
        int ymin = r.y;
        int ymax = r.y + r.height;
       
        int x, y;
        int sw, sh;
        if (this.UserLabels != null) {
            Enumeration labs = this.UserLabels.elements();
            while (labs.hasMoreElements()) {
                UserLabel ul = (UserLabel) labs.nextElement();
                x = ul.getPosition().x;
                y = h - ul.getPosition().y;
                sw = ul.getPSWidth();
                sh = ul.getPSHeight();
                if (x + sw > xmax)
                    xmax = x + sw;
                if (x < xmin)
                    xmin = x;
                if (y + sh > ymax)
                    ymax = y + sh;
                if (y - sh < ymin)
                    ymin = y - sh;
            }
        }

        if (this.UserArrows != null) {
            Enumeration arrows = this.UserArrows.elements();
            while (arrows.hasMoreElements()) {
                UserArrow ua = (UserArrow) arrows.nextElement();
                x = ua.start.x;
                y = h - ua.start.y;
                if (x > xmax)
                    xmax = x;
                if (y > ymax)
                    ymax = y;
                if (x < xmin)
                    xmin = x;
                if (y < ymin)
                    ymin = y;
                x = ua.end.x;
                y = h - ua.end.y;
                if (x > xmax)
                    xmax = x;
                if (y > ymax)
                    ymax = y;
                if (x < xmin)
                    xmin = x;
                if (y < ymin)
                    ymin = y;
            }
        }

        return new Rectangle(xmin, ymin, xmax - xmin, ymax - ymin);

    }

    /* END image handling methods */

    /* START paint and update methods */

    /* update method does double buffering */
    @Override
    public void update(Graphics g) {

        int w = this.getSize().width;
        int h = this.getSize().height;

        if ((this.OffScreenImage == null) || (this.OffDimension.width != w)
                || (this.OffDimension.height != h)) {
            this.OffScreenImage = this.createImage(w, h);
            this.OffDimension.width = w;
            this.OffDimension.height = h;
            this.OffGraphics = this.OffScreenImage.getGraphics();
        }

        this.OffGraphics.setColor(this.getBackground());
        this.OffGraphics.fillRect(0, 0, w, h);

        this.paint(this.OffGraphics);

        g.drawImage(this.OffScreenImage, 0, 0, this);

    }

    @Override
    public void paint(Graphics g) {

        g.setColor(Color.black);
        if (this.UseBorder)
            this.DrawBorder(g);
        if (this.Label != null)
            this.DrawLabel(g);
        if (this.RootSecStruc != null)
        	this.RootSecStruc.paint(g);
        if (this.InfoString != null)
            this.DrawInfoString(g);

        if (this.UserLabels != null) {
            Enumeration labs = this.UserLabels.elements();
            while (labs.hasMoreElements()) {
                UserLabel ul = (UserLabel) labs.nextElement();
                ul.Draw(g);
            }
        }

        if (this.UserArrows != null) {
            Enumeration arrows = this.UserArrows.elements();
            while (arrows.hasMoreElements()) {
                UserArrow ua = (UserArrow) arrows.nextElement();
                ua.Draw(g);
            }
        }

    }

    /* END paint and update methods */

    /* START private methods used by this class in painting/updating */

    private void DrawInfoString(Graphics g) {
        g.setFont(this.Font12);
        g.setColor(Color.black);
        g.drawString(this.InfoString, this.InfoStringPos.x, this.InfoStringPos.y);
    }

    private void DrawBorder(Graphics g) {

        g.setColor(Color.lightGray);

        int w = this.getSize().width;
        int h = this.getSize().height;

        g.drawRect(1, 1, w - 2, h - 2);

        g.setColor(Color.black);

    }

    /*
     * this private method draws the master label for the TOPS diagram as a
     * title
     */
    private void DrawLabel(Graphics g) {

        int x, y;

        if (this.Label != null) {
            x = this.getSize().width / 2;
            y = TopsDrawCanvas.BORDER / 2;
            g.setFont(this.Font18);
            x -= (g.getFontMetrics().stringWidth(this.Label)) / 2;
            g.drawString(this.Label, x, y);
        }
    }

    private UserLabel nearestUserLabel(Point p) {
        if (this.UserLabels == null)
            return null;

        Enumeration en = this.UserLabels.elements();

        UserLabel ul, nearest = null;
        int dx, dy, d2;
        int d2m = Integer.MAX_VALUE;
        while (en.hasMoreElements()) {
            ul = (UserLabel) en.nextElement();
            dx = p.x - ul.getPosition().x;
            dy = p.y - ul.getPosition().y;
            d2 = dx * dx + dy * dy;
            if (d2 < d2m) {
                d2m = d2;
                nearest = ul;
            }
        }

        return nearest;

    }

    private UserArrow nearestUserArrow(Point p) {
        if (this.UserArrows == null)
            return null;

        Enumeration en = this.UserArrows.elements();
        UserArrow ua, nearest = null;
        int cx, cy, dx, dy, r2, d2, d2min = Integer.MAX_VALUE;
        Point start, end;

        while (en.hasMoreElements()) {
            ua = (UserArrow) en.nextElement();
            start = ua.getStart();
            end = ua.getEnd();
            cx = (start.x + end.x) / 2;
            cy = (start.y + end.y) / 2;
            r2 = (start.x - cx) * (start.x - cx) + (start.y - cy)
                    * (start.y - cy);

            dx = p.x - cx;
            dy = p.y - cy;
            d2 = dx * dx + dy * dy;

            if ((d2 < d2min) && (d2 < r2)) {
                d2 = d2min;
                nearest = ua;
            }

        }

        return nearest;

    }

}
