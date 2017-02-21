package tops.view.diagram;

import java.awt.Shape;
import java.awt.geom.Arc2D;

public class Helix extends OrientedVertex {

    public Helix(boolean isDown, int position) {
        super(position, isDown);
    }

    public Shape createShape() {
        int xtnt = (this.isDown()) ? -180 : 180;
        return new Arc2D.Double(this.bb, 0, xtnt, Arc2D.CHORD);
    }
}
