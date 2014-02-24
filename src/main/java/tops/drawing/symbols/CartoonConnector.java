package tops.drawing.symbols;

import java.awt.Color;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;


/**
 * @author maclean
 *
 */
public class CartoonConnector extends ConnectionSymbol implements Cloneable {
    
    private static double pi = Math.PI;
    private static double pi2 = Math.PI / 2.0;
    private static double pi6 = Math.PI / 6.0;
    
    private int symbolNumber;
   
    public CartoonConnector(int symbolNumber, SSESymbol start, SSESymbol end) {
        super(start, end);
        this.symbolNumber = symbolNumber;
    }
    
    public Object clone() {
        return super.clone();
    }
    
    public int getSymbolNumber() {
        return this.symbolNumber;
    }
    
    public Color getColor() {
        return Color.BLACK;
    }
    
    public Shape createShape() {
        SSESymbol start = this.getStartSSESymbol();
        SSESymbol end = this.getEndSSESymbol(); 
        Point startCenter = start.getCenter();
        Point endCenter = end.getCenter();
        
        Point startPoint;
        if (start.isDown() || start instanceof Box) {
            if (start instanceof Circle) {
                startPoint = circleBorder(startCenter, endCenter, start.getRadius());
            } else if (start instanceof EquilateralTriangle) {
                startPoint = downTriangleBorder(startCenter, endCenter, start.getRadius());
            } else {
                // else a terminal box
                //startPoint = squareBorder(startCenter, endCenter, (Rectangle2D) start.getShape());
                startPoint = circleBorder(startCenter, endCenter, start.getRadius() - 2);
            }
        } else {
            startPoint = startCenter;
        }
        
        Point endPoint;
        if (end.isDown()) {
            endPoint = endCenter;
        } else {
            if (end instanceof Circle) {
                endPoint = circleBorder(endCenter, startCenter, end.getRadius());
            } else if (end instanceof EquilateralTriangle) {
                endPoint = upTriangleBorder(endCenter, startCenter, end.getRadius());
            } else {
                //endPoint = squareBorder(endCenter, startCenter, (Rectangle2D) end.getShape());
                endPoint = circleBorder(endCenter, startCenter, end.getRadius());
            }
        }
        
        return new Line2D.Double(startPoint, endPoint);
    }
    
    public Shape createSelectionBoundary() {
        Line2D line = (Line2D) this.getShape();
        
        double x1 = line.getX1();
        double x2 = line.getX2();
        double y1 = line.getY1();
        double y2 = line.getY2();
        
        double dx = x2 - x1;
        double dy = y1 - y2;
        
        double len = Math.sqrt((dx * dx) + (dy * dy));
        
        int border = 5;
        double bx = border * (dy / len);
        double by = border * (dx / len);
        
        GeneralPath selectionBoundary = new GeneralPath();
        selectionBoundary.moveTo((float) (x1 + bx), (float) (y1 + by));
        selectionBoundary.lineTo((float) (x2 + bx), (float) (y2 + by));
        selectionBoundary.lineTo((float) (x2 - bx), (float) (y2 - by));
        selectionBoundary.lineTo((float) (x1 - bx), (float) (y1 - by));
        selectionBoundary.closePath();
        
        return selectionBoundary;
    }
    
