package tops.engine.helix;

import java.util.ArrayList;
import java.util.List;

import tops.engine.Edge;
import tops.engine.TopsStringFormatException;
import tops.engine.TParser;
import tops.engine.Vertex;

public class Pattern {

    int size, vsize;

    int[] inserts;

    String outsertC; // the vertex sequence from the last edge to the C
                        // terminus

    String outsertN; // the vertex sequence from the N terminus to the last
                        // edge

    String head;

    String classification;

    Edge current;

    Edge currentChiralEdge;

    List<Vertex> vertices = new ArrayList<Vertex>();

    List<Edge> edges = new ArrayList<Edge>();

    public Pattern() {
        this.head = new String("pattern");
    }

    public Pattern(String s) throws TopsStringFormatException {
        this.deCompress(s);
        this.setIndices();
        if (!this.noEdges())
            this.current = (Edge) this.edges.get(this.edges.size() - 1);
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

    void removeLastChiral() {
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

    void addEdges(List<Edge> newEdges) {
        this.edges.addAll(newEdges);
    }

    void addVertices(List<Vertex> newVertices) {
        this.vertices.addAll(newVertices);
    }

    Vertex getVertex(int i) {
        return (Vertex) (this.vertices.get(i));
    }

    Edge getEdge(int i) {
        if (i >= this.edges.size())
            return (Edge) (this.edges.get(this.edges.size() - 1));
        else
            return (Edge) (this.edges.get(i));
    }

    boolean noEdges() {
        return this.edges.isEmpty();
    }

    int vsize() {
        return this.vertices.size();
    }

    int esize() {
        return this.edges.size();
    }

    // get the simple list of matching
    String getVertexMatchedString() {
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
    String getMatchString() {
        // StringBuffer matchresult = new StringBuffer("<Corr>[");
        StringBuffer matchresult = new StringBuffer(" [");
        for (int i = 0; i < this.vertices.size(); ++i) {
            Vertex nxt = (Vertex) this.vertices.get(i);
            if (nxt.getMatch() != 0) {
                matchresult.append(i).append('-').append(nxt.getMatch())
                        .append(",");
            }
        }
        matchresult.setCharAt(matchresult.length() - 1, ']');
        // matchresult.deleteCharAt(matchresult.length() -1);
        // matchresult.append("</Corr>");
        return matchresult.toString();
    }

    void setInserts() {
        this.inserts = new int[this.vertices.size() - 1];
        int last = 0;
        Vertex nxt;
        for (int i = 0; i < this.vertices.size(); ++i) {
            nxt = (Vertex) this.vertices.get(i);
            if (nxt.getMatch() > 0) {
                this.inserts[i] = nxt.getMatch() - last;
            } else {

            }
        }
    }

    // get the pattern with inserts eg : N[0]E[1]E[2]E[1]C
    String getInsertString(Pattern d) {
        StringBuffer insertresult = new StringBuffer(" N");
        int last = 0;
        int isize = 0;
        for (int i = 1; i < this.vertices.size() - 1; ++i) {
            Vertex nxt = (Vertex) this.vertices.get(i); // get pattern vertex ref
            isize = nxt.getMatch() - last;
            if (isize > 0) {
                insertresult.append('[').append(isize - 1).append(']');
                last = nxt.getMatch();
            } else {
                int j = last + 1;
                for (; j < ((Vertex) this.vertices.get(i + 1)).getMatch(); ++j) {
                    char dt = ((Vertex) d.vertices.get(j)).getType();
                    if (nxt.getType() == dt)
                        break;
                }
                insertresult.append("[").append((j - last) - 1).append("]");
                last = j;
            }
            insertresult.append(nxt.getType());
        }
        isize = d.vsize - last;
        // System.out.println("isize = " + isize + " last = " + last);
        if (isize > 0)
            insertresult.append('[').append(isize - 2).append(']');
        else
            insertresult.append("[0]");
        // insertresult.append("C</Dom_Inserts>"); //finished inserts
        insertresult.append("C"); // finished inserts
        return insertresult.toString();
    }

    // get an array of strings of the unattached vertices
    String[] getInsertStringArr(Pattern d, boolean flip) { // 'd' is an
                                                            // EXAMPLE, not a
                                                            // pattern
        List<String> results = new ArrayList<String>();
        int last = 1;
        int m = 0;
        for (int i = 1; i < this.vertices.size() - 1; ++i) {
            Vertex nxt = (Vertex) this.vertices.get(i); // get pattern vertex ref
            m = nxt.getMatch();
            if (m != 0) { // it IS an edge vertex, the setMatch() method is
                            // only called in the Edge class
                results.add(d.getVertexString(last, m, flip));
                last = m + 1;
            } else { // it is a random, unattached vertex. do nothing
                results.add(new String());
            }
        }
        String cOut = d.getVertexString(last, 0, flip);
        results.add(cOut); // get the C-outsert
        return (String[]) results.toArray(new String[0]);
    }

    String splice(String[] ins) {
        String s;
        int offset = 1;
        int pos;
        Vertex v;

        for (int i = 0; i < ins.length; i++) {
            s = ins[i];
            // System.out.println("s = " + s);
            for (int j = 0; j < s.length(); j++) {
                v = new Vertex(s.charAt(j), j + 1);
                pos = i + offset;
                // System.out.println("insert pos = " + pos);
                this.vertices.add(pos, v);
                this.renumber(pos, 1);
                offset++;
            }
        }
        return this.toString();
    }

    // this method could replace the /splice/ method above.
    // except that splice actually adds new vertices...
    String mergeInsertArrayWithVertices(String[] ins) {
        StringBuffer ret = new StringBuffer();
        for (int i = 0; i < ins.length; i++) {
            ret.append(((Vertex) this.vertices.get(i)).getType());
            ret.append(ins[i]);
        }
        ret.append('C');
        return ret.toString();
    }

    boolean preProcess(Pattern d) {
        if (this.esize() > d.esize()) {
            // System.err.println("pattern larger than target! pattern = " +
            // this.esize() + " target = " + d.esize());
            return false; // pattern must be smaller.
        }
        boolean found = false;
        Edge current, target;
        // matches are added to the target edge
        for (int i = 0; i < this.size; ++i) {
            current = (Edge) (this.edges.get(i));
            for (int j = 0; j < d.size; ++j) {
                target = (Edge) (d.edges.get(j));
                if ((current.matches(target)) && this.stringComp(current, target, d)) {
                    // System.err.println("current: " + i + " matches target: "
                    // + j);
                    current.addMatch(target);
                    found = true;
                }
            }
            // test to see whether new matches have been found for this edge
            if (!found)
                return false;
            found = false;
//            current.turbo(); // cast the arraylist to an array to speed things up
        }
        return true;
    }

    boolean stringComp(Edge p, Edge t, Pattern d) {
        String innerProbe = this.getVertexString(p.left.getPos(), p.right
                .getPos(), false);
        String outerProbeLeft = this.getVertexString(0, p.left.getPos(), false);
        String outerProbeRight = this.getVertexString(p.right.getPos(), 0,
                false);

        String innerTarget = d.getVertexString(t.left.getPos(), t.right
                .getPos(), false);
        String outerTargetLeft = d.getVertexString(0, t.left.getPos(), false);
        String outerTargetRight = d.getVertexString(t.right.getPos(), 0, false);

        return this.subSequenceCompare(innerProbe, innerTarget)
                && this.subSequenceCompare(outerProbeLeft, outerTargetLeft)
                && this.subSequenceCompare(outerProbeRight, outerTargetRight);
    }

    boolean subSequenceCompare(String a, String b) {
        int ptrA, ptrB;
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

    void setMovedUpTo(int k) {
        for (int i = 0; i < k; ++i) {
            ((Edge) this.edges.get(i)).moved = false;
        }
    }

    void deCompress(String s) throws TopsStringFormatException {
        try {
            TParser parser = new TParser(s);

            this.head = parser.getName();
            char[] verts = parser.getVertices();
            String[] edgeStrings = parser.getEdges();
            this.classification = parser.getClassification();
            for (int i = 0; i < verts.length; ++i) {
                this.vertices.add(new Vertex(verts[i], i));
                this.vsize++;
            }
            int l, r;
            char t;
            Vertex left, right;
            int j = 0;

            for (; j < edgeStrings.length; j += 3) {
                l = Integer.parseInt(edgeStrings[j]);
                r = Integer.parseInt(edgeStrings[j + 1]);
                t = edgeStrings[j + 2].charAt(0);
                left = (Vertex) this.vertices.get(l);
                left.setIndex((char) (t + 32));
                right = (Vertex) this.vertices.get(r);
                right.setIndex(t);
                this.edges.add(new Edge(left, right, t));
            }
            this.sortEdges();
            // fillSheets();
            // setOutserts();
            if (!this.edges.isEmpty()) {
                int first = ((Edge) this.edges.get(0)).left.getPos();
                int last = ((Edge) this.edges.get(this.edges.size() - 1)).right.getPos();
                this.outsertN = this.getVertexString(0, first, false);
                this.outsertC = this.getVertexString(last + 1, 0, false);
                // System.out.println("Edges not emptry : outsertN = " +
                // outsertN + " outsertC = " + outsertC + " first = " + first +
                // " last = " + last);
            }
        } catch (Exception e) {
            throw new TopsStringFormatException(s + " " + e.toString());
        }
    }

    void sortEdges() {
        this.size = this.edges.size();
        Edge first, second;
        for (int i = 0; i < this.size; ++i) {
            for (int j = 0; j < this.size - 1; ++j) {
                first = (Edge) this.edges.get(j);
                second = (Edge) this.edges.get(j + 1);
                // System.out.println("first = " + first.toString() + " second =
                // " + second.toString());
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
        // System.out.println("SET INDICES CALLED");
        for (int i = 0; i < this.edges.size() - 1; i++) {
            il = ((Edge) this.edges.get(i)).left.getPos();
            ir = ((Edge) this.edges.get(i)).right.getPos();
            // System.out.println("il:" + il + " ir:" + ir);
            for (int j = i + 1; j < this.edges.size(); ++j) {
                jl = ((Edge) this.edges.get(j)).left.getPos();
                jr = ((Edge) this.edges.get(j)).right.getPos();
                // System.out.println("jl:" + jl + " jr:" + jr);
                if (il == jl) {
                    ((Edge) this.edges.get(i)).S2++;
                    ((Edge) this.edges.get(j)).S1++;
                }

                if (ir == jl) {
                    ((Edge) this.edges.get(i)).E1++;
                    ((Edge) this.edges.get(j)).S1++;
                }

                if (ir == jr) {
                    ((Edge) this.edges.get(i)).E1++;
                    ((Edge) this.edges.get(j)).E1++;
                }
            }
        }
    }

    void reset() {
        for (int i = 0; i < this.edges.size(); ++i) {
            ((Edge) (this.edges.get(i))).reset();
        }
        for (int i = 1; i < this.vertices.size() - 1; ++i) { // don't bother with N/C
            ((Vertex) (this.vertices.get(i))).resetMatch();
            ((Vertex) (this.vertices.get(i))).flip();
        }
    }

    public String getVertexString(int start, int end, boolean flip) {
        if (end == 0)
            end = this.vertices.size() - 1; // miss 'C'
        if (start == 0)
            start = 1; // miss 'N'
        // System.out.println("getting vertex string from: " + start + " to " +
        // end);
        if ((start >= end) || (end > this.vertices.size()))
            return new String();
        StringBuffer vstr = new StringBuffer();
        char c;
        for (int i = start; i < end; ++i) {
            c = ((Vertex) (this.vertices.get(i))).getType();
            if (flip)
                c = (Character.isUpperCase(c) ? Character.toLowerCase(c) : Character.toUpperCase(c));
            vstr.append(c);
        }
        return vstr.toString();
    }

    public boolean isNullPattern() {
        return (this.vertices.size() < 3);
    }

    public int[] getMatches() {
        int[] matches = new int[this.vertices.size() - 2];
        for (int i = 0; i < matches.length; ++i)
            matches[i] = ((Vertex) (this.vertices.get(i + 1))).getMatch();
        return matches;
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();

        result.append(this.head);
        result.append(' ');

        for (int i = 0; i < this.vertices.size(); ++i) {
            result.append(((Vertex) this.vertices.get(i)).getType());
        }
        // ensure that the pattern has a c-terminus!
        if (result.charAt(result.length() - 1) != 'C')
            result.append('C');

        result.append(' ');

        Edge e;
        for (int j = 0; j < this.edges.size(); ++j) {
            e = (Edge) this.edges.get(j);
            result.append(e.left.getPos());
            result.append(':');
            result.append(e.right.getPos());
            result.append(e.getType());
        }
        return result.toString();
    }

    private void renumber(int from, int amount) {
        Vertex counter;
        for (int i = from + 1; i < this.vertices.size() - 1; i++) {
            counter = (Vertex) this.vertices.get(i);
            int tmp = counter.getPos();
            // System.out.println("Vertex : " + counter.getType() + "@ " + i +
            // "|" + tmp + " to Vertex " + (tmp + 1));
            counter.setPos(tmp + amount); // move up all the internal numbers!
        }
    }
}// EOC

