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

    private TreeSet<Vertex> edge_vertices;

    private Iterator<Vertex> vertexIterator;

    public EdgeVertexIterator(Iterator<Edge> edgeIterator) {
        this.edge_vertices = new TreeSet<Vertex>();
        while (edgeIterator.hasNext()) {
            Edge e = (Edge) edgeIterator.next();

            Vertex left = e.getLeftVertex();
            this.edge_vertices.add(left);

            Vertex right = e.getRightVertex();
            this.edge_vertices.add(right);
        }
        this.vertexIterator = this.edge_vertices.iterator();
    }

    public boolean hasNext() {
        return this.vertexIterator.hasNext();
    }

    public Vertex next() {
        return this.vertexIterator.next();
    }

    public void remove() {
    }
}
