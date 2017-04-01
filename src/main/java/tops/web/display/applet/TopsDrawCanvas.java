package tops.web.display.applet;

import static tops.port.model.SSEType.COIL;
import static tops.port.model.SSEType.CTERMINUS;
import static tops.port.model.SSEType.EXTENDED;
import static tops.port.model.SSEType.HELIX;
import static tops.port.model.SSEType.NTERMINUS;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import tops.dw.editor.PostscriptFactory;
import tops.dw.editor.UserArrow;
import tops.dw.editor.UserLabel;
import tops.dw.protein.Cartoon;
import tops.dw.protein.SecStrucElement;
import tops.port.model.SSEType;

/**
 * This class is a java bean which displays and permits editing of a single Tops
 * diagram
 * 
 * @author David Westhead
 * @version 1.00, 27 Mar 1997
 */
public class TopsDrawCanvas extends Canvas implements MouseListener, MouseMotionListener {

    /* START class variables */

    public static int BORDER = 50;

    public static int MIN_HEIGHT = 400;

    public static int MIN_WIDTH = 500;

    public static int PREF_HEIGHT = 400;

    public static int PREF_WIDTH = 500;

    public static final int INFO_MODE = 0; //don't need
    public static final int COLOUR_SYMBOLS_MODE = 1; //don't need
    public static final int MOVE_SYMBOLS_MODE = 2;

    public static final int MOVE_FIXEDS_MODE = 3;

    public static final int REDRAW_CONNECTIONS_MODE = 4;

    public static final int DELETE_SYMBOLS_MODE = 5;

    public static final int ROTATE_X_MODE = 6;

    public static final int ROTATE_Y_MODE = 7;

    public static final int ROTATE_Z_MODE = 8;

    public static final int REFLECT_XY_MODE = 9;

    public static final int TOGGLE_SIZE_DISPLAY = 10;//don't need
    public static final int ADD_USER_LABEL = 11; //don't need
    public static final int DELETE_USER_LABEL = 12; //don't need
    public static final int MOVE_USER_LABEL = 13; //don't need
    public static final int ADD_USER_ARROW = 14; //don't need
    public static final int DELETE_USER_ARROW = 15; //don't need
    public static final int ALIGN_X_MODE = 16; //don't need
    public static final int ALIGN_Y_MODE = 17; //don't need
     
    /* *************NEW!************** */
    public static final int ADD_STRAND_MODE = 18;

    public static final int ADD_HELIX_MODE = 19;

    public static final int FLIP_MODE = 20; // woo-ha, got'cha'll in check

    public static final int SELECT_SYMBOL_MODE = 21;

    public static final int MOVE_STRAND_MODE = 22; // ok, this is annoying, but
                                            // essential

    public static final int MOVE_HELIX_MODE = 23; // ok, this is annoying, but
                                            // essential

    /* Multiple Select Modes */
    public static final int ADD_HBOND_MODE = 24;

    public static final int LINEAR_LAYOUT_MODE = 25;

    public static final int CIRCULAR_LAYOUT_MODE = 26;

    public static final int FLIP_MULTIPLE_MODE = 27;

    /* END of class variables */

    /* START private instance variables */

    private Cartoon RootSecStruc = null;

    private String Label = "Tops diagram";

    private SecStrucElement SelectedSymbol;

    private Rectangle selectBox;

    private List<SecStrucElement> selectBoxList;

    private int minimumSeparation = 10;;

    private Float Scale = new Float(1.0);

    private String InfoString = null;

    private Point InfoStringPos = new Point();

    private int EditMode;

//    private Color CurrentColour = Color.white;

    private Color selectedStrandColor = Color.yellow;

    private Color selectedHelixColor = Color.pink;

    private Color selectedTerminusColor = Color.green;

//    private boolean SizeDisplay = true;

    private Dimension OffDimension = new Dimension();

    private Image OffScreenImage = null;

    private Graphics OffGraphics = null;

    private Font[] FontsArr = new Font[10];

    private Font Font18, Font12;

    private boolean UseBorder = true;
    
    private Vector<UserLabel> UserLabels = new Vector<UserLabel>();

    private Vector<UserArrow> UserArrows = new Vector<UserArrow>();

