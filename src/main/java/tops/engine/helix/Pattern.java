package tops.engine.helix;

import java.util.ArrayList;
import java.util.List;

import tops.engine.Edge;
import tops.engine.PatternI;
import tops.engine.TParser;
import tops.engine.TopsStringFormatException;
import tops.engine.Vertex;

public class Pattern implements PatternI {

    private String outsertC; // the vertex sequence from the last edge to the C
                        // terminus

    private String outsertN; // the vertex sequence from the N terminus to the last
                        // edge

    private String head;

    private String classification;

    private Edge currentChiralEdge;

    private List<Vertex> vertices = new ArrayList<Vertex>();

    private List<Edge> edges = new ArrayList<Edge>();

    public Pattern() {
        this.head = new String("pattern");
    }

    public Pattern(String s) throws TopsStringFormatException {
        this.deCompress(s);
        this.setIndices();
    }

    public int getNumberOfHBonds() {
        int numberOfHBonds = 0;
        for (int i = 0; i < this.edges.size(); i++) {
            Edge e = (Edge) this.edges.get(i);
            if (e.isHBond()) {
                numberOfHBonds++;
            }
        }
        return numberOfHBonds;
    }

    public int getNumberOfHPP() {
        int numberOfHBonds = 0;
        for (int i = 0; i < this.edges.size(); i++) {
            Edge e = (Edge) this.edges.get(i);
            if (e.isHPP()) {
                numberOfHBonds++;
            }
        }
        return numberOfHBonds;
    }

    public int getNumberOfChirals() {
        int numberOfChirals = 0;
        for (int i = 0; i < this.edges.size(); i++) {
            Edge e = (Edge) this.edges.get(i);
            if (e.isChiral()) {
                numberOfChirals++;
            }
        }
        return numberOfChirals;
    }

    public String getName() {
        return this.head;
    }

    public String getClassification() {
        return this.classification;
    }

    private String flipString(String toFlip) {
        StringBuffer toReturn = new StringBuffer();
        for (int i = 0; i < toFlip.length(); i++) {
            char c = toFlip.charAt(i);
            if (c >= 97)
                c -= 32;
            else
                c += 32;
            toReturn.append(c);
        }
        return toReturn.toString();
    }

    public String getOutsertN(boolean f) {
        return (f) ? this.flipString(this.outsertN) : this.outsertN;
    }

    public String getOutsertC(boolean f) {
        return (f) ? this.flipString(this.outsertC) : this.outsertC;
    }

    public void rename(String n) {
        this.head = n;
    }

    public int getCTermPosition() {
        return this.vertices.size() - 1;
    }

    public boolean addChiral(int i, int j, char c) {
        int existingPos = this.edgesContains(i, j);
        if (existingPos != -1) {
            Edge existingEdge = this.getEdge(existingPos);
            char existingEdgeType = existingEdge.getType();// the type of the
                                                            // edge (if any)
                                                            // between i & j
            if (existingEdgeType == 'P') {
                c = (c == 'R') ? 'Z' : 'X'; // make mixed type
                this.currentChiralEdge = new Edge(this.getVertex(i), this.getVertex(j), c);
                existingEdge.setType(c);
            }
            // horrible kludge to avoid A/L and A/R types
            else if (existingEdgeType == 'A') {
                return false;
            }
            // problem : edge exists, but it already has chirality!
            else {
                System.out.println("edge between " + i + " and " + j
                        + " not A or P! ");
                return false;
            }
        } else { // else, no existing edge - add new one
            Vertex lvert = (Vertex) this.vertices.get(i);
            Vertex rvert = (Vertex) this.vertices.get(j);
            Edge ne = new Edge(lvert, rvert, c);
            this.edges.add(ne);
            this.currentChiralEdge = ne;
        }
        return true;
    }

    public int edgesContains(int i, int j) {
        for (int e = 0; e < this.edges.size(); e++) {
            Edge nextEdge = (Edge) this.edges.get(e);
            if (nextEdge.atPosition(i, j)) {
                return e;
            }
        }
        return -1; // not found
    }

    public void removeLastChiral() {
        char ctype = this.currentChiralEdge.getType();
        if ((ctype == 'R') || (ctype == 'L')) {
            this.edges.remove(this.currentChiralEdge);
        } else {
            int l = this.currentChiralEdge.getLeft();
            int r = this.currentChiralEdge.getRight();
            int edgeAtPos = this.edgesContains(l, r);
            ((Edge) this.edges.get(edgeAtPos)).setType('P'); // it will only ever be
                                                        // P!
        }
    }

    public void addEdges(List<Edge> newEdges) {
        this.edges.addAll(newEdges);
    }

