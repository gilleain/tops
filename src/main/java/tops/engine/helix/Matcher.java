package tops.engine.helix;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import tops.engine.Edge;
import tops.engine.MatchHandler;
import tops.engine.MatcherI;
import tops.engine.PatternI;
import tops.engine.PlainFormatter;
import tops.engine.Result;
import tops.engine.TopsStringFormatException;
import tops.engine.Vertex;

public class Matcher implements MatcherI {

    private int k;

    private Pattern[] diagrams;

    private Stack<Integer> PVS, TVS;

    private static Logger logger = Logger
            .getLogger("tops.engine.helix.Matcher");

    public Matcher() {
        Matcher.logger.addHandler(new StreamHandler(System.out,
                new PlainFormatter()));
        Matcher.logger.setUseParentHandlers(false);
        Matcher.logger.setLevel(Level.OFF); // this can be reset by the
                                            // Explorer?
    }

    public Matcher(Pattern[] diag) {
        this();
        this.diagrams = diag;
    }

    public Matcher(String[] diag) throws TopsStringFormatException {
        this();
        this.diagrams = new Pattern[diag.length];
        for (int p = 0; p < diag.length; ++p) {
            this.diagrams[p] = new Pattern(diag[p]);
        }
    }
    
    @Override
    public void match(PatternI pattern, PatternI instance, MatchHandler matchHandler) {
    	boolean matchFound = false;
    	boolean shouldReset = false;
    	if (pattern.preProcess(instance)) {
    		if (matches(pattern, instance)) {
    			matchHandler.handle(pattern, instance);
    			matchFound = true;
    		}
    	}
    	if (!matchFound) {
    		pattern.reset();	// flip
    		shouldReset = true;
    		if (matches(pattern, instance)) {
    			matchHandler.handle(pattern, instance);
    			matchFound = true;
    		}
    	}
    	if (shouldReset) {
    		pattern.reset();
    	}
    }

    public void setLogging(boolean on) {
        if (on) {
            Matcher.logger.setLevel(Level.ALL);
            Matcher.logger.log(Level.SEVERE, "Logging set to ON");
        } else {
            Matcher.logger.setLevel(Level.OFF);
        }
    }

    public void setDiagrams(Pattern[] diagrams) {
        this.diagrams = diagrams;
    }

    public void setDiagrams(String[] diag) throws TopsStringFormatException {
        this.diagrams = new Pattern[diag.length];
        for (int i = 0; i < diag.length; ++i) {
            this.diagrams[i] = new Pattern(diag[i]);
        }
    }

    public String[] run(String[] patterns, String d)
            throws TopsStringFormatException {
        Pattern diagram = new Pattern(d);
        ArrayList<String> results = new ArrayList<String>();
        for (int i = 0; i < patterns.length; i++) {
            String result = this.match(new Pattern(patterns[i]), diagram);
            if (!result.equals(""))
                results.add(result);
        }
        return (String[]) results.toArray(new String[0]);
    }

