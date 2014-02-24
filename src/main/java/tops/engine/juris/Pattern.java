package tops.engine.juris;

import java.util.ArrayList;
import java.util.List;

import tops.engine.Edge;
import tops.engine.Vertex;

class Pattern {

    int size, vsize;

    int[] inserts;

    String outsertC; // the vertex sequence from the last edge to the C
                        // terminus

    String head;

    Edge current;

    // java.util.List sheets = new ArrayList();
    List<Vertex> vertices = new ArrayList<Vertex>();

    List<Edge> edges = new ArrayList<Edge>();

    String classification;

    Pattern() {
        this.head = new String("head");
    }

    Pattern(String s) {
        this.deCompress(s);
        this.setIndices();
        if (!this.noEdges())
            this.current = (Edge) this.edges.get(this.edges.size() - 1);
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

    int size() {
        return this.edges.size();
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
    String[] getInsertStringArr(Pattern d) {
        List<String> results = new ArrayList<String>();
        int last = 1;
        int m = 0;
        for (int i = 1; i < this.vertices.size() - 1; ++i) {
            Vertex nxt = (Vertex) this.vertices.get(i); // get pattern vertex ref
            m = nxt.getMatch();
            if (m != 0) { // it IS an edge vertex, the setMatch() method is
                            // only called in the Edge class
                results.add(d.getVertexString(last, m, false));
                last = m + 1;
            } else { // it is a random, unattached vertex. do nothing
            }
        }
        results.add(d.getVertexString(last, 0, false)); // get the C-outsert
        return (String[]) results.toArray(new String[0]);
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
            e = (Edge) this.edges.get(i - 1);
            lhe = e.left.getPos();
            e.left.setPos(lhe + l); // shift the left-hand-end
            if (s != "") { // if there is indeed an insert!
                for (j = 0; j < s.length(); j++) {
                    this.vertices.add(j + l + 1, new Vertex(s.charAt(j), j + l + 1));
                }
                l += s.length(); // acumulate
            }
            e.right.setPos(e.right.getPos() + l); // l has accumulated the
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
        Edge current, target;
        // matches are added to the target edge
        for (int i = 0; i < this.size; ++i) {
            current = (Edge) (this.edges.get(i));
            for (int j = 0; j < d.size; ++j) {
                target = (Edge) (d.edges.get(j));
                if ((current.matches(target))
                        && (noInserts || this.stringComp(current, target, d))) {
                    // System.out.println("current: " + i + " matches target: "
                    // + j);
                    current.addMatch(target);
                    found = true;
                }
            }
            // test to see whether new matches have been found for this edge
            if (!found)
                return false;
            found = false;
//            current.turbo(); // cast the arraylist to an array to speed
                                // things up
        }
        return true;
    }

    boolean stringComp(Edge p, Edge t, Pattern d) {
        String probe = this.getVertexString(p.left.getPos(), p.right.getPos(),
                false);
        String target = d.getVertexString(t.left.getPos(), t.right.getPos(),
                false);
        int ptrP, ptrT;
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
        for (int i = 0; i < k; ++i) {
            ((Edge) this.edges.get(i)).moved = false;
        }
    }

    void deCompress(String s) {
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
        for (int j = 0; j < edgeStrings.length; j += 3) {
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
            int last = ((Edge) this.edges.get(this.edges.size() - 1)).right.getPos();
            this.outsertC = this.getVertexString(last + 1, 0, false);
            // System.out.println("Edges not emptry : outsertC = " + outsertC +
            // " last = " + last);
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

    /*
     * removed because target outserts can overlap with edges void setOutserts() {
     * if (!edges.isEmpty()) { Edge e = (Edge) edges.get(0); //have to do the
     * first one separately String out = getVertexString(1, e.left.getPos(),
     * false); int last = e.right.getPos(); for (int i = 1; i < edges.size();
     * ++i) { e = (Edge) edges.get(i); out = getVertexString(last,
     * e.left.getPos(), false); e.setOutsert(out); last = e.right.getPos(); }
     * outsertC = getVertexString(last, 0, false); } }
     */

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

    String getVertexString(int start, int end, boolean flip) {
        if (end == 0)
            end = this.vertices.size();
        // System.out.println("getting vertex string from: " + start + " to " +
        // end);
        if (start >= end)
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

    int[] getMatches() {
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

    /*
     * public boolean canECurrentSheet(int max_sheet_len) { //the current sheet
     * cannot be longer than this max. It also must exist! return
     * (current.length < max_sheet_len); }
     */

    /*
     * public boolean canMakeMoreSheets() { //?sheets.size() !> edges.size()?
     * return sheets.size() < edges.size(); }
     */

    public Pattern add(char vl, char vr, char et) {
        // System.out.println("First vertex = " +
        // ((Vertex)vertices.get(0)).type);
        this.vertices.remove(0); // trim the C
        Vertex N = new Vertex('N', 0); // aargh!
        Vertex l = new Vertex(vl, 1);
        Vertex r = new Vertex(vr, 2);
        this.vertices.add(N);
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
        int rightHandEnd = this.current.right.getPos() + 1;
        Vertex r = new Vertex(vr, rightHandEnd);
        this.vertices.add(rightHandEnd, r); // add the vertex to the list
        Edge e = new Edge(this.current.right, r, et);
        this.current = e;
        this.edges.add(e); // add the edge
        this.setIndices();
        return this;
    }

    public Pattern extendLeft(char vr, char et) {
        // make a new edge from the left hand pattern node
        int rightHandEnd = this.current.right.getPos() + 1;
        Vertex r = new Vertex(vr, rightHandEnd);
        this.vertices.add(rightHandEnd, r);
        Edge e = new Edge(this.current.left, r, et);
        this.edges.add(e);
        this.setIndices();
        return this;
    }

    public Pattern extendMiddle(char vl, char et) {
        // make a new edge TO the right hand pattern node
        int leftHandEnd = this.current.right.getPos(); // !
        Vertex l = new Vertex(vl, leftHandEnd);
        this.vertices.add(leftHandEnd, l);
        this.renumber(leftHandEnd, 1);
        Edge e = new Edge(l, this.current.right, et);
        this.current = e;
        this.edges.add(e); // sort?
        this.setIndices();
        return this;
    }

    public Pattern extendTwist(char vl, char et) {
        // make a new edge TO the right hand pattern node and TWIST
        int leftHandEnd = this.current.right.getPos() - 2; // !
        Vertex l = new Vertex(vl, leftHandEnd);
        this.vertices.add(leftHandEnd, l);
        this.renumber(leftHandEnd, 1);
        Edge e = new Edge(l, this.current.right, et);
        this.edges.add(e); // sort?
        this.setIndices();
        return this;
    }

    public Pattern overlap(char vl, char vr, char et) {
        // make a new edge whose LHE is less than current's RHE.
        int leftHandEnd = this.current.left.getPos() + 1;
        int rightHandEnd = this.current.right.getPos() + 1;
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
        int leftHandEnd = this.current.left.getPos() + 1;
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
        int leftHandEnd = this.current.right.getPos() + 1;
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
        if (this.current == null)
            return false;
        return true;
    }

    public boolean canInsert() {
        if (this.current == null)
            return false;
        return true;
    }

    public boolean canAppend() {
        if (this.current == null)
            return false;
        return true;
    }

    public boolean canERight(char el) {
        if (this.current == null)
            return false;
        if (el != this.current.right.getType())
            return false;
        return true;
    }

    public boolean canELeft(char el) {
        if (this.current == null)
            return false;
        if (el != this.current.left.getType())
            return false;
        return true;
    }

    public boolean canEMiddle(char er) {
        if (this.current == null)
            return false;
        if (er != this.current.right.getType())
            return false;
        return true;
    }

    public boolean canETwist(char er) {
        if (this.current == null)
            return false;
        return false; // !!--need a good test for twisting--!!
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

