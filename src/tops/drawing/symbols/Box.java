package tops.drawing.symbols;

import java.awt.Color;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;


/**
 * @author maclean
 *
 */
public class Box extends SSESymbol implements Cloneable {
    
    public Box(int symbolNumber, String label) {
        super(symbolNumber, false);
        this.setLabel(label);
        this.setDrawLabel(true);
    }
    
    public Box(int symbolNumber, int x, int y, int radius, String label) {
        super(symbolNumber, x, y, radius, false);
        this.setLabel(label);
        this.setDrawLabel(true);
    }
    
    public Object clone() {
        return super.clone();
    }
    
    public Color getColor() {
        return Color.BLACK;
    }
    
    public Shape createShape() {
        Point c = this.getCenter();
        int r = this.getRadius();
        int w = r * 2;
        return new Rectangle2D.Double(c.x - r, c.y - r, w, w);
    }
    
    public Shape createSelectionBoundary() {
        Rectangle2D bb = this.getShape().getBounds();
        int border = 3;
        bb.setFrameFromCenter(bb.getCenterX(), bb.getCenterY(), bb.getX() - border, bb.getY() - border);
        return bb;
    }
}
