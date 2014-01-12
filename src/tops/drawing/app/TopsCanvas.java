package tops.drawing.app;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Stack;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.RepaintManager;

import tops.drawing.Cartoon;
import tops.drawing.symbols.CartoonConnector;
import tops.drawing.symbols.SSESymbol;


public class TopsCanvas extends JComponent implements Printable, MouseListener, MouseMotionListener { 

    // Activities
    public static final int SELECT           = 0;
    public static final int UNDO             = 1;
    public static final int DELETE           = 2;
    public static final int CLEAR            = 3;
    public static final int ZOOM_IN          = 4;
    public static final int ZOOM_OUT         = 5;
    public static final int SUBMIT           = 6;
    
    // Symbols
    public static final int STRAND_UP        = 7;
    public static final int STRAND_DOWN      = 8;
    public static final int HELIX_UP         = 9;
    public static final int HELIX_DOWN       = 10;
    public static final int TEMPLATE         = 11;
    
    // Arcs
    public static final int H_BOND           = 12;
    public static final int RIGHT_ARC        = 13;
    public static final int LEFT_ARC         = 14;
    public static final int RANGE            = 15;
    
    // Flips and Align
    public static final int FLIP             = 16;
    public static final int HORIZONTAL_ALIGN = 17;
    public static final int VERTICAL_ALIGN   = 18;
    public static final int FLIP_X           = 19;
    public static final int FLIP_Y           = 20;
    
    // various constants
    private static double SCALE_FACTOR = 0.1;
    
    private SSESymbol currentlyDraggedSymbol = null;
    private int state;
    
    private SSESymbol first_bond_figure = null;
    private boolean dragging = false;
    private boolean highlightFlag = false;
    
    private TopsEditor parentPanel;
    private Stack<UndoEvent> undoStack;       
    private Cartoon cartoon;
    private double scale_factor;
    private Dimension initialCanvasSize = new Dimension(400, 400);
    
    private Cursor[] cursors;

    public TopsCanvas( TopsEditor parentPanel) {
        this.parentPanel = parentPanel;

        this.cartoon = new Cartoon();
        
        undoStack = new Stack<UndoEvent>();
        dragging = false;

        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        setBackground(Color.white);
        
        int numberOfCursors = MediaCenter.cursorImageNames.length;
        cursors = new Cursor[numberOfCursors];
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        for (int i = 0; i < numberOfCursors; i++) {
            Image image = MediaCenter.getImage(MediaCenter.cursorImageNames[i]);
            if (image != null) {
                cursors[i] = toolkit.createCustomCursor(image, new Point(0, 0), "img");
            }
        }
        
        this.initialCanvasSize = new Dimension(500, 500);
        setSize(this.initialCanvasSize);
        this.fitToScreen();
    }
    
    public void setState(int state) {
        this.state = state;
        
        // XXX custom cursors disabled until attractive examples created...
//        this.setCursor(this.cursors[state]);      // uncomment this line to restore cursor support
    }
    
    public int numberOfSelectedSSESymbols() {
        return this.cartoon.numberOfSelectedSSESymbols();
    }

    public void performHorizontalAlignment(Point p) {
        this.saveState();
        ArrayList<SSESymbol> selected = cartoon.getSelectedSSESymbols();

        for (int i = 0; i < selected.size(); i++) {
            SSESymbol curFig = (SSESymbol) selected.get(i);
            if (i == 0) {
                curFig.setPosition(p.x, p.y);
            } else {
                SSESymbol prevFig = (SSESymbol) selected.get(i - 1);
                Point c = prevFig.getCenter();

                int length = 60;
                curFig.setPosition(c.x + length, c.y);
            }
        }
        this.cartoon.relayout();
        this.repaint();
    }

