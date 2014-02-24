package tops.drawing.symbols;

import java.awt.BasicStroke;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;


/**
 * @author maclean
 *
 */
public class DashedLine extends Bond {
    
    public static Stroke BOND_STROKE = 
        new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] {9}, 0 );
    
    public DashedLine(SSESymbol start, SSESymbol end, int type) {
        super(start, end, type);
    }
    
    public Shape createShape() {
        Point start = this.getStartSSESymbol().getCenter();
        Point end = this.getEndSSESymbol().getCenter();
        
        return DashedLine.BOND_STROKE.createStrokedShape(new Line2D.Double(start, end));
    }
    
    public Shape createSelectionBoundary() {
        return this.getShape();
    }

    public String toString() {
        return this.getStartSSESymbol().getSymbolNumber() + "-" + this.getEndSSESymbol().getSymbolNumber();
    }
}
