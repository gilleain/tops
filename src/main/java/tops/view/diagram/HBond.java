package tops.view.diagram;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Arc2D;


public class HBond extends Edge {

    public HBond(Vertex left, Vertex right, Type type) {
        super(left, right, type);
    }

    @Override
    public Shape createShape(double axis) {
        double x = this.left.getCenter();
        double w = this.right.getCenter() - x;
        double h = w / 2;
        double y = axis - (h / 2);

        return new Arc2D.Double(x, y, w, h, 0, 180, Arc2D.OPEN);
    }

    @Override
    public Color getColor() {
        if (this.type == Edge.Type.PARALLEL_HBOND) {
            return Color.red;
        } else {
            return Color.green;
        }
    }
}
