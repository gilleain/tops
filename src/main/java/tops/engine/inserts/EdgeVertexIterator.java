package tops.engine.inserts;

import java.util.Iterator;
import java.util.TreeSet;

import tops.engine.Edge;
import tops.engine.Vertex;

/**
 * This Iterator provides a view on a Pattern's vertices through its Edges. It
 * takes a reference to an Iterator that is a view on the edges. The edges MUST
 * be sorted.
 */
public class EdgeVertexIterator implements Iterator<Vertex> {

    private TreeSet<Vertex> edgeVertices;

    private Iterator<Vertex> vertexIterator;

    public EdgeVertexIterator(Iterator<Edge> edgeIterator) {
        this.edgeVertices = new TreeSet<>();
        while (edgeIterator.hasNext()) {
            Edge e = edgeIterator.next();

            Vertex left = e.getLeftVertex();
            this.edgeVertices.add(left);

            Vertex right = e.getRightVertex();
            this.edgeVertices.add(right);
        }
        this.vertexIterator = this.edgeVertices.iterator();
    }

    public boolean hasNext() {
        return this.vertexIterator.hasNext();
    }

    public Vertex next() {
        return this.vertexIterator.next();
    }

    @Override
    public void remove() {
        // do nothing
    }
}
