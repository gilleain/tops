package tops.engine.juris;

import java.util.ArrayList;
import java.util.List;

import tops.engine.Edge;
import tops.engine.Vertex;

class Pattern {

    private int size;
    private int vsize;

    private int[] inserts;

    private String outsertC; // the vertex sequence from the last edge to the C terminus

    private String head;

    private Edge current;

    private List<Vertex> vertices = new ArrayList<>();

    private List<Edge> edges = new ArrayList<>();

    Pattern() {
        this.head = "head";
    }

    Pattern(String s) {
        this.deCompress(s);
        this.setIndices();
        if (!this.noEdges())
            this.current = this.edges.get(this.edges.size() - 1);
    }
    
    public String getHead() {
        return this.head;
    }
    
    public String getOutsertC() {
        return this.outsertC;
    }

    Vertex getVertex(int i) {
        return this.vertices.get(i);
    }

    Edge getEdge(int i) {
        if (i >= this.edges.size())
            return this.edges.get(this.edges.size() - 1);
        else
            return this.edges.get(i);
    }

    boolean noEdges() {
        return this.edges.isEmpty();
    }

    int size() {
        return this.edges.size();
    }

    // get the correspondance list eg : [1, 2, 4, 5]
    String getMatchString() {
        StringBuilder matchresult = new StringBuilder(" [");
        for (int i = 0; i < this.vertices.size(); ++i) {
            Vertex nxt = this.vertices.get(i);
            if (nxt.getMatch() != 0) {
                matchresult.append(i).append('-').append(nxt.getMatch())
                        .append(",");
            }
        }
        matchresult.setCharAt(matchresult.length() - 1, ']');
        return matchresult.toString();
    }

    void setInserts() {
        this.inserts = new int[this.vertices.size() - 1];
        int last = 0;
        Vertex nxt;
        for (int i = 0; i < this.vertices.size(); ++i) {
            nxt = this.vertices.get(i);
            if (nxt.getMatch() > 0) {
                this.inserts[i] = nxt.getMatch() - last;
            }
        }
    }

    /**
     * Get the pattern with inserts eg : N[0]E[1]E[2]E[1]C
     * 
     * @param d
     * @return
     */
    String getInsertString(Pattern d) {
        StringBuilder insertresult = new StringBuilder(" N");
        int last = 0;
        int isize = 0;
        for (int i = 1; i < this.vertices.size() - 1; ++i) {
            Vertex nxt = this.vertices.get(i); // get pattern vertex ref
            isize = nxt.getMatch() - last;
            if (isize > 0) {
                insertresult.append('[').append(isize - 1).append(']');
                last = nxt.getMatch();
            } else {
                int j = last + 1;
                for (; j < this.vertices.get(i + 1).getMatch(); ++j) {
                    char dt = d.vertices.get(j).getType();
                    if (nxt.getType() == dt)
                        break;
                }
                insertresult.append("[").append((j - last) - 1).append("]");
                last = j;
            }
            insertresult.append(nxt.getType());
        }
        isize = d.vsize - last;
        if (isize > 0) {
            insertresult.append('[').append(isize - 2).append(']');
        } else {
            insertresult.append("[0]");
        }
        // insertresult.append("C</Dom_Inserts>"); //finished inserts
        insertresult.append("C"); // finished inserts
        return insertresult.toString();
    }

    // get an array of strings of the unattached vertices
    String[] getInsertStringArr(Pattern d) {
        List<String> results = new ArrayList<>();
        int last = 1;
        int m = 0;
        for (int i = 1; i < this.vertices.size() - 1; ++i) {
            Vertex nxt = this.vertices.get(i); // get pattern vertex ref
            m = nxt.getMatch();
            if (m != 0) { // it IS an edge vertex, the setMatch() method is
                            // only called in the Edge class
                results.add(d.getVertexString(last, m, false));
                last = m + 1;
            } else { // it is a random, unattached vertex. do nothing
            }
        }
        results.add(d.getVertexString(last, 0, false)); // get the C-outsert
        return results.toArray(new String[0]);
    }

