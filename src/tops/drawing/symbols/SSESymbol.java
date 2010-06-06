package tops.drawing.symbols;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;


/**
 * @author maclean
 *
 */
public abstract class SSESymbol implements Cloneable, Symbol {
    
    private int symbolNumber;
    private int x;
    private int y;
    private int radius;
    
    private boolean isDown;
    private boolean isSelected;
    private boolean isHighlighted;
    private boolean shouldDrawLabel;
    
    private String label;
    private Shape currentShape;
    private Shape selectionBoundary;
    
    public SSESymbol(int symbolNumber, boolean isDown) {
        this.symbolNumber = symbolNumber;
        this.isDown = isDown;
        this.isSelected = false;
        this.isHighlighted = false;
        this.shouldDrawLabel = false;
        this.currentShape = null;   // XXX
        this.selectionBoundary = null;
    }
    
    public SSESymbol(int symbolNumber, int x, int y, int radius, boolean isDown) {
        this(symbolNumber, isDown);
        this.x = x;
        this.y = y;
        this.radius = radius;
    }
    
    public Object clone() {
        try {
            SSESymbol s = (SSESymbol) super.clone();
            s.currentShape = null;
            s.selectionBoundary = null;
            return s;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public void setDrawLabel(boolean shouldDrawLabel) {
        this.shouldDrawLabel = shouldDrawLabel;
    }
    
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
    
    public boolean isDown() {
        return this.isDown;
    }
    
    public boolean hasSymbolNumber(int symbolNumber) {
        return this.symbolNumber == symbolNumber;
    }
    
    public boolean containsPoint(int x, int y) {
        if (this.currentShape != null) {
//            return this.currentShape.contains(x, y);
            return this.selectionBoundary.contains(x, y);   // not sure which is better?
        } else {
            return false;
        }
    }
    
    public void flip() {
        if (this.isDown) {
            this.isDown = false;
        } else {
            this.isDown = true;
        }
        this.recreateShape();
    }
    
    public void setColor(Color c) {
        //TODO
    }
    
    public Color getColor() {
        return Color.BLACK;
    }
    
    public void setSymbolNumber(int symbolNumber) {
        this.symbolNumber = symbolNumber;
    }
    
    public int getSymbolNumber() {
        return this.symbolNumber;
    }
    
    public void move(int xDiff, int yDiff) {
        this.setPosition(this.x + xDiff,this.y + yDiff);
    }
    
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        this.recreateShape();     // XXX : side effect!
    }
    
    public void setDimensions(int x, int y, int radius) {
        this.radius = radius;
        this.setPosition(x, y);
        System.out.println("setting center " + x + " " + y);
    }
    
    public int getRadius() {
        return this.radius;
    }
    
    public Point getCenter() {
        return new Point(this.x, this.y);
    }
    
    public Point getBoundingBoxCenter() {
    	Rectangle bounds = this.currentShape.getBounds();
    	return new Point((int)bounds.getCenterX(), (int)bounds.getCenterY());
    }
    
    public Point getCorner() {
        return new Point(this.x - this.radius, this.y - this.radius);
    }
    
    public Point getBasePoint() {
        return new Point(this.x, this.y + this.radius);
    }
    
    public Point getTopPoint() {
        return new Point(this.x, this.y - this.radius);
    }
    
    public Point getLeftPoint() {
        return new Point(this.x - this.radius, this.y);
    }
    
    public Point getRightPoint() {
        return new Point(this.x + this.radius, this.y);
    }
    
    public abstract Shape createShape();
    public abstract Shape createSelectionBoundary();
    
    public void recreateShape() {
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
    
    public void draw(Graphics2D g2) {
//        System.out.println("drawing " + this + " at " + this.x + " " + this.y + " in " + this.getShape().getBounds());
        g2.setColor(this.getColor());
        g2.draw(this.getShape());
        
        if (this.shouldDrawLabel) {
            Font f = g2.getFont();
            FontRenderContext frc = g2.getFontRenderContext();
            Rectangle2D stringBounds = f.getStringBounds(this.label, frc); 
            double sWidth = stringBounds.getWidth();
            int labelX = this.x - (int)(sWidth / 2);
            int labelY = this.y + ((int)stringBounds.getHeight() / 2);
            g2.drawString(this.label, labelX, labelY);
        }
        
        if (this.isSelected || this.isHighlighted) {
            Shape ss = this.getSelectionBoundary();
            if (this.isSelected) {
                g2.setColor(Color.RED);
            } else {
                g2.setColor(Color.LIGHT_GRAY);
            }
            g2.draw(ss);
        }
    }
  
    public String toString() {
        return "SSESymbol " + this.symbolNumber + " @ [" + this.x + ", " + this.y + "] "; 
    }
}