    public TopsDrawCanvas() {
        this.setBackground(Color.white);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);

        this.EditMode = TopsDrawCanvas.ADD_STRAND_MODE;

        Dimension ps = this.getPreferredSize();
        this.setSize(ps.width, ps.height);

        int fs, i;
        for (fs = 48, i = 0; (fs > 11) && i < this.FontsArr.length; fs -= 4, i++) {
            this.FontsArr[i] = new Font("TimesRoman", Font.PLAIN, fs);
        }
        this.Font18 = new Font("TimesRoman", Font.PLAIN, 18);
        this.Font12 = new Font("TimesRoman", Font.PLAIN, 12);

//        SizeDisplay = true;

        this.selectBoxList = new ArrayList<SecStrucElement>();
    }
    
    public Vector<String> getEPS() {

        if (this.RootSecStruc == null)
            return null;

        int w = this.getSize().width;
        int h = this.getSize().height;

        Vector<String> EPS = new Vector<String>();

        EPS = PostscriptFactory.makeEPSHeader(EPS, 0, 0, w, h);
        this.RootSecStruc.getEPS(w, h, EPS);
        
        // draw user labels
        if (this.UserLabels != null) {
            Enumeration<UserLabel> labs = this.UserLabels.elements();
            while (labs.hasMoreElements()) {
                UserLabel ul = labs.nextElement();
                EPS = ul.Draw(EPS, h);
            }
        }

        // draw user arrows
        if (this.UserArrows != null) {
            Enumeration<UserArrow> arrows = this.UserArrows.elements();
            while (arrows.hasMoreElements()) {
                UserArrow ua = arrows.nextElement();
                EPS = ua.Draw(EPS, h);
            }
        }

        // determine actual bounding box
        Rectangle bbox = RootSecStruc.epsBoundingBox(h);
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
            Enumeration<UserLabel> labs = this.UserLabels.elements();
            while (labs.hasMoreElements()) {
                UserLabel ul = labs.nextElement();
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
            Enumeration<UserArrow> arrows = this.UserArrows.elements();
            while (arrows.hasMoreElements()) {
                UserArrow ua = arrows.nextElement();
                x = ua.getStart().x;
                y = h - ua.getStart().y;
                if (x > xmax)
                    xmax = x;
                if (y > ymax)
                    ymax = y;
                if (x < xmin)
                    xmin = x;
                if (y < ymin)
                    ymin = y;
                x = ua.getEnd().x;
                y = h - ua.getEnd().y;
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

    /**
     * @param sse
     *            The SecStructElement serving as linked list root for the Tops
     *            diagram
     */
    public TopsDrawCanvas(Cartoon cartoon) {
        this();
        this.RootSecStruc = cartoon;
//        SizeDisplay = true;
    }

    /**
     * @param sse
     *            The SecStructElement serving as linked list root for the Tops
     *            diagram
     * @param lab
     *            the Label string for the diagram
     */
    public TopsDrawCanvas(Cartoon cartoon, String lab) {
        this(cartoon);
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
     * @param l
     *            the Label for the diagram
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

    /* RootSecStruc - not bound property */
    /**
     * @param Root -
     *            the root secondary structure for the diagram
     */
    public synchronized void setRootSecStruc(SecStrucElement Root) {
        this.RootSecStruc = new Cartoon(Root);
        this.repaint();
    }

    /**
     * @return the root secondary structure for the diagram
     */
    public synchronized SecStrucElement getRootSecStruc() {
        return this.RootSecStruc.getRoot();
    }

    /* the Scale - a bound property */
    /**
     * @param scale -
     *            the scaling value to apply to get from input coordinates to
     *            screen coordinates
     */
    public synchronized void setScale(float scale) {
//        Float oldf = Scale;
        this.Scale = new Float(scale);
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
     * @param em
     *            the required editor mode (see class variables *_MODE)
     */
    public synchronized void setEditMode(int em) {
        System.out.println("setting edit mode : " + em);
        this.EditMode = em;
        // UnSelect();
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

    /**
     * deletes the current diagram
     */
    public void clear() {
        this.RootSecStruc = null;
        this.repaint();
    }

    public String convertStructureToString() {
        return RootSecStruc.convertStructureToString();
    }

    /* END of methods to get and set properties */

    /* START the MouseListener interface */

    public void mousePressed(MouseEvent e) {
        Point pos = e.getPoint();
        System.out.println("currently selected symbol = " + this.SelectedSymbol);

        switch (this.EditMode) {

            case SELECT_SYMBOL_MODE:
                this.SelectByPosition(pos);
                this.repaint();
                break;

            case FLIP_MODE: // woo-ha!
                this.SelectByPosition(pos);
                this.repaint();
                break;

            case ADD_STRAND_MODE:
                this.addSymbol(EXTENDED, "U", pos.x, pos.y);
                this.repaint();
                this.setEditMode(TopsDrawCanvas.MOVE_STRAND_MODE);
                break;

            case ADD_HELIX_MODE:
                this.addSymbol(HELIX, "U", pos.x, pos.y);
                this.repaint();
                this.setEditMode(TopsDrawCanvas.MOVE_HELIX_MODE);
                break;

            case ADD_HBOND_MODE:
            case LINEAR_LAYOUT_MODE:
            case CIRCULAR_LAYOUT_MODE:
            case FLIP_MULTIPLE_MODE:
                this.UnSelect();
                this.startNewSelectBox(pos);
                this.repaint();
                break;

            case MOVE_SYMBOLS_MODE:
                this.SelectByPosition(pos);
                this.repaint();
                break;

            case MOVE_FIXEDS_MODE:
                this.SelectByPosition(pos);
                this.repaint();
                break;

            case DELETE_SYMBOLS_MODE:
                this.SelectByPosition(pos);
                this.repaint();
                break;
        }
    }

    public void mouseReleased(MouseEvent e) {

        switch (this.EditMode) {

            case FLIP_MODE:
                RootSecStruc.flip(this.SelectedSymbol);
                this.repaint();
                break;

            case MOVE_STRAND_MODE:
                this.setEditMode(TopsDrawCanvas.ADD_STRAND_MODE);
                break;

            case MOVE_HELIX_MODE:
                this.setEditMode(TopsDrawCanvas.ADD_HELIX_MODE);
                break;

            case ADD_HBOND_MODE:
            case LINEAR_LAYOUT_MODE:
            case CIRCULAR_LAYOUT_MODE:
            case FLIP_MULTIPLE_MODE:
                // updateSelectBox(pos);
                this.selectBoxDoAction();
                this.repaint();
                break;

            case MOVE_SYMBOLS_MODE:
                // UnSelect();
                this.repaint();
                break;

            case MOVE_FIXEDS_MODE:
                // UnSelect();
                this.repaint();
                break;

            case DELETE_SYMBOLS_MODE:
                this.DeleteSelected();
                // UnSelect();
                this.SelectedSymbol = this.RootSecStruc.getRoot();
                this.repaint();
                break;
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
        switch (this.EditMode) {
            /*
             * case DELETE_SYMBOLS_MODE: SelectByPosition( e.getPoint() );
             * DeleteSelected(); //UnSelect(); SelectedSymbol = RootSecStruc;
             * repaint(); break;
             */

            case REDRAW_CONNECTIONS_MODE:
                if (this.SelectedSymbol == null) {
                    this.SelectByPosition(e.getPoint());
                    if (this.SelectedSymbol != null) {
                        this.SelectedSymbol.clearConnectionTo();
                    }
                } else {
                    /*
                     * NB for double clicks two events arrive, one with
                     * ClickCount = 1 the other with ClickCount = 2
                     */
                    if (e.getClickCount() == 1) {
                        this.SelectedSymbol.addConnectionTo(e.getPoint());
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
                    this.RootSecStruc.reflectXY();
                    this.CenterDiagram();
                    this.repaint();
                }
                break;
        }
    }

    public void RotateX() {
        if (this.RootSecStruc != null) {
            this.RootSecStruc.rotateX();
            this.CenterDiagram();
            this.repaint();
        }
    }

    public void RotateY() {
        if (this.RootSecStruc != null) {
            this.RootSecStruc.rotateY();
            this.CenterDiagram();
            this.repaint();
        }
    }

    public void RotateZ() {
        if (this.RootSecStruc != null) {
            this.RootSecStruc.rotateZ();
            this.CenterDiagram();
            this.repaint();
        }
    }


    public void linearLayout(List<SecStrucElement> list, int minX, int maxX) {
        int numberOfSymbols = list.size();
        if (numberOfSymbols < 2)
            return;
        // first, sort the list by x-coordinate to ensure that we lay out in the
        // same order we started
        // SORT
        // now, find the average X-coordinate of the list
        int sumY = 0;
        Iterator<SecStrucElement> itr = list.iterator();
        SecStrucElement s;
        while (itr.hasNext()) {
            s = (SecStrucElement) itr.next();
            sumY += s.getPosition().y;
        }

        int averageY = (sumY / numberOfSymbols);
        int lineLength = maxX - minX;
        int separation = lineLength / numberOfSymbols;

        if (separation < this.minimumSeparation)
            separation = this.minimumSeparation;

        int xpos = minX;
        itr = list.iterator();
        while (itr.hasNext()) {
            s = (SecStrucElement) itr.next();
            s.placeElement(xpos, averageY);
            xpos += separation;
        }
        this.CenterDiagram();
    }

    public void circularLayout(List<SecStrucElement> list, Rectangle bounds) {
        int numberOfSymbols = list.size();
        if (numberOfSymbols < 3)
            return; // be ruth-less

        // SORT?

//        int sumX = 0;
//        int sumY = 0;

        SecStrucElement s;
        Iterator<SecStrucElement> itr = list.iterator();
        while (itr.hasNext()) {
            s = (SecStrucElement) itr.next();
//            Point pos = s.GetPosition();
//            sumX += pos.x;
//            sumY += pos.y;
        }

//        int averageX = (int) (sumX / numberOfSymbols);
//        int averageY = (int) (sumY / numberOfSymbols);
//        Point center = new Point(averageX, averageY);

        int minimumPerimiter = this.minimumSeparation * numberOfSymbols;
        int minimumRadius = (int) (minimumPerimiter / (2 * Math.PI));
        int boundsRadius = (Math.min(bounds.width, bounds.height) / 2);
        if (boundsRadius < minimumRadius)
            boundsRadius = minimumRadius;

        int startAngle = 0;
        int finishAngle = startAngle + 360;
        int angleIncrement = (360 / numberOfSymbols);

        itr = list.iterator();
        for (int angle = startAngle; angle < finishAngle; angle += angleIncrement) {
            if (!itr.hasNext())
                break;
            s = (SecStrucElement) itr.next();
            Point currentPoint = this.nextCirclePoint(boundsRadius, angle);
            s.placeElement(currentPoint.x, currentPoint.y);
        }
        this.CenterDiagram();
    }

    public Point nextCirclePoint(int radius, int angle) {
        int x = (int) (radius * Math.sin(angle));
        int y = (int) (radius * Math.cos(angle));
        return new Point(x, y);
    }

    /* END the MouseListener interface */

    /* START the MouseMotionListener interface */

    public void mouseDragged(MouseEvent e) {
        Point pos = e.getPoint();
        switch (this.EditMode) {

            case MOVE_STRAND_MODE:
            case MOVE_HELIX_MODE:
            case MOVE_SYMBOLS_MODE:
                if (this.SelectedSymbol != null) {
                    this.SelectedSymbol.setPosition(pos);
                    this.repaint();
                }
                break;

            case LINEAR_LAYOUT_MODE:
            case CIRCULAR_LAYOUT_MODE:
            case FLIP_MULTIPLE_MODE:
            case ADD_HBOND_MODE:
                this.updateSelectBox(pos);
                this.repaint();
                break;

            case MOVE_FIXEDS_MODE:
                if (this.SelectedSymbol != null) {
                    Point oldp = this.SelectedSymbol.getPosition();
                    Point newp = pos;
                    RootSecStruc.translateFixed(newp.x - oldp.x, newp.y - oldp.y);
                    this.repaint();
                }
                break;
        }
    }

    public void mouseMoved(MouseEvent e) {
    }

    /* END the MouseMotionListener interface */

    /* START methods used in editing with the mouse */

    public synchronized void startNewSelectBox(Point pos) {
        this.selectBox = new Rectangle(pos);
    }

    public synchronized void updateSelectBox(Point pos) {
        if (this.selectBox == null) {
            this.selectBox = new Rectangle(pos);
        }
        this.selectBox.add(pos);
        this.selectContained(this.selectBox, this.selectBoxList);
    }

    public synchronized void selectBoxDoAction() {
        switch (this.EditMode) {
            case FLIP_MULTIPLE_MODE:
                RootSecStruc.flipMultiple(this.selectBoxList);
                break;

            case LINEAR_LAYOUT_MODE:
                this.linearLayout(this.selectBoxList, (int) this.selectBox.getMinX(),
                        (int) this.selectBox.getMaxX());
                break;

            case CIRCULAR_LAYOUT_MODE:
                this.circularLayout(this.selectBoxList, this.selectBox);
                break;
        }
        // finished, so clean up
        this.selectBox = null;
        this.selectBoxList.clear();
        this.SelectedSymbol = this.RootSecStruc.getRoot();
    }

    public void addSymbol(SSEType type, String direction, int x, int y) {
        System.out.println("adding symbol : " + type + ", " + direction
                + " at (" + x + ", " + y + ")");
        this.SelectedSymbol = RootSecStruc.addSymbol(type, direction, x, y, SelectedSymbol);
        this.CenterDiagram();
        this.ScaleDiagram();
    }

    public synchronized void DeleteSelected() {
        RootSecStruc.delete(SelectedSymbol);
    }

    public synchronized void UnSelect() {
        System.out.println("selected symbol : " + this.SelectedSymbol
                + " unselected");
        this.SelectedSymbol = null;
    }

    public synchronized void selectContained(Rectangle r, List<SecStrucElement> list) {
        list.clear();
        list.addAll(RootSecStruc.selectContained(r));
    }

    public synchronized void SelectByPosition(Point p) {
        SecStrucElement selected = RootSecStruc.selectByPosition(p);

        if (selected != null) {
            System.out.println("selected : " + selected.toString());
            this.SelectedSymbol = selected;
        }
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
        RootSecStruc.applyScale(scale);
    }


    /**
     * this method sets up screen coordinates from the input ones it also sets
     * the scale
     * 
     * @param scale -
     *            the scaling value to apply for size
     */
    public synchronized void SetCanvasCoordinates(float scale) {
        this.setScale(scale);
        this.SetCanvasCoordinates();
    }

    /**
     * this method sets up screen coordinates from the input ones it uses the
     * current scale value
     */
    public synchronized void SetCanvasCoordinates() {
        this.ScaleDiagram();

        /*
         * inversion of y direction is required because the canvas coordinate
         * system changes the handedness of the input diagram
         */
        RootSecStruc.invertY();

        this.CenterDiagram();
    }

    /**
     * this method re-sets the coordinates to those used by the C code prior to
     * writing the file
     */
    public void SetCCodeCoordinates() {
        RootSecStruc.invertScale(getScale());
        RootSecStruc.invertY();
    }

    /**
     * a method to centre the diagram on the canvas
     */
    public void CenterDiagram() {
        Point cent = this.RootSecStruc.topsCentroid();
        RootSecStruc.translateDiagram(-cent.x + this.getSize().width / 2, -cent.y
                + this.getSize().height / 2);
        
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
            bb = this.RootSecStruc.topsBoundingBox();

            float s1 = cwidth / (bb.width);
            float s2 = cheight / (bb.height);
            s = Math.min(s1, s2);
            if (s > 1.0F)
                s = 1.0F;
        }

        return s;

    }

    /*
     * private method to get the position in diagram coordinates to refer to the
     * origin
     */
    /*
    private synchronized Point DiagramOrigin() {

        SecStrucElement s;
        int minx, miny;
        int x, y;
        Point p = new Point(0, 0);

        minx = Integer.MAX_VALUE;
        miny = Integer.MAX_VALUE;
        for (s = RootSecStruc; s != null; s = s.GetTo()) {
            if ((x = s.GetPosition().x) < minx) {
                minx = x;
                p.x = minx;
            }
            if ((y = s.GetPosition().y) < miny) {
                miny = y;
                p.y = miny;
            }
        }

        return p;

    }
    */
    /* END methods used in transformation between input and screen coordinates */

    /* START image handling methods */

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
            RootSecStruc.paint(g);
        if (this.InfoString != null)
            this.DrawInfoString(g);
        if (this.selectBox != null)
            this.drawMultipleSelectBox(g);
    }

    /* END paint and update methods */

    /* START private methods used by this class in painting/updating */

    private void drawMultipleSelectBox(Graphics g) {
        g.setColor(Color.gray);
        int x = (int) this.selectBox.getX();
        int y = (int) this.selectBox.getY();
        int width = (int) this.selectBox.getWidth();
        int height = (int) this.selectBox.getHeight();

        g.drawRect(x, y, width, height);
    }

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

    /* private method to draw a secondary structure */
    private void DrawSecStruc(SecStrucElement ss, Graphics gc) {

        int ScreenR;
        Point ScreenPos;
        Color c = ss.getColour();

        ScreenR = ss.getSymbolRadius();
        ScreenPos = ss.getPosition();

        if (ss.getType().equals("H")) {
            if ((ss == this.SelectedSymbol) || this.selectBoxList.contains(ss))
                c = this.selectedHelixColor;
            this.DrawHelix(ScreenPos.x, ScreenPos.y, ScreenR, c, gc);
        }

        if (ss.getType().equals("E")) {
            if ((ss == this.SelectedSymbol) || this.selectBoxList.contains(ss))
                c = this.selectedStrandColor;
            this.DrawStrand(ScreenPos.x, ScreenPos.y, ScreenR, ss.getDirection(), c, gc);
        }

        if ((ss.getType().equals("C")) || (ss.getType().equals("N"))) {
            if ((ss == this.SelectedSymbol) || this.selectBoxList.contains(ss))
                c = this.selectedTerminusColor;
            this.DrawTerminus(ScreenPos.x, ScreenPos.y, ScreenR, ss.getLabel(), c, gc);
        }

    }

    /* private method to draw a helix symbol (circle) */
    private void DrawHelix(int x, int y, int r, Color c, Graphics g) {

        int d = 2 * r;
        int ex = x - r;
        int ey = y - r;

        if (c == null)
            c = this.getBackground();

        g.setColor(c);
        g.fillOval(ex, ey, d, d);
        g.setColor(Color.black);
        g.drawOval(ex, ey, d, d);

    }

    /*
     * private method to draw a strand symbol (triangle, pointing up or down
     * according to strand direction )
     */
    private void DrawStrand(int x, int y, int r, String dir, Color c, Graphics g) {

        Polygon triangle = new Polygon();

        double pi6 = Math.PI / 6.0;
        double cospi6 = Math.cos(pi6);
        double sinpi6 = Math.sin(pi6);

        int rsinpi6 = (int) (r * sinpi6);
        int rcospi6 = (int) (r * cospi6);

        if (dir.equals("D")) {
            triangle.addPoint(x, y + r);
            triangle.addPoint(x - rcospi6, y - rsinpi6);
            triangle.addPoint(x + rcospi6, y - rsinpi6);
        } else {
            triangle.addPoint(x, y - r);
            triangle.addPoint(x - rcospi6, y + rsinpi6);
            triangle.addPoint(x + rcospi6, y + rsinpi6);
        }

        if (c == null)
            c = this.getBackground();

        g.setColor(c);
        g.fillPolygon(triangle);
        g.setColor(Color.black);
        g.drawPolygon(triangle);

    }

    /* private method to draw the symbol of an N or C terminus */
    private void DrawTerminus(int x, int y, int r, String lab, Color c,
            Graphics g) {

        int i;
        int FontHeight, StringWidth;
        FontMetrics fm;

        /* catch covers the situation for Xterms where fonts might be missing */
        try {
            g.setFont(this.FontsArr[0]);
            fm = g.getFontMetrics();
            FontHeight = fm.getHeight();
            StringWidth = fm.stringWidth(lab);

            for (i = 1; (i < this.FontsArr.length) && (this.FontsArr[i] != null)
                    && (Math.max(StringWidth, FontHeight) > r); i++) {
                g.setFont(this.FontsArr[i]);
                fm = g.getFontMetrics();
                FontHeight = fm.getHeight();
                StringWidth = fm.stringWidth(lab);
            }
        } catch (Exception e) {
            g.setFont(this.Font12);
            fm = g.getFontMetrics();
            FontHeight = fm.getHeight();
            StringWidth = fm.stringWidth(lab);
        }

        int cornerX = x - (StringWidth / 2);
        int cornerY = y + (FontHeight / 2);
        int boxSize = Math.max(StringWidth, FontHeight);

        if (c == null)
            c = this.getBackground();
        g.setColor(c);
        g.fillRect(x - (boxSize / 2), y - (boxSize / 2), boxSize, boxSize);

        g.setColor(Color.black);
        g.drawString(lab, cornerX, cornerY);
        g.drawRect(x - (boxSize / 2), y - (boxSize / 2), boxSize, boxSize);

    }

    /*
     * private method to draw the connection between two secondary structure
     * elements
     */
    private void DrawConnection(SecStrucElement s, SecStrucElement To, Object GraphicsOutput) {

        int FromScreenR, ToScreenR;

        if ((GraphicsOutput == null) || (s == null))
            return;

        if (To == null)
            return;

        /* Don't connect from a C terminus or to an N terminus */
        if (s.getType().equals("C") || To.getType().equals("N"))
            return;

        FromScreenR = s.getSymbolRadius();
//        size = s.GetFixNumRes();

        ToScreenR = To.getSymbolRadius();
//        size = To.GetFixNumRes();

        /*
         * in the case of no intervening connection points just join between the
         * two symbols
         */
        if (s.getConnectionTo().isEmpty()) {
            this.JoinPoints(s.getPosition(), s.getDirection(), s.getType(), FromScreenR, To
                    .getPosition(), To.getDirection(), To.getType(), ToScreenR,
                    GraphicsOutput);
        }
        /* the case where there are some intervening connection points */
        else {

            Iterator<Point> connectionEnum = s.getConnectionTo().iterator();
            Point pointTo = connectionEnum.next();

            this.JoinPoints(s.getPosition(), s.getDirection(), s.getType(), FromScreenR,
                    pointTo, "*", COIL, 0, GraphicsOutput);

            Point pointFrom;
            while (connectionEnum.hasNext()) {
                pointFrom = pointTo;
                pointTo = connectionEnum.next();
                this.JoinPoints(pointFrom, "*", COIL, 0, pointTo, "*", COIL, 0,
                        GraphicsOutput);
            }

            pointFrom = pointTo;
            this.JoinPoints(pointFrom, "*", COIL, 0, To.getPosition(), To.getDirection(),
                    To.getType(), ToScreenR, GraphicsOutput);

        }

    }

    /*
     * private method to draw the line connecting two secondary structure
     * symbols
     */
    /*
     * lines go from and the centre of the symbols except in certain cases when
     * they are drawn to/from the boundary
     */
    private void JoinPoints(Point p1, String Dir1, SSEType sseType, int Radius1,
            Point p2, String Dir2, SSEType sseType2, int Radius2,
            Object GraphicsOutput) {

        Point To, From;

        /*
         * draw from border rather than centre if direction is down (D) or if
         * Type is N or C
         */
        if (Dir1.equals("D") || sseType == CTERMINUS || sseType == NTERMINUS) {
            if (sseType == SSEType.EXTENDED) {
                From = this.DownTriangleBorder(p1, p2, Radius1);
            } else {
                From = this.CircleBorder(p1, p2, Radius1);
            }
        } else {
            From = p1;
        }

        /*
         * draw to border rather than centre if direction is up (U) or if Type
         * is N or C
         */
        if (Dir2.equals("U") || sseType2.equals("C") || sseType2.equals("N")) {
            if (sseType2.equals("E")) {
                To = this.UpTriangleBorder(p2, p1, Radius2);
            } else {
                To = this.CircleBorder(p2, p1, Radius2);
            }
        } else {
            To = p2;
        }

        // at this point the GraphicsOutput is either a Graphics
        // or a Vector to which postscript should be written
        if (GraphicsOutput instanceof Graphics) {
            Graphics gc = (Graphics) GraphicsOutput;
            gc.drawLine(From.x, From.y, To.x, To.y);
        }
        /*
         * else if ( GraphicsOutput instanceof Vector ) { Vector ps = (Vector)
         * GraphicsOutput; if ( start_new_connection ) { ps.addElement(
         * PostscriptFactory.makeMove(From.x, getSize().height-From.y) );
         * start_new_connection = false; } ps.addElement(
         * PostscriptFactory.makeLine(To.x, getSize().height - To.y) ); }
         */

    }

    /*
     * private method calculates the point on a circle of centre p1 and radius r
     * which lies on the line p1->p2
     */
    private Point CircleBorder(Point p1, Point p2, int r) {

        double xb, yb;
        double x1, y1, x2, y2;

        x1 = p1.x;
        y1 = p1.y;
        x2 = p2.x;
        y2 = p2.y;

        double s = this.Separation(p1, p2);

        if ((s < r) || (r <= 0.0))
            return p1;

        xb = x1 + (r / s) * (x2 - x1);
        yb = y1 + (r / s) * (y2 - y1);

        return new Point((int) xb, (int) yb);

    }

    /*
     * private method calculates the point on the border of a down equilateral
     * triangle centered at p1 and with radius (of the circumscribing circle) r,
     * which lies on the line p1->p2
     */
    private Point DownTriangleBorder(Point p1, Point p2, int r) {

        double xb, yb;
        double x1, y1, x2, y2;

        double theta = 0.0, gamma = 0.0;

        double l;

        double pi = Math.PI;
        double pi2 = Math.PI / 2.0;
        double pi6 = Math.PI / 6.0;

        x1 = p1.x;
        y1 = p1.y;
        x2 = p2.x;
        y2 = p2.y;

        double s = this.Separation(p1, p2);

        /* theta in range -PI < theta <= PI */
        theta = Math.atan2(y2 - y1, x2 - x1);

        if ((-pi6 < theta) && (theta <= pi2)) {
            gamma = theta - pi6;
        } else if ((pi2 < theta) && (theta <= pi)) {
            gamma = theta - 5.0 * pi6;
        } else if ((-pi < theta) && (theta <= -5.0 * pi6)) {
            gamma = 7.0 * pi6 + theta;
        } else if ((-5.0 * pi6 < theta) && (theta <= -pi6)) {
            gamma = theta + pi2;
        }

        l = (r) / (2.0 * Math.cos(gamma));

        xb = x1 + (x2 - x1) * l / s;
        yb = y1 + (y2 - y1) * l / s;

        return new Point((int) xb, (int) yb);

    }

    /*
     * private method calculates the point on the border of an up equilateral
     * triangle centered at p1 and with radius (of the circumscribing circle) r,
     * which lies on the line p1->p2
     */
    private Point UpTriangleBorder(Point p1, Point p2, int r) {

        double xb, yb;
        double x1, y1, x2, y2;

        double theta = 0.0, gamma = 0.0;

        double l;

        double pi = Math.PI;
        double pi2 = Math.PI / 2.0;
        double pi6 = Math.PI / 6.0;

        x1 = p1.x;
        y1 = p1.y;
        x2 = p2.x;
        y2 = p2.y;

        double s = this.Separation(p1, p2);

        /* theta in range -PI < theta <= PI */
        theta = Math.atan2(y2 - y1, x2 - x1);

        if ((-pi2 < theta) && (theta <= pi6)) {
            gamma = theta + pi6;
        } else if ((pi6 < theta) && (theta <= 5.0 * pi6)) {
            gamma = theta - pi2;
        } else if ((5.0 * pi6 < theta) && (theta <= pi)) {
            gamma = 7.0 * pi6 - theta;
        } else if ((-pi < theta) && (theta <= -pi2)) {
            gamma = theta + 5.0 * pi6;
        }

        l = (r) / (2.0 * Math.cos(gamma));

        xb = x1 + (x2 - x1) * l / s;
        yb = y1 + (y2 - y1) * l / s;

        return new Point((int) xb, (int) yb);

    }

    /* private method calculates the separation of two points */
    /* one day I'll put it in a more sensible place for re-use */
    private double Separation(Point p1, Point p2) {

        int sep;
        double fsep;

        sep = (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y);
        fsep = sep;

        fsep = Math.sqrt(fsep);

        return fsep;

    }

    /* END private methods used by this class in painting/updating */

}
