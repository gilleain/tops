package tops.view.model;

import java.awt.geom.Rectangle2D;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

public class Triangle implements Shape {
    
    private Vector2d orientation;
    
    private Point2d center;
    
    private double size;
    
    public Triangle(Vector2d orientation, Point2d center, double size) {
        this.orientation = orientation;
        this.center = center;
        this.size = size;
    }

    @Override
    public Point2d getCenter() {
        return center;
    }
    
    public Vector2d getOrientation() {
        return this.orientation;
    }
    
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Rectangle2D getBounds() {
        return new Rectangle2D.Double(center.x - size, center.y - size, size, size);
    }

}