    String splice(String[] ins) {
        String s = ins[0];
        int l = s.length(); // this is the CUMULATIVE shift that has to be added
                            // to vertex positions
        int j;
        if (s != "") {
            for (j = 0; j < l; j++)
                this.vertices.add(j + 1, new Vertex(s.charAt(j), j + 1)); // N-outsert
        }
        Edge e;
        int lhe;
        for (int i = 1; i < ins.length - 1; i++) {
            s = ins[i];
            e = this.edges.get(i - 1);
            lhe = e.getLeftVertex().getPos();
            e.getLeftVertex().setPos(lhe + l); // shift the left-hand-end
            if (s != "") { // if there is indeed an insert!
                for (j = 0; j < s.length(); j++) {
                    this.vertices.add(j + l + 1, new Vertex(s.charAt(j), j + l + 1));
                }
                l += s.length(); // accumulate
            }
            e.getRightVertex().setPos(e.getRightVertex().getPos() + l); // l has accumulated the
                                                    // length (if any) of insert
        }
        s = ins[ins.length - 1]; // last one
        if (s != "") {
            for (j = 0; j < l; j++)
                this.vertices.add(new Vertex(s.charAt(j), j + l + 1)); // C-outsert
        }
        return this.toString();
    }

    boolean preProcess(Pattern d, boolean noInserts) {
        if (this.size > d.size)
            return false; // pattern must be smaller.
        boolean found = false;
        // matches are added to the target edge
        for (int i = 0; i < this.size; ++i) {
            Edge currentEdge = this.edges.get(i);
            for (int j = 0; j < d.size; ++j) {
                Edge targetEdge = d.edges.get(j);
                if (currentEdge.matches(targetEdge)
                        && (noInserts || this.stringComp(currentEdge, targetEdge, d))) {
                    currentEdge.addMatch(targetEdge);
                    found = true;
                }
            }
            // test to see whether new matches have been found for this edge
            if (!found)
                return false;
            found = false;
        }
        return true;
    }

    boolean stringComp(Edge p, Edge t, Pattern d) {
        String probe = this.getVertexString(p.getLeftVertex().getPos(), p.getRightVertex().getPos(),
                false);
        String target = d.getVertexString(t.getLeftVertex().getPos(), t.getRightVertex().getPos(),
                false);
        int ptrP;
        int ptrT;
        char c;
        ptrP = ptrT = 0;
        while (ptrP < probe.length()) {
            if (ptrT >= target.length())
                return false;
            c = target.charAt(ptrT);
            if (probe.charAt(ptrP) == c) {
                ptrP++;
            }
            ptrT++;
        }
        return true;
    }

    void setMovedUpTo(int k) {
        for (int i = 0; i < k; i++) {
            this.edges.get(i).moved = false;
        }
    }

    void deCompress(String s) {
        TParser parser = new TParser(s);

        this.head = parser.getName();
        char[] verts = parser.getVertices();
        String[] edgeStrings = parser.getEdges();
        for (int i = 0; i < verts.length; ++i) {
            this.vertices.add(new Vertex(verts[i], i));
            this.vsize++;
        }
        int l;
        int r;
        char t;
        Vertex left;
        Vertex right;
        for (int j = 0; j < edgeStrings.length; j += 3) {
            l = Integer.parseInt(edgeStrings[j]);
            r = Integer.parseInt(edgeStrings[j + 1]);
            t = edgeStrings[j + 2].charAt(0);
            left = this.vertices.get(l);
            left.setIndex((char) (t + 32));
            right = this.vertices.get(r);
            right.setIndex(t);
            this.edges.add(new Edge(left, right, t));
        }
        this.sortEdges();
        if (!this.edges.isEmpty()) {
            int last = this.edges.get(this.edges.size() - 1).getRightVertex().getPos();
            this.outsertC = this.getVertexString(last + 1, 0, false);
        }
    }