    // synthesise insert ranges from the matches of the pattern to the set of
    // targets
    public String matchAndGetInserts(Pattern pattern) {
        ArrayList<int[]> ranges = new ArrayList<int[]>();
        Pattern p = pattern;
        try {
            p = new Pattern(pattern.toString()); // !!ARRRGH!
        } catch (TopsStringFormatException tsfe) {
            System.err.println("tsfe! " + tsfe);
        }
        boolean isSetup = false; // used to initialise the ranges
        for (int i = 0; i < this.diagrams.length; i++) {
            boolean hasMatched = false;
            Pattern d = this.diagrams[i];
            // System.err.println("matching " + p + " to " + d);
            String[] inserts = null;
            if (p.preProcess(d)) {
                // System.err.println("preprocessed " + p + " with " + d);
                if (this.matches(p, d)) {
                    // System.err.println("matched " + p + " to " + d);
                    this.setUnattachedVertexMatches(p, d);
                    inserts = p.getInsertStringArr(d, false);
                    hasMatched = true;
                }
            }
            p.reset();
            if (!hasMatched) {
                if (p.preProcess(d)) {
                    // System.err.println("preprocessed " + p + " with " + d);
                    if (this.matches(p, d)) {
                        // System.err.println("matched " + p + " to " + d);
                        this.setUnattachedVertexMatches(p, d);
                        inserts = p.getInsertStringArr(d, true);
                        hasMatched = true;
                    }
                }
            }
            p.reset();
            if (inserts == null) {
                System.err.println("inserts null! hasMatched = " + hasMatched
                        + " pattern = " + pattern.toString());
                return "";
            }
            // System.err.print("diagram: " + d.head + "[");
            for (int j = 0; j < inserts.length; j++) {
                int[] minmax;
                if (isSetup) {
                    minmax = (int[]) ranges.get(j);
                } else {
                    minmax = new int[2];
                    minmax[0] = -1;
                    minmax[1] = -1;
                    ranges.add(minmax);
                }
                int insertLength = inserts[j].length();
                // System.err.print(insertLength + ",");
                if (minmax[0] == -1 || insertLength < minmax[0]) { // expand
                                                                    // the
                                                                    // minimum
                                                                    // of the
                                                                    // range
                    minmax[0] = insertLength;
                }

                if (minmax[1] == -1 || insertLength > minmax[1]) { // expand
                                                                    // the
                                                                    // maximum
                                                                    // of the
                                                                    // range
                    minmax[1] = insertLength;
                }
            }
            // System.err.println("]");
            isSetup = true;
        }
        StringBuffer patternString = new StringBuffer();
        for (int k = 0; k < p.vsize() - 1; k++) {
            Vertex v = p.getVertex(k);
            int[] range = (int[]) ranges.get(k);
            patternString.append(v.getType());
            if (range[0] == range[1]) { // if the min and max are equal, use a
                                        // single number
                patternString.append("[").append(range[0]).append("]");
            } else {
                patternString.append("[").append(range[0]).append("-").append(
                        range[1]).append("]");
            }
        }
        patternString.append("C").append(" ");
        Edge e;
        for (int j = 0; j < p.esize(); ++j) {
            e = p.getEdge(j);
            patternString.append(e.getLeftVertex().getPos());
            patternString.append(':');
            patternString.append(e.getRightVertex().getPos());
            patternString.append(e.getType());
        }
        return patternString.toString();
    }

    public void setUnattachedVertexMatches(Pattern p, Pattern d) {
        this.setUnattachedVertexMatches(p, d, 0, p.vsize(), 0, d.vsize());
    }

    public void setUnattachedVertexMatches(Pattern p, Pattern d,
            int patternStart, int patternStop, int targetStart, int targetStop) {
        // go through the vertices, setting any that are not set
        int k = targetStart + 1;
        for (int j = patternStart + 1; j < patternStop; j++) {
            Vertex pV = p.getVertex(j);
            // System.err.println("pattern vertex " + j + " = " + pV);
            while (k < targetStop) {
                Vertex tV = d.getVertex(k);
                // System.err.println("target vertex " + k + " = " + tV);
                k++;
                if (pV.getType() == tV.getType()) {
                    int matchPos = pV.getMatch();
                    if (matchPos == 0) { // this must be an unattached vertex
                        // System.err.println("setting : " + pV + " to match " +
                        // tV);
                        pV.setMatch(tV); // set this to the first thing it
                                            // matches!
                    } else {
                        k = matchPos + 1; // start the search from the match!
                    }
                    break;
                }
            }
        }
    }

    public String match(Pattern p, Pattern d) {
        String result = new String();
        boolean matchfound = false;
        if (p.preProcess(d)) {
            if (this.matches(p, d)) {
                matchfound = true;
                result += p.getMatchString();
                result += "\t";
                result += p.getInsertString(d);
                result += "\t";
                result += p.getClassification();
            }
        }
        p.reset();
        // NOTE: Resetting now FLIPS too!
        if (p.preProcess(d)) {
            if (this.matches(p, d)) {
                if (!matchfound) {
                    matchfound = true;
                    result += p.getMatchString();
                    result += "\t";
                    result += p.getInsertString(d);
                }
            }
        }
        p.reset();
        if (matchfound)
            return p.toString() + "\t" + result;
        else
            return new String();
    }

    // return a Result array rather than a String array
    public Result[] runResults(Pattern p) {
        List<Result> results = new ArrayList<Result>();
        for (int i = 0; i < this.diagrams.length; ++i) {
            Result result = new Result();
            result.setID(this.diagrams[i].getName());
            boolean matchfound = false;
            if (p.preProcess(this.diagrams[i])) {
                if (this.matches(p, this.diagrams[i])) {
                    matchfound = true;
                    result.setData(p.getInsertString(this.diagrams[i]) + "\t"
                            + p.getVertexMatchedString());
                }
            }
            p.reset();
            // NOTE: Resetting now FLIPS too!
            if (p.preProcess(this.diagrams[i])) {
                if (this.matches(p, this.diagrams[i])) {
                    if (!matchfound) {
                        matchfound = true;
                        result.setData(p.getInsertString(this.diagrams[i]) + "\t"
                                + p.getVertexMatchedString());
                    }
                }
            }
            p.reset();
            if (matchfound)
                results.add(result);
        }
        return (Result[]) results.toArray(new Result[0]);
    }

