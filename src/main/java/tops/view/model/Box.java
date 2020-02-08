package tops.view.model;

import java.awt.geom.Rectangle2D;

import javax.vecmath.Point2d;

public class Box implements Shape {
    
    private Point2d center;
    
    private double width;
    
    private double height;
    
    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public Box(Point2d center) {
        this.center = center;
    }

    @Override
    public Point2d getCenter() {
        return center;
    }
    
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Rectangle2D getBounds() {
        double w2 = width / 2.0;
        double h2 = height / 2.0;
        return new Rectangle2D.Double(center.x - w2, center.y - h2, width, height);
    }
    

}