    public void performVerticalAlignment(Point p) {
        this.saveState();
        // want the 'N' to start from where p is.
        ArrayList<SSESymbol> selected = this.cartoon.getSelectedSSESymbols();

        for (int i = 0; i < selected.size(); i++) {
            SSESymbol curFig = (SSESymbol) selected.get(i);
            if (i == 0)
                curFig.setPosition(p.x, p.y);
            else    // the figure is the same as the previous with the x co-ord increased
            {
                //get the previous figs co-ords
                SSESymbol prevFig = (SSESymbol)selected.get(i - 1);
                Point c = prevFig.getCenter();
                int length = 60;
                curFig.setPosition(c.x, c.y + length);
            }
        }
        this.cartoon.relayout();
        this.repaint();
    }
    
    public void flipSymbol(Point p) {
        SSESymbol selected = this.cartoon.selectSSESymbol(p);
        if (selected != null) {
            this.saveState();
            this.cartoon.flipSymbol(selected);
            this.repaint();
        }
    }

    // flips the etire cartoon in the X axis
    public void flipXAxis(Point p) {
        this.saveState();
        this.cartoon.flipXAxis(p);
        this.repaint();
    }

    // flips the entitre cartoon in the y-axis
    public void flipYAxis(Point p) {
        this.saveState();
        this.cartoon.flipYAxis(p);
        parentPanel.setAsUnSaved();
        this.repaint();
    }

    public static void printComponent() {
//        new Printer(panel).print();
    }

    // prints the diagram to the file
    public void print() {
        PrinterJob printJob = PrinterJob.getPrinterJob();
        PageFormat pf = printJob.defaultPage();

        // we will always print out in landscape
        pf.setOrientation(PageFormat.LANDSCAPE);

        printJob.setPrintable(this, pf);
        if (printJob.printDialog())
            try {

                printJob.print();
            } catch (PrinterException pe) {
                System.out.println("Error printing: " + pe);
            }
    }

