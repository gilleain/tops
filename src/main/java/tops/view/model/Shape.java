package tops.view.model;

import java.awt.geom.Rectangle2D;

import javax.vecmath.Point2d;

public interface Shape {
    
    Point2d getCenter();
    
    Rectangle2D getBounds();
    
    void accept(Visitor visitor);
    
    public interface Visitor {
        
        void visit(Shape shape);
    }

}
