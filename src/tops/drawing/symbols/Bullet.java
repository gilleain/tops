package tops.drawing.symbols;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;


/**
 * @author maclean
 *
 */
public class Bullet extends SSESymbol {
    
    public Bullet(int symbolNumber, boolean isDown) {
        super(symbolNumber, isDown);
    }

    public Bullet(int symbolNumber, int x, int y, int radius, boolean isDown) {
        super(symbolNumber, x, y, radius, isDown);
    }

    public Shape createShape() {
        Point c = this.getCorner();
        int r = this.getRadius();
        int w = r * 2;
        int w2 = w * 2;
        if (this.isDown()) {
            return new Arc2D.Double(c.x, c.y - w, w, w2, 180, 180, Arc2D.CHORD);
        } else {
            return new Arc2D.Double(c.x, c.y, w, w2, 0, 180, Arc2D.CHORD);
        }
    }
    
    public Shape createSelectionBoundary() {
        Rectangle2D bb = this.getShape().getBounds();
        int border = 3;
        int b2 = border * 2;
        double x = bb.getX();
        double y = bb.getY();
        double w = bb.getWidth();
        double h = bb.getHeight();
        double h2 = h / 2;
        
        if (this.isDown()) {
            return new Rectangle2D.Double(x - border, y + h2 - border, w + b2, h2 + b2);
        } else {
            return new Rectangle2D.Double(x - border, y - border, w + b2, h2 + b2);
        }
    }
}
