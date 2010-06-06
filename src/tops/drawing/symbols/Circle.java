package tops.drawing.symbols;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;


/**
 * @author maclean
 *
 */
public class Circle extends SSESymbol implements Cloneable {
    
    public Circle(int symbolNumber, boolean isDown) {
        super(symbolNumber, isDown);
    }
    
    public Circle(int symbolNumber, int x, int y, int radius, boolean isDown) {
        super(symbolNumber, x, y, radius, isDown);
    }
    
    public Object clone() {
        return super.clone();
    }
    
    public Shape createShape() {
        Point c = this.getCorner();
        int d = this.getRadius() * 2;
        return new Ellipse2D.Double(c.x, c.y, d, d);
    }
    
    public Shape createSelectionBoundary() {
        Rectangle2D bb = this.getShape().getBounds();
        int border = 3;
        bb.setFrameFromCenter(bb.getCenterX(), bb.getCenterY(), bb.getX() - border, bb.getY() - border);
        return bb;
    }

}
