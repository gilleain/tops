package tops.web.display.applet;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import tops.dw.protein.*;

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

    // static final int INFO_MODE = 0; //don't need
    // static final int COLOUR_SYMBOLS_MODE = 1; //don't need
    static final int MOVE_SYMBOLS_MODE = 2;

    static final int MOVE_FIXEDS_MODE = 3;

    static final int REDRAW_CONNECTIONS_MODE = 4;

    static final int DELETE_SYMBOLS_MODE = 5;

    static final int ROTATE_X_MODE = 6;

    static final int ROTATE_Y_MODE = 7;

    static final int ROTATE_Z_MODE = 8;

    static final int REFLECT_XY_MODE = 9;

    // static final int TOGGLE_SIZE_DISPLAY = 10;//don't need
    // static final int ADD_USER_LABEL = 11; //don't need
    // static final int DELETE_USER_LABEL = 12; //don't need
    // static final int MOVE_USER_LABEL = 13; //don't need
    // static final int ADD_USER_ARROW = 14; //don't need
    // static final int DELETE_USER_ARROW = 15; //don't need
    // static final int ALIGN_X_MODE = 16; //don't need
    // static final int ALIGN_Y_MODE = 17; //don't need
    /* *************NEW!************** */
    static final int ADD_STRAND_MODE = 18;

    static final int ADD_HELIX_MODE = 19;

    static final int FLIP_MODE = 20; // woo-ha, got'cha'll in check

    static final int SELECT_SYMBOL_MODE = 21;

    static final int MOVE_STRAND_MODE = 22; // ok, this is annoying, but
                                            // essential

    static final int MOVE_HELIX_MODE = 23; // ok, this is annoying, but
                                            // essential

    /* Multiple Select Modes */
    static final int ADD_HBOND_MODE = 24;

    static final int LINEAR_LAYOUT_MODE = 25;

    static final int CIRCULAR_LAYOUT_MODE = 26;

    static final int FLIP_MULTIPLE_MODE = 27;

    /* END of class variables */

    /* START private instance variables */

    private SecStrucElement RootSecStruc = null;

    private String Label = "Tops diagram";

    private SecStrucElement SelectedSymbol;

    private Rectangle selectBox;

    private ArrayList<SecStrucElement> selectBoxList;

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

    /* END of private instance variables */

    /* START Constructors */
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

    /**
     * @param sse
     *            The SecStructElement serving as linked list root for the Tops
     *            diagram
     */
    public TopsDrawCanvas(SecStrucElement sse) {
        this();
        this.RootSecStruc = sse;
//        SizeDisplay = true;
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
        if (this.RootSecStruc == null)
            return new String();

        StringBuffer topsString = new StringBuffer();
        for (SecStrucElement s = this.RootSecStruc; s != null; s = s.GetTo()) {
            char type = s.Type.charAt(0);
            type = (s.Direction.equals("D")) ? Character.toLowerCase(type)
                    : type;
            topsString.append(type);
        }
        return topsString.toString();
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
                this.addSymbol("E", "U", pos.x, pos.y);
                this.repaint();
                this.setEditMode(TopsDrawCanvas.MOVE_STRAND_MODE);
                break;

            case ADD_HELIX_MODE:
                this.addSymbol("H", "U", pos.x, pos.y);
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
                this.flip(this.SelectedSymbol);
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
                this.SelectedSymbol = this.RootSecStruc;
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
                    this.CenterDiagram();
                    this.repaint();
                }
                break;
        }
    }

    public void RotateX() {
        if (this.RootSecStruc != null) {
            this.RootSecStruc.RotateX();
            this.CenterDiagram();
            this.repaint();
        }
    }

    public void RotateY() {
        if (this.RootSecStruc != null) {
            this.RootSecStruc.RotateY();
            this.CenterDiagram();
            this.repaint();
        }
    }

    public void RotateZ() {
        if (this.RootSecStruc != null) {
            this.RootSecStruc.RotateZ();
            this.CenterDiagram();
            this.repaint();
        }
    }

    public void flip(SecStrucElement s) {
        if (s.Direction.equals("U"))
            s.Direction = "D";
        else if (s.Direction.equals("D"))
            s.Direction = "U";
        this.repaint();
    }

    public void flipMultiple(ArrayList<SecStrucElement> list) {
        Iterator<SecStrucElement> itr = list.iterator();
        while (itr.hasNext()) {
            SecStrucElement s = (SecStrucElement) itr.next();
            this.flip(s);
        }
    }

    public void linearLayout(ArrayList<SecStrucElement> list, int minX, int maxX) {
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
            sumY += s.GetPosition().y;
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
            s.PlaceElement(xpos, averageY);
            xpos += separation;
        }
        this.CenterDiagram();
    }

    public void circularLayout(ArrayList<SecStrucElement> list, Rectangle bounds) {
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
            s.PlaceElement(currentPoint.x, currentPoint.y);
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
                    this.SelectedSymbol.SetPosition(pos);
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
                    Point oldp = this.SelectedSymbol.GetPosition();
                    Point newp = pos;
                    this.SelectedSymbol.TranslateFixed(newp.x - oldp.x, newp.y
                            - oldp.y);
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
                this.flipMultiple(this.selectBoxList);
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
        this.SelectedSymbol = this.RootSecStruc;
    }

    public synchronized void addSymbol(String type, String direction, int x,
            int y) {
        int defaultSeparation = 30; // ARBITRARY!
        int defaultRadius = 10; // ARBITRARY!
        System.out.println("adding symbol : " + type + ", " + direction
                + " at (" + x + ", " + y + ")");

        SecStrucElement newSSE = new SecStrucElement();
        newSSE.Type = type;
        newSSE.Direction = direction;
        newSSE.PlaceElement(x, y);
        newSSE.SetSymbolRadius(defaultRadius);

        if (this.RootSecStruc == null) {
            // make N and C terminii
            SecStrucElement nTerminus = new SecStrucElement();
            nTerminus.Type = "N";
            nTerminus.Direction = "U";
            nTerminus.Label = "N";
            nTerminus.PlaceElement(x - defaultSeparation, y); // ARBITRARY!
            nTerminus.SetSymbolRadius(defaultRadius);
            nTerminus.SetTo(newSSE);

            newSSE.SetFrom(nTerminus);

            SecStrucElement cTerminus = new SecStrucElement();
            cTerminus.Type = "C";
            cTerminus.Direction = "U";
            cTerminus.Label = "C";
            cTerminus.PlaceElement(x + defaultSeparation, y); // ARBITRARY!
            cTerminus.SetSymbolRadius(defaultRadius);
            cTerminus.SetFrom(newSSE);

            newSSE.SetTo(cTerminus);

            this.RootSecStruc = nTerminus;
        } else {
            // deal with the C-terminus in a special way - add the new SSE
            // _before_ it
            if (this.SelectedSymbol.Type.equals("C")) {
                newSSE.SetTo(this.SelectedSymbol);
                this.SelectedSymbol.SetFrom(newSSE);
                newSSE.SetFrom(this.SelectedSymbol.GetFrom());
                this.SelectedSymbol.GetFrom().SetTo(newSSE);
            } else {
                this.SelectedSymbol.GetTo().SetFrom(newSSE);
                newSSE.SetTo(this.SelectedSymbol.GetTo());
                newSSE.SetFrom(this.SelectedSymbol);
                this.SelectedSymbol.SetTo(newSSE);
            }
        }

        this.SelectedSymbol = newSSE;
        this.CenterDiagram();
        this.ScaleDiagram();
    }

    public synchronized void DeleteSelected() {
        // can't delete terminii!
        if ((this.SelectedSymbol.Type.equals("N"))
                || (this.SelectedSymbol.Type.equals("C"))) {
            System.err.println("can't delete terminii!");
            return;
        }

        // delete terminii if we delete the final symbol
        if ((this.SelectedSymbol.GetFrom().Type.equals("N"))
                && (this.SelectedSymbol.GetTo().Type.equals("C"))) {
            this.RootSecStruc = null;
            return;
        }

        if (this.SelectedSymbol != null) {
            this.RootSecStruc = this.SelectedSymbol.Delete();
        }
    }

    public synchronized void UnSelect() {
        System.out.println("selected symbol : " + this.SelectedSymbol
                + " unselected");
        this.SelectedSymbol = null;
    }

    public synchronized void selectContained(Rectangle r, ArrayList<SecStrucElement> list) {
        list.clear();

        for (SecStrucElement s = this.RootSecStruc; s != null; s = s.GetTo()) {
            Point pos = s.GetPosition();
            if (r.contains(pos))
                list.add(s);
        }
    }

    public synchronized void SelectByPosition(Point p) {

        SecStrucElement selected = null;

        if (p != null) {

            SecStrucElement s;
            double minsep = Double.POSITIVE_INFINITY;
            double sep;
            Point ps;

            for (s = this.RootSecStruc; s != null; s = s.GetTo()) {
                ps = s.GetPosition();
                sep = this.Separation(p, ps);
                if ((sep < s.GetSymbolRadius()) && (sep < minsep)) {
                    minsep = sep;
                    selected = s;
                }
            }
        }

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
        this.ApplyScale(scale);
    }

    /**
     * this methods applies the inverse of the scale to the diagram, it does not
     * do a repaint
     */
    public synchronized void InvertScale() {
        float scale = this.getScale();
        if (scale > 0.0)
            scale = 1.0F / scale;
        else
            scale = 1.0F;
        this.ApplyScale(scale);
    }

    /* private method to apply a scaling value to the diagram */
    public synchronized void ApplyScale(float scale) {

        SecStrucElement s;
        Point p;
        int x, y, r;
        Vector<Point> conns;
        Enumeration<Point> en;

        for (s = this.RootSecStruc; s != null; s = s.GetTo()) {

            p = s.GetPosition();
            x = p.x;
            y = p.y;
            x = Math.round(scale * x);
            y = Math.round(scale * y);
            p.x = x;
            p.y = y;

            r = s.GetSymbolRadius();
            s.SetSymbolRadius(Math.round(r * scale));

            if ((conns = s.GetConnectionTo()) != null) {
                en = conns.elements();
                while (en.hasMoreElements()) {
                    p = en.nextElement();
                    x = p.x;
                    y = p.y;
                    x = Math.round(scale * x);
                    y = Math.round(scale * y);
                    p.x = x;
                    p.y = y;
                }
            }

        }

    }

    /**
     * this method translates the diagram, it does not do a repaint
     * 
     * @param x -
     *            the x translation to apply
     * @param y -
     *            the y translation to apply
     */
    public synchronized void TranslateDiagram(int x, int y) {
        SecStrucElement s;
        Vector<Point> conns;
        Enumeration<Point> en;
        Point p;

        for (s = this.RootSecStruc; s != null; s = s.GetTo()) {
            s.GetPosition().x += x;
            s.GetPosition().y += y;

            if ((conns = s.GetConnectionTo()) != null) {
                en = conns.elements();
                while (en.hasMoreElements()) {
                    p = (Point) en.nextElement();
                    p.x += x;
                    p.y += y;
                }
            }

        }

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
        this.InvertY();

        this.CenterDiagram();
    }

    /* private method to invert the diagrams Y coordinates */
    private synchronized void InvertY() {
    	Vector<Point> conns;
        Enumeration<Point> en;
        Point p;
        SecStrucElement s;

        for (s = this.RootSecStruc; s != null; s = s.GetTo()) {

            s.GetPosition().y *= -1;

            if ((conns = s.GetConnectionTo()) != null) {
                en = conns.elements();
                while (en.hasMoreElements()) {
                    p = (Point) en.nextElement();
                    p.y *= -1;
                }
            }
        }

    }

    /**
     * this method re-sets the coordinates to those used by the C code prior to
     * writing the file
     */
    public void SetCCodeCoordinates() {
        this.InvertScale();
        this.InvertY();
    }

    /**
     * a method to centre the diagram on the canvas
     */
    public void CenterDiagram() {

        try {
            Point cent = this.RootSecStruc.TopsCentroid();
            this.TranslateDiagram(-cent.x + this.getSize().width / 2, -cent.y
                    + this.getSize().height / 2);
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
            this.DrawTopsDiagram(g);
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

    /* this private method draws a TOPS diagram, symbols first then connections */
    private void DrawTopsDiagram(Graphics g) {

        SecStrucElement s;

        for (s = this.RootSecStruc; s != null; s = s.GetTo()) {
            this.DrawSecStruc(s, g);
        }

        for (s = this.RootSecStruc; (s != null) && (s.GetTo() != null); s = s
                .GetTo()) {
            this.DrawConnection(s, g);
        }

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

        ScreenR = ss.GetSymbolRadius();
        ScreenPos = ss.GetPosition();

        if (ss.Type.equals("H")) {
            if ((ss == this.SelectedSymbol) || this.selectBoxList.contains(ss))
                c = this.selectedHelixColor;
            this.DrawHelix(ScreenPos.x, ScreenPos.y, ScreenR, c, gc);
        }

        if (ss.Type.equals("E")) {
            if ((ss == this.SelectedSymbol) || this.selectBoxList.contains(ss))
                c = this.selectedStrandColor;
            this.DrawStrand(ScreenPos.x, ScreenPos.y, ScreenR, ss.Direction, c, gc);
        }

        if ((ss.Type.equals("C")) || (ss.Type.equals("N"))) {
            if ((ss == this.SelectedSymbol) || this.selectBoxList.contains(ss))
                c = this.selectedTerminusColor;
            this.DrawTerminus(ScreenPos.x, ScreenPos.y, ScreenR, ss.Label, c, gc);
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
    private void DrawConnection(SecStrucElement s, Object GraphicsOutput) {

        SecStrucElement To;
        int FromScreenR, ToScreenR;

        if ((GraphicsOutput == null) || (s == null))
            return;

        To = s.GetTo();
        if (To == null)
            return;

        /* Don't connect from a C terminus or to an N terminus */
        if (s.Type.equals("C") || To.Type.equals("N"))
            return;

        FromScreenR = s.GetSymbolRadius();
//        size = s.GetFixNumRes();

        ToScreenR = To.GetSymbolRadius();
//        size = To.GetFixNumRes();

        /*
         * in the case of no intervening connection points just join between the
         * two symbols
         */
        if (s.GetConnectionTo().isEmpty()) {
            this.JoinPoints(s.GetPosition(), s.Direction, s.Type, FromScreenR, To
                    .GetPosition(), To.Direction, To.Type, ToScreenR,
                    GraphicsOutput);
        }
        /* the case where there are some intervening connection points */
        else {

            Enumeration<Point> ConnectionEnum = s.GetConnectionTo().elements();
            Point PointTo = ConnectionEnum.nextElement();

            this.JoinPoints(s.GetPosition(), s.Direction, s.Type, FromScreenR,
                    PointTo, "*", "*", 0, GraphicsOutput);

            Point PointFrom;
            while (ConnectionEnum.hasMoreElements()) {
                PointFrom = PointTo;
                PointTo = (Point) ConnectionEnum.nextElement();
                this.JoinPoints(PointFrom, "*", "*", 0, PointTo, "*", "*", 0,
                        GraphicsOutput);
            }

            PointFrom = PointTo;
            this.JoinPoints(PointFrom, "*", "*", 0, To.GetPosition(), To.Direction,
                    To.Type, ToScreenR, GraphicsOutput);

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
    private void JoinPoints(Point p1, String Dir1, String Type1, int Radius1,
            Point p2, String Dir2, String Type2, int Radius2,
            Object GraphicsOutput) {

        Point To, From;

        /*
         * draw from border rather than centre if direction is down (D) or if
         * Type is N or C
         */
        if (Dir1.equals("D") || Type1.equals("C") || Type1.equals("N")) {
            if (Type1.equals("E")) {
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
        if (Dir2.equals("U") || Type2.equals("C") || Type2.equals("N")) {
            if (Type2.equals("E")) {
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