    public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
        if (pageIndex > 0) {
            return (NO_SUCH_PAGE);
        } else {
            Graphics2D g2d = (Graphics2D)g;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            
            disableDoubleBuffering(this);
            this.paint(g2d);
            enableDoubleBuffering(this);
            
            return (PAGE_EXISTS);
        }
    }

    public static void disableDoubleBuffering(Component c) {
        RepaintManager currentManager = RepaintManager.currentManager(c);
        currentManager.setDoubleBufferingEnabled(false);
    }

    public static void enableDoubleBuffering(Component c) {
        RepaintManager currentManager = RepaintManager.currentManager(c);
        currentManager.setDoubleBufferingEnabled(true);
    }

    public BufferedImage getImage(String imageType) {
        Dimension size = this.cartoon.getSize();

        int border = 50;
        int w = size.width + border;
        int h = size.height + border;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        this.paint(g2);
        g2.dispose();

        return image;
    }
    
    public double getScaleFactor() {
        return this.scale_factor;
    }

    public void zoomOut() {
        if (scale_factor < 0.5) {
//            System.err.println("Cannot Zoom out any more!");
        } else if (scale_factor == 1.0) {
            fitToScreen();
            scale_factor -= SCALE_FACTOR;
        } else {
            scale_factor -= SCALE_FACTOR;

            Dimension cur_dim = this.getPreferredSize();
            // determine the new size of
            int new_width = (int)(cur_dim.width / (1.0 + SCALE_FACTOR));
            int new_height = (int)(cur_dim.height / (1.0 + SCALE_FACTOR));

            this.setPreferredSize(new Dimension(new_width, new_height));
            
        }
        refreshCanvas();
    }

    public void zoomToPrintSize() {
        scale_factor = 0.7;
        Dimension cur_dim = this.getPreferredSize();
        // determine the new size of
        int new_width = (int)(cur_dim.width * scale_factor);
        int new_height = (int)(cur_dim.height * scale_factor);

        this.setPreferredSize(new Dimension(new_width, new_height));
    }


    public void fitToScreen() {

        Dimension inital_dim = this.initialCanvasSize;
        this.setPreferredSize(inital_dim);
        refreshCanvas();

        if (cartoon.numberOfSSESymbols() > 0)
            this.resizeCanvas();
    }

    // zooms in on the diagram
    public void zoomIn() {
        if (scale_factor < 3.0) {
            scale_factor += SCALE_FACTOR;

            //Dimension cur_dim = this.parentPanel.canvas_container.getPreferredSize();
            Dimension cur_dim = this.getPreferredSize();


            // increase the width of the canvas
            Dimension new_dim = new Dimension(
                                    cur_dim.width + (int)(cur_dim.width * SCALE_FACTOR),
                                    cur_dim.height + (int)(cur_dim.height * SCALE_FACTOR));

            this.setPreferredSize(new_dim);

            //int fig_length = cartoon.numberOfSSESymbols();
            
            /*wtf was this MEANT to be doing?? Idiot!
            
            ArrayList figs = cartoon.getSSESymbols();
            if (fig_length > 0) {
                SSESymbol fig = (SSESymbol)(figs.get(fig_length - 1));
                figs.remove(fig);
                figs.add(fig_length - 1, fig);
            }
            */

            refreshCanvas();
        } else {
            System.err.println("Cannot Zoom in any further!");
        }
    }

    public void refreshCanvas() {
        this.setBackground(Color.white);
        this.repaint();
        this.parentPanel.repaint();
    }
    
    public Cartoon getCartoon() {
        return this.cartoon;
    }
    
    // XXX : moved from Validator
    public static int yes_no_dialog(String text, String heading) {

        int n =
            JOptionPane.showConfirmDialog(
                null,
                text,
                heading,
                JOptionPane.YES_NO_OPTION);
        return n;
    }
    
    public static int generic_dialog(String text, String heading, int dialog_type) {

        int n =
            JOptionPane.showConfirmDialog(
                null,
                text,
                heading,
                dialog_type);
        return n;
    }

    public void setDefaultCursor() {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }

    // loads the new figures - used when loading from file
    public void loadNewComponents(Cartoon loadedCartoon) {
        clearCanvas();
        this.cartoon = loadedCartoon;
        this.repaint();
    }
    
    public void moveAllSSESymbols(int x , int y) {
        this.cartoon.moveSelectedSSESymbols(x, y);      // FIXME
    }
    
    public void resizeCanvas() {
        // I don't know what all this crap does
        /*
        Dimension cartoonSize = this.cartoon.getSize();
        Dimension initialCanvasDim = this.getPreferredSize();

        int new_x, new_y;

        // new x value
        if (cartoonSize.width > initialCanvasDim.getWidth())
            new_x = cartoonSize.width;
        else
            new_x = (int) initialCanvasDim.getWidth();

        // new y value
        if (cartoonSize.height > initialCanvasDim.getHeight())
            new_y = cartoonSize.height;
        else
            new_y = (int)initialCanvasDim.getHeight();

        // resize the canvas accordingly
        this.setPreferredSize(new Dimension(new_x + 100, new_y + 130));

        // XXX wtf
        this.zoomIn();
        this.zoomOut();

        this.repaint();
        this.parentPanel.repaint();
        */

    }

    public void paint( Graphics g ) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        this.cartoon.draw(g);
    }
  
    public void saveState() {
        this.undoStack.push(new UndoEvent(this.cartoon));
        this.parentPanel.setAsUnSaved();
    }

    public void revert() throws EmptyStackException {
        UndoEvent undo = (UndoEvent) this.undoStack.pop();
        this.cartoon = undo.getCartoon();
        System.out.println("reverting to " + this.cartoon);
        this.cartoon.deselectAllSSESymbols();
        this.cartoon.relayout();
        repaint();
    }
    
    public void rangeInsert(Point p) {
        
    }

    public void fireFlipSelected() {
        this.saveState();
        this.cartoon.flip();
        repaint();
    }

    // clears everything on the main canvas
    public void clearCanvas() {
        this.saveState();
        this.cartoon.clear();
        repaint();
    }
    
    public void selectAll() {
        this.cartoon.selectAllSSESymbols();
        this.repaint();
    }
    
    public void center() {
        Point canvasCenter = new Point(this.getWidth() / 2, this.getHeight() / 2);
        this.cartoon.centerOn(canvasCenter);
        repaint();
    }
    
    public void deselectAll() {
        this.cartoon.deselectAllSSESymbols();
        this.repaint();
    }
    
    public void deleteSymbolAt(Point p) {
        SSESymbol selected = this.selectSSESymbol(p);
        if (selected != null) {
            this.saveState();
            this.cartoon.deleteSSESymbol(selected);
        }
    }

    public void createSymbol(Point p, int currentState) {
        this.saveState();
        
        int symbol_int = cartoon.numberOfSSESymbols() + 1;
        int w = this.getWidth();
        int h = this.getHeight();
        int d = Math.max(w, h);
        switch (currentState) {
            case STRAND_UP:
                this.cartoon.createUpStrand(symbol_int, p.x, p.y, d);
                break;
            case STRAND_DOWN:
                this.cartoon.createDownStrand(symbol_int, p.x, p.y, d);
                break;
            case HELIX_UP:
                this.cartoon.createUpHelix(symbol_int, p.x, p.y, d);
                break;
            case HELIX_DOWN:
                this.cartoon.createDownHelix(symbol_int, p.x, p.y, d);
                break;
            default:
                break;
        }
        cartoon.deselectAllSSESymbols();
        this.repaint();
    }
    
    public void createBond(Point p) {
        SSESymbol selectedSSESymbol = selectSSESymbol(p);
        // we have not selected the first symbol in our bond
        if (first_bond_figure == null) { //no bond started
            //just starting an arc
            first_bond_figure = selectedSSESymbol;
        } else {   // we have already selected a figure
            if (selectedSSESymbol != null) {
                if (first_bond_figure == selectedSSESymbol) {  // drawing between same symbols
                    return;
                } else {
                    SSESymbol source = first_bond_figure;
                    SSESymbol dest = selectedSSESymbol;

                    if (state == RIGHT_ARC) {
                        if (cartoon.canCreateRArc(source, dest)) {
                            this.saveState();
                            cartoon.createRightArc(source, dest);
                        }
                    } else if (state == LEFT_ARC) {
                        if (cartoon.canCreateLArc(source, dest)) {
                            this.saveState();
                            cartoon.createLeftArc(source, dest);
                        }
                    } else if (state == H_BOND) {      
                        if (cartoon.canCreateHydrogenBond(source, dest)) {
                            if (cartoon.shouldCreateParallelBond(source, dest)) {
                                this.saveState();
                                cartoon.createPBond(source, dest);
                            } else if (cartoon.shouldCreateAntiParallelBond(source, dest)) {
                                this.saveState();
                                cartoon.createABond(source, dest);
                            }
                        }
                    }
                    setDefaultCursor();
                    first_bond_figure = null;
                    cartoon.deselectAllSSESymbols();
                    repaint();
                }
            } else   {// we have the first then didn't select a second, selected the canvas
                setDefaultCursor();
            }
        }
    }
    
    public void clickSelect(Point p) {
//        SSESymbol selected = this.cartoon.toggleSelectSSESymbol(p);
        SSESymbol clicked = this.cartoon.getSSESymbolAt(p);
        if (clicked != null) {
            if (clicked.isSelected()) {
                clicked.setSelectionState(false);
            } else {
                clicked.setSelectionState(true);
            }
            repaint();
        }
    }

    public void mouseClicked(MouseEvent e) {
        Point p = e.getPoint();
        
        switch(this.state) {
            case SELECT:
                this.clickSelect(p); break;
            case STRAND_UP:
            case STRAND_DOWN:
            case HELIX_UP:
            case HELIX_DOWN:
                this.createSymbol(p, state); break;
            case FLIP:
                this.flipSymbol(p); break;
            case FLIP_X:
                this.flipXAxis(p); break;
            case FLIP_Y:
                this.flipYAxis(p); break;
            case DELETE:
                this.deleteSymbolAt(p); break;
            case H_BOND:
            case RIGHT_ARC:
            case LEFT_ARC:
                this.createBond(p); break;
            case HORIZONTAL_ALIGN:
                performHorizontalAlignment(p); break;
            case VERTICAL_ALIGN:
                performVerticalAlignment(p); break;
            case RANGE:
                rangeInsert(p); break;
            default:
                break;
        }
    }

    public void mousePressed(MouseEvent e) {
        SSESymbol fig = cartoon.getSelectedSSESymbol(e.getPoint());

        if (fig != null) {
            dragging = true;
            this.currentlyDraggedSymbol = fig;
            System.out.println("Dragging Symbol " + currentlyDraggedSymbol);
        } else {
            this.currentlyDraggedSymbol = null;
        }
    }

    public void mouseReleased(MouseEvent e) {
        dragging = false;
        if (this.currentlyDraggedSymbol != null) {
            cartoon.deselectAllSSESymbols();
            repaint();
        }
        this.currentlyDraggedSymbol = null;
    }
    
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    public SSESymbol selectSSESymbol(Point p) {
        SSESymbol selected = this.cartoon.selectSSESymbol(p);
        if (selected != null) {
            repaint();
        }
        return selected;
    }

    // for when you are moving the selected shape around the screen
    public void mouseDragged(MouseEvent e) {
        if (dragging) {
            Point new_point = e.getPoint();

            if (this.currentlyDraggedSymbol != null) {

                // work out how much we should move the others by
                Point orig_point = this.currentlyDraggedSymbol.getCenter();

                // work out the  x and y differences
                int x_diff = new_point.x - orig_point.x;
                int y_diff = new_point.y - orig_point.y;
                
                //System.out.println("Moving by " + x_diff + " " + y_diff);

                cartoon.moveSelectedSSESymbols(x_diff, y_diff);

                repaint();
                parentPanel.setAsUnSaved();
            }
        }
    }
    
    public void highlightConnector(Point p) {
        if (!cartoon.hasSelectedConnector()) {

            //check if we have selected a connection
            for (int i = 0; i < cartoon.numberOfConnectors(); i++) {
                CartoonConnector con = cartoon.getCartoonConnector(i);

                if (con != null && con.containsPoint(p.x, p.y)) {
                    con.setSelectionState(true);
                    cartoon.setSelectedConnector(con);
                    repaint();
                    break;     // can only have one selected at any one time
                }
            }
        } else    {  // we have a selected connection
            // moved the mouse away from the selected connector
            CartoonConnector selectedConnector = cartoon.getSelectedConnector();
            if (!selectedConnector.containsPoint(p.x, p.y)) { 
                //!selected_connector.contains(point))
                selectedConnector.setSelectionState(false);
                cartoon.setSelectedConnector(null);
                repaint();
            }
        }
    }
    
    public void highlightSSESymbol(Point p) {
        SSESymbol selected = this.cartoon.toggleHighlightSSESymbol(p);
        if (selected == null) {
            if (!highlightFlag) {
                repaint();
                this.highlightFlag = true;
            }
        } else {
            if (highlightFlag) {
                repaint();
                this.highlightFlag = false;
            }
        }
    }

    // select a connector
    public void mouseMoved(MouseEvent me) {
        Point point = me.getPoint();

        if (state == STRAND_UP || state == STRAND_DOWN ||
                state == HELIX_UP || state == HELIX_DOWN || state == RANGE) {
            this.highlightConnector(point);
        } else if (state == SELECT) {
            this.highlightSSESymbol(point);
        }
    }
}
