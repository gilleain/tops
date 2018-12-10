package tops.engine.drg;

import java.util.ArrayList;
import java.util.List;

import tops.engine.Edge;
import tops.engine.PatternI;
import tops.engine.TParser;
import tops.engine.TopsStringFormatException;
import tops.engine.Vertex;

public class Pattern implements PatternI {

    private String head;	

    private String classification;

    private List<Vertex> vertices;

    private List<Edge> edges;
    
    private int[] inserts;
    
    // the vertex sequence from the last edge to the C terminus
    private String outsertC;
    // the position of the last vertex in an edge
    private int lastEdgeVertex;

    // the vertex sequence from the N terminus to the last edge
    private String outsertN; 

    private float compression;
    
    private Vertex lhv;
    private Vertex rhv;

    private Edge currentChiralEdge;
    
    private List<Vertex> currentSheet;

    public Pattern() {
        this.head = "pattern:";
        this.vertices = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.outsertC = "";
        this.lastEdgeVertex = -1;
        this.outsertN = "";
        this.compression = 1.0f;
        
        this.lhv = null;	//XXX
        this.rhv = null;	//XXX
        this.currentSheet = new ArrayList<>();
    }

    public Pattern(String s) throws TopsStringFormatException {
    	this();
        this.parse(s);
        this.setIndices();
    }
    
    /*
     * Copy constructor.
     */
    public Pattern(Pattern p) {
    	this();
    	for (int i = 0; i < p.vsize(); i++) {
    		this.vertices.add(new Vertex(p.vertices.get(i)));
    	}
    	
    	for (int i = 0; i < p.esize(); i++) {
    		Edge other = p.edges.get(i);
    		Vertex l = this.vertices.get(other.getLeft());
    		Vertex r = this.vertices.get(other.getRight());
    		this.edges.add(new Edge(l, r, other.getType()));
    	}
    	
    	for (int i = 0; i < p.currentSheet.size(); i++) {
    		Vertex v = p.currentSheet.get(i);
    		this.currentSheet.add(this.vertices.get(v.getPos()));
    	}
    	
    	this.setOutserts();
    	this.setIndices();
    }
    
    // this method wouldn't be necessary if patterns were more sensible!
    public void addTermini() {
    	this.vertices.add(new Vertex('N', 0));
    	this.vertices.add(new Vertex('C', 1));
    }
    
    public int sizeOfCurrentSheet() {
    	return this.currentSheet.size();
    }
    
    public void addNewSheet(int i, int j, Edge e) {
    	// insert the vertices and create the primary 
        // edge of which this sheet is composed
    	Vertex l = new Vertex(e.getLType(), i);
    	Vertex r = new Vertex(e.getRType(), j);
    	
        this.vertices.add(i, l);
        this.vertices.add(j, r);
        this.renumberFrom(i);
        
        this.currentSheet.clear();
        this.currentSheet.add(l);
        this.currentSheet.add(r);
        
        // at this point, the sheet == this edge
        this.edges.add(new Edge(l, r, e.getType()));	// TODO : sort!
    }
    
    public Vertex getLHV() {
    	return this.lhv;
    }

    public Vertex getRHV() {
    	return this.rhv;
    }
    
    public int getLEndpoint() {
//        return this.lhv.getPos();
    	return 1;	//FIXME
    }

    public int getREndpoint() {
//        return this.rhv.getPos();
    	return this.vsize();	// FIXME
    }

    public boolean canExtend(int i, char typ) {
        return typ == this.getVertexType(i);
    }

    public boolean canCyclise(int i, int j, Edge e) {
        return  !this.edgeBetween(i, j) 
        		&& this.getVertexType(i) == e.getLType() 
        		&& this.getVertexType(j) == e.getRType();
    }
    
    public char getVertexType(int i) {
    	return this.vertices.get(i).getType();
    }
    
    private boolean edgeBetween(int i, int j) {
    	 for (Edge e : this.edges) {
              if (e.getLeft() == i && e.getRight() == j) {
            	  return true;
              }
          }
          return false;
    }

    public void insertAt(int i, char vtype) {
        this.insertBefore(i + 1, vtype);
    }

    public void insertBefore(int i, char vtype) {
    	Vertex v = new Vertex(vtype, i);
        this.vertices.add(i, v);
        if (i == this.getLEndpoint()) {
            this.lhv = this.vertices.get(i);
        }
        if (i > this.getREndpoint()) {
            this.rhv = this.vertices.get(i);
        }
        this.renumberFrom(i + 1);
        this.currentSheet.add(v);
    }

    public void extend(int i, int j, Edge e) {
        this.edges.add(this.makeEdge(i, j, e.getType()));
        this.sortEdges();	// TODO : more efficient to sort as we add somehow? 
        // FIXME - convert to a ordered set?
    }

