package tops.view.diagram;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;

public class Term extends Vertex {

    public Term(int position) {
        super(position);
    }

    @Override
    public Shape createShape() {
        return new Rectangle2D.Double(this.bb.getX(), this.bb.getY(), 
                this.bb.getWidth(), this.bb.getHeight() / 2);
    }
}
