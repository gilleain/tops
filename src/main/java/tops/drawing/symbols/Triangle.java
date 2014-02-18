package tops.drawing.symbols;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;


/**
 * @author maclean
 *
 */
public class Triangle extends SSESymbol {
    
    public Triangle(int symbolNumber, boolean isDown) {
        super(symbolNumber, isDown);
    }
    
    public Triangle(int symbolNumber, int x, int y, int radius, boolean isDown) {
        super(symbolNumber, x, y, radius, isDown);
    }
    
    public Shape createShape() {
        Point c = this.getCorner();
        int r = this.getRadius();
        int w = r * 2;
        int tipY = (this.isDown()) ? c.y + w : c.y;
        int baseY = (this.isDown()) ? c.y : c.y + w;

        GeneralPath gp = new GeneralPath();
        gp.moveTo(c.x + r, tipY);
        gp.lineTo(c.x + w, baseY);
        gp.lineTo(c.x, baseY);
        gp.closePath();
        return gp;
    }
    
    public Shape createSelectionBoundary() {
        Rectangle2D bb = this.getShape().getBounds();
        int border = 3;
        bb.setFrameFromCenter(bb.getCenterX(), bb.getCenterY(), bb.getX() - border, bb.getY() - border);
        return bb;
    }
}