    public String[] run(String pattern) {
        try {
            return this.run(new Pattern(pattern));
        } catch (TopsStringFormatException tsfe) {
            System.err.println(tsfe);
            return null;
        }
    }

    public String[] run(Pattern p) {
        List<String> results = new ArrayList<String>();
        for (int i = 0; i < this.diagrams.length; ++i) {
            String result = new String(this.diagrams[i].toString());
            boolean matchfound = false;
            if (p.preProcess(this.diagrams[i])) {
                if (this.matches(p, this.diagrams[i])) {
                    matchfound = true;
                    result += p.getMatchString();
                    result += p.getInsertString(this.diagrams[i]);
                }
            }
            p.reset();
            // NOTE: Resetting now FLIPS too!
            if (p.preProcess(this.diagrams[i])) {
                if (this.matches(p, this.diagrams[i])) {
                    if (!matchfound) {
                        matchfound = true;
                        result += p.getMatchString();
                        result += p.getInsertString(this.diagrams[i]);
                    }
                }
            }
            p.reset();
            if (matchfound)
                results.add(result);
        }
        return (String[]) results.toArray(new String[0]);
    }

    // generate string arrays of the insert strings from an edgepattern
    public String[][] generateInserts(Pattern p) {
        String[][] results = new String[this.diagrams.length][];
        for (int i = 0; i < this.diagrams.length; ++i) {
            if (p.preProcess(this.diagrams[i])) {
                if (this.matches(p, this.diagrams[i])) {
                    results[i] = p.getInsertStringArr(this.diagrams[i], false);
                }
            }
            p.reset(); // and flip, of course
            if (p.preProcess(this.diagrams[i])) {
                if (this.matches(p, this.diagrams[i])) {
                    // this OVERWRITES the previous array if the pattern matches
                    // twice!
                    results[i] = p.getInsertStringArr(this.diagrams[i], true);
                }
            }
            p.reset();
        }
        return results;
    }

