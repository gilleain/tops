package tops.view.model;

import java.awt.geom.Rectangle2D;

public class BoundsVisitor implements Shape.Visitor {

    private Rectangle2D bounds = new Rectangle2D.Double();
    
    @Override
    public void visit(Shape shape) {
        Rectangle2D shapeBounds = shape.getBounds();
        if (shapeBounds != null) {  // XXX TODO : should we allow null bounds?
            bounds.add(shapeBounds);
        }
    }
    
    public Rectangle2D getBounds() {
        return this.bounds;
    }

}