    void sortEdges() {
        this.size = this.edges.size();
        Edge first;
        Edge second;
        for (int i = 0; i < this.size; ++i) {
            for (int j = 0; j < this.size - 1; ++j) {
                first = this.edges.get(j);
                second = this.edges.get(j + 1);
                if (first.greaterThan(second)) {
                    this.edges.set(j, second);
                    this.edges.set(j + 1, first);
                }
            }
        }
    }

    void setIndices() {
        int il = 0;
        int jl = 0;
        int ir = 0;
        int jr = 0;
        for (int i = 0; i < this.edges.size() - 1; i++) {
            il = this.edges.get(i).getLeftVertex().getPos();
            ir = this.edges.get(i).getRightVertex().getPos();
            for (int j = i + 1; j < this.edges.size(); ++j) {
                jl = this.edges.get(j).getLeftVertex().getPos();
                jr = this.edges.get(j).getRightVertex().getPos();
                if (il == jl) {
                    edges.get(i).addS2();
                    edges.get(j).addS1();
                }

                if (ir == jl) {
                    edges.get(i).addE1();
                    edges.get(j).addS1();
                }

                if (ir == jr) {
                    edges.get(i).addE1();
                    edges.get(j).addE1();
                }
            }
        }
    }

    void reset() {
        for (int i = 0; i < this.edges.size(); ++i) {
            this.edges.get(i).reset();
        }
        for (int i = 1; i < this.vertices.size() - 1; ++i) { // don't bother with N/C
            this.vertices.get(i).resetMatch();
            this.vertices.get(i).flip();
        }
    }

    String getVertexString(int start, int end, boolean flip) {
        if (end == 0)
            end = this.vertices.size();
        if (start >= end)
            return "";
        StringBuilder vstr = new StringBuilder();
        char c;
        for (int i = start; i < end; ++i) {
            c = this.vertices.get(i).getType();
            if (flip)
                c = (Character.isUpperCase(c) ? Character.toLowerCase(c) : Character.toUpperCase(c));
            vstr.append(c);
        }
        return vstr.toString();
    }

    int[] getMatches() {
        int[] matches = new int[this.vertices.size() - 2];
        for (int i = 0; i < matches.length; ++i)
            matches[i] = this.vertices.get(i + 1).getMatch();
        return matches;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append(this.head);
        result.append(' ');

        for (int i = 0; i < this.vertices.size(); ++i) {
            result.append(this.vertices.get(i).getType());
        }
        // ensure that the pattern has a c-terminus!
        if (result.charAt(result.length() - 1) != 'C')
            result.append('C');

        result.append(' ');

        Edge e;
        for (int j = 0; j < this.edges.size(); ++j) {
            e = this.edges.get(j);
            result.append(e.getLeftVertex().getPos());
            result.append(':');
            result.append(e.getRightVertex().getPos());
            result.append(e.getType());
        }
        return result.toString();
    }

    public Pattern add(char vl, char vr, char et) {
        this.vertices.remove(0); // trim the C
        Vertex nVertex = new Vertex('N', 0); // aargh!
        Vertex l = new Vertex(vl, 1);
        Vertex r = new Vertex(vr, 2);
        this.vertices.add(nVertex);
        this.vertices.add(l);
        this.vertices.add(r);
        Edge e = new Edge(l, r, et);
        this.current = e;
        this.edges.add(e);
        this.setIndices();
        return this;
    }

    public Pattern extendRight(char vr, char et) {
        // make a new edge from the right hand pattern node
        int rightHandEnd = this.current.getRightVertex().getPos() + 1;
        Vertex r = new Vertex(vr, rightHandEnd);
        this.vertices.add(rightHandEnd, r); // add the vertex to the list
        Edge e = new Edge(this.current.getRightVertex(), r, et);
        this.current = e;
        this.edges.add(e); // add the edge
        this.setIndices();
        return this;
    }

    public Pattern extendLeft(char vr, char et) {
        // make a new edge from the left hand pattern node
        int rightHandEnd = this.current.getRightVertex().getPos() + 1;
        Vertex r = new Vertex(vr, rightHandEnd);
        this.vertices.add(rightHandEnd, r);
        Edge e = new Edge(this.current.getLeftVertex(), r, et);
        this.edges.add(e);
        this.setIndices();
        return this;
    }

