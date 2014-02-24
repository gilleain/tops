package tops.drawing.symbols;

import java.awt.Color;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;


/**
 * @author maclean
 *
 */
public class Line extends ConnectionSymbol {
    
    public Line(SSESymbol start, SSESymbol end) {
        super(start, end);
    }
    
    public Color getColor() {
        return Color.BLACK;
    }
    
    public Shape createShape() {
        Point startRight = this.getStartSSESymbol().getRightPoint();
        Point endLeft = this.getEndSSESymbol().getLeftPoint();
        return new Line2D.Double(startRight, endLeft);
    }
    
    public Shape createSelectionBoundary() {
        Line2D line = (Line2D) this.getShape();
        
        int border = 4;
        double upperY = line.getY1() - border;
        double width = line.getX2() - line.getX1();
        double height = border * 2;
        Rectangle2D selectionBoundary = new Rectangle2D.Double(line.getX1(), upperY, width, height); 

        return selectionBoundary;
    }

    public String toString() {
        return this.getStartSSESymbol().getSymbolNumber() + "-" + this.getEndSSESymbol().getSymbolNumber();
    }
}
