package tops.engine.inserts;

import java.util.ArrayList;
import java.util.Iterator;

import java.util.logging.Level;
import java.util.logging.Logger;

import tops.engine.Edge;
import tops.engine.Vertex;

public class Pattern {

    private int size, vsize;

    // the vertex sequence from the last edge to the C terminus
    public String outsertC; 

    // the vertex sequence from the N terminus to the last edge
    public String outsertN; 

    private String head;

    private String classification;

    private Edge currentChiralEdge;

    private ArrayList vertices;

    private ArrayList edges;

    private ArrayList insertList;

    private static Logger logger = Logger
            .getLogger("tops.engine.inserts.Pattern");

    private float compression;

    public Pattern() {
        this.head = new String("pattern:");
        this.vertices = new ArrayList();
        this.edges = new ArrayList();
        this.insertList = new ArrayList();
        this.outsertC = "";
        this.outsertN = "";
        this.compression = 1.0f;
        Pattern.logger.entering("tops.engine.inserts.Pattern", "Pattern");
    }

    public Pattern(String s) {
        this();
        this.parse(s);
        this.setIndices();
    }

    public String getName() {
        return this.head;
    }

    public String getClassification() {
        return this.classification;
    }

    public void setCompression(float compression) {
        this.compression = compression;
    }

    public float getCompression() {
        return this.compression;
    }

    public void setVertices(char[] verts) {
        Pattern.logger.finest("setting vertices to : " + new String(verts));

        for (int i = 0; i < verts.length; ++i) {
            this.vertices.add(new Vertex(verts[i], i));
            this.vsize++;
        }
    }

    public void setEdges(String[] edgeStrings, boolean withInserts) {
        int l, r;
        char t;
        Vertex left, right;

        try {
            for (int j = 0; j < edgeStrings.length; j += 3) {
                l = Integer.parseInt(edgeStrings[j]);
                r = Integer.parseInt(edgeStrings[j + 1]);
                t = edgeStrings[j + 2].charAt(0);
                left = (Vertex) this.vertices.get(l);
                left.setIndex((char) (t + 32));
                right = (Vertex) this.vertices.get(r);
                right.setIndex(t);
                Edge edge = new Edge(left, right, t);
                this.edges.add(edge);
                if (withInserts) {
                    this.setEdgeRange(edge);
                }
            }
        } catch (ArrayIndexOutOfBoundsException ioobe) {
            Pattern.logger.throwing("tops.engine.inserts.Pattern", "setEdges", ioobe);
        }
    }

    /**
     * take the sequence of vertices and inserts between the endpoints of an
     * edge and set the min, max values for the range of said edge by summing
     * the inserts along with the min and max.
     */
    public void setEdgeRange(Edge e) {
        int rangeMinimum = 0;
        int rangeMaximum = 0;
        int left = e.getLeft();
        int right = e.getRight();
        ArrayList subSequence = this.getSubSequence(left, right);
        Iterator itr = subSequence.iterator();
        while (itr.hasNext()) {
            Object o = itr.next();
            if (o instanceof Vertex) {
                rangeMinimum++;
                rangeMaximum++;
            } else if (o instanceof Insert) {
                Insert i = (Insert) o;
                rangeMinimum += i.getMinSize();
                rangeMaximum += i.getMaxSize();
            } else {
                System.err.println("oh no!");
            }
        }
        Pattern.logger.info("setting range to [" + rangeMinimum + ", " + rangeMaximum
                + "]");
        e.setRangeMinimum(rangeMinimum);
        e.setRangeMaximum(rangeMaximum);
    }

    public void setInserts(String[] insertStrings) {
        this.insertList = new ArrayList();
        for (int i = 0; i < insertStrings.length; i++) {
            String insertString = insertStrings[i];
            Insert insert = this.determineInsertType(insertString);
            Pattern.logger.info("determined insert : " + insert.toString());
            this.insertList.add(insert);
        }
    }

    public Insert determineInsertType(String insertString) {
        if (insertString.equals("")) {
            return new StringInsert("");
        }

        char firstCharacter = insertString.charAt(0);
        if (Character.isDigit(firstCharacter)) {
            if ((insertString.indexOf(RangeInsert.separator)) != -1) {
                return new RangeInsert(insertString);
            } else {
                return new NumberInsert(insertString);
            }
        } else {
            return new StringInsert(insertString);
        }
    }