    public Pattern extendMiddle(char vl, char et) {
        // make a new edge TO the right hand pattern node
        int leftHandEnd = this.current.getRightVertex().getPos(); // !
        Vertex l = new Vertex(vl, leftHandEnd);
        this.vertices.add(leftHandEnd, l);
        this.renumber(leftHandEnd, 1);
        Edge e = new Edge(l, this.current.getRightVertex(), et);
        this.current = e;
        this.edges.add(e); // sort?
        this.setIndices();
        return this;
    }

    public Pattern extendTwist(char vl, char et) {
        // make a new edge TO the right hand pattern node and TWIST
        int leftHandEnd = this.current.getRightVertex().getPos() - 2; // !
        Vertex l = new Vertex(vl, leftHandEnd);
        this.vertices.add(leftHandEnd, l);
        this.renumber(leftHandEnd, 1);
        Edge e = new Edge(l, this.current.getRightVertex(), et);
        this.edges.add(e); // sort?
        this.setIndices();
        return this;
    }

    public Pattern overlap(char vl, char vr, char et) {
        // make a new edge whose LHE is less than current's RHE.
        int leftHandEnd = this.current.getLeftVertex().getPos() + 1;
        int rightHandEnd = this.current.getRightVertex().getPos() + 1;
        Vertex l = new Vertex(vl, leftHandEnd);
        Vertex r = new Vertex(vr, rightHandEnd);
        this.vertices.add(leftHandEnd, l);
        this.vertices.add(rightHandEnd, r);
        this.renumber(leftHandEnd, 1);
        Edge e = new Edge(l, r, et);
        this.edges.add(e);
        this.setIndices();
        return this;
    }

    public Pattern insert(char vl, char vr, char et) {
        // make a new edge whose RHE is less than current's RHE.
        int leftHandEnd = this.current.getLeftVertex().getPos() + 1;
        int rightHandEnd = leftHandEnd + 1;
        Vertex l = new Vertex(vl, leftHandEnd);
        Vertex r = new Vertex(vr, rightHandEnd);
        this.vertices.add(leftHandEnd, l);
        this.vertices.add(rightHandEnd, r);
        this.renumber(rightHandEnd, 2);
        Edge e = new Edge(l, r, et);
        this.edges.add(e);
        this.setIndices();
        return this;
    }

    public Pattern append(char vl, char vr, char et) {
        // make a new edge with both ends larger than current's RHE.
        int leftHandEnd = this.current.getRightVertex().getPos() + 1;
        int rightHandEnd = leftHandEnd + 1;
        Vertex l = new Vertex(vl, leftHandEnd);
        Vertex r = new Vertex(vr, rightHandEnd);
        this.vertices.add(leftHandEnd, l);
        this.vertices.add(rightHandEnd, r);
        this.renumber(rightHandEnd, 1);
        Edge e = new Edge(l, r, et);
        this.edges.add(e);
        this.setIndices();
        return this;
    }

    public boolean canOverlap() {
        return this.current != null;
    }

    public boolean canInsert() {
        return this.current != null;
    }

    public boolean canAppend() {
        return this.current != null;
    }

    public boolean canERight(char el) {
        if (this.current == null)
            return false;
        return el == this.current.getRightVertex().getType();
    }

    public boolean canELeft(char el) {
        if (this.current == null)
            return false;
        return el == this.current.getLeftVertex().getType();
    }

    public boolean canEMiddle(char er) {
        if (this.current == null)
            return false;
        return er == this.current.getRightVertex().getType();
    }

    public boolean canETwist(char er) {
        if (this.current == null)
            return false;
        return false; // !!--need a good test for twisting--!!
    }

    private void renumber(int from, int amount) {
        Vertex counter;
        for (int i = from + 1; i < this.vertices.size() - 1; i++) {
            counter = this.vertices.get(i);
            int tmp = counter.getPos();
            counter.setPos(tmp + amount); // move up all the internal numbers!
        }
    }
}

