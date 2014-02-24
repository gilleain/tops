package tops.drawing.symbols;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;


/**
 * @author maclean
 *
 */
public class EquilateralTriangle extends SSESymbol implements Cloneable {
    
    public EquilateralTriangle(int symbolNumber, boolean isDown) {
        super(symbolNumber, isDown);
    }
    
    public EquilateralTriangle(int symbolNumber, int x, int y, int radius, boolean isDown) {
        super(symbolNumber, x, y, radius, isDown);
    }
    
    public Object clone() {
        return super.clone();
    }
    
    public Shape createShape() {
        Point c = this.getCenter();
        int x = c.x;
        int y = c.y;
        int r = this.getRadius();
      
        double pi6 = Math.PI / 6.0;
        double cospi6 = Math.cos(pi6);
        double sinpi6 = Math.sin(pi6);

        int rsinpi6 = (int) (r * sinpi6);
        int rcospi6 = (int) (r * cospi6);

        GeneralPath triangle = new GeneralPath();
        if (this.isDown()) {
            triangle.moveTo(x, y + r);
            triangle.lineTo(x - rcospi6, y - rsinpi6);
            triangle.lineTo(x + rcospi6, y - rsinpi6);
        } else {
            triangle.moveTo(x, y - r);
            triangle.lineTo(x - rcospi6, y + rsinpi6);
            triangle.lineTo(x + rcospi6, y + rsinpi6);
        }
        triangle.closePath();
        return triangle;
    }
    
    public Shape createSelectionBoundary() {
        Rectangle2D bb = this.getShape().getBounds();
        int border = 3;
        bb.setFrameFromCenter(bb.getCenterX(), bb.getCenterY(), bb.getX() - border, bb.getY() - border);
        return bb;
    }
}
