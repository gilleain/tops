package tops.engine.inserts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tops.engine.Edge;
import tops.engine.PatternI;
import tops.engine.Vertex;

public class Pattern implements PatternI {

    private int size;

    // the vertex sequence from the last edge to the C terminus
    private String outsertC; 

    // the vertex sequence from the N terminus to the last edge
    private String outsertN; 

    private String head;

    private String classification;

    private Edge currentChiralEdge;

    private List<Vertex> vertices;

    private List<Edge> edges;

    private List<Insert> insertList;

    private static Logger logger = Logger.getLogger(Pattern.class.getName());

    private float compression;

    public Pattern() {
        this.head = "pattern:";
        this.vertices = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.insertList = new ArrayList<>();
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
        logger.log(Level.FINEST, "setting vertices to : {0}", verts);

        for (int i = 0; i < verts.length; ++i) {
            this.vertices.add(new Vertex(verts[i], i));
        }
    }

    public void setEdges(String[] edgeStrings, boolean withInserts) {
        int l;
        int r;
        char t;
        Vertex left;
        Vertex right;

        try {
            for (int j = 0; j < edgeStrings.length; j += 3) {
                l = Integer.parseInt(edgeStrings[j]);
                r = Integer.parseInt(edgeStrings[j + 1]);
                t = edgeStrings[j + 2].charAt(0);
                left = this.vertices.get(l);
                left.setIndex((char) (t + 32));
                right = this.vertices.get(r);
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
        List<Object> subSequence = this.getSubSequence(left, right);
        for(Object o : subSequence) {
            if (o instanceof Vertex) {
                rangeMinimum++;
                rangeMaximum++;
            } else if (o instanceof Insert) {
                Insert i = (Insert) o;
                rangeMinimum += i.getMinSize();
                rangeMaximum += i.getMaxSize();
            } else {
                logger.warning("oh no!");
            }
        }
        logger.log(Level.INFO, "setting range to [{0}, {1}]", new Object[] {rangeMinimum, rangeMaximum});
        e.setRangeMinimum(rangeMinimum);
        e.setRangeMaximum(rangeMaximum);
    }

    public void setInserts(String[] insertStrings) {
        this.insertList = new ArrayList<>();
        for (int i = 0; i < insertStrings.length; i++) {
            String insertString = insertStrings[i];
            Insert insert = this.determineInsertType(insertString);
            logger.log(Level.INFO, "determined insert : {0}", insert);
            this.insertList.add(insert);
        }
    }

    public Insert determineInsertType(String insertString) {
        if (insertString.equals("")) {
            return new StringInsert("");
        }

        char firstCharacter = insertString.charAt(0);
        if (Character.isDigit(firstCharacter)) {
            if ((insertString.indexOf(RangeInsert.SEPARATOR)) != -1) {
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

        this.insertList = new ArrayList<>();

        int lastVertexPosition = 0;
        EdgeVertexIterator connectedVertices = this.getEdgeVertexIterator();
        while (connectedVertices.hasNext()) {
            Vertex currentVertex = connectedVertices.next();
            int currentVertexPosition = currentVertex.getPos();
            if ((currentVertexPosition - lastVertexPosition) > 1) {
                String insertString = this.getVertexStringBetween(
                        lastVertexPosition, currentVertexPosition);
                this.insertList.add(new StringInsert(insertString));
            } else {
                this.insertList.add(new StringInsert(""));
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
            this.insertList.add(new StringInsert(""));
        }
    }

    public EdgeVertexIterator getEdgeVertexIterator() {
        return new EdgeVertexIterator(this.edges.iterator());
    }

    public String getVertexStringWithInserts() {
        StringBuilder vertexStringWithInserts = new StringBuilder();
        vertexStringWithInserts.append('N');
        Insert outsertN0 = this.insertList.get(0);
        logger.log(Level.INFO, "got outsert : {0}", outsertN0);
        vertexStringWithInserts.append('[').append(outsertN0.toString()).append(']');
        for (int i = 1; i < this.vertices.size() - 1; i++) {
            char c = this.vertices.get(i).getType();
            vertexStringWithInserts.append(c);
            Insert insert = this.insertList.get(i);
            logger.log(Level.INFO, "got insert : {0}", insert);
            vertexStringWithInserts.append('[').append(insert.toString()).append(']');
        }
        vertexStringWithInserts.append('C');

        return vertexStringWithInserts.toString();
    }

    private String flipString(String toFlip) {
        StringBuilder toReturn = new StringBuilder();
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
                logger.log(Level.WARNING, "edge between {0} and {1} not A or P!", new Object[] {i, j});
                return false;
            }
        } else { // else, no existing edge - add new one
            Vertex lvert = this.vertices.get(i);
            Vertex rvert = this.vertices.get(j);
            Edge ne = new Edge(lvert, rvert, c);
            this.edges.add(ne);
            this.currentChiralEdge = ne;
        }
        return true;
    }

    public int edgesContains(int i, int j) {
        for (int e = 0; e < this.edges.size(); e++) {
            Edge nextEdge = this.edges.get(e);
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
            this.edges.get(edgeAtPos).setType('P'); // it will only ever be P!
        }
    }

    public void addEdges(List<Edge> newEdges) {
        this.edges.addAll(newEdges);
    }

    public void addVertices(List<Vertex> newVertices) {
        this.vertices.addAll(newVertices);
    }

    public Vertex getVertex(int i) {
        return this.vertices.get(i);
    }

    public Edge getEdge(int i) {
        if (i >= this.edges.size()) {
            return this.edges.get(this.edges.size() - 1);
        } else {
            return this.edges.get(i);
        }
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
        Iterator<Insert> itr = this.insertList.iterator();
        while (itr.hasNext()) {
            Insert i = itr.next();
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

    public boolean isLargerThan(PatternI d) {
        return this.vsize() > d.vsize() || this.esize() > d.esize();
    }

    public boolean isSmallerThan(Pattern other) {
        return this.getVSize() < other.getVSize() || this.esize() < other.esize();
    }

    public boolean preProcess(PatternI d) {
        if (isLargerThan(d)) {
            Pattern.logger.info("pattern is larger than its target!");
            return false; // pattern must be smaller.
        }

        boolean found = false;
        Edge current;
        Edge target;
        // matches are added to the target edge
        for (int i = 0; i < this.size; ++i) {
            found = false;
            current = this.edges.get(i);
            for (int j = 0; j < d.vsize(); ++j) {
                target = d.getEdge(j);
                logger.log(Level.INFO, "matching current: {0} and target: {1}", new Object[] {i, j});
                boolean indicesMatch = current.matches(target);
                if (!indicesMatch) {
                    logger.log(Level.INFO, "edges {0} ({1}) != {2} ({3}) => indices do not match", 
                            new Object[] {i, current, j, target});
                    continue;
                }

                boolean subSeqMatch = subSequenceCompareWithInserts(current, target, d);
                if (!subSeqMatch) {
                    logger.log(Level.INFO, "edges {0} ({1}) != {2} ({3}) => subsequences do not match", 
                            new Object[] {i, current, j, target});
                    continue;
                }

                boolean rangeMatches = this.rangeMatches(current, target);
                if (!rangeMatches) {
                    logger.log(Level.INFO, "edges {0} ({1}) != {2} ({3}) => range does not match", 
                            new Object[] {i, current, j, target});
                    continue;
                }

                current.addMatch(target);
                found = true;
            }
            // test to see whether new matches have been found for this edge
            if (!found) {
                return false;
            }
        }
        return true;
    }

    // TEMPORARY DECORATOR TO CAPTURE RANGE COMPARISON - USE
    // edge.rangeMatches(otherEdge)
    public boolean rangeMatches(Edge thisEdge, Edge thatEdge) {
        StringBuilder matching = new StringBuilder();
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
        logger.log(Level.INFO, "{0}", matching);
        return matches;
    }

    // get the simple list of matching
    public String getVertexMatchedString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < this.vertices.size(); ++i) {
            Vertex nxt = this.vertices.get(i);
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

    public List<Object> getSubSequence(int leftPos, int rightPos) {

        List<Object> subSequence = new ArrayList<>();
        int start = leftPos + 1;
        int end = rightPos - 1;

        for (int i = start; i <= end; i++) {
            subSequence.add(this.insertList.get(i - 1)); // add the insert before the vertex
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
                for (; j < this.vertices.get(i + 1).getMatch(); ++j) {
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
        insertresult.append("C"); // finished inserts
        return insertresult.toString();
    }

    // get an array of strings of the unattached vertices
    // 'd' is an EXAMPLE, not a pattern
    public String[] getInsertStringArr(PatternI d, boolean flip) { 
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
    public String mergeInsertArrayWithVertices(String[] ins) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < ins.length; i++) {
            ret.append(this.vertices.get(i).getType());
            ret.append(ins[i]);
        }
        ret.append('C');
        return ret.toString();
    }

    public boolean stringComp(Edge p, Edge t, Pattern d) {
        String innerProbe = getVertexString(
        		p.getLeftVertex().getPos(), p.getRightVertex().getPos(), false);
        String outerProbeLeft = getVertexString(0, p.getLeftVertex().getPos(), false);
        String outerProbeRight = getVertexString(p.getRightVertex().getPos(), 0, false);

        String innerTarget = 
        		d.getVertexString(t.getLeftVertex().getPos(), t.getRightVertex().getPos(), false);
        String outerTargetLeft = d.getVertexString(0, t.getLeftVertex().getPos(), false);
        String outerTargetRight = d.getVertexString(t.getRightVertex().getPos(), 0, false);

        return this.subSequenceCompare(innerProbe, innerTarget)
                && this.subSequenceCompare(outerProbeLeft, outerTargetLeft)
                && this.subSequenceCompare(outerProbeRight, outerTargetRight);
    }

    public boolean subSequenceCompareWithInserts(Edge pEdge, Edge tEdge, PatternI d) {
        int pLeft = pEdge.getLeft();
        int pRight = pEdge.getRight();
        int tLeft = tEdge.getLeft();
        int tRight = tEdge.getRight();
        int pEnd = this.vsize() - 1; // -1 to convert from #items to index
                                        // (ie a[6] is the 7th element)
        int tEnd = d.vsize() - 1;

        boolean outerLeft = compareRecursively(0, pLeft, 0, tLeft, d);
        logger.log(Level.INFO, "outerL compare {0}", outerLeft);
        if (!outerLeft)
            return false;

        boolean inner = compareRecursively(pLeft, pRight, tLeft, tRight, d);
        logger.log(Level.INFO, "inner compare {0}", inner);
        if (!inner)
            return false;

        boolean outerRight = compareRecursively(pRight, pEnd, tRight, tEnd, d);
        logger.log(Level.INFO, "outerR compare {0}", outerRight);
        return !outerRight;
    }

    public boolean compareRecursively(int patternStart, int patternEnd,
            int targetStart, int targetEnd, PatternI d) {
        logger.log(Level.INFO, "pattern=({0}, {1}) target=({2},{3})", 
                new Object[] {patternStart, patternEnd, targetStart, targetEnd});
        
        return recursiveCompare(patternStart, patternEnd, targetStart, targetEnd, d);
    }

    public boolean recursiveCompare(int patternPosition, int patternEnd,
            int targetPosition, int targetEnd, PatternI d) {
        logger.log(Level.INFO, "pattern=({0}, {1}) target=({2},{3})", 
                new Object[] {patternPosition, patternEnd, targetPosition, targetEnd});
        if (patternPosition > patternEnd || targetPosition > targetEnd)
            return false;
        char patternChar = getVertex(patternPosition).getType();
        char targetChar = d.getVertex(targetPosition).getType();
        if (patternChar == targetChar) {
            logger.log(Level.INFO, "{0} == {1}", new Object[] {patternChar, targetChar});
            if (patternPosition >= patternEnd) { // got to the last part of
                                                    // the pattern
                return true;
            }
            Insert insert = this.insertList.get(patternPosition);
            int min;
            int max;
            if (insert.isNull()) {
                min = 0;
                // fenceposts - a distance of 1 means 0 things in between
                max = (targetEnd - targetPosition) - 1; 
            } else {
                min = insert.getMinSize();
                max = insert.getMaxSize();
            }
            logger.log(Level.INFO, "range from {0} to {1}", new Object[] { min, max });
            for (int i = min; i <= max; i++) {
                int nextPosition = targetPosition + (i + 1); // fenceposts -
                                                                // an insert of
                                                                // 0 means
                                                                // moving 1
                                                                // forward
                logger.log(Level.INFO, "trying next position : {0}", nextPosition);
                if (this.recursiveCompare(patternPosition + 1, patternEnd,
                        nextPosition, targetEnd, d)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean subSequenceCompareWithInserts(int patternStart,
            int patternEnd, int targetStart, int targetEnd, Pattern target) {
     
        int patternPtr = patternStart;
        int targetPtr = targetStart;
        int maxPtr = targetStart;
        char patternChar;
        char targetChar;

        // -1 because the first insert before vertex[patternStart] is
        // insertList[patternStart - 1]
        int firstInsertIndex = patternStart - 1;
        Insert outsertN0 = this.insertList.get(firstInsertIndex);
        int min = outsertN0.getMinSize();
        int max = outsertN0.getMaxSize();
        targetPtr += min;
        maxPtr += max;

        // this checks for single-insert subsequences - it can be GREATER 
        // since n + 1 greater than m - 1 if (m - n) equals 1
        if (patternStart >= patternEnd) {
            return targetStart <= targetPtr && maxPtr > targetEnd;
        }

        while (patternPtr <= patternEnd) { // less than OR EQUAL because we
                                            // want to consider the
                                            // vertex[patternEnd]
            logger.log(Level.INFO, "pattern_ptr= {0}", patternPtr);
            if (targetPtr > targetEnd) {
                logger.info("target pointer > targetEnd");
                return false;
            }

            if ((maxPtr != -1) && (targetPtr > maxPtr)) {
                logger.log(Level.INFO, "target pointer > max pointer => target_ptr= {0} max_ptr= {1}"
                        , new Object[] {targetPtr, maxPtr});
                return false;
            }

            patternChar = getVertex(patternPtr).getType();
            targetChar = target.getVertex(targetPtr).getType();

            logger.log(Level.INFO, "comparing {0} to {1}", new Object[] {patternChar, targetChar});
            if (patternChar == targetChar) {
                patternPtr++;
                maxPtr = targetPtr;
                logger.log(Level.INFO, 
                        "characters identical, incrementing pattern_ptr, setting max_ptr to {0}", targetPtr);
                if (patternPtr >= patternEnd) {
                    continue;
                }
                Insert range = this.insertList.get(patternPtr);
                if (range.isNull()) {
                    maxPtr = -1;
                    targetPtr++;
                    logger.info("range is null, setting max_ptr to -1, incrementing target_ptr");
                    continue;
                }
                targetPtr += range.getMinSize();
                maxPtr += range.getMaxSize() + 1;
                logger.log(Level.INFO, "target_ptr now {0} and max_ptr is {1} ", new Object[] {targetPtr, maxPtr});
            }
            targetPtr++;
        }

        Insert outsertC0 = this.insertList.get(patternEnd);
        min = outsertC0.getMinSize();
        max = outsertC0.getMaxSize();
        int minEndpoint = targetPtr + min + 1; // !
        int maxEndpoint = targetPtr + max + 1; // !
        if (targetEnd < minEndpoint || targetEnd > maxEndpoint) {
            logger.log(Level.INFO, "target pointer out of range of final outsert => {0} {1} {2}", 
                    new Object[] {minEndpoint, targetEnd, maxEndpoint});
            return false;
        }

        return true;
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

    private void setOutserts() {
        int first = edges.get(0).getLeftVertex().getPos();
        int last = edges.get(edges.size() - 1).getRightVertex().getPos();
        this.outsertN = getVertexString(0, first, false);
        this.outsertC = getVertexString(last + 1, 0, false);
    }

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

    public void setMovedUpTo(int k) {
        for (int i = 0; i < k; ++i) {
            this.edges.get(i).moved = false;
        }
    }

    public void sortEdges() {
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

    public void setIndices() {
        int il = 0;
        int jl = 0;
        int ir = 0;
        int jr = 0;
        for (int i = 0; i < this.edges.size() - 1; i++) {
            il = edges.get(i).getLeftVertex().getPos();
            ir = edges.get(i).getRightVertex().getPos();
            for (int j = i + 1; j < edges.size(); ++j) {
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
            Vertex v = this.vertices.get(i);
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
            return "";
        StringBuilder vstr = new StringBuilder();
        char c;
        for (int i = start; i < end; ++i) {
            c = this.vertices.get(i).getType();
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
            matches[i] = this.vertices.get(i + 1).getMatch();
        }
        return matches;
    }

    private void renumber(int from, int amount) {
        Vertex counter;
        for (int i = from + 1; i < this.vertices.size() - 1; i++) {
            counter = this.vertices.get(i);
            int tmp = counter.getPos();
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
        StringBuilder result = new StringBuilder();

        result.append(this.head);
        result.append(' ');

        result.append(this.getVertexString());

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

