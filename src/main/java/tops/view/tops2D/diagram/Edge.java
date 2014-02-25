package tops.view.tops2D.diagram;

import java.awt.Color;
import java.awt.Shape;


abstract class Edge {

    public static final int LEFT_CHIRAL = 0;

    public static final int RIGHT_CHIRAL = 1;

    public static final int PARALLEL_HBOND = 2;

    public static final int ANTIPARALLEL_HBOND = 3;

    public final int type;

    protected SSE left, right;

    protected Shape s; // could be a shape?

    public Edge(SSE left, SSE right, int type) {
        this.left = left;
        this.right = right;
        this.type = type;
    }

    public abstract Shape createShape(double axis);

    public abstract Color getColor();

    public Shape getShape(double axis) {
        if (this.s == null) {
            this.s = this.createShape(axis);
        }
        return this.s;
    }
}