    public void parse(String s) {
        TParser parser = new TParser(s);

        this.head = parser.getName();
        if (parser.hasInserts()) {
            char[] verts = parser.getVerticesWithInserts();
            this.setVertices(verts);
            String[] inserts = parser.getInserts();
            this.setInserts(inserts);
            String[] edgeStrings = parser.getEdges();
            this.setEdges(edgeStrings, true);
        } else {
            char[] verts = parser.getVertices();
            this.setVertices(verts);
            String[] edgeStrings = parser.getEdges();
            this.setEdges(edgeStrings, false);
        }
        this.classification = parser.getClassification();

        this.sortEdges();

        if (!this.edges.isEmpty()) {
            this.setOutserts();
        }
    }

    /**
     * For backward compatibility - converts strings without explicit inserts.
     * So the instance "NEHEC 1:3P" becomes "NE[H]EC 1:3P". Clear?
     */
    public void convertDisconnectedVerticesToInserts() {
        Pattern.logger.info("converting Disconnected Vertices To Inserts");

        this.insertList = new ArrayList();

        int lastVertexPosition = 0;
        EdgeVertexIterator connectedVertices = this.getEdgeVertexIterator();
        while (connectedVertices.hasNext()) {
            Vertex currentVertex = (Vertex) connectedVertices.next();
            int currentVertexPosition = currentVertex.getPos();
            if ((currentVertexPosition - lastVertexPosition) > 1) {
                String insertString = this.getVertexStringBetween(
                        lastVertexPosition, currentVertexPosition);
                this.insertList.add(new StringInsert(insertString));
            } else {
                this.insertList.add(new StringInsert(new String()));
            }
            lastVertexPosition = currentVertexPosition;
        }

        // get the final 'insert' (or 'outsert')
        int maxNumberOfVertices = this.vertices.size() - 1;
        if (lastVertexPosition < maxNumberOfVertices) {
            String outsertString = this.getVertexStringBetween(
                    lastVertexPosition, maxNumberOfVertices);
            this.insertList.add(new StringInsert(outsertString));
        } else {
            this.insertList.add(new StringInsert(new String()));
        }
    }

    public EdgeVertexIterator getEdgeVertexIterator() {
        return new EdgeVertexIterator(this.edges.iterator());
    }

