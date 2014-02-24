package tops.view.tops2D.diagram;

import java.awt.Shape;
import java.awt.geom.Arc2D;

public class Helix extends SSE {

    public Helix(boolean b, int i) {
        super(b, i);
    }

    public Shape createShape() {
        int xtnt = (this.isDown) ? -180 : 180;
        return new Arc2D.Double(this.bb, 0, xtnt, Arc2D.CHORD);
    }
}
