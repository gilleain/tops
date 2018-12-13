package tops.drawing.app;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import tops.drawing.Diagram;
import tops.drawing.actions.Action;
import tops.drawing.actions.AddArc;
import tops.drawing.actions.DeleteDiagram;
import tops.drawing.actions.DeleteSymbols;
import tops.drawing.actions.FlipSymbols;
import tops.drawing.actions.InsertSSE;
import tops.drawing.symbols.Arc;
import tops.drawing.symbols.Bullet;
import tops.drawing.symbols.Line;
import tops.drawing.symbols.SSESymbol;
import tops.drawing.symbols.Symbol;
import tops.drawing.symbols.Triangle;


public class DiagramEditorCanvas extends Canvas implements MouseListener, MouseMotionListener {
    private Diagram diagram;
    private Deque<Action> undoStack;
    private Deque<Action> redoStack;
    private DiagramEditorToolbar diagramEditorToolbar;
//    private Rectangle2D selectionBox; TODO

    private Point lastClickedPoint = null;
    private boolean highlightFlag = false;     // purely an optimization device : see moveHighlight
    
    //state constants
    private int state;
    private static final int INITIAL_STATE       = 0;
    private static final int SELECTING           = 1;
    private static final int ADDING_UP_STRAND    = 2;
    private static final int ADDING_DOWN_STRAND  = 3;
    private static final int ADDING_UP_HELIX     = 4;
    private static final int ADDING_DOWN_HELIX   = 5;
    private static final int MAKING_HBOND        = 6;
    private static final int MAKING_LEFT_CHIRAL  = 7;
    private static final int MAKING_RIGHT_CHIRAL = 8;
    private static final int DELETING = 9;
    private static final int FLIPPING = 10;
    
    public DiagramEditorCanvas(int width, int height, DiagramEditorToolbar undoToolbar) {
        //set up standard canvas type options
        this.setBackground(Color.WHITE);
        this.setSize(width, height);
        
        //set up the undo/redo stacks
        this.undoStack = new ArrayDeque<>();
        this.redoStack = new ArrayDeque<>();
        
        //initialise a blank diagram
        this.diagram = new Diagram();
        this.diagram.setSize(width, height);
        this.diagram.createTerminii();
        
        //register the diagramEditorToolbar as a listener..NOT the best solution
        this.diagramEditorToolbar = undoToolbar;
        
        this.state = DiagramEditorCanvas.INITIAL_STATE;
        
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }
    
    public void setState(char stateCharacter) {
        switch (stateCharacter) {
            case 'S': 
                this.state = DiagramEditorCanvas.SELECTING;
                break;
            case 'E': 
                this.state = DiagramEditorCanvas.ADDING_UP_STRAND;
                break;
            case 'e': 
                this.state = DiagramEditorCanvas.ADDING_DOWN_STRAND;
                break;
            case 'H': 
                this.state = DiagramEditorCanvas.ADDING_UP_HELIX;
                break;
            case 'h': 
                this.state = DiagramEditorCanvas.ADDING_DOWN_HELIX;
                break;
            case 'B':
                this.state = DiagramEditorCanvas.MAKING_HBOND;
                break;
            case 'L':
                this.state = DiagramEditorCanvas.MAKING_LEFT_CHIRAL;
                break;
            case 'R':
                this.state = DiagramEditorCanvas.MAKING_RIGHT_CHIRAL;
                break;
            case 'D':
                this.state = DiagramEditorCanvas.DELETING;
                break;
            case 'F':
                this.state = DiagramEditorCanvas.FLIPPING;
                break;
            default :
                break;
        }
    }
    
    public void mouseClicked(MouseEvent e) {
        switch (this.state) {
            case DiagramEditorCanvas.ADDING_UP_STRAND:
                this.addUpStrand();
                break;
            case DiagramEditorCanvas.ADDING_DOWN_STRAND:
                this.addDownStrand();
                break;
            case DiagramEditorCanvas.ADDING_UP_HELIX:
                this.addUpHelix();
                break;
            case DiagramEditorCanvas.ADDING_DOWN_HELIX:
                this.addDownHelix();
                break;
            case DiagramEditorCanvas.SELECTING:
                this.clickSelect(e.getX(), e.getY(), e.isShiftDown());
                break;
            case DiagramEditorCanvas.MAKING_HBOND:
            case DiagramEditorCanvas.MAKING_LEFT_CHIRAL:
            case DiagramEditorCanvas.MAKING_RIGHT_CHIRAL:
                this.bondingSelect(e.getX(), e.getY());
                break;
            case DiagramEditorCanvas.DELETING:
                this.delete(e.getX(), e.getY());
                break;
            case DiagramEditorCanvas.FLIPPING:
                this.flip(e.getX(), e.getY());
                break;
            default: 
                break;
        }
        if (this.diagram.isEmpty()) {
            this.diagramEditorToolbar.disableSelectionButton();
        } else {
            this.diagramEditorToolbar.enableSelectionButton();
        }
    }
    
