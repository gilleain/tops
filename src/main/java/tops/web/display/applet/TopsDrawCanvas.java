package tops.web.display.applet;

import static tops.port.model.Direction.UP;
import static tops.port.model.SSEType.EXTENDED;
import static tops.port.model.SSEType.HELIX;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import tops.dw.protein.Cartoon;
import tops.dw.protein.SecStrucElement;
import tops.port.model.Direction;
import tops.port.model.SSEType;
import tops.view.cartoon.CartoonDrawer;

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

    private Cartoon cartoon = null;

    private String label = "Tops diagram";

    private SecStrucElement selectedSymbol;

    private Rectangle selectBox;

    private List<SecStrucElement> selectBoxList;

    private int minimumSeparation = 10;;

    private Float scale = new Float(1.0);

    private String infoString = null;

    private Point infoStringPos = new Point();

    private int editMode;

//    private Color CurrentColour = Color.white;

    private Color selectedStrandColor = Color.yellow;

    private Color selectedHelixColor = Color.pink;

    private Color selectedTerminusColor = Color.green;

//    private boolean SizeDisplay = true;

    private Dimension offDimension = new Dimension();

    private Image offScreenImage = null;

    private Graphics offGraphics = null;

    private Font[] fontsArr = new Font[10];
    
    private Font font18;
    
    private Font font12;

    private boolean useBorder = true;
    
    private CartoonDrawer drawer; 

    public TopsDrawCanvas() {
        drawer = new CartoonDrawer();
        this.setBackground(Color.white);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);

        this.editMode = TopsDrawCanvas.ADD_STRAND_MODE;

        Dimension ps = this.getPreferredSize();
        this.setSize(ps.width, ps.height);

        int fs, i;
        for (fs = 48, i = 0; (fs > 11) && i < this.fontsArr.length; fs -= 4, i++) {
            this.fontsArr[i] = new Font("TimesRoman", Font.PLAIN, fs);
        }
        this.font18 = new Font("TimesRoman", Font.PLAIN, 18);
        this.font12 = new Font("TimesRoman", Font.PLAIN, 12);