    public boolean runsSuccessfully(Pattern p) {
        Matcher.logger.log(Level.INFO, "matching : " + p);
        boolean matchFound;
        for (int i = 0; i < this.diagrams.length; ++i) {
            matchFound = false;
            if (p.preProcess(this.diagrams[i])) {
                if (this.matches(p, this.diagrams[i])) {
                    Matcher.logger.log(Level.INFO, "matchfound=true1 " + p + " : "
                            + this.diagrams[i]);
                    matchFound = true;
                }
            }

            if (!matchFound) { // if we don't find a match in one direction,
                                // try the other
                p.reset(); // first, flip

                if (p.preProcess(this.diagrams[i])) {
                    if (this.matches(p, this.diagrams[i])) {
                        Matcher.logger.log(Level.INFO, "matchfound=true2 " + p
                                + " : " + this.diagrams[i]);
                        matchFound = true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
                p.reset(); // finally, flip back.
            }

            Matcher.logger.log(Level.INFO, "matched : " + matchFound);
            if (!matchFound) {
                return false;
            }
        }
        if (this.stringMatch(p.getVertexString(0, 0, false))) {
            Matcher.logger.log(Level.INFO, "stringmatch=true " + p);
            return true;
        } else {
            return false;
        }
        // NOTE : now testing vertex match for each graph as it's matched
    }

    public int numberMatching(Pattern p) {
        boolean matchFound;
        int numberMatching = 0;
        String patternString = p.getVertexString(0, 0, false);
        for (int i = 0; i < this.diagrams.length; ++i) {
            matchFound = false;
            if (p.preProcess(this.diagrams[i])) {
                if (this.matches(p, this.diagrams[i])) {
                    String vstr = this.diagrams[i].getVertexString(0, 0, false);
                    if (this.subSeqMatch(patternString, vstr)) {
                        matchFound = true;
                    }
                }
            }
            if (!matchFound) {
                p.reset();
                if (p.preProcess(this.diagrams[i])) {
                    if (this.matches(p, this.diagrams[i])) {
                        String reverse_vstr = this.diagrams[i].getVertexString(0, 0,
                                false);
                        if (this.subSeqMatch(patternString, reverse_vstr)) {
                            matchFound = true;
                        }
                    }
                }
                p.reset();
            }
            if (matchFound) {
                numberMatching++;
            }
        }
        return numberMatching;
    }

    public boolean runChiral(Pattern p) {
        p.reset();
        boolean matchFound;
        for (int i = 0; i < this.diagrams.length; ++i) {
            matchFound = false;
            if (p.preProcess(this.diagrams[i])) {
                if (this.matches(p, this.diagrams[i])) {
                    matchFound = true;
                }
            }
            if (!matchFound) {
                p.reset();
                if (p.preProcess(this.diagrams[i])) {
                    if (this.matches(p, this.diagrams[i])) {
                        matchFound = true;
                    } else {
                        p.reset();
                        return false;
                    }
                } else {
                    p.reset();
                    return false;
                }
                p.reset();
            }

        }
        // System.err.println("run chiral successful for " + p);
        return true;
    }

    public boolean subSeqMatch(String probe, String target) {
        if (probe == null) {
            return true; // empty string always matches!
        }
        if (target == null) {
            return false; // but no string will match it!
        }

        char c;
        int ptrP = 0;
        int ptrT = 0;
        while (ptrP < probe.length()) {
            if (ptrT >= target.length()) {
                return false;
            }
            c = target.charAt(ptrT);
            if (probe.charAt(ptrP) == c) {
                ptrP++;
            }
            ptrT++;
        }
        return true;
    }

    public boolean subSeqMatch(Edge c, int last, PatternI p, PatternI d) {
        String probe = p.getVertexString(
        		c.getLeftVertex().getPos() + 1, c.getRightVertex().getPos(), false);
        String target = d.getVertexString(
                c.getCurrentMatch().getLeftVertex().getPos() + 1,
                c.getCurrentMatch().getRightVertex().getPos(), false);
        return this.subSeqMatch(probe, target);
    }

    public boolean stringMatch(String patternString) {
        String target, reverse;
        for (int i = 0; i < this.diagrams.length; ++i) {
            target = this.diagrams[i].getVertexString(0, 0, false);
            reverse = this.diagrams[i].getVertexString(0, 0, true);
            if (this.subSeqMatch(patternString, target)
                    || this.subSeqMatch(patternString, reverse))
                return true;
        }
        return false;
    }

    public int stringMatchUnion(String patt) {
        int numMatch = this.diagrams.length;
        if (patt.equals("NC "))
            return numMatch; // -!-
        String patternString = patt.substring(patt.indexOf('N') + 1, patt
                .lastIndexOf('C'));
        String target, reverse;
        for (int i = 0; i < this.diagrams.length; ++i) {
            target = this.diagrams[i].getVertexString(0, 0, false);
            reverse = this.diagrams[i].getVertexString(0, 0, true);
            if ((this.subSeqMatch(patternString, target) && this.subSeqMatch(
                    patternString, reverse)))
                numMatch--;
        }
        return numMatch;
    }

    public boolean stringMatch(String patt, int i) { // variant for
                                                        // efficiency
        if (patt.equals("NC "))
            return true; // -!-
        String patternString = patt.substring(patt.indexOf('N') + 1, patt
                .lastIndexOf('C'));
        String target, reverse;
        target = this.diagrams[i].getVertexString(0, 0, false);
        reverse = this.diagrams[i].getVertexString(0, 0, true);
        if (this.subSeqMatch(patternString, target)
                && this.subSeqMatch(patternString, reverse))
            return false;
        return true;
    }

    @Override
    public boolean matches(PatternI p, PatternI d) {
        this.k = 0;
        int last = 1;
        while (this.k < p.esize()) { // Run through the pattern edges.
            Edge current = p.getEdge(this.k);
            if (!current.hasMoreMatches()) {
                // System.err.println("nomorematches");
                return false;
            }

            if (findNextMatch(p, current) 
            		&& verticesIncrease(p, current)
                    && subSeqMatch(current, last, p, d)) {
                last = current.getRightVertex().getPos() + 1; // aargh!
                ++this.k;
            } else {
                if (this.k > 0) {
                    if (!nextSmallestEdge(p, current)) {
                        // System.err.println("!nextsmallestedge");
                        return false;
                    }
                }
                p.setMovedUpTo(this.k);
                if (!advancePositions(p, current)) {
                    // System.err.println("!advancepostions");
                    return false;
                }
            }
        } // end while(k)

        if (p.noEdges()) {
            return true;
        } else {
            Edge e = p.getEdge(this.k); // IE : get the last edge
            int rhe = e.getCurrentMatch().getRightVertex().getPos();

            String doutCup = d.getVertexString(rhe + 1, 0, false);
            String doutCdn = d.getVertexString(rhe + 1, 0, true);
            if (subSeqMatch(p.getOutsertC(false), doutCup)
                    || subSeqMatch(p.getOutsertC(false), doutCdn)) {
                return true;
            } else {
                // System.err.println("nosubseqmatch");
                return false;
            }
        }

    }

    public boolean findNextMatch(PatternI p, Edge current) {
        int c = 0;
        // System.out.println("findNextMatch");
        if (this.k == 0) {
            while (current.hasMoreMatches()) {
                Edge match = current.getCurrentMatch();
                if (current.alreadyMatched(match)) {
                    current.setEndMatches(match);
                    return true;
                } else {
                    current.moveMatchPtr();
                    c++;
                }
            }
            current.resetMatchPtr(-c); // Move it BACK!! doh! idiot : (
            return false;
        } else {
            Edge last = (p.getEdge(this.k - 1)).getCurrentMatch();
            while (current.hasMoreMatches()) {
                Edge match = current.getCurrentMatch();
                if ((current.alreadyMatched(match))
                        && (match.greaterThan(last))) {
                    current.setEndMatches(match);
                    return true;
                } else {
                    current.moveMatchPtr();
                    c++;
                }
            }
            current.resetMatchPtr(-c); // Move it BACK!! doh! idiot : (
            return false;
        }
    }

    public boolean nextSmallestEdge(PatternI p, Edge current) {
        // System.out.println("nextSmallestMatch");
        boolean found = false;
        Edge last = (p.getEdge(this.k - 1)).getCurrentMatch();
        while ((current.hasMoreMatches()) && (found == false)) {
            Edge match = current.getCurrentMatch();
            if (match.greaterThan(last))
                found = true;
            else
                current.moveMatchPtr();
        }
        return found;
    }

    boolean verticesIncrease(PatternI p, Edge current) {
        int last = 0;
        int[] m = p.getMatches();
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

    private boolean advancePositions(PatternI p, Edge current) {
        // 'isRight' tells you which end of backedge pvert is...
        // System.out.println("advancePositions");
        boolean found = false, isRight = false, attached = false;
        int wt2 = 0, vt2 = 0;
        int pvert = current.getLeftVertex().getPos();
        int tvert = (current.getCurrentMatch()).getLeftVertex().getPos();

        this.PVS = new Stack<Integer>();
        this.TVS = new Stack<Integer>();
        this.PVS.push(new Integer(pvert));
        this.TVS.push(new Integer(tvert));

        while (!this.PVS.empty()) {
            pvert = PVS.pop().intValue();
            tvert = TVS.pop().intValue();
            // System.out.println("pvert, tvert = " + pvert + ", " + tvert);
            // FORALL EDGES LESS THAN THE STUCK EDGE
            for (int j = this.k - 1; j >= 0; --j) {
                Edge backedge = p.getEdge(j);
                isRight = false;
                attached = false;

                // IF THEY ARE ATTACHED TO SAID STUCK EDGE
                if (pvert == backedge.getRightVertex().getPos()) {
                    attached = true;
                    isRight = true;
                } else if (pvert == backedge.getLeftVertex().getPos())
                    attached = true;

                if (attached) {
                    if (backedge.moved == false) {

                        backedge.moved = true;
                        found = false;
                        while ((backedge.hasMoreMatches()) && (found == false)) {
                            Edge e = (backedge.getCurrentMatch());
                            wt2 = e.getRightVertex().getPos();
                            vt2 = e.getLeftVertex().getPos();
                            if ((wt2 >= tvert)
                                    || ((vt2 >= tvert) && (isRight == true))) {
                                if (isRight) {
                                    tvert = wt2;
                                    if (backedge.getLeftVertex().getMatch() != 0) {
                                        backedge.getLeftVertex().resetMatch();
                                        pvert = backedge.getLeftVertex().getPos();
                                        this.PVS.push(new Integer(pvert));
                                        this.TVS.push(new Integer(tvert));
                                    }
                                } else {
                                    tvert = vt2;
                                    if (backedge.getRightVertex().getMatch() != 0) {
                                        backedge.getRightVertex().resetMatch();
                                        pvert = backedge.getRightVertex().getPos();
                                        this.PVS.push(new Integer(pvert));
                                        this.TVS.push(new Integer(tvert));
                                    }
                                }
                                found = true;
                            } else {
                                backedge.moveMatchPtr();
                            }
                        }
                    } else {
                        return false;
                    } // DANGER WIL ROBINSON!

                } // end if (attached)

            } // end for all edges less than current

        } // end while PVS != 0

        // move edgeIndex to the smallest edge with an unmatched end
        for (int l = 0; l < p.esize(); ++l) {
            if (p.getEdge(l).getLeftVertex().getMatch() == 0
                    || p.getEdge(l).getRightVertex().getMatch() == 0) {
                this.k = l;
                break;
            }
        }
        return found;

    }// end of backtrack
}