    public void mouseEntered(MouseEvent e) { 
        // no op
    }
    public void mouseExited(MouseEvent e) { 
     // no op
    }
    public void mousePressed(MouseEvent e) { 
     // no op
    }
    public void mouseReleased(MouseEvent e) { 
     // no op
    }
    
    public void mouseDragged(MouseEvent e) {
        if (lastClickedPoint != null) {
            // TODO
        }
        
    }
    
    public void mouseMoved(MouseEvent e) {
        this.moveHighlight(e.getX(), e.getY());
    }
    
    
    /**
     * Highlight symbols or connections (when adding symbols) when mouse-overing them.
     * 
     * @param x the point x coord.
     * @param y the point y coord.
     */
    private void moveHighlight(int x, int y) {
        Symbol selected;
        if (this.state == ADDING_UP_STRAND || this.state == ADDING_DOWN_STRAND 
                || this.state == ADDING_UP_HELIX || this.state == ADDING_DOWN_HELIX) {
            selected = this.diagram.toggleHighlightConnectionAt(x, y);
        } else {
            selected = this.diagram.toggleHighlightSymbolAt(x, y);
        }
        
        if (selected != null) {             // we have mouse-overed a symbol..
            if (!this.highlightFlag) {     // ... and no symbol is highlighted
                this.repaint();
                this.highlightFlag = true;
            }
        } else {                            // no symbol is under the cursor...
            if (this.highlightFlag) {      // ... but we need to switch off highlights
                this.repaint();
                this.highlightFlag = false;
            }
        }
    }

    /**
     * Connect symbols with an arc.
     * 
     * @param x the click x.
     * @param y the click y.
     */
    private void bondingSelect(int x, int y) {
        List<SSESymbol> selectedSymbols = this.diagram.getSelectedSymbols();
        SSESymbol selected = this.diagram.selectSymbolAt(x, y);
        if (selectedSymbols.isEmpty() && selected != null) {
            if (selectedSymbols.size() > 1) {
                // this shouldn't happen - it means three symbols selected!
            } else {
                SSESymbol previous = selectedSymbols.get(0);
                
                // check that previous is before selected - otherwise swap!
                if (previous.getSymbolNumber() > selected.getSymbolNumber()) {
                    SSESymbol tmp = previous;
                    previous = selected;
                    selected = tmp;
                }
                Arc arc;                
                if (this.state == DiagramEditorCanvas.MAKING_HBOND) {
                    if ((previous.isDown() && selected.isDown()) 
                            || (!previous.isDown() && !selected.isDown())) {
                        arc = new Arc(previous, selected, Arc.PARALLEL_HBOND);
                    } else {
                        arc = new Arc(previous, selected, Arc.ANTIPARALLEL_HBOND);
                    }
                } else if (this.state == DiagramEditorCanvas.MAKING_LEFT_CHIRAL){
                    arc = new Arc(previous, selected, Arc.LEFT_CHIRAL);
                } else {
                    arc = new Arc(previous, selected, Arc.RIGHT_CHIRAL);
                }
                this.doAction(new AddArc(arc, this.diagram));
                this.diagram.unselectAll();
                this.repaint();
            }
        }
    }

    /**
     * Select symbols if clicking on them, or start a select box.
     * 
     * @param x the point x coord.
     * @param y the point y coord.
     * @param shiftDown true if shift key is pressed.
     */
    private void clickSelect(int x, int y, boolean shiftDown) {
        SSESymbol selected;
        if (shiftDown) {
            selected = this.diagram.selectSymbolAt(x, y);
        } else {
            selected = this.diagram.toggleSelectSymbolAt(x, y);
        }
        
        if (selected != null) {         // we have selected a symbol
            this.repaint();
            this.lastClickedPoint = null;
        } else {                        // clicked elsewhere
            this.lastClickedPoint = new Point(x, y);
        }
    }

