package tops.engine.drg;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import tops.engine.Edge;
import tops.engine.Vertex;

public class Sheet {

    private ArrayList<Vertex> vertices; // a reference to the underlying list of vertices

    private Stack<Edge> edges; // a stack to enable mistakes to be popped off

    private int lastInsertedVertex; // last vertex insertion point in the List 

    private Vertex rhv, lhv;

    public Sheet(Vertex l, Vertex r, char type, ArrayList<Vertex> v) {
        this.vertices = v;
        this.edges = new Stack<Edge>();
        this.lastInsertedVertex = 0;
        this.lhv = l;
        this.rhv = r;

        // insert the vertices and create the primary 
        // edge of which this sheet is composed
        this.vertices.add(l.getPos(), l);
        this.vertices.add(r.getPos(), r);
        this.renumberFrom(l.getPos());
        
        // at this point, the sheet == this edge
        this.edges.push(new Edge(this.lhv, this.rhv, type)); 
        
    }

    public int getLEndpoint() {
        return this.lhv.getPos();
    }

    public int getREndpoint() {
        return this.rhv.getPos();
    }

    public ArrayList<Edge> getEdges() {
        return new ArrayList<Edge>(this.edges);
    }

    public boolean canExtend(int i, char typ) {
        char vtyp = ((Vertex) this.vertices.get(i)).getType();
        return (vtyp == typ);
    }

    public boolean canCyclise(int i, int j, char ityp, char jtyp) {
        // doesn't actually matter what type the edge is, so long as there is
        // not another at those positions!
    	Edge edge = new Edge(new Vertex(ityp, i), new Vertex(jtyp, j), 'P');
        if (this.edgesContains(edge)) {
            return false;
        }
        return (((Vertex) this.vertices.get(i)).getType() == ityp 
        		&& ((Vertex) this.vertices.get(j)).getType() == jtyp);
    }

    // why doesn't the edges Stack use its 'contains' method right?
    private boolean edgesContains(Edge edge) {
        Iterator<Edge> itr = this.edges.iterator();
        while (itr.hasNext()) {
            Edge e = (Edge) itr.next();
            if (e.equals(edge)) {
            	return true;
            }
        }
        return false;
    }

    public void insertAt(int i, char vtype) {
        this.insertBefore(i + 1, vtype);
    }

    public void insertBefore(int i, char vtype) {
        this.vertices.add(i, new Vertex(vtype, i));
        this.lastInsertedVertex = i;
        if (i == this.getLEndpoint()) {
            this.lhv = (Vertex) this.vertices.get(i);
        }
        if (i > this.getREndpoint()) {
            this.rhv = (Vertex) this.vertices.get(i);
        }
        this.renumberFrom(i + 1);
    }

    public void extend(int i, int j, char etype) {
        this.edges.push(this.makeEdge(i, j, etype));
    }

    private Edge makeEdge(int i, int j, char e) {
    	 // get the current vertex to extend from
        Vertex l = (Vertex) this.vertices.get(i);
                                                
        // get the next vertex to be extended to
        Vertex r = (Vertex) this.vertices.get(j);
                                                 
        return new Edge(l, r, e);
    }

    public void undoLastCycle() {
        if (!this.edges.isEmpty())
            this.edges.pop();
    }

    public void undoLastMove() {
        if (!this.edges.isEmpty()) {
            this.edges.pop();
        }
        
        if (this.lastInsertedVertex == this.lhv.getPos()) {
            this.lhv = (Vertex) this.vertices.get(this.lastInsertedVertex + 1);
        }
        
        if (this.lastInsertedVertex == this.rhv.getPos()) {
            this.rhv = (Vertex) this.vertices.get(this.lastInsertedVertex + 1);
        }
        
        this.vertices.remove(this.lastInsertedVertex);
        this.renumberFrom(this.lastInsertedVertex);
    }

    public void remove() {
        int lhe = this.lhv.getPos();
        this.vertices.remove(lhe);
        this.vertices.remove(this.rhv.getPos() - 1);
        this.renumberFrom(lhe);
    }

    private void renumberFrom(int i) {
        int p = this.vertices.size();
        for (int j = i; j < p; j++) {
            ((Vertex) (this.vertices.get(j))).setPos(j);
        }
    }

    @Override
    public String toString() {
        return new String("vertices = " + this.vertices.toString() + 
        		", edges = " + this.edges.toString());
    }

    public static void main(String[] args) {
        ArrayList<Vertex> v = new ArrayList<Vertex>();
        v.add(new Vertex('N', 0));
        v.add(new Vertex('C', 1));
        
        // create a sheet composed of a P edge
        Sheet s = new Sheet(new Vertex('E', 1), new Vertex('E', 2), 'P', v);
        
        System.out.println("Before extension: " + s.toString());
        s.insertBefore(2, 'e');
        
        s.extend(2, 3, 'A'); // define the limits of the extension?
        System.out.println("After extension : " + s.toString());
        
        s.undoLastMove();
        System.out.println("After rewind : " + s.toString());
    }
}