    public String getVertexStringWithInserts() {
        StringBuffer vertexStringWithInserts = new StringBuffer();
        vertexStringWithInserts.append('N');
        Insert outsertN = (Insert) this.insertList.get(0);
        Pattern.logger.info("got outsert : " + outsertN.toString());
        vertexStringWithInserts.append('[').append(outsertN.toString()).append(
                ']');
        for (int i = 1; i < this.vertices.size() - 1; i++) {
            char c = ((Vertex) (this.vertices.get(i))).getType();
            vertexStringWithInserts.append(c);
            Insert insert = (Insert) this.insertList.get(i);
            Pattern.logger.info("got insert : " + insert.toString());
            vertexStringWithInserts.append('[').append(insert.toString())
                    .append(']');
        }
        vertexStringWithInserts.append('C');

        return vertexStringWithInserts.toString();
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

    public void addEdges(ArrayList newEdges) {
        this.edges.addAll(newEdges);
    }

    public void addVertices(ArrayList newVertices) {
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

    public int minimumInsertSize() {
        int sum = 0;
        Iterator itr = this.insertList.iterator();
        while (itr.hasNext()) {
            Insert i = (Insert) itr.next();
            sum += i.getMinSize();
        }
        return sum;
    }

    /**
     * Return the minimum /size/ of the pattern in terms of the number of
     * vertices and inserts. Simply sums the vertices and (minimum) inserts.
     */
    public int getVSize() {
        int sum = 0;
        sum += this.vsize();
        sum += this.minimumInsertSize();
        return sum;
    }

    public boolean isLargerThan(Pattern other) {
        return (this.getVSize() > other.getVSize())
                || (this.esize() > other.esize());
    }

    public boolean isSmallerThan(Pattern other) {
        return (this.getVSize() < other.getVSize())
                || (this.esize() < other.esize());
    }

    public boolean preProcess(Pattern d) {
        if (this.isLargerThan(d)) {
            Pattern.logger.info("pattern is larger than its target!");
            return false; // pattern must be smaller.
        }

        boolean found = false;
        Edge current, target;
        // matches are added to the target edge
        for (int i = 0; i < this.size; ++i) {
            found = false;
            current = (Edge) (this.edges.get(i));
            for (int j = 0; j < d.size; ++j) {
                target = (Edge) (d.edges.get(j));
                Pattern.logger.info("matching current: " + i + " and target: " + j);
                boolean indicesMatch = current.matches(target);
                if (!indicesMatch) {
                    Pattern.logger.info("edges " + i + "(" + current + ") != " + j
                            + "(" + target + ") => indices don't match");
                    continue;
                }

                boolean subSeqMatch = this.subSequenceCompareWithInserts(
                        current, target, d);
                if (!subSeqMatch) {
                    Pattern.logger.info("edges " + i + "(" + current + ") != " + j
                            + "(" + target + ") => sub sequences don't match");
                    continue;
                }

                boolean rangeMatches = this.rangeMatches(current, target);
                if (!rangeMatches) {
                    Pattern.logger.info("edges " + i + "(" + current + ") != " + j
                            + "(" + target + ") => range doesn't match");
                    continue;
                }

                current.addMatch(target);
                found = true;
            }
            // test to see whether new matches have been found for this edge
            if (!found) {
                return false;
            }
            
            // cast the arraylist to an array to speed things up
//            current.turbo(); 
        }
        return true;
    }

    // TEMPORARY DECORATOR TO CAPTURE RANGE COMPARISON - USE
    // edge.rangeMatches(otherEdge)
    public boolean rangeMatches(Edge thisEdge, Edge thatEdge) {
        StringBuffer matching = new StringBuffer();
        matching.append("matching range : [");
        matching.append(thisEdge.getRangeMinimum());
        matching.append(", ");
        matching.append(thisEdge.getRangeMaximum());
        matching.append("] to endpoints: (");
        matching.append(thatEdge.getLeft());
        matching.append(", ");
        matching.append(thatEdge.getRight());
        matching.append(")");

        boolean matches = thisEdge.rangeMatches(thatEdge);
        matching.append(" " + matches);
        Pattern.logger.info(matching.toString());
        return matches;
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
        StringBuffer matchresult = new StringBuffer(" [");
        for (int i = 0; i < this.vertices.size(); ++i) {
            Vertex nxt = (Vertex) this.vertices.get(i);
            if (nxt.getMatch() != 0) {
                matchresult.append(i).append('-').append(nxt.getMatch())
                        .append(",");
            }
        }
        matchresult.setCharAt(matchresult.length() - 1, ']');
        return matchresult.toString();
    }

    /* ****************** INSERTS!! ****************** */

    public ArrayList getSubSequence(int leftPos, int rightPos) {

        ArrayList subSequence = new ArrayList();
        int start = leftPos + 1;
        int end = rightPos - 1;

        for (int i = start; i <= end; i++) {
            subSequence.add(this.insertList.get(i - 1)); // add the insert
                                                            // before the vertex
            subSequence.add(this.vertices.get(i)); // add the vertex itself
        }

        subSequence.add(this.insertList.get(end)); // add the trailing insert

        return subSequence;
    }

    public String getOutsertN(boolean flip) {
        return (flip) ? this.flipString(this.outsertN) : this.outsertN;
    }

    public String getOutsertC(boolean flip) {
        return (flip) ? this.flipString(this.outsertC) : this.outsertC;
    }

    // get the pattern with inserts eg : N[0]E[1]E[2]E[1]C
    public String getInsertString(Pattern d) {
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
        insertresult.append("C"); // finished inserts
        return insertresult.toString();
    }

    // get an array of strings of the unattached vertices
    // 'd' is an EXAMPLE, not a pattern
    public String[] getInsertStringArr(Pattern d, boolean flip) { 
        ArrayList results = new ArrayList();
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

    public String splice(String[] ins) {
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
    public String mergeInsertArrayWithVertices(String[] ins) {
        StringBuffer ret = new StringBuffer();
        for (int i = 0; i < ins.length; i++) {
            ret.append(((Vertex) this.vertices.get(i)).getType());
            ret.append(ins[i]);
        }
        ret.append('C');
        return ret.toString();
    }

    public boolean stringComp(Edge p, Edge t, Pattern d) {
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

    public boolean subSequenceCompareWithInserts(Edge pEdge, Edge tEdge,
            Pattern target) {
        int pLeft = pEdge.getLeft();
        int pRight = pEdge.getRight();
        int tLeft = tEdge.getLeft();
        int tRight = tEdge.getRight();
        int pEnd = this.vsize() - 1; // -1 to convert from #items to index
                                        // (ie a[6] is the 7th element)
        int tEnd = target.vsize() - 1;

        // boolean outerLeft = subSequenceCompareWithInserts(1, pLeft - 1, 1,
        // tLeft - 1, target);
        boolean outerLeft = this.compareRecursively(0, pLeft, 0, tLeft, target);
        Pattern.logger.info("outerL compare " + outerLeft);
        if (!outerLeft)
            return false;

        // boolean inner = subSequenceCompareWithInserts(pLeft + 1, pRight - 1,
        // tLeft + 1, tRight - 1, target);
        boolean inner = this.compareRecursively(pLeft, pRight, tLeft, tRight, target);
        Pattern.logger.info("inner compare " + inner);
        if (!inner)
            return false;

        // boolean outerRight = subSequenceCompareWithInserts(pRight + 1, pEnd -
        // 1, tRight + 1, tEnd - 1, target);
        boolean outerRight = this.compareRecursively(pRight, pEnd, tRight, tEnd,
                target);
        Pattern.logger.info("outerR compare " + outerRight);
        if (!outerRight)
            return false;

        return true;
    }

    public boolean compareRecursively(int patternStart, int patternEnd,
            int targetStart, int targetEnd, Pattern target) {
        Pattern.logger.info("pattern=(" + patternStart + ", " + patternEnd
                + ") target=(" + targetStart + ", " + targetEnd + ")");
        return this.recursiveCompare(patternStart, patternEnd, targetStart,
                targetEnd, target);
    }

    public boolean recursiveCompare(int patternPosition, int patternEnd,
            int targetPosition, int targetEnd, Pattern target) {
        Pattern.logger.info("pattern=(" + patternPosition + ", " + patternEnd
                + ") target=(" + targetPosition + ", " + targetEnd + ")");
        if (patternPosition > patternEnd || targetPosition > targetEnd)
            return false;
        char pattern_char = this.getTypeOfVertex(patternPosition);
        char target_char = target.getTypeOfVertex(targetPosition);
        if (pattern_char == target_char) {
            Pattern.logger.info(pattern_char + " == " + target_char);
            if (patternPosition >= patternEnd) { // got to the last part of
                                                    // the pattern
                return true;
            }
            Insert insert = (Insert) this.insertList.get(patternPosition);
            int min, max;
            if (insert.isNull()) {
                min = 0;
                max = (targetEnd - targetPosition) - 1; // fenceposts - a
                                                        // distance of 1 means 0
                                                        // things in between
            } else {
                min = insert.getMinSize();
                max = insert.getMaxSize();
            }
            Pattern.logger.info("range from " + min + " to " + max);
            for (int i = min; i <= max; i++) {
                int nextPosition = targetPosition + (i + 1); // fenceposts -
                                                                // an insert of
                                                                // 0 means
                                                                // moving 1
                                                                // forward
                Pattern.logger.info("trying next position : " + nextPosition);
                if (this.recursiveCompare(patternPosition + 1, patternEnd,
                        nextPosition, targetEnd, target)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean subSequenceCompareWithInserts(int patternStart,
            int patternEnd, int targetStart, int targetEnd, Pattern target) {
        Pattern.logger.info("patternStart=" + patternStart + " patternEnd="
                + patternEnd + " targetStart=" + targetStart + " targetEnd="
                + targetEnd);
        int pattern_ptr = patternStart;
        int target_ptr = targetStart;
        int max_ptr = targetStart;
        char pattern_char;
        char target_char;

        // -1 because the first insert before vertex[patternStart] is
        // insertList[patternStart - 1]
        int firstInsertIndex = patternStart - 1;
        Insert outsertN = (Insert) this.insertList.get(firstInsertIndex);
        int min = outsertN.getMinSize();
        int max = outsertN.getMaxSize();
        target_ptr += min;
        max_ptr += max;

        // this checks for single-insert subsequences - it can be GREATER since
        // n + 1 > m - 1 if (m - n) = 1
        if (patternStart >= patternEnd) {
            Pattern.logger.info("targetStart=" + targetStart + " <= target_ptr="
                    + target_ptr + " max_ptr=" + max_ptr + " > targetEnd ="
                    + targetEnd);
            return (targetStart <= target_ptr) && (max_ptr > targetEnd);
        }

        while (pattern_ptr <= patternEnd) { // less than OR EQUAL because we
                                            // want to consider the
                                            // vertex[patternEnd]
            Pattern.logger.info("pattern_ptr=" + pattern_ptr);
            if (target_ptr > targetEnd) {
                Pattern.logger.info("target pointer > targetEnd");
                return false;
            }

            if ((max_ptr != -1) && (target_ptr > max_ptr)) {
                Pattern.logger.info("target pointer > max pointer => target_ptr="
                        + target_ptr + " max_ptr=" + max_ptr);
                return false;
            }

            pattern_char = this.getTypeOfVertex(pattern_ptr);
            target_char = target.getTypeOfVertex(target_ptr);

            Pattern.logger.info("comparing " + pattern_char + " to " + target_char);
            if (pattern_char == target_char) {
                pattern_ptr++;
                max_ptr = target_ptr;
                Pattern.logger
                        .info("characters identical, incrementing pattern_ptr, setting max_ptr to "
                                + target_ptr);
                if (pattern_ptr >= patternEnd) {
                    continue;
                }
                Insert range = (Insert) this.insertList.get(pattern_ptr);
                if (range.isNull()) {
                    max_ptr = -1;
                    target_ptr++;
                    Pattern.logger
                            .info("range is null, setting max_ptr to -1, incrementing target_ptr");
                    continue;
                }
                target_ptr += range.getMinSize();
                max_ptr += range.getMaxSize() + 1;
                Pattern.logger.info("target_ptr now " + target_ptr + " and max_ptr is "
                        + max_ptr);
            }
            target_ptr++;
        }

        Insert outsertC = (Insert) this.insertList.get(patternEnd);
        min = outsertC.getMinSize();
        max = outsertC.getMaxSize();
        int min_endpoint = target_ptr + min + 1; // !
        int max_endpoint = target_ptr + max + 1; // !
        if (targetEnd < min_endpoint || targetEnd > max_endpoint) {
            Pattern.logger.info("target pointer out of range of final outsert => "
                    + min_endpoint + " " + targetEnd + " " + max_endpoint);
            return false;
        }

        return true;
    }

    public char getTypeOfVertex(int i) {
        return ((Vertex) this.vertices.get(i)).getType();
    }

    public boolean subSequenceCompare(String a, String b) {
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

    private void setOutserts() {
        int first = ((Edge) this.edges.get(0)).left.getPos();
        int last = ((Edge) this.edges.get(this.edges.size() - 1)).right.getPos();
        this.outsertN = this.getVertexString(0, first, false);
        this.outsertC = this.getVertexString(last + 1, 0, false);
        // System.out.println("Edges not emptry : outsertN = " + outsertN + "
        // outsertC = " + outsertC + " first = " + first + " last = " + last);
    }

    /* ****************** INSERTS!! ****************** */

    public boolean verticesIncrease() {
        int last = 0;
        int[] m = this.getMatches();
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

    public void setMovedUpTo(int k) {
        for (int i = 0; i < k; ++i) {
            ((Edge) this.edges.get(i)).moved = false;
        }
    }

    public void sortEdges() {
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

    public void setIndices() {
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

    public void reset() {
        for (int i = 0; i < this.edges.size(); ++i) {
            ((Edge) (this.edges.get(i))).reset();
        }
        for (int i = 1; i < this.vertices.size() - 1; ++i) { // don't bother with N/C
            Vertex v = (Vertex) this.vertices.get(i);
            v.resetMatch();
            v.flip();
        }
    }

    public String getVertexStringBetween(int start, int end) {
        return this.getVertexString(start + 1, end, false);
    }

    public String getVertexString(int start, int end, boolean flip) {
        if (end == 0)
            end = this.vertices.size() - 1; // miss 'C'
        if (start == 0)
            start = 1; // miss 'N'
        if (start >= end)
            return new String();
        StringBuffer vstr = new StringBuffer();
        char c;
        for (int i = start; i < end; ++i) {
            c = ((Vertex) (this.vertices.get(i))).getType();
            if (flip)
                c = (Character.isUpperCase(c) ? Character.toLowerCase(c)
                        : Character.toUpperCase(c));
            vstr.append(c);
        }
        return vstr.toString();
    }

    public boolean isNullPattern() {
        return (this.vertices.size() < 3);
    }

    public int[] getMatches() {
        int[] matches = new int[this.vertices.size() - 2];
        for (int i = 0; i < matches.length; ++i) {
            matches[i] = ((Vertex) (this.vertices.get(i + 1))).getMatch();
        }
        return matches;
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

    public String getVertexString() {
        if ((this.insertList == null) || (this.insertList.isEmpty())) {
            return "N" + this.getVertexString(0, 0, false) + "C";
        } else {
            return this.getVertexStringWithInserts();
        }
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();

        result.append(this.head);
        result.append(' ');

        result.append(this.getVertexString());

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

    public static void main(String[] args) {
        Pattern p = new Pattern(args[0]);
        Logger.getLogger("tops.engine.inserts.Pattern").setLevel(Level.ALL);
        System.out.println(p.getVertexString(0, 0, false));
        System.out.println(p);
    }

}// EOC