    private Edge makeEdge(int i, int j, char e) {
    	 // get the current vertex to extend from
        Vertex l = this.vertices.get(i);
                                                
        // get the next vertex to be extended to
        Vertex r = this.vertices.get(j);
                                                 
        return new Edge(l, r, e);
    }
    
    private void renumberFrom(int i) {
        int p = this.vertices.size();
        for (int j = i; j < p; j++) {
            this.vertices.get(j).setPos(j);
        }
    }

    public void setCompression(float compression) {
        this.compression = compression;
    }

    public float getCompression() {
        return this.compression;
    }

    public int getNumberOfHBonds() {
        int numberOfHBonds = 0;
        for (Edge e : this.edges) {
            char type = e.getType();
            if ((type == 'A') 
            		|| (type == 'P') 
            		|| (type == 'Z') 
            		|| (type == 'X')) {
                numberOfHBonds++;
            }
        }
        return numberOfHBonds;
    }

    public int getNumberOfChirals() {
        int numberOfChirals = 0;
        for (Edge e : this.edges) {
            char type = e.getType();
            if ((type == 'R') || (type == 'L') || (type == 'Z')
                    || (type == 'X')) {
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
            
            // the type of the edge (if any) between i & j
            char existingEdgeType = existingEdge.getType();
            
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
                System.out.println("edge between " + i + " and " + j + " not A or P! ");
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
            // it will only ever be P!
            this.edges.get(edgeAtPos).setType('P'); 
        }
    }

    public void addEdges(List<Edge> newEdges) {
        this.edges.addAll(newEdges);
    }

    public void addVertices(List<Vertex> newVertices) {
        this.vertices.addAll(newVertices);
    }

    public int getLastEdgeVertexPosition() {
    	return this.lastEdgeVertex;
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
    
    public int indexOfFirstUnmatchedEdge() {
    	for (int i = 0; i < this.esize(); i++) {
    		if (this.edgeUnmatched(i)) {
    			return i;
    		}
    	}
    	return -1;
    }
    
    public boolean edgeUnmatched(int i) {
    	Edge e = this.edges.get(i);
    	return e.getLeftMatch() == 0 || e.getRightMatch() == 0;
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

    // get the correspondence list eg : [1, 2, 4, 5]
    public String getMatchString() {
        StringBuilder matchresult = new StringBuilder(" [");
        for (int i = 0; i < this.vertices.size(); ++i) {
            Vertex nxt = this.vertices.get(i);
            if (nxt.getMatch() != 0) {
                matchresult.append(i).append('-')
                .append(nxt.getMatch()).append(",");
            }
        }
        matchresult.setCharAt(matchresult.length() - 1, ']');
        return matchresult.toString();
    }
    
    public int[] getMatches() {
        int[] matches = new int[vertices.size() - 2];
        for (int i = 0; i < matches.length; ++i) {
            matches[i] = vertices.get(i + 1).getMatch();
        }
        return matches;
    }

    public void setInserts() {
        this.inserts = new int[this.vertices.size() - 1];
        int last = 0;
        Vertex nxt;
        for (int i = 0; i < this.vertices.size(); ++i) {
            nxt = this.vertices.get(i);
            if (nxt.getMatch() > 0) {
                this.inserts[i] = nxt.getMatch() - last;
            } else {
                // TODO
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
        ArrayList<String> results = new ArrayList<>();
        int last = 1;
        int m = 0;
        for (int i = 1; i < this.vertices.size() - 1; ++i) {
            Vertex nxt = this.vertices.get(i); // get pattern vertex ref
            m = nxt.getMatch();
            // it IS an edge vertex, setMatch() only called in the Edge class
            if (m != 0) { 
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
            ret.append(this.vertices.get(i).getType());
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
        // matches are added to the target edge
        for (int i = 0; i < this.esize(); ++i) {
            Edge current = this.edges.get(i);
            for (int j = 0; j < d.esize(); ++j) {
                Edge target = d.getEdge(j);
                if ((current.matches(target)) && 
                		efficientStringComp(current, target, d)) {
                    current.addMatch(target);
                    found = true;
                }
            }
            // test to see whether new matches have been found for this edge
            if (!found) {
                return false;
            }
            found = false;
        }
        return true;
    }

    // TODO : remove this, after testing that efficientStringComp does the equivalent!
    public boolean stringComp(Edge p, Edge t, Pattern d) {
    	int pl = p.getLeft();
    	int pr = p.getRight();
    	int tl = t.getLeft();
    	int tr = t.getRight();
    	
        String innerProbe = this.getVertexString(pl, pr, false);
        String outerProbeLeft = this.getVertexString(0, pl, false);
        String outerProbeRight = this.getVertexString(pr, 0, false);
        
        String innerTarget = d.getVertexString(tl, tr, false);
        String outerTargetLeft = d.getVertexString(0, tl, false);
        String outerTargetRight = d.getVertexString(tr, 0, false);

        return this.subSequenceCompare(innerProbe, innerTarget)
        	&& this.subSequenceCompare(outerProbeLeft, outerTargetLeft)
            && this.subSequenceCompare(outerProbeRight, outerTargetRight);
    }
    
    public boolean efficientStringComp(Edge p, Edge t, PatternI d) {
    	int pl = p.getLeft();
    	int pr = p.getRight();
    	int tl = t.getLeft();
    	int tr = t.getRight();
    	
    	return this.subSequenceCompare(pl, pr, tl, tr, d, false)
        	&& this.subSequenceCompare(0, pl, 0, tl, d, false)
        	&& this.subSequenceCompare(pr, 0, tr, 0, d, false);
    }
    
    public boolean stringMatch(PatternI other, boolean flipped) {
    	return this.subSequenceCompare(0, 0, 0, 0, other, flipped);
    }
    
    // TODO : use this, test it, replace stringComp...
    public boolean subSequenceCompare(int start, int end, 
    		int otherStart, int otherEnd, PatternI other, boolean flipped) {
    	int ptrA = start;
    	int ptrB = otherStart;
    	
    	if (end == 0) {
    		end = this.vsize();
    	} 
    	
    	if (otherEnd == 0) { 
    		otherEnd = other.vsize();
    	}
    	
    	while (ptrA < end) {
            if (ptrB >= otherEnd)
                return false;
            char c = other.getVertex(ptrB).getType();
            if (flipped) {
            	if (Character.isUpperCase(c)) {
            		c += 32;
            	} else {
            		c -= 32;
            	}
            } 
            if (this.getVertexType(ptrA) == c) {
                ptrA++;
            }
            ptrB++;
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

    public void setMovedUpTo(int k) {
        for (int i = 0; i < k; ++i) {
            this.edges.get(i).moved = false;
        }
    }

    private void parse(String s) throws TopsStringFormatException {
        try {
            TParser parser = new TParser(s);

            this.head = parser.getName();
            char[] verts = parser.getVertices();
            String[] edgeStrings = parser.getEdgesAsStrings();
            this.classification = parser.getClassification();
            for (int i = 0; i < verts.length; ++i) {
                this.vertices.add(new Vertex(verts[i], i));
            }

            for (int j = 0; j < edgeStrings.length; j += 3) {
                int l = Integer.parseInt(edgeStrings[j]);
                int r = Integer.parseInt(edgeStrings[j + 1]);
                char t = edgeStrings[j + 2].charAt(0);
                Vertex left = this.vertices.get(l);
                left.setIndex((char) (t + 32));
                Vertex right = this.vertices.get(r);
                right.setIndex(t);
                this.edges.add(new Edge(left, right, t));
            }
            this.sortEdges();
            this.setOutserts();
        } catch (Exception e) {
            throw new TopsStringFormatException(s + " " + e.toString());
        }
    }
    
    private void setOutserts() {
    	if (!this.edges.isEmpty()) {
            int first = this.edges.get(0).getLeftVertex().getPos();
            int last = this.edges.get(this.edges.size() - 1).getRightVertex().getPos();
            this.outsertN = this.getVertexString(0, first, false);
            this.outsertC = this.getVertexString(last + 1, 0, false);
            
            // TODO : replace the use of outsertC with this?
            this.lastEdgeVertex = last;
        }
    }

    public void sortEdges() {		// TODO Replace with Collections.sort??
        Edge first;
        Edge second;
        for (int i = 0; i < this.esize(); ++i) {
            for (int j = 0; j < this.esize() - 1; ++j) {
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
        for (int i = 0; i < this.edges.size() - 1; i++) {
        	Edge ei = this.edges.get(i); 
            int il = ei.getLeft();
            int ir = ei.getRight();
            for (int j = i + 1; j < this.edges.size(); ++j) {
            	Edge ej = this.edges.get(j);
                int jl = ej.getLeft();
                int jr = ej.getRight();
                if (il == jl) {
                    ei.addS2();
                    ej.addS1();
                }

                if (ir == jl) {
                    ei.addE1();
                    ej.addS1();
                }

                if (ir == jr) {
                    ei.addE1();
                    ej.addE1();
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

    public String getWholeVertexString() {
        return "N" + this.getVertexString(0, 0, false) + "C";
    }

    public String getEdgeString() {
        StringBuilder result = new StringBuilder();
        for (Edge e : this.edges) {
            result.append(e.getLeftVertex().getPos());
            result.append(':');
            result.append(e.getRightVertex().getPos());
            result.append(e.getType());
        }
        return result.toString();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append(this.getName());
        result.append(' ');
        result.append(this.getWholeVertexString());
        result.append(' ');
        result.append(this.getEdgeString());
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

}