    private void delete(int x, int y) {
        SSESymbol selected = this.diagram.selectSymbolAt(x, y);
        ArrayList<SSESymbol> selectedSymbols = new ArrayList<>();
        selectedSymbols.add(selected);
        this.doAction(new DeleteSymbols(selectedSymbols, this.diagram));
    }
    
    private void flip(int x, int y) {
        SSESymbol selected = this.diagram.selectSymbolAt(x, y);
        if (selected != null ) {
            ArrayList<Integer> selectedSymbols = new ArrayList<>();
            selectedSymbols.add(selected.getSymbolNumber());
            this.doAction(new FlipSymbols(selectedSymbols, this.diagram));
            this.diagram.unselectAll();
        }
    }

    @Override
    public void paint(Graphics g) {
        this.diagram.paint((Graphics2D) g);
    }
    
    public void setDiagram(Diagram diagram) {
        this.diagram = diagram;
    }
    
    public void flipDiagram() {
        this.diagram.flip();
    }
    
    public boolean isEmpty() {
        return this.diagram.isEmpty();
    }
    
     /* ACTIONS */
    
    public void doAction(Action action) {
        //do the action
        System.out.println("Performing action : " + action.description());
        action.doIt();
        
        //set the undo button to enabled if there was nothing on the stack
        if (this.undoStack.isEmpty()) { this.diagramEditorToolbar.toggleUndoButton(true); }
        
        //push the action on the undo stack
        this.undoStack.push(action);
        this.repaint();
    }
    
    public void undo() {
        //get the action from the stack and undo it
        Action action = this.undoStack.pop();
        System.out.println("Undoing action : " + action.description());
        action.undoIt();
        
        //if this was the last action on the undo stack, disable the button
        if (this.undoStack.isEmpty()) { this.diagramEditorToolbar.toggleUndoButton(false); }
        
        //if this would be the first action on the redo stack, enable the button
        if (this.redoStack.isEmpty()) { this.diagramEditorToolbar.toggleRedoButton(true); }
        
        //now add the action to the redo stack
        this.redoStack.push(action);
        this.repaint();
    }
    
    public void redo() {
        //get the action from the stack and redo it
        Action action = this.redoStack.pop();
        System.out.println("Redoing action : " + action.description());
        action.doIt();
               
        //if this was the last action on the redo stack, disable the button
        if (this.redoStack.isEmpty()) { this.diagramEditorToolbar.toggleRedoButton(false); }
        
        //if this would be the first action on the undo stack, enable the button
        if (this.undoStack.isEmpty()) { this.diagramEditorToolbar.toggleUndoButton(true); }
        
        //now add the action to the undo stack
        this.undoStack.push(action);
        this.repaint();
    }
    
    public boolean hasMoreActionsToUndo() { 
        return !this.undoStack.isEmpty(); 
    }
    
    public boolean hasMoreActionsToRedo() { 
        return !this.redoStack.isEmpty(); 
    }
    
    /* ACTIONS */
    
    public void addUpStrand() {
        Line connection = this.diagram.getHighlightedConnection();
        if (connection != null) {
            this.addSSE(new Triangle(0, false), connection);
        }
    }
    
    public void addDownStrand() {
        Line connection = this.diagram.getHighlightedConnection();
        if (connection != null) {
            this.addSSE(new Triangle(0, true), connection);
        }
    }
    
    public void addUpHelix() {
        Line connection = this.diagram.getHighlightedConnection();
        if (connection != null) {
            this.addSSE(new Bullet(0, false), connection);
        }
    }
    
    public void addDownHelix() {
        Line connection = this.diagram.getHighlightedConnection();
        if (connection != null) {
            this.addSSE(new Bullet(0, true), connection);
        }
    }
    
    public void addSSE(SSESymbol sseToAdd, Line connection) {
        this.doAction(new InsertSSE(sseToAdd, connection, this.diagram));
    }

    public void unselectAll() {
        this.diagram.unselectAll();
    }

    public void flipSelected() {
        ArrayList<Integer> selectedSymbolNumbers = this.diagram.getSelectedSymbolNumbers();
        this.doAction(new FlipSymbols(selectedSymbolNumbers, this.diagram));
    }
    
    public void deleteDiagram() {
        this.doAction(new DeleteDiagram(this.diagram, this));
    }

}
