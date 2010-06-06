package tops.drawing.symbols;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;


/**
 * @author maclean
 *
 */
public abstract class ConnectionSymbol implements Cloneable, Symbol {
    
    private SSESymbol start;
    private SSESymbol end;
    
    private Shape currentShape;
    private Shape selectionBoundary;
    
    private boolean isSelected;
    private boolean isHighlighted;
    
    public ConnectionSymbol(SSESymbol start, SSESymbol end) {
        this.start = start;
        this.end = end;
        
        this.currentShape = null;
        this.selectionBoundary = null;
        this.isSelected = false;
        this.isHighlighted = false;
    }
    
    public Object clone() {
        try {
            ConnectionSymbol c = (ConnectionSymbol) super.clone();
            
            // we DON'T clone the sses, otherwise we might end up
            // with multiple different clones of each sse!
            // c.start = (SSESymbol) this.start.clone();
            // c.end = (SSESymbol) this.end.clone();
            
            c.currentShape = null;
            c.selectionBoundary = null;
            return c;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }
    
    public boolean contains(SSESymbol sseSymbol) {
        return this.start == sseSymbol || this.end == sseSymbol;
    }
    
    public SSESymbol getStartSSESymbol() {
        return this.start;
    }
    
    public void setStartSSESymbol(SSESymbol start) {
        this.start = start;
    }
    
    public SSESymbol getEndSSESymbol() {
        return this.end;
    }
    
    public void setEndSSESymbol(SSESymbol end) {
        this.end = end;
    }

    public abstract Shape createShape();
    public abstract Shape createSelectionBoundary();
    public abstract Color getColor();
    
    public boolean isSelected() {
        return this.isSelected;
    }
    
    public void setSelectionState(boolean isSelected) {
        this.isSelected = isSelected;
    }
    
    public boolean isHighlighted() {
        return this.isHighlighted;
    }
    
    public void setHighlightState(boolean isHighlighted) {
        this.isHighlighted = isHighlighted;
    }
    
    public void recreateShape() {
//        System.out.println("recreating shape " + this);
        this.currentShape = this.createShape();
        this.selectionBoundary = this.createSelectionBoundary();
    }
    
    public Shape getShape() {
        if (this.currentShape == null) {
            this.recreateShape();
        }
        return this.currentShape;
    }
    
    public Shape getSelectionBoundary() {
        if (this.selectionBoundary == null) {
            this.recreateShape();
        }
        return this.selectionBoundary;
    }
    
    public boolean containsPoint(int x, int y) {
        if (this.currentShape != null) {
            return this.getSelectionBoundary().contains(x, y);
        } else {
            return false;
        }
    }
    
    public void draw(Graphics2D g2) {
        g2.setColor(this.getColor());
        g2.draw(this.getShape());
        
        if (this.isSelected) {
            g2.setColor(Color.GRAY);
            g2.draw(this.getSelectionBoundary());
        } else if (this.isHighlighted) {
            g2.setColor(Color.GRAY);
            g2.draw(this.getSelectionBoundary());
        }
        
    }

    public String toString() {
        return this.start.getSymbolNumber() + "-" + this.end.getSymbolNumber();
    }
}
