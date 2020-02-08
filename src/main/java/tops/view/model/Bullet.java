package tops.view.model;

import java.awt.geom.Rectangle2D;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

public class Bullet implements Shape {
    
    private Vector2d orientation;
    
    private Point2d center;
    
    public Bullet(Vector2d orientation, Point2d center) {
        this.orientation = orientation;
        this.center = center;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
    
    @Override
    public Point2d getCenter() {
        return center;
    }

    @Override
    public Rectangle2D getBounds() {
        // TODO Auto-generated method stub
        return null;
    }
}
