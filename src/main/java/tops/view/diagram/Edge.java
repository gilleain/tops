package tops.view.diagram;

import java.awt.Color;
import java.awt.Shape;


abstract class Edge {
    
    public enum Type {
        LEFT_CHIRAL('L'),
        RIGHT_CHIRAL('R'),
        PARALLEL_HBOND('P'),
        ANTIPARALLEL_HBOND('A');
        
        private char code;
        
        private Type(char code) {
            this.code = code;
        }
        
        public char getCode() {
            return this.code;
        }
    }

    public final Type type;

    protected Vertex left, right;

    protected Shape s; // could be a shape?

    public Edge(Vertex left, Vertex right, Type type) {
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