    /* Sadly, this method doesn't work.
    private Point squareBorder(Point p1, Point p2, Rectangle2D square) {
        double x;
        double y;
        int xDiff = p2.x - p1.x;
        int yDiff = p2.y - p1.y;
        double h = square.getHeight();
        double w = square.getWidth();
        
        switch (square.outcode(p2)) {
            case Rectangle2D.OUT_TOP:
                if (xDiff == 0) {
                    x = p1.x;
                } else {
                    x = p1.x + xDiff * (p2.y / yDiff);
                }
                y = square.getY();
//                break;
            case Rectangle2D.OUT_BOTTOM:
                if (xDiff == 0) {
                    x = p1.x;
                } else {
                    x = p1.x + xDiff * ((h - p1.y) / yDiff);
                }
                y = square.getY() + h;
//                break;
            case Rectangle2D.OUT_LEFT:
                x = square.getX();
                if (yDiff == 0) {
                    y = p1.y;
                } else {
                    y = p1.y + yDiff * (p2.x / xDiff);
                }
//                break;
            case Rectangle2D.OUT_RIGHT:
                x = square.getX() + w;
                if (yDiff == 0) {
                    y = p1.y;
                } else {
                    y = p1.y + yDiff * ((w - p1.x) / xDiff);
                }
//                break;
            default:
                x = p1.x;
                y = p1.y;
        }
        return new Point((int)x, (int)y);
    }
    */
    
    /*
     *  Following methods stolen wholesale from original TopsDrawCanvas...
     */
    
    /*
     * calculates the point on a circle of centre p1 and radius r
     * which lies on the line p1->p2
     */
    private Point circleBorder(Point p1, Point p2, int r) {
        double xDiff = p2.x - p1.x;
        double yDiff = p2.y - p1.y;

        double s = this.separation(xDiff, yDiff);

        if ((s < r) || (r <= 0.0))
            return p1;

        int xb = (int) (p1.x + (r / s) * (xDiff));
        int yb = (int) (p1.y + (r / s) * (yDiff));

        return new Point(xb, yb);
    }

    /*
     * calculates the point on the border of a down equilateral
     * triangle centered at p1 and with radius (of the circumscribing circle) r,
     * which lies on the line p1->p2
     */
    private Point downTriangleBorder(Point p1, Point p2, int r) {

        double xDiff = p2.x - p1.x;
        double yDiff = p2.y - p1.y;

        /* theta in range -PI < theta <= PI */
        double theta = Math.atan2(yDiff, xDiff);

        double gamma = 0.0;
        if ((-pi6 < theta) && (theta <= pi2)) {
            gamma = theta - pi6;
        } else if ((pi2 < theta) && (theta <= pi)) {
            gamma = theta - 5.0 * pi6;
        } else if ((-pi < theta) && (theta <= -5.0 * pi6)) {
            gamma = 7.0 * pi6 + theta;
        } else if ((-5.0 * pi6 < theta) && (theta <= -pi6)) {
            gamma = theta + pi2;
        }

        double l = (r) / (2.0 * Math.cos(gamma));
        double s = this.separation(xDiff, yDiff);

        int xb = (int) (p1.x + (xDiff * l / s));
        int yb = (int) (p1.y + (yDiff * l / s));

        return new Point(xb, yb);
    }

    /*
     * calculates the point on the border of an up equilateral
     * triangle centered at p1 and with radius (of the circumscribing circle) r,
     * which lies on the line p1->p2
     */
    private Point upTriangleBorder(Point p1, Point p2, int r) {

        double xDiff = p2.x - p1.x;
        double yDiff = p2.y - p1.y;

        /* theta in range -PI < theta <= PI */
        double theta = Math.atan2(yDiff, xDiff);
        
        double gamma = 0.0;
        if ((-pi2 < theta) && (theta <= pi6)) {
            gamma = theta + pi6;
        } else if ((pi6 < theta) && (theta <= 5.0 * pi6)) {
            gamma = theta - pi2;
        } else if ((5.0 * pi6 < theta) && (theta <= pi)) {
            gamma = 7.0 * pi6 - theta;
        } else if ((-pi < theta) && (theta <= -pi2)) {
            gamma = theta + 5.0 * pi6;
        }

        double l = (r) / (2.0 * Math.cos(gamma));
        double s = this.separation(xDiff, yDiff);

        int xb = (int) (p1.x + (xDiff * l / s));
        int yb = (int) (p1.y + (yDiff * l / s));

        return new Point((int) xb, (int) yb);
    }

    private double separation(double xDiff, double yDiff) {
        return Math.sqrt((xDiff * xDiff) + (yDiff * yDiff));
    }

}
