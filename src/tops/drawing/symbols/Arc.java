package tops.drawing.symbols;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Arc2D;


/**
 * @author maclean
 *
 */
public class Arc extends Bond {
   
    public Arc(SSESymbol start, SSESymbol end, int type) {
        super(start, end, type);
    }
    
    public Shape createShape() {
        if (this.getType() == Arc.LEFT_CHIRAL || this.getType() == Arc.RIGHT_CHIRAL) {
            Point startBase = this.getStartSSESymbol().getBasePoint();
            double x = startBase.x;
            double w = this.getEndSSESymbol().getCenter().x - x;
            double h = w / 2;
            double y = startBase.y - (h / 2);
            return new Arc2D.Double(x, y, w, h, 180, 180, Arc2D.OPEN);
        } else {
            Point startTop = this.getStartSSESymbol().getTopPoint();
            double x = startTop.x;
            double w = this.getEndSSESymbol().getCenter().x - x;
            double h = w / 2;
            double y = startTop.y - (h / 2);
            return new Arc2D.Double(x, y, w, h, 0, 180, Arc2D.OPEN);
        }
    }
    
    public Shape createSelectionBoundary() {
        return this.getShape();     // TODO
    }

}