//        SizeDisplay = true;

        this.selectBoxList = new ArrayList<SecStrucElement>();
    }
    
    public String getEPS() throws IOException {
        StringWriter stringWriter = new StringWriter();
        drawer.draw("", "PS", cartoon, new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
    

    /**
     * @param sse
     *            The SecStructElement serving as linked list root for the Tops
     *            diagram
     */
    public TopsDrawCanvas(Cartoon cartoon) {
        this();
        this.cartoon = cartoon;
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
        this.label = lab;
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
        return this.label;
    }

    /**
     * @param l
     *            the Label for the diagram
     */
    public synchronized void setLabel(String l) {
        this.label = l;
        this.repaint();
    }

    /**
     * @param use -
     *            sets whether a border should be drawn or not
     */
    public synchronized void setUseBorder(boolean use) {
        this.useBorder = use;
    }

    /* RootSecStruc - not bound property */
    /**
     * @param Root -
     *            the root secondary structure for the diagram
     */
    public synchronized void setRootSecStruc(SecStrucElement Root) {
        this.cartoon = new Cartoon(Root);
        this.repaint();
    }


    /* the Scale - a bound property */
    /**
     * @param scale -
     *            the scaling value to apply to get from input coordinates to
     *            screen coordinates
     */
    public synchronized void setScale(float scale) {
//        Float oldf = Scale;
        this.scale = new Float(scale);
        this.repaint();
    }

    /**
     * @return - the scaling value to apply to get from input coordinates to
     *         screen coordinates
     */
    public synchronized float getScale() {
        return this.scale.floatValue();
    }

    /**
     * sets the editor mode for mouse operations
     * 
     * @param em
     *            the required editor mode (see class variables *_MODE)
     */
    public synchronized void setEditMode(int em) {
        System.out.println("setting edit mode : " + em);
        this.editMode = em;
        // UnSelect();
    }

    /**
     * returns the current editor mode
     */
    public synchronized int getEditMode() {
        return this.editMode;
    }

    /**
     * sets the information string
     * 
     * @param s -
     *            the new information string
     */
    public void setInfoString(String s) {
        this.infoString = s;
    }

    /**
     * returns the current information string
     */
    public String getInfoString() {
        return this.infoString;
    }

    /**
     * sets the information string position
     */
    public void setInfoStringPos(Point p) {
        this.infoStringPos.x = p.x;
        this.infoStringPos.y = p.y;
    }

    /**
     * returns the information string position
     */
    public Point getInfoStringPos() {
        return this.infoStringPos;
    }

    /**
     * deletes the current diagram
     */
    public void clear() {
        this.cartoon = null;
        this.repaint();
    }

    public String convertStructureToString() {
        return cartoon.convertStructureToString();
    }

    /* END of methods to get and set properties */

    /* START the MouseListener interface */

    public void mousePressed(MouseEvent e) {
        Point pos = e.getPoint();
        System.out.println("currently selected symbol = " + this.selectedSymbol);

        switch (this.editMode) {

            case SELECT_SYMBOL_MODE:
                this.selectByPosition(pos);
                this.repaint();
                break;

            case FLIP_MODE: // woo-ha!
                this.selectByPosition(pos);
                this.repaint();
                break;

            case ADD_STRAND_MODE:
                this.addSymbol(EXTENDED, UP, pos.x, pos.y);
                this.repaint();
                this.setEditMode(TopsDrawCanvas.MOVE_STRAND_MODE);
                break;

            case ADD_HELIX_MODE:
                this.addSymbol(HELIX, UP, pos.x, pos.y);
                this.repaint();
                this.setEditMode(TopsDrawCanvas.MOVE_HELIX_MODE);
                break;

            case ADD_HBOND_MODE:
            case LINEAR_LAYOUT_MODE:
            case CIRCULAR_LAYOUT_MODE:
            case FLIP_MULTIPLE_MODE:
                this.unSelect();
                this.startNewSelectBox(pos);
                this.repaint();
                break;

            case MOVE_SYMBOLS_MODE:
                this.selectByPosition(pos);
                this.repaint();
                break;

            case MOVE_FIXEDS_MODE:
                this.selectByPosition(pos);
                this.repaint();
                break;

            case DELETE_SYMBOLS_MODE:
                this.selectByPosition(pos);
                this.repaint();
                break;
        }
    }

    public void mouseReleased(MouseEvent e) {

        switch (this.editMode) {

            case FLIP_MODE:
                cartoon.flip(this.selectedSymbol);
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
                this.deleteSelected();
                // UnSelect();
                this.selectedSymbol = null;
                this.repaint();
                break;
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
        switch (this.editMode) {
            /*
             * case DELETE_SYMBOLS_MODE: SelectByPosition( e.getPoint() );
             * DeleteSelected(); //UnSelect(); SelectedSymbol = RootSecStruc;
             * repaint(); break;
             */

            case REDRAW_CONNECTIONS_MODE:
                if (this.selectedSymbol == null) {
                    this.selectByPosition(e.getPoint());
                    if (this.selectedSymbol != null) {
                        this.selectedSymbol.clearConnectionTo();
                    }
                } else {
                    /*
                     * NB for double clicks two events arrive, one with
                     * ClickCount = 1 the other with ClickCount = 2
                     */
                    if (e.getClickCount() == 1) {
                        this.selectedSymbol.addConnectionTo(e.getPoint());
                        this.repaint();
                    } else {
                        this.unSelect();
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
                if (this.cartoon != null) {
                    this.cartoon.reflectXY();
                    this.centerDiagram();
                    this.repaint();
                }
                break;
        }
    }

    public void RotateX() {
        if (this.cartoon != null) {
            this.cartoon.rotateX();
            this.centerDiagram();
            this.repaint();
        }
    }

    public void RotateY() {
        if (this.cartoon != null) {
            this.cartoon.rotateY();
            this.centerDiagram();
            this.repaint();
        }
    }

    public void RotateZ() {
        if (this.cartoon != null) {
            this.cartoon.rotateZ();
            this.centerDiagram();
            this.repaint();
        }
    }

    /* END the MouseListener interface */

    /* START the MouseMotionListener interface */

    public void mouseDragged(MouseEvent e) {
        Point pos = e.getPoint();
        switch (this.editMode) {

            case MOVE_STRAND_MODE:
            case MOVE_HELIX_MODE:
            case MOVE_SYMBOLS_MODE:
                if (this.selectedSymbol != null) {
                    this.selectedSymbol.setPosition(pos);
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
                if (this.selectedSymbol != null) {
                    Point oldp = this.selectedSymbol.getPosition();
                    Point newp = pos;
                    cartoon.translateFixed(newp.x - oldp.x, newp.y - oldp.y);
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
        switch (this.editMode) {
            case FLIP_MULTIPLE_MODE:
                cartoon.flipMultiple(selectBoxList);
                break;

            case LINEAR_LAYOUT_MODE:
                new LinearLayout(minimumSeparation).layout(selectBoxList, selectBox);
                this.centerDiagram();
                break;

            case CIRCULAR_LAYOUT_MODE:
                new CircularLayout(minimumSeparation).layout(selectBoxList, selectBox);
                this.centerDiagram();
                break;
        }
        // finished, so clean up
        this.selectBox = null;
        this.selectBoxList.clear();
        this.selectedSymbol = null;
    }

    public void addSymbol(SSEType type, Direction direction, int x, int y) {
        System.out.println("adding symbol : " + type + ", " + direction
                + " at (" + x + ", " + y + ")");
        this.selectedSymbol = cartoon.addSymbol(type, direction, x, y, selectedSymbol);
        this.centerDiagram();
        this.scaleDiagram();
    }

    public void deleteSelected() {
        cartoon.delete(selectedSymbol);
    }

    public void unSelect() {
        System.out.println("selected symbol : " + this.selectedSymbol
                + " unselected");
        this.selectedSymbol = null;
    }

    public void selectContained(Rectangle r, List<SecStrucElement> list) {
        list.clear();
        list.addAll(cartoon.selectContained(r));
    }

    public void selectByPosition(Point p) {
        SecStrucElement selected = cartoon.selectByPosition(p);

        if (selected != null) {
            System.out.println("selected : " + selected.toString());
            this.selectedSymbol = selected;
        }
    }

    /* END methods used in editing with the mouse */

    /* START methods used in transformation between input and screen coordinates */

    /**
     * this method applies the current scale value to the diagram, it does not
     * do a repaint
     */
    public synchronized void scaleDiagram() {
        float scale = this.getScale();
        if (scale <= 0.0)
            scale = 1.0F;
        cartoon.applyScale(scale);
    }


    /**
     * this method sets up screen coordinates from the input ones it also sets
     * the scale
     * 
     * @param scale -
     *            the scaling value to apply for size
     */
    public synchronized void setCanvasCoordinates(float scale) {
        this.setScale(scale);
        this.setCanvasCoordinates();
    }

    /**
     * this method sets up screen coordinates from the input ones it uses the
     * current scale value
     */
    public synchronized void setCanvasCoordinates() {
        this.scaleDiagram();

        /*
         * inversion of y direction is required because the canvas coordinate
         * system changes the handedness of the input diagram
         */
        cartoon.invertY();

        this.centerDiagram();
    }

    /**
     * this method re-sets the coordinates to those used by the C code prior to
     * writing the file
     */
    public void setCCodeCoordinates() {
        cartoon.invertScale(getScale());
        cartoon.invertY();
    }

    /**
     * a method to centre the diagram on the canvas
     */
    public void centerDiagram() {
        Point cent = this.cartoon.topsCentroid();
        cartoon.translateDiagram(-cent.x + this.getSize().width / 2, -cent.y
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

        if (this.cartoon != null) {
            bb = this.cartoon.boundingBox();

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

        if ((this.offScreenImage == null) || (this.offDimension.width != w)
                || (this.offDimension.height != h)) {
            this.offScreenImage = this.createImage(w, h);
            this.offDimension.width = w;
            this.offDimension.height = h;
            this.offGraphics = this.offScreenImage.getGraphics();
        }

        this.offGraphics.setColor(this.getBackground());
        this.offGraphics.fillRect(0, 0, w, h);

        this.paint(this.offGraphics);

        g.drawImage(this.offScreenImage, 0, 0, this);

    }

    @Override
    public void paint(Graphics g) {
        g.setColor(Color.black);
        if (this.useBorder)
            this.DrawBorder(g);
        if (this.label != null)
            this.DrawLabel(g);
        
        drawer.draw(g, getWidth(), getHeight(), cartoon);
        
        if (this.infoString != null)
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
        g.setFont(this.font12);
        g.setColor(Color.black);
        g.drawString(this.infoString, this.infoStringPos.x, this.infoStringPos.y);
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

        if (this.label != null) {
            x = this.getSize().width / 2;
            y = TopsDrawCanvas.BORDER / 2;
            g.setFont(this.font18);
            x -= (g.getFontMetrics().stringWidth(this.label)) / 2;
            g.drawString(this.label, x, y);
        }
    }

    public Cartoon getCartoon() {
        return cartoon;
    }

}
