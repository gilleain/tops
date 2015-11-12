package tops.view.tops2D.diagram;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Arc2D;


public class Chiral extends Edge {

    public Chiral(Vertex left, Vertex right, int type) {
        super(left, right, type);
    }

    public Shape createShape(double axis) {
        double x = this.left.getCenter();
        double w = this.right.getCenter() - x;
        double h = w / 2;
        double y = axis - (h / 2);

        return new Arc2D.Double(x, y + this.left.getSize(), w, h, 180, 180, Arc2D.OPEN);
    }

    public Color getColor() {
        if (this.type == Edge.LEFT_CHIRAL) {
            return Color.orange;
        } else {
            return Color.blue;
        }
    }
}
