package tops.view.tops2D.diagram;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;

public class Term extends SSE {

    public Term(boolean b, int i) {
        super(b, i);
    }

    @Override
    public Shape createShape() {
        return new Rectangle2D.Double(this.bb.getX(), this.bb.getY(), 
                this.bb.getWidth(), this.bb.getHeight() / 2);
    }
}
