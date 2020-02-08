package tops.view.model;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2d;

public class ShapeList implements Shape {
    
    private List<Shape> shapes = new ArrayList<>();
    
    public void add(Shape shape) {
        shapes.add(shape);
    }
    
    public Shape get(int index) {
        return shapes.get(index);
    }

    @Override
    public Point2d getCenter() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
        for (Shape shape : shapes) {
            shape.accept(visitor);
        }
    }

    @Override
    public Rectangle2D getBounds() {
        // TODO Auto-generated method stub
        return null;
    }

}