    public void addVertices(List<Vertex> newVertices) {
        this.vertices.addAll(newVertices);
    }

    public Vertex getVertex(int i) {
        return (Vertex) (this.vertices.get(i));
    }

    public Edge getEdge(int i) {
        if (i >= this.edges.size())
            return (Edge) (this.edges.get(this.edges.size() - 1));
        else
            return (Edge) (this.edges.get(i));
    }

    public boolean noEdges() {
        return this.edges.isEmpty();
    }

    public int vsize() {
        return this.vertices.size();
    }

    public int esize() {
        return this.edges.size();
    }

    // get the simple list of matching
    public String getVertexMatchedString() {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < this.vertices.size(); ++i) {
            Vertex nxt = (Vertex) this.vertices.get(i);
            int nxtMatch = nxt.getMatch();
            if (nxtMatch != 0) {
                result.append(nxtMatch).append('.');
            }
        }
        result.deleteCharAt(result.length() - 1); // remove the last dot
        return result.toString();
    }

    // get the correspondance list eg : [1, 2, 4, 5]
    public String getMatchString() {
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

    public void setInserts() {
        int[] inserts = new int[this.vertices.size() - 1];
        int last = 0;
        Vertex nxt;
        for (int i = 0; i < this.vertices.size(); ++i) {
            nxt = this.vertices.get(i);
            if (nxt.getMatch() > 0) {
                inserts[i] = nxt.getMatch() - last;
            }
        }
    }

    // get the pattern with inserts eg : N[0]E[1]E[2]E[1]C
    public String getInsertString(PatternI d) {
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
                for (; j < (this.vertices.get(i + 1)).getMatch(); ++j) {
                    char dt = d.getVertex(j).getType();
                    if (nxt.getType() == dt)
                        break;
                }
                insertresult.append("[").append((j - last) - 1).append("]");
                last = j;
            }
            insertresult.append(nxt.getType());
        }
        isize = d.vsize() - last;
        if (isize > 0)
            insertresult.append('[').append(isize - 2).append(']');
        else
            insertresult.append("[0]");
        // insertresult.append("C</Dom_Inserts>"); //finished inserts
        insertresult.append("C"); // finished inserts
        return insertresult.toString();
    }

    // get an array of strings of the unattached vertices
    public String[] getInsertStringArr(PatternI d, boolean flip) { // 'd' is an
                                                            // EXAMPLE, not a
                                                            // pattern
        List<String> results = new ArrayList<>();
        int last = 1;
        int m = 0;
        for (int i = 1; i < this.vertices.size() - 1; ++i) {
            Vertex nxt = this.vertices.get(i); // get pattern vertex ref
            m = nxt.getMatch();
            if (m != 0) { // it IS an edge vertex, the setMatch() method is
                            // only called in the Edge class
                results.add(d.getVertexString(last, m, flip));
                last = m + 1;
            } else { // it is a random, unattached vertex. do nothing
                results.add("");
            }
        }
        String cOut = d.getVertexString(last, 0, flip);
        results.add(cOut); // get the C-outsert
        return results.toArray(new String[0]);
    }

    public String splice(String[] ins) {
        String s;
        int offset = 1;
        int pos;
        Vertex v;

        for (int i = 0; i < ins.length; i++) {
            s = ins[i];
            for (int j = 0; j < s.length(); j++) {
                v = new Vertex(s.charAt(j), j + 1);
                pos = i + offset;
                this.vertices.add(pos, v);
                this.renumber(pos, 1);
                offset++;
            }
        }
        return this.toString();
    }

    // this method could replace the /splice/ method above.
    // except that splice actually adds new vertices...
    public String mergeInsertArrayWithVertices(String[] ins) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < ins.length; i++) {
            ret.append((this.vertices.get(i)).getType());
            ret.append(ins[i]);
        }
        ret.append('C');
        return ret.toString();
    }

    public boolean preProcess(PatternI d) {
        if (this.esize() > d.esize()) {
            return false; // pattern must be smaller.
        }
        boolean found = false;
        Edge target;
        // matches are added to the target edge
        for (Edge current : edges) {
            for (int j = 0; j < d.esize(); ++j) {
                target = d.getEdge(j);
                if ((current.matches(target)) && stringComp(current, target, d)) {
                    current.addMatch(target);
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

    public boolean stringComp(Edge p, Edge t, PatternI d) {
        String innerProbe = getVertexString(
        		p.getLeftVertex().getPos(), p.getRightVertex().getPos(), false);
        
        String outerProbeLeft = getVertexString(0, p.getLeftVertex().getPos(), false);
        String outerProbeRight = getVertexString(p.getRightVertex().getPos(), 0, false);

        String innerTarget = d.getVertexString(
        		t.getLeftVertex().getPos(), t.getRightVertex().getPos(), false);
        String outerTargetLeft = d.getVertexString(0, t.getLeftVertex().getPos(), false);
        String outerTargetRight = d.getVertexString(t.getRightVertex().getPos(), 0, false);

        return this.subSequenceCompare(innerProbe, innerTarget)
                && this.subSequenceCompare(outerProbeLeft, outerTargetLeft)
                && this.subSequenceCompare(outerProbeRight, outerTargetRight);
    }

    public boolean subSequenceCompare(String a, String b) {
        int ptrA;
        int ptrB;
        char c;
        ptrA = ptrB = 0;
        while (ptrA < a.length()) {
            if (ptrB >= b.length())
                return false;
            c = b.charAt(ptrB);
            if (a.charAt(ptrA) == c) {
                ptrA++;
            }
            ptrB++;
        }
        return true;
    }

    public void setMovedUpTo(int k) {
        for (int i = 0; i < k; ++i) {
            (this.edges.get(i)).moved = false;
        }
    }

    public void deCompress(String s) throws TopsStringFormatException {
        try {
            TParser parser = new TParser(s);

            this.head = parser.getName();
            char[] verts = parser.getVertices();
            String[] edgeStrings = parser.getEdgesAsStrings();
            this.classification = parser.getClassification();
            for (int i = 0; i < verts.length; ++i) {
                this.vertices.add(new Vertex(verts[i], i));
            }
            int l;
            int r;
            char t;
            Vertex left;
            Vertex right;
            int j = 0;

            for (; j < edgeStrings.length; j += 3) {
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
                int first = edges.get(0).getLeftVertex().getPos();
                int last = edges.get(edges.size() - 1).getRightVertex().getPos();
                this.outsertN = this.getVertexString(0, first, false);
                this.outsertC = this.getVertexString(last + 1, 0, false);
            }
        } catch (Exception e) {
            throw new TopsStringFormatException(s + " " + e.toString());
        }
    }

    public void sortEdges() {
        int size = this.edges.size();
        Edge first;
        Edge second;
        for (int i = 0; i < size; ++i) {
            for (int j = 0; j < size - 1; ++j) {
                first = edges.get(j);
                second = edges.get(j + 1);
                if (first.greaterThan(second)) {
                    this.edges.set(j, second);
                    this.edges.set(j + 1, first);
                }
            }
        }
    }

    public void setIndices() {
        int il = 0;
        int jl = 0;
        int ir = 0;
        int jr = 0;
        for (int i = 0; i < this.edges.size() - 1; i++) {
            il = edges.get(i).getLeftVertex().getPos();
            ir = edges.get(i).getRightVertex().getPos();
            for (int j = i + 1; j < this.edges.size(); ++j) {
                jl = edges.get(j).getLeftVertex().getPos();
                jr = edges.get(j).getRightVertex().getPos();
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

    public void reset() {
        for (int i = 0; i < this.edges.size(); ++i) {
            this.edges.get(i).reset();
        }
        for (int i = 1; i < this.vertices.size() - 1; ++i) { // don't bother with N/C
            this.vertices.get(i).resetMatch();
            this.vertices.get(i).flip();
        }
    }

    public String getVertexString(int start, int end, boolean flip) {
        if (end == 0)
            end = this.vertices.size() - 1; // miss 'C'
        if (start == 0)
            start = 1; // miss 'N'
        if ((start >= end) || (end > this.vertices.size()))
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

    public boolean isNullPattern() {
        return (this.vertices.size() < 3);
    }

    @Override
    public int[] getMatches() {
        int[] matches = new int[vertices.size() - 2];
        for (int i = 0; i < matches.length; ++i) {
            matches[i] = vertices.get(i + 1).getMatch();
        }
        return matches;
    }
    
    @Override
    public boolean verticesIncrease() {
        int last = 0;
        int[] m = getMatches();
        for (int i = 0; i < m.length; i++) {
            if (m[i] != 0) {
                if (m[i] <= last) {
                    return false;
                } else {
                    last = m[i];
                }
            }
        }
        return true;
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

    private void renumber(int from, int amount) {
        Vertex counter;
        for (int i = from + 1; i < this.vertices.size() - 1; i++) {
            counter = this.vertices.get(i);
            int tmp = counter.getPos();
            counter.setPos(tmp + amount); // move up all the internal numbers!
        }
    }

	@Override
	public boolean subSequenceCompare(int i, int right, int j, int right2,
			PatternI d, boolean b) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getLastEdgeVertexPosition() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int indexOfFirstUnmatchedEdge() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean stringMatch(PatternI diagram, boolean flip) {
		// TODO Auto-generated method stub
		return false;
	}
}

