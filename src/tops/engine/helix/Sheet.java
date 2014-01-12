package tops.engine.helix;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.Vector; //why use Vector &! ArrayList?

import tops.engine.Edge;
import tops.engine.Vertex;

public class Sheet {

    private List<Vertex> vertices; // a reference to the underlying list of vertices

    private Stack<Edge> edges; // this is a stack to enable mistakes to be simply
                            // popped off the end

    private int lastInsertedVertex; // position in the List where a vertex was
                                    // last inserted

    private Vertex rhv, lhv;

    public Sheet(Vertex l, Vertex r, char type, List<Vertex> v) {
        // init variables
        this.vertices = v;
        this.edges = new Stack<Edge>();
        this.lastInsertedVertex = 0;
        this.lhv = l;
        this.rhv = r;

        // insert the vertices and create the primary edge of which this sheet
        // is composed
        // System.out.print(vertices + " adding : " + l);
        this.vertices.add(l.getPos(), l);
        // System.out.println(" " + vertices);
        // System.out.print(vertices + " adding : " + r);
        this.vertices.add(r.getPos(), r);
        // System.out.println(" " + vertices);
        this.renumberFrom(l.getPos());
        this.edges.push(new Edge(this.lhv, this.rhv, type)); // at this point, the sheet ==
                                                // this edge
    }

    public int getLEndpoint() {
        return this.lhv.getPos();
    }

    public int getREndpoint() {
        return this.rhv.getPos();
    }

    public List<Edge> getEdges() {
        return new ArrayList<Edge>(this.edges);
    }

    public boolean canExtend(int i, char typ) {
        char vtyp = ((Vertex) this.vertices.get(i)).getType();
        // System.out.println("? " + vtyp + "==" + typ + " ?");
        return (vtyp == typ);
    }

    public boolean canCyclise(int i, int j, char ityp, char jtyp) {
        // doesn't actually matter what type the edge is, so long as there is
        // not another at those positions!
        if (this.edgesContains(new Edge(new Vertex(ityp, i), new Vertex(jtyp, j),
                'P')))
            return false;
        return (((Vertex) this.vertices.get(i)).getType() == ityp && ((Vertex) this.vertices
                .get(j)).getType() == jtyp);
    }

    // why doesn't the edges Stack use its 'contains' method right?
    private boolean edgesContains(Edge dat) {
        Iterator<Edge> itr = this.edges.iterator();
        while (itr.hasNext()) {
            Edge e = (Edge) itr.next();
            // System.out.println("edge : " + e + " and edge : " + dat + " " +
            // e.equals(dat));
            if (e.equals(dat))
                return true;
        }
        return false;
    }

    public void insertAt(int i, char vtype) {
        this.insertBefore(i + 1, vtype);
    } // hohoho.

    public void insertBefore(int i, char vtype) {
        // System.out.print(vertices + " adding : " + vtype + ":" + i);
        this.vertices.add(i, new Vertex(vtype, i));
        // System.out.println(" " + vertices);
        this.lastInsertedVertex = i;
        // System.out.println(" inserting before i = " + i + " lhe = " +
        // getLEndpoint() + " rhe = " + getREndpoint());
        if (i == this.getLEndpoint())
            this.lhv = (Vertex) this.vertices.get(i);
        if (i > this.getREndpoint())
            this.rhv = (Vertex) this.vertices.get(i);
        this.renumberFrom(i + 1);
    }

    public void extend(int i, int j, char etype) {
        this.edges.push(this.makeEdge(i, j, etype));
    }

    private Edge makeEdge(int i, int j, char e) {
        Vertex l = (Vertex) this.vertices.get(i); // get the current vertex to
                                                // extend from
        Vertex r = (Vertex) this.vertices.get(j); // get the next vertex to be
                                                // extended to
        return new Edge(l, r, e);
    }

    public void undoLastCycle() {
        if (!this.edges.isEmpty())
            this.edges.pop();
    }

    public void undoLastMove() {
        if (!this.edges.isEmpty())
            this.edges.pop();
        if (this.lastInsertedVertex == this.lhv.getPos())
            this.lhv = (Vertex) this.vertices.get(this.lastInsertedVertex + 1);
        if (this.lastInsertedVertex == this.rhv.getPos())
            this.rhv = (Vertex) this.vertices.get(this.lastInsertedVertex + 1);
        this.vertices.remove(this.lastInsertedVertex);
        // System.out.println("lastInsertedVertex = " + lastInsertedVertex);
        this.renumberFrom(this.lastInsertedVertex);
    }

    public void remove() {
        int lhe = this.lhv.getPos();
        // System.out.println(vertices + " removing : " + lhv);
        this.vertices.remove(lhe);
        // System.out.println(vertices + " removing : " + rhv);
        this.vertices.remove(this.rhv.getPos() - 1);
        this.renumberFrom(lhe);
    }

    private void renumberFrom(int i) {
        int p = this.vertices.size();
        for (int j = i; j < p; j++) {
            // System.out.println("renumbering : " + j);
            ((Vertex) (this.vertices.get(j))).setPos(j);
        }
    }

    @Override
    public String toString() {
        return new String("vertices = " + this.vertices.toString() + ", edges = "
                + this.edges.toString());
    }

    public static void main(String[] args) {
        List<Vertex> v = new Vector<Vertex>();
        v.add(new Vertex('N', 0));
        v.add(new Vertex('C', 1));

        Sheet s = new Sheet(new Vertex('E', 1), new Vertex('E', 2), 'P', v); // create
                                                                                // a
                                                                                // sheet
                                                                                // composed
                                                                                // of a
                                                                                // P
                                                                                // edge
        // System.out.println("Before extension: " + s.toString());
        s.insertBefore(2, 'e');
        s.extend(2, 3, 'A'); // define the limits of the extension?
        // System.out.println("After extension : " + s.toString());
        s.undoLastMove();
        // System.out.println("After rewind : " + s.toString());
    }
}
