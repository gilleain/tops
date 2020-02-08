package tops.view.model;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

/**
 * An arc shape defined by a start, end, height, and orientation.
 * 
 * @author gilleain
 *
 */
public class Arc implements Shape {
    
    public Vector2d getOrientation() {
        return orientation;
    }

    public Point2d getStart() {
        return start;
    }

    public Point2d getEnd() {
        return end;
    }

    public Color getColor() {
        return color;
    }
    
    public double getHeight() {
        return this.height;
    }

    private Vector2d orientation;
    
    private Point2d start;
    
    private Point2d end;
    
    private double height;
    
    private Color color;

    public Arc(Vector2d orientation, Point2d start, Point2d end, double height, Color color) {
        super();
        this.orientation = orientation;
        this.start = start;
        this.end = end;
        this.height = height;
        this.color = color;
    }
    
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Point2d getCenter() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public Rectangle2D getBounds() {
        double width = end.x - start.x;
        double x = start.x;
        double y = start.y - height;    // XXX ?
        return new Rectangle2D.Double(x, y, width, height);
    }

}
